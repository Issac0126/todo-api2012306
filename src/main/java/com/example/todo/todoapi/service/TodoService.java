package com.example.todo.todoapi.service;

import com.example.todo.todoapi.dto.request.TodoModifyRequestDTO;
import com.example.todo.todoapi.dto.request.TodoCreateRequestDTO;
import com.example.todo.todoapi.dto.response.TodoDetailResponseDTO;
import com.example.todo.todoapi.dto.response.TodoListResponseDTO;
import com.example.todo.todoapi.entity.Todo;
import com.example.todo.todoapi.repository.TodoRepository;
import com.example.todo.userapi.entity.User;
import com.example.todo.userapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor //@Autowired 생략
@Transactional
public class TodoService {

    private final TodoRepository todoRepository;
    private final UserRepository userRepository;

    //할 일 목록 조회
    // 요청에 따라 데이터 갱신, 삭제 등이 발생을 하면
    // 요청이 반영된 최신 데이터 내용을 클라이언트에게 전달하기 위해
    // 목록 리턴 메서드를 서비스에서 처리한다.
    public TodoListResponseDTO retrieve(String userId){
        
        // 로그인 한 유저의 정보 데이터베이스에서 조회
        User user = getUser(userId);
        
        List<Todo> entityList = todoRepository.findAllByUser(user);

        List<TodoDetailResponseDTO> dtoList
                = entityList.stream()
                .map(todo -> new TodoDetailResponseDTO(todo))
                .collect(Collectors.toList());

        return TodoListResponseDTO.builder()
                .todos(dtoList)
                .build();
    }
    
    private User getUser(String userId) { //회원 정보가 없습니다 메서드 생성
        User user = userRepository.findById(userId).orElseThrow(
                        () -> new RuntimeException("회원 정보가 없습니다."));
        return user;
    }
    
    
    //할 일 삭제하기
    public TodoListResponseDTO delete(final String todoId, String userId) {

        try {
            todoRepository.deleteById(todoId);
        } catch (Exception e) {
            log.error("존재하지 않는 아이디입니다. ID: {}, 에러원인: {}"
                    , todoId, e.getMessage());
            throw new RuntimeException("존재하지 않는 아이디입니다.");
        }

        return retrieve(userId);
    }

    // 할 일 등록하기
    public TodoListResponseDTO insert(
       final TodoCreateRequestDTO dto,
       final String userId)
            throws RuntimeException{
        
        Todo todo = dto.toEntity(getUser(userId));
        
        todoRepository.save(todo);
        return retrieve(userId);
    }

    // done 수정하기 (할 일 체크)
//    public TodoListResponseDTO modify(TodoModifyRequestDTO dto)
//            throws RuntimeException {
//        log.info("service 진입"+dto);
//        Todo updateTodo = todoRepository.findById(dto.getId())
//                .orElseThrow( () -> new RuntimeException(dto.getId() + "번 게시물이 존재하지 않습니다."));
//        updateTodo.setDone(dto.isDone());
//        log.info("service에서 setDone 완료");
//        todoRepository.save(updateTodo);
//
//        return retrieve();
//    }

    public TodoListResponseDTO modify(TodoModifyRequestDTO dto, String userId)
            throws RuntimeException {
        log.info("service 진입"+dto);
        Optional<Todo> targetEntity
                = todoRepository.findById(dto.getId());

        targetEntity.ifPresent(entity -> {
            entity.setDone(dto.isDone());

            todoRepository.save(entity);
            log.info("service에서 setDone 완료");
        });
        return retrieve(userId);
    }


}
