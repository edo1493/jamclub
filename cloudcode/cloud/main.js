require('cloud/webhooks.js');

// Use Parse.Cloud.define to define as many cloud functions as you want.
// For example:
Parse.Cloud.define("decreaseBudget", function(request, response) {
  var attack_id = request.params.attack_id;
  var amount = request.params.amount;

  var query = new Parse.Query("Attack");
  query.get( request.params.attack_id, {
    success: function(attack) {
      var budget_left =  attack.get('budget_left');
      var new_budget = budget_left - amount;
      attack.set('budget_left', new_budget);
      attack.save(null, {
        success: function(savedAttack) {
          response.success("new budget: " + attack.get('budget_left') );
        }
      });

    },
    error: function(object, error) {},
  });

});

Parse.Cloud.beforeSave("Attack", function(request, response) {

  // calculate budget from payment info
  if (request.object.get('budget_left') == null) {
    var budget = request.object.get('budget');
    request.object.set('budget_left', budget);
    var payment_info = request.object.get('payment_info');
    if (budget == null && payment_info != null) {
      var amount = parseInt(payment_info.amount);
      request.object.set("budget", amount);
      request.object.set("budget_left", amount);
      console.log("amount: "+amount);
    }
  }

  response.success();

});


Parse.Cloud.afterSave("Attack", function(request, response) {

  var notif_dispatched = request.object.get('notifications_dispatched');
  if (notif_dispatched == null || notif_dispatched == false) {
    // dispatch call to arms notifications to all non-anonymous users
    var userQuery = new Parse.Query(Parse.User);
    userQuery.exists("email");

    var pushQuery = new Parse.Query(Parse.Installation);
    pushQuery.exists("user");
    pushQuery.include("user");
    pushQuery.matchesQuery("user", userQuery);

    var attack_id = request.object.id;
    console.log("attack_id: " + attack_id);

    Parse.Push.send({
      where: pushQuery,
      data: {
        alert: "Call to arms: we need your help!",
        attack_id: attack_id,
      }
    }, {
      success: function() {
        // Push was successful
        request.object.set('notifications_dispatched', true);
        request.object.save();
      },
      error: function(error) {
        // Handle error
      }
    });
  }
});


Parse.Cloud.beforeSave("Acceptance", function(request, response) {

  if (request.object.get('request_id') == null) {
    // get user's uber token
    if (request.user != null) {
      var token = request.user.get('uber_access_token');
      console.log("user:" + request.user.get('email'))
      // get attacks coordinate
      var attack = request.object.get('attack');
      attack.fetch({
        success: function(object) {
          var latitude = object.get('latitude');
          var longitude = object.get('longitude');

          // get all products - sort by min price
          Parse.Cloud.httpRequest({
            url: 'https://api.uber.com/v1/estimates/price',
            headers: {
                'Authorization': 'Bearer '+token
            },
            params: {
              start_latitude: latitude,
              start_longitude: longitude,
              end_latitude: latitude,
              end_longitude: longitude,
            },
            success: function(httpResponse) {
              var products = httpResponse['data']['prices'];

              products.sort(function(a,b) {
                if (a.high_estimate < b.high_estimate)
                  return -1;
                if (a.high_estimate > b.high_estimate)
                  return 1;
                return 0;
              });

              // products now contains array of uber opts ordered from cheapest
              products.forEach(function(product, index) {
                if (product.high_estimate != null) {

                  // check if estimate is within budget
                  var estimate = product.high_estimate;
                  console.log("estimate:" + estimate);

                  var budget_left = object.get('budget_left');
                  console.log("budget_left:" + budget_left);

                  if (budget_left - estimate >= 0) {
                    // there is still money left
                    // request car
                    var product_id = product.product_id;
                    var product_name = product.display_name;

                    console.log("coordinates: " + latitude + " " + longitude);
                    console.log("product_id: " + product_id);
                    //console.log("token: " + token);

                    var request_lock = false;

                    Parse.Cloud.httpRequest({
                      method: 'POST',
                      url: 'https://sandbox-api.uber.com/v1/requests',
                      headers: {
                          'Authorization': 'Bearer '+token,
                          'Content-Type': 'application/json;',
                      },
                      body: {
                        product_id: product_id,
                        start_latitude: latitude,
                        start_longitude: longitude,
                        //end_latitude: latitude,
                        //end_longitude: longitude,
                      },
                      success: function(httpResponse) {
                        if (!request_lock) {
                          request_lock = true;

                          console.log('success');
                          console.log(httpResponse['data']);

                          // register request_id in db
                          request.object.set("request_id", httpResponse['data']['request_id']);
                          request.object.set("request_status", httpResponse['data']['status']);
                          request.object.set("request_surge_multiplier", httpResponse['data']['surge_multiplier']);
                          request.object.set("request_product_id", product_id);
                          request.object.set("request_product_name", product_name);
                          request.object.set("request_estimate", estimate);

                          // reduce budget_left
                          Parse.Cloud.run("decreaseBudget", {
                            attack_id: object.id,
                            amount: estimate,
                          }, {
                            success: function(result) {
                              console.log("budget decreased");
                              response.success();
                            },
                            error: function(error) {}
                          });

                        }
                      },
                      error: function(httpResponse) {
                        console.error('error');
                        console.error(httpResponse['data']);
                      }
                    });
                  }
                }
              });
            },
            error: function(httpResponse) {
              console.error(httpResponse);
              response.error();
            }
          });
        },
        error: function(object, error) {
          console.error(error);
          response.error();
        }
      });

    } else {
      response.success();
    }

  } else {
    response.success();
  }

});


