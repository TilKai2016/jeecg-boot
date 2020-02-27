package org.jeecg.modules.sso.cache;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;


@Setter
@Getter
@ToString
public class OauthClientToRedisBO implements Serializable {

    private static final long serialVersionUID = 5004734902174453355L;

    /**
     * id
     */
    private Long id;

    /**
     * 客户端名称
     */
    private String clientName;

    /**
     * 客户端id
     */
    private String clientId;

    /**
     * 客户端密钥
     */
    private String clientSecret;

    /**
     * 客户端URL
     */
    private String clientUrl;

    /**
     * 客户端描述
     */
    private String clientDesc;

    private String logoUrl;

}
