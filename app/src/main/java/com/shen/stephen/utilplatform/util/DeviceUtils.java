package com.shen.stephen.utilplatform.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.DisplayMetrics;
import android.util.Log;

import com.shen.stephen.utilplatform.PKIApplication;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class DeviceUtils {
	private DeviceUtils() {
		// Avoid construct outside.
	}

	private static String LOG_TAG = DeviceUtils.class.getSimpleName();

	/**
	 * The user-visible version string. E.g., "1.0" or "3.4b5".
	 * 
	 * @return
	 */
	public static String getOS() {
		return android.os.Build.VERSION.RELEASE;
	}

	/**
	 * The end-user-visible name for the end product.
	 * 
	 * @return
	 */
	public static String getDeviceModel() {

		return android.os.Build.MODEL;
	}

	/**
	 * The user-visible SDK version of the framework in its raw String
	 * representation.
	 * 
	 * @return
	 */
	public static String getSDKVersion() {
		return String.valueOf(android.os.Build.VERSION.SDK_INT);
	}
	
	public static String getMACAddress() {
		try {
            WifiManager wifi = (WifiManager) PKIApplication.getContext()
                    .getSystemService(Context.WIFI_SERVICE);
            WifiInfo info = wifi.getConnectionInfo();
            String wifiMac = info.getMacAddress();
            return wifiMac;
        } catch (Exception e) {
            e.printStackTrace();
            Log.w(LOG_TAG, "Can not get wifi mac address");
        }
		
		return "";
	}

	/**
	 * Get the network status string. if network is not available return
	 * "Network is off", otherwise return the string like: "Network type:Wifi".
	 * 
	 */
	public static String getNetworkStatus() {
		ConnectivityManager connectivityManager = (ConnectivityManager) PKIApplication
				.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager
				.getActiveNetworkInfo();
		if (activeNetworkInfo == null || !activeNetworkInfo.isConnected()) {
			return "Network is off";
		}

		return "Network type:" + activeNetworkInfo.getTypeName();
	}

	/**
	 * Get IP address of the device.
	 *
	 */
	public static String getIp() {
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface
					.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf
						.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress()) {
						return inetAddress.getHostAddress().toString();
					}
				}
			}
		} catch (SocketException ex) {
			Log.d(LOG_TAG, ex.toString());
		}
		return "";
	}

    /**
     * get the size of the screen.
     *
     */
    public static DisplayMetrics getScreenSize() {
        DisplayMetrics dm = PKIApplication.getContext().getResources().getDisplayMetrics();
        return dm;
    }
}