Parse.Cloud.define('uberStatusUpdate', function(request, response) {
  console.log("req_id: " + request.params.request_id);
  var query = new Parse.Query("Acceptance");
  query.equalTo("request_id", request.params.request_id);
  query.find({
    success: function(list) {
      if (list != null) {
        var acceptance = list[0];

        var user = acceptance.get("user");
        user.fetch({
          success: function(userObj) {
            var token = userObj.get('uber_access_token');

            var current_status = acceptance.get('request_status');
            var new_status;
            if (current_status == "processing")
              new_status = "accepted";
            //else if (current_status == "accepted")
            //  new_status = "arriving";
            if (new_status != null) {
              // uber sandbox trigger arriving state
              console.log("setting to:" + new_status);
              Parse.Cloud.httpRequest({
                method: 'PUT',
                url: 'https://sandbox-api.uber.com/v1/sandbox/requests/' + acceptance.get('request_id'),
                headers: {
                    'Authorization': 'Bearer '+token,
                    'Content-Type': 'application/json;',
                },
                body: {
                  status: new_status,
                },
                success: function(httpResponse) {
                  console.log("updated=");
                  console.log(httpResponse.text);

                  Parse.Cloud.run("uberUpdateLocation", {
                    request_id: acceptance.get('request_id'),
                    token: token,
                    acceptance_id: acceptance.id,
                  }, {
                    success: function(result) {
                      response.success(result);
                    },
                    error: function(error) {
                      response.error(error);
                    }
                  });

                },
                error: function(httpResponse) {
                  console.log('error=');
                  console.error(httpResponse.text);
                  response.error();
                },
              });
            } else {
              // status is ok, just update location
              Parse.Cloud.run("uberUpdateLocation", {
                request_id: acceptance.get('request_id'),
                token: token,
                acceptance_id: acceptance.id,
              }, {
                success: function(result) {
                  response.success(result);
                },
                error: function(error) {
                  response.error(error);
                }
              });
            }
          },
        });
      }
    }
  });
});

Parse.Cloud.define("uberUpdateLocation", function(request, response) {

  Parse.Cloud.httpRequest({
    method: 'GET',
    url: 'https://sandbox-api.uber.com/v1/requests/' + request.params.request_id,
    headers: {
        'Authorization': 'Bearer '+request.params.token,
    },
    success: function(status) {
      console.log('status=');
      console.log(status.text);
      var query = new Parse.Query("Acceptance");
      query.get(request.params.acceptance_id, {
        success: function(acceptance) {
          acceptance.set("request_status", status['data']['status']);
          acceptance.set("request_surge_multiplier", status['data']['surge_multiplier']);
          if (status['data']['location'] != null) {
            acceptance.set("current_location", status['data']['location']);
            acceptance.set("current_latitude", status['data']['location']['latitude']);
            acceptance.set("current_longitude", status['data']['location']['longitude']);
            acceptance.set("current_bearing", status['data']['location']['bearing']);
            acceptance.save(null, {
              success: function(savedAcc) {
                response.success(status['data']['location']);
              }
            });
          }
        },
        error: function(error) {
          consol.error(error);
          response.error(error);
        }
      });
    },
    error: function(httpResponse) {
      console.log("error=");
      console.error(httpResponse.text);
      response.error(error);
    }
  });

});

Parse.Cloud.define("checkAvailability", function(request, response) {
  var latitude = request.params.latitude;
  var longitude = request.params.longitude;

  Parse.Cloud.httpRequest({
    method: "GET",
    url: "https://api.uber.com/v1/products",
    params: {
      server_token: "ksG2fhbBgiH4HGp83Vh9GQp0EaOgewPellvW3f1h",
      latitude: latitude,
      longitude: longitude,
    },
    success: function(httpResponse) {
      console.log(httpResponse.text);
      response.success(httpResponse.text);
    },
    error: function(httpResponse) {
      console.error(httpResponse.text);
      response.error(httpResponse.text);
    },
  });
});

  Parse.Cloud.beforeDelete("Acceptance", function(request, response) {
    console.log("ASDF");
    var acceptance_req_id = request.object.get('request_id');
    var acceptance_user = request.object.get('user');
    acceptance_user.fetch({
      success: function(userObj) {
        var user_token = userObj.get('uber_access_token');
        Parse.Cloud.httpRequest({
          method: 'DELETE',
          url: 'https://sandbox-api.uber.com/v1/requests/' + acceptance_req_id,
          headers: {
              'Authorization': 'Bearer '+user_token,
          },
          success: function(httpResponse) {
            console.log("cancelled request");
            console.log(httpResponse.text);
            response.success();
          },
          error: function(httpResponse) {
            console.error(httpResponse);
            response.error();
          },
        });
      },
      error: function(object, error) {
        console.error(error);
        response.error();
      }
    });
  });
