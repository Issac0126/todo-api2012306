package com.example.todo.todoapi.api;

import com.example.todo.todoapi.dto.request.TodoModifyRequestDTO;
import com.example.todo.todoapi.dto.request.TodoCreateRequestDTO;
import com.example.todo.todoapi.dto.response.TodoListResponseDTO;
import com.example.todo.todoapi.service.TodoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/todos")
//@CrossOrigin(origins = "http://localhost:3000")
public class TodoController {

    private final TodoService todoService;

    //할 일 등록 요청
    @PostMapping
    public ResponseEntity<?> createTodo(
            @Validated @RequestBody TodoCreateRequestDTO dto
            , BindingResult result){
        log.info("등록 요청 들어옴! /api/todos/ POST request");

        if(result.hasErrors()){ //만일 입력값 검증에서 걸렸을 경우
            log.warn("입력값 검증에서 에러 발생! 원인:", result.getFieldError());
//            FieldError fieldError = result.getFieldError();
            return ResponseEntity.badRequest().body(result.getFieldError());
        }

        try {
            TodoListResponseDTO responseDTO = todoService.insert(dto);
            return ResponseEntity.ok().body(responseDTO);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("할 일 등록 실패함. 원인: "+e.getMessage());
        }
    }


    //할 일 삭제 요청
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTodo(@PathVariable("id") String todoId){
        log.info("삭제 요청 들어옴! /api/todos/{id} DELETE request", todoId);

        if(todoId == null || todoId.trim().equals("")){ //null이거나 비어있거나
            return ResponseEntity.badRequest()
                    .body(TodoListResponseDTO.builder().error("ID를 전달해주세요."));
        }

        try {
            TodoListResponseDTO responseDTO = todoService.delete(todoId);
            return ResponseEntity.ok().body(responseDTO);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(TodoListResponseDTO.builder().error("삭제 실패!"+e.getMessage()));
        }

    }


    //할 일 목록 요청
    @GetMapping
    public ResponseEntity<?> retrieveTodoList(){
        log.info("목록 요청 들어옴! /api/todos/ GET request");

        //가장 기본 기능이라 에러가 날 경우 프로그램 자체가 동작하지 않음. 때문에 try/catch문 작성은 생략.
        TodoListResponseDTO responseDTO = todoService.retrieve();
        return ResponseEntity.ok().body(responseDTO);

    }

    //할 일 수정(체크) 요청  //수정 현재 작동안됨! 수정 필요.
    @RequestMapping(method = {RequestMethod.PUT, RequestMethod.PATCH})
    public ResponseEntity<?> updateTodo(@Validated @RequestBody TodoModifyRequestDTO dto, BindingResult result){
        log.info("수정 요청 들어옴! /api/todos/ PUT or PATCH request");
        log.info("들어온 dto: "+dto);

        if(result.hasErrors()){
            return ResponseEntity.badRequest().body("잘못된 ID입니다."+result.getFieldError());
        }

        try {
            TodoListResponseDTO responseDTO = todoService.modify(dto);
            return ResponseEntity.ok().body(responseDTO);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(TodoListResponseDTO.builder().error(e.getMessage()));
        }
    }

}
