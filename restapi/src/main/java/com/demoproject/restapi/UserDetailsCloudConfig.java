package com.demoproject.restapi;

public class UserDetailsCloudConfig {
    private static UserDetailsCloudConfig instance;

    private static String baseUrl;


    private UserDetailsCloudConfig() {
        if (BuildConfig.DEBUG) {
            baseUrl = BuildConfig.DEV_API_BASE_URL;
        } else {
            baseUrl = BuildConfig.PRODUCTION_API_BASE_URL;

        }
    }

    static UserDetailsCloudConfig getInstance() {
        if (instance == null) {
            synchronized (UserDetailsCloud.class) {
                if (instance == null) {
                    instance = new UserDetailsCloudConfig();
                }
            }
        }
        return instance;
    }

    String getBaseUrl() {
        return baseUrl;
    }
}
