/* (C) 2025 HMM Corp. All rights reserved. */
package com.hmm.cbui.core.config;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import org.springframework.web.server.ServerWebExchange;

import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity // Webflux에서 security를 사용하기위한 설정
public class SecurityConfig {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Bean
  public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
    return http.authorizeExchange(
            exchange ->
                exchange
                    .pathMatchers(
                        "/public/**",
                        "/ws/**",
                        "/login/**",
                        "/livechat-api-test.html", // livechat API 테스트페이지 인증 없이 접근 가능 (JHM 개발용)
                        "/websocket-test.html", // websocket 테스트페이지 인증 없이 접근 가능 (JHM 개발용)
                        "auth-api-test.html", // auth API 테스트페이지 인증 없이 접근 가능 (JHM 개발용)
                        "/api/**" // 라이브챗 API 인증 없이 접근 가능 (JHM 개발용)
                        ) // ws 인증 없이 접근 가능( JHM 개발용 )
                    .permitAll() // /public 경로는 인증 없이 접근 가능
                    .anyExchange()
                    .authenticated() // 그 외 모든 요청은 인증 필요
            )
        .addFilterAt(authenticationWebFilter(), SecurityWebFiltersOrder.AUTHENTICATION)
        .httpBasic(Customizer.withDefaults()) // HTTP Basic 인증 활성화
        .formLogin(ServerHttpSecurity.FormLoginSpec::disable) // 기본 로그인 폼 제공 중지
        .csrf(ServerHttpSecurity.CsrfSpec::disable) // CSRF 보호 비활성화 (API 서버일 경우 주로 사용)
        .build();
  }

  @Bean
  public MapReactiveUserDetailsService userDetailsService() {
    UserDetails user =
        User.withUsername("user")
            .password("{noop}hmm1234**") // {noop}은 암호화 없이 사용
            .roles("USER")
            .build();
    return new MapReactiveUserDetailsService(user);
  }

  @Bean
  public AuthenticationWebFilter authenticationWebFilter() {
    AuthenticationWebFilter filter = new AuthenticationWebFilter(reactiveAuthenticationManager());

    filter.setRequiresAuthenticationMatcher(ServerWebExchangeMatchers.pathMatchers("/login"));

    filter.setServerAuthenticationConverter(
        new ServerAuthenticationConverter() {
          @Override
          public Mono<Authentication> convert(ServerWebExchange exchange) {
            return exchange
                .getRequest()
                .getBody()
                .reduce(
                    (buffer1, buffer2) -> {
                      buffer1.write(buffer2);
                      return buffer1;
                    })
                .map(
                    dataBuffer -> {
                      String body =
                          StandardCharsets.UTF_8.decode(dataBuffer.asByteBuffer()).toString();
                      try {
                        Map<String, String> credentials = objectMapper.readValue(body, Map.class);
                        String username = credentials.get("username");
                        String password = credentials.get("password");
                        return new UsernamePasswordAuthenticationToken(username, password);
                      } catch (Exception e) {
                        throw new RuntimeException("Invalid login payload", e);
                      }
                    });
          }
        });

    filter.setAuthenticationSuccessHandler(
        (exchange, authentication) ->
            Mono.fromRunnable(
                () -> {
                  ServerHttpResponse response = exchange.getExchange().getResponse();
                  response.setStatusCode(HttpStatus.OK);
                }));

    filter.setAuthenticationFailureHandler(
        (exchange, exception) ->
            Mono.fromRunnable(
                () -> {
                  ServerHttpResponse response = exchange.getExchange().getResponse();
                  response.setStatusCode(HttpStatus.UNAUTHORIZED);
                }));
    // Redis 세션 저장소 사용
    filter.setSecurityContextRepository(new WebSessionServerSecurityContextRepository());

    return filter;
  }

  @Bean
  public ReactiveAuthenticationManager reactiveAuthenticationManager() {
    return authentication -> {
      String username = authentication.getName();
      String password = authentication.getCredentials().toString();

      if ("user".equals(username) && "hmm1234**".equals(password)) {
        return Mono.just(
            new UsernamePasswordAuthenticationToken(
                username,
                password,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))));
      } else {
        return Mono.error(new BadCredentialsException("Invalid credentials"));
      }
    };
  }
}
