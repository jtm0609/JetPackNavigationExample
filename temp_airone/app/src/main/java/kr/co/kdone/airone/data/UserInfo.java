package kr.co.kdone.airone.data;

import java.io.Serializable;

/**
 * 사용자의 정보를 저장하는 클래스.
 */

public class UserInfo implements Serializable {
    private String id;
    private String password;
    private String email;
    private String userLevel;
    private String name;
    private String mobile;
    private String MainUserID;
    private String AuthFG;
    private String SEQ;
    private String AuthCode;
    private String SENDTIME;
    private int Uid;
    private int isNewNotice;
    private int isNewSmart;
    private String joinDate;

    public String getJoinDate() {
        return joinDate;
    }

    public void setJoinDate(String joinDate) {
        this.joinDate = joinDate;
    }

    public int getIsNewNotice() {
        return isNewNotice;
    }

    public void setIsNewNotice(int isNewNotice) {
        this.isNewNotice = isNewNotice;
    }

    public int getIsNewSmart() {
        return isNewSmart;
    }

    public void setIsNewSmart(int isNewSmart) {
        this.isNewSmart = isNewSmart;
    }

    public int getUid() {
        return Uid;
    }

    public void setUid(int uid) {
        Uid = uid;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getMobile() {
        return mobile;
    }

    public void setUserLevel(String userLevel) {
        this.userLevel = userLevel;
    }

    public String getUserLevel() {
        return userLevel;
    }

    public void setMainUserID(String mainUserID) {
        MainUserID = mainUserID;
    }

    public String getMainUserID() {
        return MainUserID;
    }

    public void setAuthFG(String authFG) {
        AuthFG = authFG;
    }

    public String getAuthFG() {
        return AuthFG;
    }

    public void setSEQ(String SEQ) {
        this.SEQ = SEQ;
    }

    public String getSEQ() {
        return SEQ;
    }

    public void setAuthCode(String authCode) {
        AuthCode = authCode;
    }

    public String getAuthCode() {
        return AuthCode;
    }

    public void setSENDTIME(String SENDTIME) {
        this.SENDTIME = SENDTIME;
    }

    public String getSENDTIME() {
        return SENDTIME;
    }
}
