import os
from flask import Flask
from flask import request, redirect, jsonify
from flaskext.mysql import MySQL

from rauth import OAuth2Service
import requests

import json

mysql = MySQL()
app = Flask(__name__)
app.config['MYSQL_DATABASE_USER'] = 'b6cfca83d5344f'
app.config['MYSQL_DATABASE_PASSWORD'] = '58aab4d7'
app.config['MYSQL_DATABASE_DB'] = 'heroku_95c046540c1de7a'
app.config['MYSQL_DATABASE_HOST'] = 'us-cdbr-iron-east-02.cleardb.net'
mysql.init_app(app)


app.debug = True

@app.route('/')
def hello():
    return 'Hello World!'

@app.route('/uberlogin')
def uberlogin():
    uber_api = OAuth2Service(
        client_id='Du20hXHCp_xmm8xuOWsVNApLCxVcMKv1',
        client_secret='3h4Mi45G9cbBjN89XIp1nssAKnn5WD_rcccN0B4q',
        name='The Jam Club',
        authorize_url='https://login.uber.com/oauth/authorize',
        access_token_url='https://login.uber.com/oauth/token',
        base_url='https://api.uber.com/v1/',
    )

    parameters = {
        'response_type': 'code',
        'redirect_uri': 'https://jamclub.herokuapp.com/ubertoken',
        'scope': 'profile request',
    }

    # Redirect user here to authorize your application
    login_url = uber_api.get_authorize_url(**parameters)

    return login_url

@app.route('/ubertoken')
def ubertoken():

    # Once your user has signed in using the previous step you should redirect
    # them here

    parameters = {
        'redirect_uri': 'https://jamclub.herokuapp.com/ubertoken',
        'code': request.args.get('code'),
        'grant_type': 'authorization_code',
    }

    response = requests.post(
        'https://login.uber.com/oauth/token',
        auth=(
            'Du20hXHCp_xmm8xuOWsVNApLCxVcMKv1',
            '3h4Mi45G9cbBjN89XIp1nssAKnn5WD_rcccN0B4q',
        ),
        data=parameters,
    )

    # This access_token is what we'll use to make requests in the following
    # steps
    access_token = response.json().get('access_token')

    return redirect('https://thejamclub.parseapp.com/ubertoken?access_token='+access_token)

    """
    # Sync uber user to DB and return user info
    url = 'https://api.uber.com/v1/me'
    response = requests.get(
        url,
        headers={
            'Authorization': 'Bearer %s' % request.args.get('access_token')
        }
    )
    data = response.json()
    uuid = data.get('uuid')

    cursor = mysql.get_db().cursor
    # cursor.execute("INSERT INTO `user` (`uuid`, `uber_token`) VALUES ('"+uuid+"', '"+access_token+"');")
    return redirect('http://localhost/?user_id='+data.get('uuid'))
    """
