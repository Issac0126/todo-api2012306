package com.example.todo.config;

import com.example.todo.filter.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.filter.CorsFilter;

import javax.servlet.Filter;

//@Configuration // 설정 클래스 용도로 사용하도록 스프링에 등록하는 어노테이션
@EnableWebSecurity //시큐리티 설정 파일로 사용할 클래스 선언. @Configuration를 가지고 있음.
@RequiredArgsConstructor
public class WebSecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    
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
           .httpBasic().disable()
           //세션 인증을 사용하지 않겠다
           .sessionManagement()
           .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
           .and()
           //어떤 요청에서 인증을 안 할 것인지 설정, 언제 할 것인지 설정
           .authorizeRequests()
           .antMatchers("/", "/api/auth/**").permitAll()
//           .antMatchers(HttpMethod.POST, "/api/todos").gasRole("ADMIN")
            // => ADMIN 권한을 가진 사람만 통과시키겠다
           .anyRequest().authenticated();
        
        //토큰 인증 필터 연결
        http.addFilterAfter(
           jwtAuthFilter,
           CorsFilter.class  //import 주의: 스프링 꺼로
        );
        
        return http.build();
    }



}




