package com.example.todo.userapi.entity;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter @Setter
@ToString @EqualsAndHashCode(of = "id")
@NoArgsConstructor @AllArgsConstructor
@Builder
@Entity
@Table(name = "tbl_user")
public class User {

    @Id
    @Column(name = "user_id")
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
    private String id; //계정명이 아니라 식별 코드

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String userName;

    @CreationTimestamp
    private LocalDateTime joinDate;
    
    @Enumerated(EnumType.STRING)
//    @ColumnDefault("'COMMON'")
    //enum에서 default를 지정할 땐 '홑 따옴표'로 한 번 더 감싸줘야 한다.
    @Builder.Default
    private Role role = Role.COMMON; //유저 권한
    
    private String profileImg;
    

    
    
    //등급 수정 메서드 setter의 역할을 해줌.
    public void changeRole(Role role){
        this.role = role;
    }
    
    
    
}
