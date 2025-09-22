/* (C) 2025 HMM Corp. All rights reserved. */
package com.hmm.cbui.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity // Webflux에서 security를 사용하기위한 설정
public class SecurityConfig {

  @Bean
  public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
    return http.authorizeExchange(
            exchange ->
                exchange
                    .pathMatchers(
                        "/public/**",
                        "/ws/**",
                        "livechat-api-test.html", // livechat API 테스트페이지 인증 없이 접근 가능 (JHM 개발용)
                        "websocket-test.html", // websocket 테스트페이지 인증 없이 접근 가능 (JHM 개발용)
                        "auth-api-test.html", // auth API 테스트페이지 인증 없이 접근 가능 (JHM 개발용)
                        "/api/livechat/**", // 라이브챗 API 인증 없이 접근 가능 (JHM 개발용)
                        "/api/auth/**" // 인증 API 인증 없이 접근 가능 (JHM 개발용)
                        ) // ws 인증 없이 접근 가능( JHM 개발용 )
                    .permitAll() // /public 경로는 인증 없이 접근 가능
                    .anyExchange()
                    .authenticated() // 그 외 모든 요청은 인증 필요
            )
        .httpBasic(Customizer.withDefaults()) // HTTP Basic 인증 활성화
        .formLogin(ServerHttpSecurity.FormLoginSpec::disable) // 기본 로그인 폼 제공 중지
        .csrf(ServerHttpSecurity.CsrfSpec::disable) // CSRF 보호 비활성화 (API 서버일 경우 주로 사용)
        .build();
  }

  @Bean
  public MapReactiveUserDetailsService userDetailsService() {
    UserDetails user =
        User.withUsername("user")
            .password("{noop}password") // {noop}은 암호화 없이 사용
            .roles("USER")
            .build();
    return new MapReactiveUserDetailsService(user);
  }
}
