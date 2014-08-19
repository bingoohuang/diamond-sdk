package org.n3r.diamond.sdk.domain;


import java.io.Serializable;

// 根据dataId,groupName精确查询返回的对象
public class ContextResult  implements Serializable {
    private boolean success; // 是否成功
    private int statusCode; // 状态码
    private String statusMsg = ""; // 状态信息
    private String receiveResult; // 回传信息
    private DiamondStone stone; // 配置对象包括[内容，dataId，groupName]

    public ContextResult() {

    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean isSuccess) {
        this.success = isSuccess;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getStatusMsg() {
        return statusMsg;
    }

    public void setStatusMsg(String statusMsg) {
        this.statusMsg = statusMsg;
    }

    public DiamondStone getStone() {
        return stone;
    }

    public void setStone(DiamondStone stone) {
        this.stone = stone;
    }

    public String getReceiveResult() {
        return receiveResult;
    }

    public void setReceiveResult(String receiveResult) {
        this.receiveResult = receiveResult;
    }

    @Override
    public String toString() {
        return "[" + "statusCode=" + statusCode + ",success=" + success
                + ",statusMsg=" + statusMsg + ",receiveResult=" + receiveResult
                + ",[stone=" + stone + "]]";
    }

}