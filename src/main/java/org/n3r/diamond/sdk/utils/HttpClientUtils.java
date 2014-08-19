package org.n3r.diamond.sdk.utils;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
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
}
