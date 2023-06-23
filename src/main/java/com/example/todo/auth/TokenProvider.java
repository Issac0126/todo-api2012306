package com.example.todo.auth;

import com.example.todo.userapi.entity.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Component
@Slf4j
// 역할: 토큰을 발급하고, 서명 위조를 검사하는 객체
public class TokenProvider {

   // 서명에 사용할 값 (512비트(=64 byte) 이상의 랜덤 문자열) 너무 짧으면 탈취당할 위협이 있음.
   @Value("${jwt.secret}")
   private String SECRET_KEY;
   
   /**
    * JSON Web Token을 생성하는 메서드
    * @param userEntity - 토큰의 내용(클레임)에 포함될 유저 정보
    * @return - 생성된 JSON을 암호화한 토큰값.
    */
   //토큰 생성 메서드
   public String createToken(User userEntity){
      
      //토큰 만료시간 생성
      Date expiry = Date.from(
             Instant.now().plus(1, ChronoUnit.DAYS) //하루
      );
      
      
      //토큰 생성
      /*
         {
            "iss": "서비스이름",
            "exp": "2023-07-23",
            "iat": "2023-06-23",
            "email": "로그인한 사람 이메일",
            "role": "Premium"
            ...
            == 서명
         }
       */
      
      //추가 클레임 정의
      Map<String, Object> claims = new HashMap<>();
      claims.put("email", userEntity.getEmail());
      claims.put("rle", userEntity.getRole());
      //토큰에 집어넣고 싶은 데이터가 있으면 claims.put(데이터)로 집어넣는다.
      
      return Jwts.builder()
         //token header에 들어갈 서명
         .signWith(
            Keys.hmacShaKeyFor(SECRET_KEY.getBytes()),
            SignatureAlgorithm.HS512
         )
         //token payload에 들어갈 클레임 설정.
         .setIssuer("콩")           //iss: 발급자 정보
         .setIssuedAt(new Date())   //iat: 발급시간
         .setExpiration(expiry)     //exp: 만료시간
         .setSubject(userEntity.getId()) //sub: 토큰을 식별할 수 있는 주요 데이터
         .setClaims(claims)
         .compact();
   }
   
   
   
   


}
