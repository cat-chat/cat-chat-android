package com.catchat.app;

import com.parse.ParseInstallation;
import com.parse.ParseUser;

public class Utils {

    public static void mapInstallationToCurrentUser() {
        ParseInstallation.getCurrentInstallation().put("user", ParseUser.getCurrentUser());
        ParseInstallation.getCurrentInstallation().saveEventually();
    }
}
