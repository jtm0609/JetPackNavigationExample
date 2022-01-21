package kr.co.kdone.airone.utils;

import android.content.Context;
import android.os.StrictMode;

import java.util.regex.Pattern;

public class WifiUtils {

    //For Device
    private static final String ENTER = "\r";
    public static int COMMAND = 1;
    /**
     * Transparent Transmission
     */
    public static int TTS = 2;
    public static int RESPONSE_CMD = 3;
    public static int RESPONSE_TTS = 4;
    public static final String PREFERENCES_MODULE_MID = "module_mid";
    public static final String PREFERENCES_SCAN_RESULT_PASSWD = "scan_result";
    public static final String SECURITY_WEP = "wep";
    public static final String SECURITY_OPEN = "OPEN";
    public static final String SECURITY_SHARED = "SHARED";
    public static final String SECURITY_WPAPSK = "WPAPSK";
    public static final String SECURITY_WPA2PSK = "WPA2PSK";
    public static final String SECURITY_NONE = "NONE";
    public static final String SECURITY_WEP_A = "WEP-A";
    public static final String SECURITY_WEP_H = "WEP-H";
    public static final String SECURITY_TKIP = "TKIP";
    public static final String SECURITY_AES = "AES";
    public static final String SECURITY_OPEN_NONE = "open,none";
    public static final String SECURITY_OPEN_WEP_A = "open,wep-a";
    public static final String SECURITY_OPEN_WEP_H = "open,wep-h";
    public static final String SECURITY_SHARED_WEP_A = "shared,wep-a";
    public static final String SECURITY_SHARED_WEP_H = "shared,wep-h";
    public static final String SECURITY_WPAPSK_AES = "wpapsk,aes";
    public static final String SECURITY_WPAPSK_TKIP = "wpapsk,tkip";
    public static final String SECURITY_WPA2PSK_AES = "wpa2psk,aes";
    public static final String SECURITY_WPA2PSK_TKIP = "wpa2psk,tkip";
    private static final String KEYSTORE_SPACE = "keystore://";
    public static final String LAST_SCAN_RESULT_CONNECTED = "last_scan_result_connected";
    public static final int WEP_ASCII = 1;
    public static final int WEP_HEX = 2;
    public static final int WEP_INVALID = -1;

    public static final String SSID_VALID_ROOMCON = "NTR";
    public static final String SSID_VALID_MONITOR = "NTS";

    public static void forceStrictMode() {

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
    }

    public static String gernerateCMD(String text) {

        if (text == null) {
            return null;
        }

        return text + ENTER;
    }

    public static boolean isIP(String str) {
        Pattern pattern = Pattern.compile("\\b((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])" +
                "\\.((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\." +
                "((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\." +
                "((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\b");
        return pattern.matcher(str).matches();
    }

    public static int getUdpScanPort(Context context) {

        String port = context.getSharedPreferences(
                context.getPackageName() + "_preferences", Context.MODE_PRIVATE)
                .getString(WifiConstants.KEY_UDP_PORT, WifiConstants.UDP_PORT + "");
        try {
            return Integer.valueOf(port);
        } catch (Exception e) {
            return WifiConstants.UDP_SCAN_PORT;
        }
    }

    public static int getUdpPort(Context context) {

        String port = context.getSharedPreferences(
                context.getPackageName() + "_preferences", Context.MODE_PRIVATE)
                .getString(WifiConstants.KEY_UDP_PORT, WifiConstants.UDP_PORT + "");
        try {
            return Integer.valueOf(port);
        } catch (Exception e) {
            return WifiConstants.UDP_PORT;
        }
    }

    public synchronized static final String parseSecurity(String capabilities) {

        if (capabilities == null) {
            return null;
        }

        capabilities = capabilities.replace("][", ";").replace("[", "").replace("]", "");
        System.out.println("capabilities: " + capabilities);

        if (capabilities.contains("WEP")) {
            return SECURITY_WEP;
        }

        int wpa = -1;
        int wpa2 = -1;
        String[] caps = capabilities.split(";");
        for (int i = 0; i < caps.length; i++) {
            if (caps[i].contains("WPA2") && caps[i].contains("PSK")) {
                wpa2 = i;
            }else if (caps[i].contains("WPA") && caps[i].contains("PSK")) {
                wpa = i;
            }
        }

        if (wpa2 != -1) {

            if (caps[wpa2].contains("CCMP")) {
                return SECURITY_WPA2PSK_AES;
            }
            if (caps[wpa2].contains("TKIP")) {
                return SECURITY_WPA2PSK_TKIP;
            }
        }

        if (wpa != -1) {

            if (caps[wpa].contains("CCMP")) {
                return SECURITY_WPAPSK_AES;
            }
            if (caps[wpa].contains("TKIP")) {
                return SECURITY_WPAPSK_TKIP;
            }
        }

        return SECURITY_OPEN_NONE;
    }

}
