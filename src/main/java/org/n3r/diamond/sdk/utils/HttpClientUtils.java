package org.n3r.diamond.sdk.utils;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.n3r.diamond.sdk.domain.DiamondConf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.List;
import java.util.zip.GZIPInputStream;


public class HttpClientUtils {
    static Logger log = LoggerFactory.getLogger(HttpClientUtils.class);

    public static String getContentFromResponse(HttpMethod httpMethod) {
        String result = null;
        if (isZipContent(httpMethod)) {
            InputStream is = null;
            GZIPInputStream gzin = null;
            try {
                is = httpMethod.getResponseBodyAsStream();
                gzin = new GZIPInputStream(is);
                result = IOUtils.toString(gzin);
            } catch (Exception e) {
                log.error("ungzip error", e);
            } finally {
                IOUtils.closeQuietly(gzin);
                IOUtils.closeQuietly(is);
            }
        } else {
            try {
                result = httpMethod.getResponseBodyAsString();
            } catch (Exception e) {
                log.error("getResponseBodyAsString error", e);
            }
        }

        return StringEscapeUtils.unescapeHtml4(result);
    }

    public static boolean isZipContent(HttpMethod httpMethod) {
        Header responseHeader = httpMethod.getResponseHeader(Constants.CONTENT_ENCODING);
        if (null == responseHeader) return false;

        String acceptEncoding = responseHeader.getValue();
        return acceptEncoding.toLowerCase().indexOf("gzip") > -1;
    }


    public static void configureGetMethod(HttpMethod method, int require_timeout) {
        method.addRequestHeader(Constants.ACCEPT_ENCODING, "gzip,deflate");
        method.addRequestHeader("Accept", "application/json");
        // 设置请求超时时间
        method.getParams().setParameter(HttpMethodParams.SO_TIMEOUT, require_timeout);
    }

    public static void setBasicAuth(HttpClient client, String host, int port, String basicAuth) {
        if (Strings.isNullOrEmpty(basicAuth)) return;

        List<String> splits = Splitter.on(':').trimResults().splitToList(basicAuth);

        if (splits.size() < 2) return;
        String userName = splits.get(0);
        String passWord = splits.get(1);

        client.getParams().setAuthenticationPreemptive(true);
        Credentials credentials = new UsernamePasswordCredentials(userName, passWord);
        AuthScope authScope = new AuthScope(host, port, AuthScope.ANY_REALM);
        client.getState().setCredentials(authScope, credentials);
    }

    public static void setBasicAuth(HttpClient client, DiamondConf diamondConf) {
        setBasicAuth(client, diamondConf.getDiamondIp(), diamondConf.getDiamondPort(), diamondConf.getBasicAuth());
    }
}
