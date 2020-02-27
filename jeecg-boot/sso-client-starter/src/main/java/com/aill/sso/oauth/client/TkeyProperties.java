package com.aill.sso.oauth.client;

import com.aill.sso.oauth.client.utils.CodecUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Description:
 *    客户端信息配置实体
 * @author tilkai
 * @version 1.0
 * @date 2020/2/26 2:56 下午
 */
@ConfigurationProperties(prefix = "tkey.sso.oauth.client")
public class TkeyProperties {

    /**
     * 客户端id
     */
    private String clientId;

    /**
     * 客户端名称
     */
    private String clientName;

    /**
     * 客户端密钥
     */
    private String clientSecret;

    /**
     * 客户端备注/描述
     */
    private String clientDesc;

    /**
     * SSO服务根目录地址
     */
    private String tkeyServerUri;

    private Boolean enableCodeCallbackToFront = false;
    private String clientCodeCallbackUri;

    /**
     * 登出操作回调Uri
     */
    private String clientLogoutRedirectUri;

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getClientDesc() {
        return clientDesc;
    }

    public void setClientDesc(String clientDesc) {
        this.clientDesc = clientDesc;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public void setTkeyServerUri(String tkeyServerUri) {
        this.tkeyServerUri = tkeyServerUri;
    }

    public String getTkeyServerUri() {
        return tkeyServerUri;
    }

    public String getClientCodeCallbackUri() {
        return clientCodeCallbackUri;
    }

    public void setClientCodeCallbackUri(String clientCodeCallbackUri) {
        this.clientCodeCallbackUri = clientCodeCallbackUri;
    }

    public String getClientLogoutRedirectUri() {
        return clientLogoutRedirectUri;
    }

    public void setClientLogoutRedirectUri(String clientLogoutRedirectUri) {
        this.clientLogoutRedirectUri = clientLogoutRedirectUri;
    }

    public Boolean getEnableCodeCallbackToFront() {
        return enableCodeCallbackToFront;
    }

    public void setEnableCodeCallbackToFront(Boolean enableCodeCallbackToFront) {
        this.enableCodeCallbackToFront = enableCodeCallbackToFront;
    }

    // ======================================================

    public String getFinalLogoutUri() {
        return getFinalLogoutUri(clientLogoutRedirectUri);
    }

    public String getFinalLogoutUri(String redirectUri) {
        if (StringUtils.isBlank(redirectUri)) {
            redirectUri = clientLogoutRedirectUri;
        }
        return getTkeyServerLogoutUri() + "?redirect_uri=" + redirectUri;
    }

    public String getFinalRedirectUri(javax.servlet.http.HttpServletRequest request) {
        return getFinalRedirectUri(request, false);
    }

    public String getFinalRedirectUri(javax.servlet.http.HttpServletRequest request, Boolean useReferer) {
        String sourceRequestURL = request.getRequestURL().toString();
        String queryParam = request.getQueryString();
        if (StringUtils.isNotBlank(queryParam)) {
            sourceRequestURL = sourceRequestURL + "?" + queryParam;
        }

        if (useReferer) {
            String refererUrl = request.getHeader("referer");
            if (StringUtils.isNotBlank(refererUrl)) {
                sourceRequestURL = refererUrl;
            }
        }

        sourceRequestURL = CodecUtil.encodeURL(sourceRequestURL);
        clientCodeCallbackUri = StringUtils.removeEnd(clientCodeCallbackUri, "/");
        sourceRequestURL = CodecUtil.encodeURL(clientCodeCallbackUri + "?redirect_uri=" + sourceRequestURL);
        return tkeyServerUri + "/oauth/authorize?response_type=code&client_id=" + clientId + "&redirect_uri=" + sourceRequestURL;
    }

    public String getTkeyServerLogoutUri() {
        return tkeyServerUri + "/oauth/logout";
    }

    public String getAccessTokenUri() {
        return tkeyServerUri + "/oauth/token";
    }

    public String getUserInfoUri() {
        return tkeyServerUri + "/oauth/userinfo";
    }

    public String getAuthorizeUri() {
        return tkeyServerUri + "/oauth/authorize";
    }

}
