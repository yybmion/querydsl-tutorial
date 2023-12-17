package com.query.demo;

import com.query.demo.entity.Member;
import com.query.demo.entity.QMember;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@SpringBootTest
@Transactional
@Commit
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
        /**
         * member5,6는 groupby, having 예제부터 추가!
         */
        Member member5 = new Member("uiui", 32, job2);
        Member member6 = new Member("toobi", 28, job2);


        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);
        em.persist(member5);
        em.persist(member6);
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

    @Test
    public void searchQuerydsl(){
        QMember m = new QMember("m");

        Member findMember = queryFactory
                .selectFrom(m)
                .where(m.username.eq("yoobin")
                        .and(m.age.eq(31)))
                .fetchOne();
        Assertions.assertThat(findMember.getUsername()).isEqualTo("yoobin");
    }

    @Test
    public void sort() {

        QMember m = new QMember("m");

        List<Member> result = queryFactory
                .selectFrom(m)
                .orderBy(m.age.desc().nullsLast())
                .fetch();

        Member member1 = result.get(0);

        Assertions.assertThat(member1.getAge()).isEqualTo(35);

    }


    @Test
    public void pagingDsl() {

        QMember m = new QMember("m");

        List<Member> result = queryFactory
                .selectFrom(m)
                .orderBy(m.username.asc())
                .offset(1)
                .limit(2)
                .fetch();
        Assertions.assertThat(result.size()).isEqualTo(2);
    }

    @Test
    public void groupByHavingTest() {

        QMember m = new QMember("m");

        List<Tuple> result = queryFactory
                .select(m.username , m.age.avg())
                .from(m)
                .groupBy(m.username)
                .having(m.age.gt(25))
                .fetch();

        Tuple getResult = result.get(0);

        Assertions.assertThat(getResult.get(m.username)).isEqualTo("minsu");
    }

    @Test
    public void subqueryTest() {

        QMember m = new QMember("m");

        List<Member> results = queryFactory
                .select(m)
                .from(m)
                .where(m.age.eq(
                        JPAExpressions
                                .select(m.age.max())
                                .from(m)
                ))
                .fetch();

        // 결과 확인
        for (Member result : results) {
            System.out.println(result.getUsername() + ", " + result.getAge());
        }
    }

    @Test
    public void subqueryTest2() {

        QMember m = new QMember("m");

        List<Member> results = queryFactory
                .select(m)
                .from(m)
                .where(m.age.in(
                        JPAExpressions
                                .select(m.age)
                                .from(m)
                                .where(m.age.lt(29))
                ))
                .fetch();

        // 결과 확인
        for (Member result : results) {
            System.out.println(result.getUsername() + ", " + result.getAge());
        }
    }

    @Test
    public void constantQuery(){

        QMember m = new QMember("m");

        List<Tuple> result = queryFactory
                .select(m.username, Expressions.constant("A"))
                .from(m)
                .fetch();

        Tuple results = result.get(0);

        System.out.println(results);
    }

    @Test
    public void constantQuery2(){

        QMember m = new QMember("m");

        List<String> result = queryFactory
                .select(m.username.concat("_").concat(m.age.stringValue()).prepend("HELLO_"))
                .from(m)
                .fetch();

        String results = result.get(0);
        String results2 = result.get(1);

        System.out.println(results);
        System.out.println(results2);
    }


}
