package com.practice.jwttutorial.config;

import com.practice.jwttutorial.jwt.JwtAccessDeniedHandler;
import com.practice.jwttutorial.jwt.JwtAuthenticationEntryPoint;
import com.practice.jwttutorial.jwt.JwtSecurityConfig;
import com.practice.jwttutorial.jwt.TokenProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@EnableWebSecurity
@Configuration
public class SecurityConfig {

    private final TokenProvider tokenProvider;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

    public SecurityConfig(TokenProvider tokenProvider,
                          JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
                          JwtAccessDeniedHandler jwtAccessDeniedHandler) {
        this.tokenProvider = tokenProvider;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
        this.jwtAccessDeniedHandler = jwtAccessDeniedHandler;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring().antMatchers("/h2-console/**", "/favicon.ico");
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf().disable()           // token을 사용하는 방식이기 때문에 csrf를 disable

                .exceptionHandling()
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                .accessDeniedHandler(jwtAccessDeniedHandler)                // Exception handling 관련 클래스 추가

                .and()
                .headers()
                .frameOptions()
                .sameOrigin()       // enable h2-console

                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)     // 세션을 사용하지 않기 때문에 STATELESS로 설정

                .and()
                .authorizeRequests()        // HttpServletRequest를 사용하는 요청들에 대한 접근제한 설정
                .antMatchers("/api/hello").permitAll()      // /api/hello 에 대한 요청은 인증없이 접근을 허용
                .antMatchers("/api/authenticate").permitAll()       // 회원가입, 로그인의 경우 토큰이 없는 상태에서 요청이 들어오므로 permitAll 설정 추가
                .antMatchers("/api/signup").permitAll()
                .anyRequest().authenticated()      // 나머지 요청들은 모두 인증이 필요

                .and()
                .apply(new JwtSecurityConfig(tokenProvider));   // JwtSecurityConfig 클래스 적용

        return http.build();
    }
}
