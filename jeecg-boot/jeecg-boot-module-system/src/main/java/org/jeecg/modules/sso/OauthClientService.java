package org.jeecg.modules.sso;

import com.aill.sso.oauth.client.TkeyProperties;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.constant.GlobalVariable;
import org.jeecg.common.util.JsonUtil;
import org.jeecg.modules.message.util.redis.StringRedisService;
import org.jeecg.modules.sso.cache.OauthClientToRedisBO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
@Slf4j
public class OauthClientService {

    @Autowired
    private TkeyProperties tkeyProperties;
    /**
     * 这里必须采用 value 为 String 类型，不然 客户端和服务端都要读取该信息，但是又没有共同的类进行序列化，所以必须转换成 JSON 字符串
     * 如果后面出现的公共类越来越多我再考虑独立出一个 jar 包出来维护
     */
    @Autowired
    private StringRedisService<String, String> clientRedisService;

    public void initClientToRedis() {
        OauthClientToRedisBO oauthClientToRedisBO = new OauthClientToRedisBO();
        oauthClientToRedisBO.setClientId(tkeyProperties.getClientId());
        oauthClientToRedisBO.setClientName(tkeyProperties.getClientName());
        oauthClientToRedisBO.setClientDesc(tkeyProperties.getClientDesc());
        oauthClientToRedisBO.setClientSecret(tkeyProperties.getClientSecret());
        oauthClientToRedisBO.setClientUrl(""); // TODO TilKai :  2020/2/26 tkeyProperties.getClientUri 
        oauthClientToRedisBO.setLogoUrl(""); // TODO TilKai :  2020/2/26 logoUrl
        clientRedisService.set(GlobalVariable.REDIS_CLIENT_ID_KEY_PREFIX + tkeyProperties.getClientId(), JsonUtil.toJson(oauthClientToRedisBO));
    }

    //=====================================业务处理  end=====================================

    //=====================================私有方法 start=====================================

//    private void initCreateBasicParam(OauthClient oauthClient) {
//        long currentEpochMilli = DatetimeUtil.currentEpochMilli();
//        oauthClient.setId(GenerateIdUtil.getId());
//        if (null == oauthClient.getStateEnum()) {
//            oauthClient.setStateEnum(StateEnum.ENABLE.getCode());
//        }
//        oauthClient.setDeleteEnum(DeleteEnum.NOT_DELETE.getCode());
//        oauthClient.setCreateDate(currentEpochMilli);
//        oauthClient.setCreateUserId(currentEpochMilli);
//        oauthClient.setUpdateDate(currentEpochMilli);
//        oauthClient.setUpdateUserId(currentEpochMilli);
//    }
    //=====================================私有方法  end=====================================

}
