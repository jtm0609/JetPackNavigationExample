package kr.co.kdone.airone.udp;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;

import java.io.Serializable;

import kr.co.kdone.airone.utils.WifiUtils;

import static kr.co.kdone.airone.utils.CommonUtils.SECURITY_EAP;
import static kr.co.kdone.airone.utils.CommonUtils.SECURITY_NONE;
import static kr.co.kdone.airone.utils.CommonUtils.SECURITY_PSK;
import static kr.co.kdone.airone.utils.CommonUtils.SECURITY_WEP;

/**
 * Created by LIM on 2017-10-16.
 */

public class WifiAP implements Serializable {
    //public int channel;
    public String ssid;
    public String bssid;
    public String auth;
    public int security;
    public String enc;
    public String pass;
    public int rssi;
    public ScanResult info;

    public WifiAP(){}

    public WifiAP(String data){
        String[] datas = data.split(",");
        //this.channel = Integer.valueOf(datas[0]);
        this.ssid = datas[1];
        this.bssid = datas[2];

        if(datas[3].contains("/")){
            String[] auth_enc = datas[3].split("/");
            if(auth_enc.length >= 1) {
                this.auth = auth_enc[0];
            }
            if(auth_enc.length >= 2) {
                this.enc = auth_enc[1];
            }
        }

        try {
            this.rssi = Integer.valueOf(datas[4]);
        } catch (NumberFormatException e){
            this.rssi = -85;
        }
        this.security = getSecurity(auth);
    }

    public int getSecurity(String s) {
        if(s==null){
            return SECURITY_NONE;
        }
        if (s.contains("WEP")) {
            return SECURITY_WEP;
        } else if (s.contains("PSK")) {
            return SECURITY_PSK;
        } else if (s.contains("EAP")) {
            return SECURITY_EAP;
        }
        return SECURITY_NONE;
    }

    public int getLevel() {
        int rssiLevel = rssi/20;
        rssiLevel = rssiLevel>4?4:rssiLevel;
        if(rssiLevel < 0) rssiLevel = 0;
        return rssiLevel;
    }

    public void setRssi(byte nRssi){
        try {
            this.rssi = Integer.valueOf(nRssi);
        } catch (NumberFormatException e){
            this.rssi = -85;
        }
    }
}
