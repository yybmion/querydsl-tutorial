package com.query.demo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "department_id")
    private Long id;
    private String dName;

    @OneToMany(mappedBy = "department")
    private List<Member> members = new ArrayList<>();

    public Department(String dName) {
        this.dName = dName;
    }
}
