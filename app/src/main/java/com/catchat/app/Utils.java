package com.catchat.app;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.util.Log;

import com.parse.ParseFacebookUtils;
import com.parse.ParseInstallation;
import com.parse.ParseUser;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class Utils {

    private static SimpleDateFormat mIso8601Format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private static SimpleDateFormat mDisplayDateTime = new SimpleDateFormat("HH:mm dd MMM");

    public static void mapInstallationToCurrentUser() {
        ParseInstallation.getCurrentInstallation().put("user", ParseUser.getCurrentUser());
        ParseInstallation.getCurrentInstallation().saveEventually();
    }

    public static List<String> getFBPermissions() {
        return Arrays.asList(ParseFacebookUtils.Permissions.User.EMAIL, "user_friends");
    }

    public static String formatDate(Date date) {
        return mIso8601Format.format(date);
    }

    public static String getDisplayDate(String date) {
        Date dateTime = parseDate(date);

        if (dateTime != null) {
            return mDisplayDateTime.format(dateTime);
        }

        return date;
    }

    private static Date parseDate(String date) {
        try {
            return mIso8601Format.parse(date);
        } catch (ParseException e) {
            Log.e("CatChatTag", "Failed to parse date: " + date);
        }
        return null;
    }

    public static List<String> getPrimaryEmailAddresses(Context context) {
        AccountManager accountManager = AccountManager.get(context);
        Account[] accounts = accountManager.getAccountsByType("com.google");

        if (accounts.length > 0) {
            List<String> emails = new ArrayList<String>();
            for (Account a : accounts) {
                if (a != null) {
                    emails.add(a.name);
                }
            }
            return emails;
        }

        return null;
    }
}
