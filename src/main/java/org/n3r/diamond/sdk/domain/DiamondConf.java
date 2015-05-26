package org.n3r.diamond.sdk.domain;

import java.text.MessageFormat;

/**
 * 单个diamond基本信息配置类
 */
public class DiamondConf {
    // basic 认证, 格式: username:password
    private String basicAuth;

    // diamondServer web访问地址
    private String diamondIp;

    // diamondServer web访问端口
    private int diamondPort;

    // diamondServer web登录用户名
    private String diamondUsername;

    // diamondServer web登录密码
    private String diamondPassword;
    private static MessageFormat DIAMONDURL_FORMAT = new MessageFormat("http://{0}:{1}");

    public DiamondConf() {

    }

    public DiamondConf(String diamondIp, int diamondPort, String diamondUsername, String diamondPassword, String basicAuth) {
        this(diamondIp, diamondPort, diamondUsername, diamondPassword);
        this.basicAuth = basicAuth;
    }

    public DiamondConf(String diamondIp, int diamondPort, String diamondUsername, String diamondPassword) {
        this.diamondIp = diamondIp;
        this.diamondPort = diamondPort;
        this.diamondUsername = diamondUsername;
        this.diamondPassword = diamondPassword;
    }

    //合成diamond访问路径
    public String getDiamondConUrl() {
        return DIAMONDURL_FORMAT.format(diamondIp, diamondPort);
    }

    public String getDiamondIp() {
        return diamondIp;
    }


    public void setDiamondIp(String diamondIp) {
        this.diamondIp = diamondIp;
    }


    public int getDiamondPort() {
        return diamondPort;
    }


    public void setDiamondPort(int diamondPort) {
        this.diamondPort = diamondPort;
    }


    public String getDiamondUsername() {
        return diamondUsername;
    }


    public void setDiamondUsername(String diamondUsername) {
        this.diamondUsername = diamondUsername;
    }


    public String getDiamondPassword() {
        return diamondPassword;
    }


    public void setDiamondPassword(String diamondPassword) {
        this.diamondPassword = diamondPassword;
    }

    public String getBasicAuth() {
        return basicAuth;
    }

    public void setBasicAuth(String basicAuth) {
        this.basicAuth = basicAuth;
    }

    @Override
    public String toString() {
        return "[diamondIp=" + diamondIp + ",diamondPort=" + diamondPort + "]";
    }
}

