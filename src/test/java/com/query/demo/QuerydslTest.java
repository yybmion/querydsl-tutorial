package com.query.demo;

import com.query.demo.entity.Member;
import com.query.demo.entity.QMember;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.FactoryBasedNavigableListAssert.assertThat;

@SpringBootTest
@Transactional
public class QuerydslTest {

    @PersistenceContext
    EntityManager em;

    JPAQueryFactory queryFactory;
    @BeforeEach
    public void before() {

        queryFactory = new JPAQueryFactory(em);

        String job1 = "Front Engineering";
        String job2 = "Back Engineering";
        Member member1 = new Member("yoobin", 31, job1);
        Member member2 = new Member("jihyun", 22, job1);
        Member member3 = new Member("musk", 28, job2);
        Member member4 = new Member("minsu", 35, job2);
        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);
    }

    @Test
    public void startQuerydsl(){
        QMember m = new QMember("m");

        Member findMember = queryFactory
                .select(m)
                .from(m)
                .where(m.username.eq("yoobin"))
                .fetchOne();
        Assertions.assertThat(findMember.getUsername()).isEqualTo("yoobin");
    }

}
