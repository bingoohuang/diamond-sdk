package org.n3r.diamond.sdk.domain;

import org.apache.commons.codec.digest.DigestUtils;

import java.io.Serializable;

public class DiamondStone implements Serializable, Comparable<DiamondStone> {
    private long id;
    private String group;
    private String dataId;
    private String content;
    private String description;
    private String md5;
    private boolean valid = true;

    // 批量查询时, 单条数据的状态码, 具体的状态码在Constants.java中
    private int status;
    // 批量查询时, 单条数据的信息
    private String message;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "DiamondStone{" +
                "id=" + id +
                ", group='" + group + '\'' +
                ", dataId='" + dataId + '\'' +
                ", content='" + content + '\'' +
                ", description='" + description + '\'' +
                ", md5='" + md5 + '\'' +
                ", valid=" + valid +
                ", status=" + status +
                ", message='" + message + '\'' +
                '}';
    }

    public DiamondStone() {
    }

    public DiamondStone(String dataId, String group, String content, String description, boolean valid) {
        super();
        this.dataId = dataId;
        this.content = content;
        this.group = group;
        if (this.content != null) this.md5 = DigestUtils.md5Hex(this.content);
        this.description = description;
        this.valid = valid;
    }


    public long getId() {
        return id;
    }


    public void setId(long id) {
        this.id = id;
    }


    public String getDataId() {
        return dataId;
    }


    public void setDataId(String dataId) {
        this.dataId = dataId;
    }


    public String getGroup() {
        return group;
    }


    public void setGroup(String group) {
        this.group = group;
    }

    public String getContent() {
        return content;
    }


    public void setContent(String content) {
        this.content = content;
    }


    public String getMd5() {
        return md5;
    }


    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    @Override
    public int compareTo(DiamondStone o) {
        if (o == null)
            return 1;

        if (this.dataId == null && o.getDataId() != null)
            return -1;
        int cmpDataId = this.dataId.compareTo(o.getDataId());
        if (cmpDataId != 0) {
            return cmpDataId;
        }
        if (this.group == null && o.getGroup() != null)
            return -1;
        int cmpGroup = this.group.compareTo(o.getGroup());
        if (cmpGroup != 0) {
            return cmpGroup;
        }
        if (this.content == null && o.getContent() != null)
            return -1;
        int cmpContent = this.content.compareTo(o.getContent());
        if (cmpContent != 0) {
            return cmpContent;
        }
        return 0;
    }
}
