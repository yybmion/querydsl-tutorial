package com.query.demo.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(of = {"id", "username", "age"})
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "member_id")
    private Long id;
    private String username;
    private int age;
    private String team;
    public Member(String username) {
        this.username = username;
    }
    public Member(String username, int age, String team) {
        this.username = username;
        this.age = age;
        this.team = team;
    }
}
