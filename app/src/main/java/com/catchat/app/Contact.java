package com.catchat.app;

public class Contact {

    private final String mEmailAddress;
    private final String mName;

    public Contact(String name, String emailAddress) {
        mName = name;
        mEmailAddress = emailAddress;
    }

    public String email() {
        return mEmailAddress;
    }
}
