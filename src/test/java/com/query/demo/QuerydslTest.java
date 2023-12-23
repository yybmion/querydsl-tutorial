package com.query.demo;

import com.query.demo.entity.Department;
import com.query.demo.entity.Member;
import com.query.demo.entity.QDepartment;
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

        Department department1 = new Department("department1");
        Department department2 = new Department("department2");
        em.persist(department1);
        em.persist(department2);

        String job1 = "Front Engineering";
        String job2 = "Back Engineering";
        Member member1 = new Member("yoobin", 31, job1,department1);
        Member member2 = new Member("jihyun", 22, job1,department1);
        Member member3 = new Member("musk", 28, job2,department2);
        Member member4 = new Member("minsu", 35, job2,department2);
        /**
         * member5,6는 groupby, having 예제부터 추가!
         */
        Member member5 = new Member("uiui", 32, job2,department1);
        Member member6 = new Member("toobi", 28, job2,department2);


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

        Member result = queryFactory
                .select(m)
                .from(m)
                .where(m.username.eq("yoobin"))   //where을 통해 member의 username yoobin과 같은 검색 조건
                .fetchOne();        // 결과 하나를 가져와
        Assertions.assertThat(result.getUsername()).isEqualTo("yoobin");  //그것이 yoobin인지 확인
    }

    @Test
    public void searchQuerydsl(){
        QMember m = new QMember("m");

        Member result = queryFactory
                .selectFrom(m)
                .where(m.username.eq("yoobin")
                        .and(m.age.eq(31)))             // 조건 하나를 더 추가, 나이가 31과 같은 검색 조건
                .fetchOne();
        Assertions.assertThat(result.getUsername()).isEqualTo("yoobin");  // 그것이 yoobin인지 확인
    }

    @Test
    public void sortQuerydsl() {

        QMember m = new QMember("m");

        List<Member> result = queryFactory
                .selectFrom(m)
                .orderBy(m.age.desc().nullsLast())      // 나이를 내림차순으로 정렬
                .fetch();           // list로 반환

        Member member1 = result.get(0);         // 그중 첫번째 가져와

        Assertions.assertThat(member1.getAge()).isEqualTo(35);   //그 멤버의 나이가 35인지 확인

    }


    @Test
    public void pagingQuerydsl() {

        QMember m = new QMember("m");

        List<Member> result = queryFactory
                .selectFrom(m)
                .orderBy(m.username.asc())          // username을 올림차순으로 정렬한다음
                .offset(1)                          // 결과값중 첫번째부터
                .limit(3)                           // 세번쨰 까지 가져온다.
                .fetch();
        Assertions.assertThat(result.size()).isEqualTo(3);  // 따라서 개수는 3개
    }

    @Test
    public void joinQuerydsl() throws Exception{
        QMember m = new QMember("m");
        QDepartment d = new QDepartment("d");

        List<Member> result = queryFactory
                .selectFrom(m)
                .join(m.department, d)              // member와 department를 join시키고 별칭 d로 지정
                .where(d.dName.eq("department1"))       // join시킨 테이블에서 dName이 department1과 이름이 같은 것을 검색 조건
                .fetch();

        Assertions.assertThat(result)
                .extracting("username") // 결과 list에서 username을 뽑아
                .containsExactly("yoobin","jihyun","uiui"); // yoobin, jihyun,uiui 가 맞는지 확인
    }

    @Test
    public void groupByHavingQuerydsl() {

        QMember m = new QMember("m");

        List<Tuple> result = queryFactory
                .select(m.username , m.age.avg())
                .from(m)
                .groupBy(m.username)        // username으로 그룹화하고
                .having(m.age.gt(25))   // 그중 25세 이상만 가져와
                .fetch();

        Tuple getResult = result.get(0); // 첫전째 결과값만 가져와

        Assertions.assertThat(getResult.get(m.username)).isEqualTo("minsu");  // 그 이름이 minsu가 맞는지 확인
    }

    @Test
    public void subqueryQuerydsl() {

        QMember m = new QMember("m");

        List<Member> results = queryFactory
                .select(m)
                .from(m)
                .where(m.age.eq(                        // 나이가 최대값인검색 조건에서 그 중 최대값과 나이가 같은 것만 검색함
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
    public void subqueryQuerydsl2() {

        QMember m = new QMember("m");

        List<Member> results = queryFactory
                .select(m)
                .from(m)
                .where(m.age.in(                            // 29보다 낮은 나이 검색 조건인 나이와 딱 같으면 검색
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
    public void constantQuerydsl(){

        QMember m = new QMember("m");

        List<Tuple> result = queryFactory
                .select(m.username, Expressions.constant("CALL"))       // CALL 상수를 username 뒤에 더해준다.
                .from(m)
                .fetch();

        Tuple results = result.get(0);

        System.out.println(results);
    }

    @Test
    public void constantQuerydsl2(){

        QMember m = new QMember("m");

        List<String> result = queryFactory
                .select(m.username.concat("_").concat(m.age.stringValue()).prepend("HELLO_"))
                .from(m)
                .fetch();
                                                    // username뒤에 _ 더하고 나이를 붙여준다. 앞에는 HELLO_를 더한다.
        String results = result.get(0);
        String results2 = result.get(1);

        System.out.println(results);
        System.out.println(results2);
    }


}
