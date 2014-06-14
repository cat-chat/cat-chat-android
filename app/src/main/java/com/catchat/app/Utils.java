package com.catchat.app;

import com.parse.ParseFacebookUtils;
import com.parse.ParseInstallation;
import com.parse.ParseUser;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class Utils {

    private static SimpleDateFormat mIso8601Format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static void mapInstallationToCurrentUser() {
        ParseInstallation.getCurrentInstallation().put("user", ParseUser.getCurrentUser());
        ParseInstallation.getCurrentInstallation().saveInBackground();
    }

    public static List<String> getFBPermissions() {
        return Arrays.asList(ParseFacebookUtils.Permissions.User.EMAIL, "user_friends");
    }

    public static String formatDate(Date date) {
        return mIso8601Format.format(date);
    }
}
