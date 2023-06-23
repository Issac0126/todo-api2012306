package com.example.todo.exception;

import lombok.NoArgsConstructor;

@NoArgsConstructor //기본 생성자
public class NoRegisteredArgumentsException
   extends RuntimeException{
   
   // 기본 생성자 + 에러메세지를 받는 생성자
   
   public NoRegisteredArgumentsException(String message) {
      super(message);
      
   }
   
}
