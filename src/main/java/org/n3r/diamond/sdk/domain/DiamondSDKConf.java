package org.n3r.diamond.sdk.domain;


import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.List;


public class DiamondSDKConf {

    private static final long serialVersionUID = 8378550702596810462L;

    private String serverId;

    // 多个diamond配置
    private List<DiamondConf> diamondConfs;


    // 构造时需要传入diamondConfs 列表
    public DiamondSDKConf(List<DiamondConf> diamondConfs) {
        this.diamondConfs = diamondConfs;
    }


    // setter,getter
    public String getServerId() {
        return serverId;
    }


    public void setServerId(String serverId) {
        this.serverId = serverId;
    }


    public List<DiamondConf> getDiamondConfs() {
        return diamondConfs;
    }


    public void setDiamondConfs(List<DiamondConf> diamondConfs) {
        this.diamondConfs = diamondConfs;
    }


    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}