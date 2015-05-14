package com.magnaideas.jamclub;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseInstallation;
import com.parse.ParseUser;

public class JamClubApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Enable Local Datastore.
        Parse.enableLocalDatastore(this);

        Parse.initialize(this, "YO9PnxwMx00UPahR2zJP2Lh8IiitPO63uBQ8u0NF", "iwBrWy8xqdUUhU6y3owDtDz22vHXFnhDH0YpMiJF");

        ParseUser.enableAutomaticUser();
    }
}
