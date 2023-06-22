package com.example.todo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

//@Configuration // 설정 클래스 용도로 사용하도록 스프링에 등록하는 어노테이션
@EnableWebSecurity //시큐리티 설정 파일로 사용할 클래스 선언. @Configuration를 가지고 있음.
public class WebSecurityConfig {

    @Bean   //비밀번호 암호화 설정
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean   //시큐리티 설정
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
        //Security 모듈이 기본적으로 제공하는 보안 정책 해제
        http
                .cors()
                .and()
                .csrf().disable()
                .httpBasic().disable();

        return http.build();
    }



}




