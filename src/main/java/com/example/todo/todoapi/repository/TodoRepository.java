package com.example.todo.todoapi.repository;

import com.example.todo.todoapi.entity.Todo;
import com.example.todo.userapi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TodoRepository
        extends JpaRepository<Todo, String> {
   
   
   @Query("SELECT t FROM Todo t WHERE t.user = :user")
   // SELECT * FROM tbl_todo WHERE user_id = ?
   //특정 회원의 할 일 목록 리턴
   List<Todo> findAllByUser(User user);
   
   
   //특정 회원이 작성한 할 일 목록의 개수를 리턴
   @Query("SELECT COUNT(*) FROM Todo t WHERE t.user = :user")
   int countByUser(@Param("user") User user);



}
