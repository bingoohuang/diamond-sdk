package org.n3r.diamond.sdk;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.lang3.StringUtils;
import org.n3r.diamond.sdk.domain.*;
import org.n3r.diamond.sdk.utils.Constants;
import org.n3r.diamond.sdk.utils.HttpClientUtils;
import org.n3r.diamond.sdk.utils.PatternUtils;
import org.n3r.diamond.sdk.utils.RandomDiamondUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DiamondSDK {
    private Logger log = LoggerFactory.getLogger(DiamondSDK.class);

    private DiamondSDKConf diamondSDKConf;

    // 连接超时时间
    private int connectionTimeout;
    // 请求超时时间
    private int requireTimeout;

    private final HttpClient client;
    private final MultiThreadedHttpConnectionManager connectionManager;

    public DiamondSDK(DiamondSDKConf diamondSDKConf) {
        this(3000, 3000, diamondSDKConf);
    }

    // 构造时需要传入连接超时时间，请求超时时间
    public DiamondSDK(int connectionTimeout, int requireTimeout, DiamondSDKConf diamondSDKConf) {
        if (connectionTimeout < 0) throw new IllegalArgumentException("连接超时时间设置必须大于0[单位(毫秒)]!");
        if (requireTimeout < 0) throw new IllegalArgumentException("请求超时时间设置必须大于0[单位(毫秒)]!");

        this.connectionTimeout = connectionTimeout;
        this.requireTimeout = requireTimeout;
        this.diamondSDKConf = diamondSDKConf;


        connectionManager = new MultiThreadedHttpConnectionManager();
        int maxHostConnections = 50;
        connectionManager.getParams().setDefaultMaxConnectionsPerHost(maxHostConnections);
        connectionManager.getParams().setStaleCheckingEnabled(true);
        this.client = new HttpClient(connectionManager);

        // 设置连接超时时间
        client.getHttpConnectionManager().getParams().setConnectionTimeout(this.connectionTimeout);
        // 设置读超时为1分钟
        client.getHttpConnectionManager().getParams().setSoTimeout(60 * 1000);
        client.getParams().setContentCharset("UTF-8");
    }

    public void close() {
        connectionManager.shutdown();
    }

    public ContextResult post(String groupName, String dataId, String context) {
        return post(groupName, dataId, context, null, true, false);
    }

    public ContextResult post(String groupName, String dataId, String context,
                              String description) {
        return post(groupName, dataId, context, description, true, false);
    }

    public ContextResult post(String groupName, String dataId, String context,
                              String description, boolean valid, boolean encrypt) {
        if (validate(dataId, groupName, context))
            return postConfig(dataId, groupName, context, description, valid, encrypt);

        ContextResult response = new ContextResult();
        response.setSuccess(false);
        response.setStatusMsg("请确保dataId,group,content不为空");
        return response;
    }


    public ContextResult update(String groupName, String dataId, String context) {
        if (validate(dataId, groupName, context))
            return updateConfig(dataId, groupName, context, null, true, false);

        ContextResult response = new ContextResult();
        response.setSuccess(false);
        response.setStatusMsg("请确保dataId,group,content不为空");
        return response;
    }

    public PageContextResult<DiamondStone> queryBy(String dataIdPattern,
                                                   String groupNamePattern,
                                                   long currentPage,
                                                   long sizeOfPerPage) {
        return processQuery(dataIdPattern, groupNamePattern, null, currentPage, sizeOfPerPage);
    }


    /**
     * 根据指定的 dataId,组名和content到指定配置的diamond来查询数据列表 如果模式中包含符号'*',则会自动替换为'%'并使用[
     * like ]语句 如果模式中不包含符号'*'并且不为空串（包括" "）,则使用[ = ]语句
     */
    public PageContextResult<DiamondStone> queryBy(String dataIdPattern,
                                                   String groupNamePattern,
                                                   String contentPattern,
                                                   long currentPage,
                                                   long sizeOfPerPage) {
        return processQuery(dataIdPattern, groupNamePattern, contentPattern, currentPage, sizeOfPerPage);
    }

    /**
     * 使用指定的diamond和指定的dataId,groupName来精确查询数据
     */
    public ContextResult get(String groupName, String dataId) {
        ContextResult result = new ContextResult();
        PageContextResult<DiamondStone> pageContextResult = processQuery(dataId, groupName, null, 1, 1);
        result.setStatusMsg(pageContextResult.getStatusMsg());
        result.setSuccess(pageContextResult.isSuccess());
        result.setStatusCode(pageContextResult.getStatusCode());
        if (pageContextResult.isSuccess()) {
            List<DiamondStone> list = pageContextResult.getDiamondData();
            if (list != null && !list.isEmpty()) {
                DiamondStone info = list.iterator().next();
                result.setStone(info);
                result.setReceiveResult(info.getContent());
                result.setStatusCode(pageContextResult.getStatusCode());

            }
        }

        return result;
    }

    private ContextResult postConfig(String dataId, String groupName, String context,
                                     String description, boolean valid, boolean encrypt) {
        ContextResult response = new ContextResult();
        if (!login(response)) {
            response.setSuccess(false);
            response.setStatusMsg("登录失败,造成错误的原因可能是指定的serverId为空或不存在");
            return response;
        }

        String postUrl = "/diamond-server/admin.do?method=postConfig";
        PostMethod post = new PostMethod(postUrl);
        HttpClientUtils.configureGetMethod(post, requireTimeout);
        try {
            setRequestBody(dataId, groupName, context, description, valid, encrypt, post);

            DiamondStone configInfo = new DiamondStone();
            configInfo.setDataId(dataId);
            configInfo.setGroup(groupName);
            configInfo.setContent(context);
            response.setStone(configInfo);

            int status = client.executeMethod(post);

            String contentFromResponse = HttpClientUtils.getContentFromResponse(post);
            response.setSuccess(false);
            response.setReceiveResult(contentFromResponse);
            response.setStatusCode(status);
            if (status == HttpStatus.SC_OK) {
                if (contentFromResponse.contains("Submit Successfully!")) {
                    response.setSuccess(true);
                    response.setStatusMsg("推送处理成功");
                }
                log.info("推送处理返回, dataId=" + dataId + ",group=" + groupName + ",content=" + context);
            } else if (status == HttpStatus.SC_REQUEST_TIMEOUT) {
                response.setStatusMsg("推送处理超时, 默认超时时间为:" + requireTimeout + "毫秒");
                log.error("推送处理超时，默认超时时间为:" + requireTimeout + "毫秒, dataId=" + dataId + ",group=" + groupName
                        + ",content=" + context);
            } else {
                response.setStatusMsg("推送处理失败, 状态码为:" + status);
                log.error("推送处理失败:" + response.getReceiveResult() + ",dataId=" + dataId + ",group=" + groupName
                        + ",content=" + context);
            }
        } catch (Exception e) {
            response.setStatusMsg("推送处理发生Exception：" + e.getMessage());
            log.error("推送处理发生IOException: dataId=" + dataId + ",group=" + groupName + ",content=" + context, e);
        } finally {
            post.releaseConnection();
        }

        return response;
    }

    private void setRequestBody(String dataId, String groupName, String context, String description, boolean valid, boolean encrypt, PostMethod post) {
        List<NameValuePair> pairs = Lists.newArrayList();
        pairs.add(new NameValuePair("dataId", dataId));
        pairs.add(new NameValuePair("group", groupName));
        pairs.add(new NameValuePair("content", context));
        if (description != null) pairs.add(new NameValuePair("description", description));
        if (valid) pairs.add(new NameValuePair("valid", "on"));
        if (encrypt) pairs.add(new NameValuePair("encrypt", "on"));

        post.setRequestBody(Iterables.toArray(pairs, NameValuePair.class));
    }


    private ContextResult updateConfig(String dataId, String groupName, String context,
                                       String description, boolean valid, boolean encrypt) {
        ContextResult response = new ContextResult();
        if (!login(response)) {
            response.setSuccess(false);
            response.setStatusMsg("登录失败,造成错误的原因可能是指定的serverId为空");
            return response;
        }

        // 是否存在此dataId,groupName的数据记录
        ContextResult result = null;
        result = get(dataId, groupName);
        if (null == result || !result.isSuccess()) {
            response.setSuccess(false);
            response.setStatusMsg("找不到需要修改的数据记录，记录不存在!");
            log.warn("找不到需要修改的数据记录，记录不存在! dataId=" + dataId + ",group=" + groupName);
            return response;
        }

        String postUrl = "/diamond-server/admin.do?method=updateConfig";
        PostMethod post = new PostMethod(postUrl);
        HttpClientUtils.configureGetMethod(post, requireTimeout);
        try {
            setRequestBody(dataId, groupName, context, description, valid, encrypt, post);

            DiamondStone configInfo = new DiamondStone();
            configInfo.setDataId(dataId);
            configInfo.setGroup(groupName);
            configInfo.setContent(context);
            log.debug("待推送的修改ConfigInfo: {}", configInfo);
            response.setStone(configInfo);
            int status = client.executeMethod(post);
            String responseBodyAsString = HttpClientUtils.getContentFromResponse(post);
            response.setReceiveResult(responseBodyAsString);
            response.setStatusCode(status);
            log.info("状态码：" + status + ",响应结果：" + responseBodyAsString);
            if (status == HttpStatus.SC_OK) {
                response.setSuccess(true);
                response.setStatusMsg("推送修改处理成功");
                log.info("推送修改处理成功");
            } else if (status == HttpStatus.SC_REQUEST_TIMEOUT) {
                response.setSuccess(false);
                response.setStatusMsg("推送修改处理超时，默认超时时间为:" + requireTimeout + "毫秒");
                log.error("推送修改处理超时，默认超时时间为:" + requireTimeout + "毫秒, dataId=" + dataId + ",group=" + groupName
                        + ",content=" + context);
            } else {
                response.setSuccess(false);
                response.setStatusMsg("推送修改处理失败");
                log.error("推送修改处理失败:" + response.getReceiveResult() + ",dataId=" + dataId + ",group=" + groupName
                        + ",content=" + context);
            }
        } catch (Exception e) {
            response.setSuccess(false);
            response.setStatusMsg("推送修改方法执行过程发生Exception：" + e.getMessage());
            log.error("在推送修改({}, {}, {})执行过程中发生IOException:{}",
                    dataId, groupName, context, e.getMessage());
        } finally {
            post.releaseConnection();
        }

        return response;
    }


    static final String LIST_FORMAT_URL =
            "/diamond-server/admin.do?method=listConfig&group=%s&dataId=%s&pageNo=%d&pageSize=%d";
    static final String LIST_LIKE_FORMAT_URL =
            "/diamond-server/admin.do?method=listConfigLike&group=%s&dataId=%s&pageNo=%d&pageSize=%d";

    private PageContextResult<DiamondStone> processQuery(String dataIdPattern, String groupNamePattern,
                                                         String contentPattern, long currentPage, long sizeOfPerPage) {
        PageContextResult<DiamondStone> response = new PageContextResult<DiamondStone>();
        ContextResult loginResult = new ContextResult();
        if (!login(loginResult)) {
            response.setSuccess(false);
            response.setStatusMsg("登录失败:" + loginResult);
            return response;
        }

        boolean hasPattern = PatternUtils.hasCharPattern(dataIdPattern)
                || PatternUtils.hasCharPattern(groupNamePattern)
                || PatternUtils.hasCharPattern(contentPattern);
        String url;
        if (hasPattern) {
            if (!StringUtils.isBlank(contentPattern)) {
                log.warn("注意, 正在根据内容来进行模糊查询, dataIdPattern=" + dataIdPattern + ",groupNamePattern=" + groupNamePattern
                        + ",contentPattern=" + contentPattern);
                // 模糊查询内容，全部查出来
                url = String.format(LIST_LIKE_FORMAT_URL, urlSafe(groupNamePattern), urlSafe(dataIdPattern), 1, Integer.MAX_VALUE);
            } else
                url = String.format(LIST_LIKE_FORMAT_URL, urlSafe(groupNamePattern), urlSafe(dataIdPattern), currentPage, sizeOfPerPage);
        } else {
            url = String.format(LIST_FORMAT_URL, urlSafe(groupNamePattern), urlSafe(dataIdPattern), currentPage, sizeOfPerPage);
        }

        GetMethod method = new GetMethod(url);
        HttpClientUtils.configureGetMethod(method, requireTimeout);
        try {

            int status = client.executeMethod(method);
            response.setStatusCode(status);
            switch (status) {
                case HttpStatus.SC_OK:
                    String json = "";
                    try {
                        json = HttpClientUtils.getContentFromResponse(method).trim();

                        Page<DiamondStone> page = JSON.parseObject(json, new TypeReference<Page<DiamondStone>>() {
                        });
                        if (page != null) {
                            List<DiamondStone> diamondData = getConfigInfos(contentPattern, currentPage, sizeOfPerPage, page);
                            response.setOriginalDataSize(diamondData.size());
                            response.setTotalCounts(page.getTotalCount());
                            response.setCurrentPage(currentPage);
                            response.setSizeOfPerPage(sizeOfPerPage);
                        } else {
                            response.setOriginalDataSize(0);
                            response.setTotalCounts(0);
                            response.setCurrentPage(currentPage);
                            response.setSizeOfPerPage(sizeOfPerPage);
                        }
                        response.operation();

                        List<DiamondStone> pageItems = new ArrayList<DiamondStone>();
                        if (page != null) {
                            pageItems = page.getPageItems();
                        }
                        response.setDiamondData(pageItems);
                        response.setSuccess(true);
                        response.setStatusMsg("指定diamond的查询完成");
                    } catch (Exception e) {
                        response.setSuccess(false);
                        response.setStatusMsg("反序列化失败,错误信息为：" + e.getLocalizedMessage());
                        log.error("反序列化page对象失败, dataId=" + dataIdPattern + ",group=" + groupNamePattern + ",json=" + json, e);
                    }
                    break;
                case HttpStatus.SC_REQUEST_TIMEOUT:
                    response.setSuccess(false);
                    response.setStatusMsg("查询数据超时" + requireTimeout + "毫秒");
                    log.error("查询数据超时，默认超时时间为:" + requireTimeout + "毫秒, dataId=" + dataIdPattern + ",group="
                            + groupNamePattern);
                    break;
                default:
                    response.setSuccess(false);
                    response.setStatusMsg("查询数据出错，服务器返回状态码为" + status);
                    log.error("查询数据出错，状态码为：" + status + ",dataId=" + dataIdPattern + ",group=" + groupNamePattern
                    );
                    break;
            }
        } catch (Exception e) {
            response.setSuccess(false);
            response.setStatusMsg("查询数据出错,错误信息如下：" + e.getMessage());
            log.error("查询数据出错, dataId=" + dataIdPattern + ",group=" + groupNamePattern, e);
        } finally {
            // 释放连接资源
            method.releaseConnection();
        }

        return response;
    }

    private List<DiamondStone> getConfigInfos(String contentPattern,
                                              long currentPage,
                                              long sizeOfPerPage,
                                              Page<DiamondStone> page) {
        List<DiamondStone> diamondData = page.getPageItems();
        if (!StringUtils.isBlank(contentPattern)) {
            Pattern pattern = Pattern.compile(contentPattern.replaceAll("\\*", ".*"));
            List<DiamondStone> newList = new ArrayList<DiamondStone>();
            Collections.sort(diamondData);
            int totalCount = 0;
            long begin = sizeOfPerPage * (currentPage - 1);
            long end = sizeOfPerPage * currentPage;
            for (DiamondStone configInfo : diamondData) {
                if (configInfo.getContent() != null) {
                    Matcher m = pattern.matcher(configInfo.getContent());
                    if (m.find()) {
                        // 只添加sizeOfPerPage个
                        if (totalCount >= begin && totalCount < end) {
                            newList.add(configInfo);
                        }
                        totalCount++;
                    }
                }
            }
            page.setPageItems(newList);
            page.setTotalCount(totalCount);
        }
        return diamondData;
    }


    /**
     * 字段dataId,groupName,context为空验证,有一个为空立即返回false
     */
    private boolean validate(String dataId, String groupName, String context) {
        if (StringUtils.isBlank(dataId)) return false;
        if (StringUtils.isBlank(groupName)) return false;
        if (StringUtils.isBlank(context)) return false;
        return true;
    }


    public ContextResult delete(String id) {
        ContextResult response = new ContextResult();
        if (!login(response)) return response;

        String encodedId = urlSafe(id);
        String url = "/diamond-server/admin.do?method=deleteConfig&id=" + encodedId;
        GetMethod method = new GetMethod(url);
        HttpClientUtils.configureGetMethod(method, requireTimeout);
        try {

            int status = client.executeMethod(method);
            response.setStatusCode(status);
            switch (status) {
                case HttpStatus.SC_OK:
                    response.setSuccess(true);
                    String contentFromResponse = HttpClientUtils.getContentFromResponse(method);
                    response.setReceiveResult(contentFromResponse);
                    response.setStatusMsg("删除成功, url=" + url);
                    log.warn("删除配置数据成功, url=" + url);
                    break;
                case HttpStatus.SC_REQUEST_TIMEOUT:
                    response.setSuccess(false);
                    response.setStatusMsg("删除数据超时" + requireTimeout + "毫秒");
                    log.error("删除数据超时，默认超时时间为:" + requireTimeout + "毫秒, id=" + id);
                    break;
                default:
                    response.setSuccess(false);
                    response.setStatusMsg("删除数据出错，服务器返回状态码为" + status);
                    log.error("删除数据出错，状态码为：" + status + ", id=" + id);
                    break;
            }
        } catch (Exception e) {
            response.setSuccess(false);
            response.setStatusMsg("删除数据出错,错误信息如下：" + e.getMessage());
            log.error("删除数据出错, id={}", id, e);
        } finally {
            method.releaseConnection();
        }

        return response;
    }

    private String urlSafe(String id)  {
        try {
            return URLEncoder.encode(id, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }


    public BatchContextResult<DiamondStone> batchQuery(String serverId, String groupName, List<String> dataIds) {
        BatchContextResult<DiamondStone> response = new BatchContextResult<DiamondStone>();

        if (dataIds == null) {
            log.error("dataId list cannot be null, serverId=" + serverId + ",group=" + groupName);
            response.setSuccess(false);
            response.setStatusMsg("dataId list cannot be null");
            return response;
        }

        // 将dataId的list处理为用一个不可见字符分隔的字符串
        StringBuilder dataIdBuilder = new StringBuilder();
        for (String dataId : dataIds)
            dataIdBuilder.append(dataId).append(Constants.LINE_SEPARATOR);

        String dataIdStr = dataIdBuilder.toString();
        ContextResult loginResult = new ContextResult();
        if (!login(loginResult)) {
            response.setSuccess(false);
            response.setStatusMsg("login fail:" + loginResult.getStatusMsg());
            return response;
        }

        PostMethod post = new PostMethod("/diamond-server/admin.do?method=batchQuery");
        post.getParams().setParameter(HttpMethodParams.SO_TIMEOUT, requireTimeout);
        try {
            NameValuePair dataId_value = new NameValuePair("dataIds", dataIdStr);
            NameValuePair group_value = new NameValuePair("group", groupName);

            post.setRequestBody(new NameValuePair[]{dataId_value, group_value});

            int status = client.executeMethod(post);
            response.setStatusCode(status);
            String responseMsg = post.getResponseBodyAsString();
            response.setResponseMsg(responseMsg);

            if (status == HttpStatus.SC_OK) {
                String json = null;
                try {
                    json = responseMsg;

                    List<DiamondStone> configInfoExList = JSON.parseArray(json, DiamondStone.class);
                    response.getResult().addAll(configInfoExList);

                    response.setSuccess(true);
                    response.setStatusMsg("batch query success");
                    log.info("batch query success, serverId=" + serverId + ",dataIds=" + dataIdStr + ",group="
                            + groupName + ",json=" + json);
                } catch (Exception e) {
                    response.setSuccess(false);
                    response.setStatusMsg("batch query deserialize error");
                    log.error("batch query deserialize error, serverId=" + serverId + ",dataIdStr=" + dataIdStr
                            + ",group=" + groupName + ",json=" + json, e);
                }

            } else if (status == HttpStatus.SC_REQUEST_TIMEOUT) {
                response.setSuccess(false);
                response.setStatusMsg("batch query timeout, socket timeout(ms):" + requireTimeout);
                log.error("batch query timeout, socket timeout(ms):" + requireTimeout + ", serverId=" + serverId
                        + ",dataIds=" + dataIdStr + ",group=" + groupName);
            } else {
                response.setSuccess(false);
                response.setStatusMsg("batch query fail, status:" + status);
                log.error("batch query fail, status:" + status + ", response:" + responseMsg
                        + ",dataIds=" + dataIdStr + ",group=" + groupName);
            }
        } catch (Exception e) {
            response.setSuccess(false);
            response.setStatusMsg("batch query io exception：" + e.getMessage());
            log.error("batch query io exception, serverId=" + serverId + ",dataIds=" + dataIdStr + ",group="
                    + groupName, e);
        } finally {
            post.releaseConnection();
        }

        return response;
    }


    public BatchContextResult<DiamondStone> batchAddOrUpdate(String serverId, String groupName,
                                                             Map<String, String> dataId2ContentMap) {
        BatchContextResult<DiamondStone> response = new BatchContextResult<DiamondStone>();

        if (dataId2ContentMap == null) {
            log.error("dataId2ContentMap cannot be null, serverId=" + serverId + " ,group=" + groupName);
            response.setSuccess(false);
            response.setStatusMsg("dataId2ContentMap cannot be null");
            return response;
        }

        // 将dataId和content的map处理为用一个不可见字符分隔的字符串
        StringBuilder allDataIdAndContentBuilder = new StringBuilder();
        for (String dataId : dataId2ContentMap.keySet()) {
            String content = dataId2ContentMap.get(dataId);
            allDataIdAndContentBuilder.append(dataId + Constants.WORD_SEPARATOR + content).append(Constants.LINE_SEPARATOR);
        }

        String allDataIdAndContent = allDataIdAndContentBuilder.toString();

        ContextResult loginResult = new ContextResult();
        if (!login(loginResult)) {
            response.setSuccess(false);
            response.setStatusMsg("login fail:" + loginResult.getStatusMsg());
            return response;
        }

        PostMethod post = new PostMethod("/diamond-server/admin.do?method=batchAddOrUpdate");
        post.getParams().setParameter(HttpMethodParams.SO_TIMEOUT, requireTimeout);
        try {
            NameValuePair dataId_value = new NameValuePair("allDataIdAndContent", allDataIdAndContent);
            NameValuePair group_value = new NameValuePair("group", groupName);

            post.setRequestBody(new NameValuePair[]{dataId_value, group_value});

            int status = client.executeMethod(post);
            response.setStatusCode(status);
            String responseMsg = post.getResponseBodyAsString();
            response.setResponseMsg(responseMsg);

            if (status == HttpStatus.SC_OK) {
                try {
                    List<DiamondStone> configInfoExList = JSON.parseArray(responseMsg, DiamondStone.class);
                    response.getResult().addAll(configInfoExList);
                    response.setStatusMsg("batch write success");
                    log.info("batch write success,serverId=" + serverId + ",allDataIdAndContent=" + allDataIdAndContent
                            + ",group=" + groupName + ",json=" + responseMsg);
                } catch (Exception e) {
                    response.setSuccess(false);
                    response.setStatusMsg("batch write deserialize error");
                    log.error("batch write deserialize error, serverId=" + serverId + ",allDataIdAndContent="
                            + allDataIdAndContent + ",group=" + groupName + ",json=" + responseMsg, e);
                }
            } else if (status == HttpStatus.SC_REQUEST_TIMEOUT) {
                response.setSuccess(false);
                response.setStatusMsg("batch write timeout, socket timeout(ms):" + requireTimeout);
                log.error("batch write timeout, socket timeout(ms):" + requireTimeout + ", serverId=" + serverId
                        + ",allDataIdAndContent=" + allDataIdAndContent + ",group=" + groupName);
            } else {
                response.setSuccess(false);
                response.setStatusMsg("batch write fail, status:" + status);
                log.error("batch write fail, status:" + status + ", response:" + responseMsg
                        + ",allDataIdAndContent=" + allDataIdAndContent + ",group=" + groupName);
            }
        } catch (Exception e) {
            response.setSuccess(false);
            response.setStatusMsg("batch write io exception：" + e.getMessage());
            log.error("batch write io exception, serverId=" + serverId + ",allDataIdAndContent=" + allDataIdAndContent
                    + ",group=" + groupName, e);
        } finally {
            post.releaseConnection();
        }

        return response;
    }

    private boolean login(ContextResult response) {
        RandomDiamondUtils util = new RandomDiamondUtils(diamondSDKConf.getDiamondConfs());
        if (diamondSDKConf.getDiamondConfs().size() == 0) return false;

        while (util.getRetryTimes() < util.getMaxTimes()) {
            DiamondConf diamondConf = util.generatorOneDiamondConf();
            if (diamondConf == null) break;

            client.getHostConfiguration().setHost(diamondConf.getDiamondIp(), diamondConf.getDiamondPort(), "http");
            PostMethod post = new PostMethod("/diamond-server/login.do?method=login");
            HttpClientUtils.configureGetMethod(post, requireTimeout);
            NameValuePair username = new NameValuePair("username", diamondConf.getDiamondUsername());
            NameValuePair password = new NameValuePair("password", diamondConf.getDiamondPassword());
            post.setRequestBody(new NameValuePair[]{username, password});

            try {
                int state = client.executeMethod(post);
                if (state == HttpStatus.SC_OK) {
                    String responseText = HttpClientUtils.getContentFromResponse(post);
                    if (!responseText.contains("Login failed, username or password error")) return true;
                    response.setStatusMsg(responseText);
                    return false;
                }
            } catch (Exception e) {
                response.setStatusMsg(e.getMessage());
                log.error("登录过程发生IOException", e);
            } finally {
                post.releaseConnection();
            }
        }

        return false;
    }

    private boolean logout(ContextResult response) {
        PostMethod post = new PostMethod("/diamond-server/login.do?method=logout");
        HttpClientUtils.configureGetMethod(post, requireTimeout);

        try {
            int state = client.executeMethod(post);
            String responseText = HttpClientUtils.getContentFromResponse(post);
            if (state == HttpStatus.SC_OK) {
                if (responseText.contains("successfully")) return true;
                response.setStatusMsg(responseText);
            }
        } catch (Exception e) {
            response.setStatusMsg(e.getMessage());
            log.error("登出过程发生IOException", e);
        } finally {
            post.releaseConnection();
        }

        return false;
    }

}
