package com.example.todo.todoapi.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TodoListResponseDTO {

    private String error;
    private List<TodoDetailResponseDTO> todos;


}
