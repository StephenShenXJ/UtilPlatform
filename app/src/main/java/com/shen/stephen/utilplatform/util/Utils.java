package com.shen.stephen.utilplatform.util;

import android.content.Context;
import android.content.pm.PackageManager;

import com.shen.stephen.utilplatform.PKIApplication;

/**
 * Created by ChengCn on 6/8/2016.
 */
public class Utils {
    private Utils(){}

    // get project package name
    public static String getPackageName(Context context) {
        try {
            String packageName = context.getApplicationContext()
                    .getPackageName();
            return packageName;
        } catch (Exception e) {
            e.printStackTrace();
            return "com.shen.stephen.utilplatform";
        }
    }

    /**
     * Get app version name.
     */
    public static String getAppVersionName() {
        String versionName;

        try {
            versionName = PKIApplication
                    .getContext()
                    .getPackageManager()
                    .getPackageInfo(
                            PKIApplication.getContext().getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            versionName = "1.0";
        }

        return versionName;
    }

    /**
     * Get the version code of the app.
     *
     * @return the version code of the app.
     */
    public static int getAppVersionCode() {
        int versionCode = 0;

        try {
            versionCode = PKIApplication
                    .getContext()
                    .getPackageManager()
                    .getPackageInfo(
                            PKIApplication.getContext().getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return versionCode;
    }
}
