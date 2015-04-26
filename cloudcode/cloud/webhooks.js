var express = require('express');
var app = express();

// Global app configuration section
app.use(express.bodyParser());  // Populate req.body

app.get('/ubertoken', function(req,res) {
  /*
  if (req.query['hub.verify_token'] == verify_token) {
    res.status(200).send(req.query['hub.challenge']);
  } else {
    res.status(401).send("verify_token does not match");
  }
  */

  var token = req.query['access_token']
  console.log("token: " + token);
  Parse.Cloud.httpRequest({
    url: 'https://api.uber.com/v1/me',
    headers: {
        'Authorization': 'Bearer '+token
    },
    success: function(httpResponse) {
      console.log("textresponse: " + httpResponse.text);
      console.log("uuid: " + httpResponse['uuid']);

      var uberdata = httpResponse.data;
      var ubertoken = token;

      // store to db
      Parse.Cloud.useMasterKey();

      var query = new Parse.Query(Parse.User);
      query.equalTo("uber_uuid", uberdata['uuid']);
      query.find({
        success: //(function(httpResponse,token) {
          function(users) {
            var user;
            if (users.length > 0) {
              // edit first user
              user = users[0];
            } else {
              // sign up
              user = new Parse.User();
              user.set("username", uberdata['email']);
            }
            user.set("uber_uuid", uberdata['uuid']);
            user.set("uber_access_token", ubertoken);
            user.set("email", uberdata['email']);
            user.set("first_name", uberdata['first_name']);
            user.set("last_name", uberdata['last_name']);
            user.set("picture", uberdata['picture']);
            user.set("password", ubertoken);
            user.save(null, {
              success: function(user) {
                res.redirect('http://localhost/?username='+uberdata['email']+'&password='+ubertoken);
              },
              error: function(user, error) {
                console.error("Error: " + error.code + " " + error.message);
                res.redirect('http://error/');
              }
            });
          }
        // })(httpResponse,token),
        ,error: function(error) {
          console.error("Error: " + error.code + " " + error.message);
        }
      });

    },
    error: function(httpResponse) {
      console.error('Request failed with response code ' + httpResponse.status);
      res.redirect('http://error/')
    }
  });

});

/*
app.post('/friends_changed', function(req, res) {
  console.log('friends_changed POST triggered');
  console.log('Request: ' + req.query);
  res.send(req.query['hub.challenge']);
});
*/

app.listen();
