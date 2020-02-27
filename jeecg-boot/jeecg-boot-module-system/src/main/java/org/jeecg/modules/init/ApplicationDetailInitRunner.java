package org.jeecg.modules.init;

import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.sso.OauthClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Description:
 *    初始化加载工程信息
 * @author tilkai
 * @version 1.0
 * @date 2020/2/26 3:48 下午
 */
@Slf4j
@Component
public class ApplicationDetailInitRunner implements ApplicationRunner {

    @Autowired
    private OauthClientService oauthClientService;

    @Override
    public void run(ApplicationArguments args) throws Exception {

        // 缓存当前项目SSO客户端信息入缓存
        log.info("=================================预设 Redis SSO客户端数据 Start=================================");
        oauthClientService.initClientToRedis();
        log.info("=================================预设 Redis SSO客户端数据 End=================================");
    }
}
