/*
 * Copyright (c) 2020-2030 ZHENGGENGWEI(码匠君)<herodotus@aliyun.com>
 *
 * Dante Cloud licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Dante Cloud 采用APACHE LICENSE 2.0开源协议，您在使用过程中，需要注意以下几点：
 *
 * 1.请不要删除和修改根目录下的LICENSE文件。
 * 2.请不要删除和修改 Dante Cloud 源码头部的版权声明。
 * 3.请保留源码和相关描述文件的项目出处，作者声明等。
 * 4.分发源码时候，请注明软件出处 https://gitee.com/dromara/dante-cloud
 * 5.在修改包名，模块名称，项目代码等时，请注明软件出处 https://gitee.com/dromara/dante-cloud
 * 6.若您的项目无法满足以上几点，可申请商业授权
 */

package cn.herodotus.dante.authentication.configuration;

import cn.herodotus.dante.module.security.processor.HerodotusSecurityMetadataSource;
import cn.herodotus.engine.captcha.core.processor.CaptchaRendererFactory;
import cn.herodotus.engine.oauth2.authorization.authorization.OAuth2FormLoginConfigurer;
import cn.herodotus.engine.oauth2.authorization.properties.OAuth2UiProperties;
import cn.herodotus.engine.oauth2.core.definition.service.ClientDetailsService;
import cn.herodotus.engine.oauth2.core.definition.strategy.StrategyUserDetailsService;
import cn.herodotus.engine.oauth2.core.processor.HerodotusSecurityConfigureHandler;
import cn.herodotus.engine.oauth2.core.response.DefaultOAuth2AuthenticationEventPublisher;
import cn.herodotus.engine.oauth2.core.response.HerodotusAccessDeniedHandler;
import cn.herodotus.engine.oauth2.core.response.HerodotusAuthenticationEntryPoint;
import cn.herodotus.engine.oauth2.metadata.processor.ExpressionSecurityMetadataParser;
import cn.herodotus.engine.oauth2.server.authorization.processor.HerodotusClientDetailsService;
import cn.herodotus.engine.oauth2.server.authorization.processor.HerodotusUserDetailsService;
import cn.herodotus.engine.oauth2.server.authorization.service.OAuth2ApplicationService;
import cn.herodotus.engine.oauth2.server.resource.converter.HerodotusJwtAuthenticationConverter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationEventPublisher;
import org.springframework.security.config.annotation.ObjectPostProcessor;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;

/**
 * <p>Description: 默认安全配置 </p>
 *
 * @author : gengwei.zheng
 * @date : 2022/2/12 20:53
 */
@EnableWebSecurity
public class DefaultSecurityConfiguration {

    private static final Logger log = LoggerFactory.getLogger(DefaultSecurityConfiguration.class);

    private final ExpressionSecurityMetadataParser securityMetadataExpressionParser;
    private final HerodotusSecurityMetadataSource herodotusSecurityMetadataSource;
    private final HerodotusSecurityConfigureHandler herodotusSecurityConfigureHandler;
    private final JwtDecoder jwtDecoder;
    private final OAuth2UiProperties uiProperties;
    private final CaptchaRendererFactory captchaRendererFactory;

    @Autowired
    public DefaultSecurityConfiguration(ExpressionSecurityMetadataParser securityMetadataExpressionParser, HerodotusSecurityMetadataSource herodotusSecurityMetadataSource, HerodotusSecurityConfigureHandler herodotusSecurityConfigureHandler, JwtDecoder jwtDecoder, OAuth2UiProperties uiProperties, CaptchaRendererFactory captchaRendererFactory) {
        this.securityMetadataExpressionParser = securityMetadataExpressionParser;
        this.herodotusSecurityMetadataSource = herodotusSecurityMetadataSource;
        this.herodotusSecurityConfigureHandler = herodotusSecurityConfigureHandler;
        this.jwtDecoder = jwtDecoder;
        this.uiProperties = uiProperties;
        this.captchaRendererFactory = captchaRendererFactory;
    }

    @Bean
    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http, UserDetailsService userDetailsService) throws Exception {

        log.debug("[Herodotus] |- Core [Default Security Filter Chain] Auto Configure.");
        // 禁用CSRF 开启跨域
        http.csrf().disable().cors();

        // @formatter:off
        http.authorizeRequests(authorizeRequests ->
                        authorizeRequests
                                .antMatchers(herodotusSecurityConfigureHandler.getPermitAllArray()).permitAll()
                                .antMatchers(herodotusSecurityConfigureHandler.getStaticResourceArray()).permitAll()
                                .requestMatchers(EndpointRequest.toAnyEndpoint()).permitAll()
                                .anyRequest().authenticated()
                                .withObjectPostProcessor(new ObjectPostProcessor<FilterSecurityInterceptor>() {
                                    @Override
                                    public <O extends FilterSecurityInterceptor> O postProcess(O fsi) {
                                        securityMetadataExpressionParser.setFilterInvocationSecurityMetadataSource(fsi.getSecurityMetadataSource());
                                        fsi.setSecurityMetadataSource(herodotusSecurityMetadataSource);
                                        return fsi;
                                    }
                                }))
                .formLogin(form -> {
                            form.loginPage(uiProperties.getLoginPageUrl())
                                    .usernameParameter(uiProperties.getUsernameParameter())
                                    .passwordParameter(uiProperties.getPasswordParameter());
                            if (StringUtils.isNotBlank(uiProperties.getFailureForwardUrl())) {
                                form.failureForwardUrl(uiProperties.getFailureForwardUrl());
                            }
                            if (StringUtils.isNotBlank(uiProperties.getSuccessForwardUrl())) {
                                form.successForwardUrl(uiProperties.getSuccessForwardUrl());
                            }
                        }
                )
                .exceptionHandling()
                .authenticationEntryPoint(new HerodotusAuthenticationEntryPoint())
                .accessDeniedHandler(new HerodotusAccessDeniedHandler())
                .and()
                .oauth2ResourceServer(configurer -> configurer
                        .jwt(jwt -> jwt.decoder(jwtDecoder).jwtAuthenticationConverter(new HerodotusJwtAuthenticationConverter()))
                        .bearerTokenResolver(new DefaultBearerTokenResolver())
                        .accessDeniedHandler(new HerodotusAccessDeniedHandler())
                        .authenticationEntryPoint(new HerodotusAuthenticationEntryPoint()))
                .apply(new OAuth2FormLoginConfigurer(userDetailsService, uiProperties, captchaRendererFactory));
        // @formatter:on
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    @ConditionalOnMissingBean
    public AuthenticationEventPublisher authenticationEventPublisher(ApplicationContext applicationContext) {
        log.debug("[Herodotus] |- Bean [Authentication Event Publisher] Auto Configure.");
        return new DefaultOAuth2AuthenticationEventPublisher(applicationContext);
    }

    @Bean
    @ConditionalOnMissingBean
    public UserDetailsService userDetailsService(StrategyUserDetailsService strategyUserDetailsService) {
        HerodotusUserDetailsService herodotusUserDetailsService = new HerodotusUserDetailsService(strategyUserDetailsService);
        log.debug("[Herodotus] |- Bean [Herodotus User Details Service] Auto Configure.");
        return herodotusUserDetailsService;
    }

    @Bean
    @ConditionalOnMissingBean
    public ClientDetailsService clientDetailsService(OAuth2ApplicationService applicationService) {
        HerodotusClientDetailsService herodotusClientDetailsService = new HerodotusClientDetailsService(applicationService);
        log.debug("[Herodotus] |- Bean [Herodotus Client Details Service] Auto Configure.");
        return herodotusClientDetailsService;
    }
}
