package com.catchat.app;

import com.parse.ParseFacebookUtils;
import com.parse.ParseInstallation;
import com.parse.ParseUser;

import java.util.Arrays;
import java.util.List;

public class Utils {

    public static void mapInstallationToCurrentUser() {
        ParseInstallation.getCurrentInstallation().put("user", ParseUser.getCurrentUser());
        ParseInstallation.getCurrentInstallation().saveEventually();
    }

    public static List<String> getFBPermissions() {
        return Arrays.asList(ParseFacebookUtils.Permissions.User.EMAIL, "user_friends");
    }
}
