package com.example.todo.todoapi.dto.request;

import com.example.todo.todoapi.entity.Todo;
import com.example.todo.userapi.entity.User;
import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter @Setter
@ToString @EqualsAndHashCode
@AllArgsConstructor @NoArgsConstructor
@Builder
public class TodoCreateRequestDTO {

    @NotBlank
    @Size(min = 2, max = 10)
    private String title;
    
    public Todo toEntity(){
        return Todo.builder()
           .title(this.title)
           .build();
    }
    
    // dto를 엔티티로 변환
    public Todo toEntity(User user){
        return Todo.builder()
                .title(this.title)
                .user(user)
                .build();
    }

}
