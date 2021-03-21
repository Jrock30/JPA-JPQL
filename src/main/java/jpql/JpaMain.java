package jpql;

import javax.persistence.*;
import java.util.List;

/**
 * JPQL 문법
 *   - select m from Member as m where m.age > 18
 *   - 엔티티와 속성은 대소문자 구분O (Member, age)
 *   - JPQL 키워드는 대소문자 구분X (SELECT, FROM, where)
 *   - 엔티티 이름 사용, 테이블 이름이 아님(Member)
 *   - 별칭은 필수(m) (as는 생략가능)
 *
 * 결과 조회 API
 *   - query.getResultList(): 결과가 하나 이상일 때, 리스트 반환 query.getSingleResult(): 결과가 정확히 하나, 단일 객체 반환
 *   - 결과가 없으면 빈리스트 반환
 *   - 결과가 없으면: javax.persistence.NoResultException
 *   - 둘 이상이면: javax.persistence.NonUniqueResultException
 *
 * 프로젝션
 *   - SELECT 절에 조회할 대상을 지정하는 것
 *   - 프로젝션 대상: 엔티티, 임베디드 타입, 스칼라 타입(숫자, 문자등 기본 데이터 타입)
 *   - SELECT m FROM Member m -> 엔티티 프로젝션
 *   - SELECT m.team FROM Member m -> 엔티티 프로젝션 (inner 조인해서 가져옴) (묵시적 조인)
 *      - (select t from Member m join m.team t, Team.class) 왠만하면 SQL 과 비슷하게 보이도록 조인하자. (명시적 조인)
 *   - SELECT m.address FROM Member m -> 임베디드 타입 프로젝션
 *   - SELECT m.username, distinct m.age FROM Member m -> 스칼라 타입 프로젝션
 *   - DISTINCT로 중복 제거
 *
 * 프로젝션 - 여러 값 조회
 *   - SELECT m.username, m.age FROM Member m
 *   - 1. Query 타입으로 조회
 *   - 2. Object[] 타입으로 조회
 *   - 3. new 명령어로 조회
 *      - 단순 값을 DTO로 바로 조회
 *      - SELECT new jpabook.jpql.UserDTO(m.username, m.age) FROM Member m
 *      - 패키지명을 포함한 전체 클래스명 입력
 *      - 순서와 타입이 일치하는 생성자 필요
 *
 * 페이징 API
 *   - JPA는 페이징을 다음 두 API로 추상화
 *   - setFirstResult(int startPosition) : 조회 시작 위치(0부터 시작)
 *   - setMaxResults(int maxResult) : 조회할 데이터 수
 *
 * 조인
 * 내부 조인: SELECT m FROM Member m [INNER] JOIN m.team t
 * 외부 조인: SELECT m FROM Member m LEFT [OUTER] JOIN m.team t
 * 세타 조인: select count(m) from Member m, Team t where m.username = t.name
 *
 * 조인 - ON 절
 *   ON절을 활용한 조인(JPA 2.1부터 지원)
 *    - 1.조인대상필터링
 *    - 2.연관관계 없는 엔티티 외부 조인(하이버네이트 5.1부터)
 *
 * 조인 대상 필터링
 *   - 예) 회원과 팀을 조인하면서, 팀 이름이 A인 팀만 조인
 *   - JPQL: SELECT m, t FROM Member m LEFT JOIN m.team t on t.name = 'A'
 *   - SQL: SELECT m.*, t.* FROM Member m LEFT JOIN Team t ON m.TEAM_ID=t.id and t.name='A'
 *
 * 연관관계 없는 엔티티 외부 조인
 *   - 예) 회원의 이름과 팀의 이름이 같은 대상 외부 조인
 *   - JPQL: SELECT m, t FROM Member m LEFT JOIN Team t on m.username = t.name
 *   - SQL: SELECT m.*, t.* FROM Member m LEFT JOIN Team t ON m.username = t.name
 *
 * 서브 쿼리
 *   - 나이가 평균보다 많은 회원
 *     - select m from Member m where m.age > (select avg(m2.age) from Member m2)
 *   - 한 건이라도 주문한고객
 *     - select m from Member m where (select count(o) from Order o where m = o.member) > 0
 *
 * 서브 쿼리 지원 함수
 *   - [NOT] EXISTS (subquery): 서브쿼리에 결과가 존재하면 참
 *     - {ALL | ANY | SOME} (subquery)
 *     - ALL 모두 만족하면 참
 *     - ANY, SOME: 같은 의미, 조건을 하나라도 만족하면 참
 *   - [NOT] IN (subquery): 서브쿼리의 결과 중 하나라도 같은 것이 있으면 참
 *
 * 서브 쿼리 - 예제
 *   - 팀A 소속인 회원
 *       select m from Member m where exists (select t from m.team t where t.name = ‘팀A')
 *   - 전체 상품 각각의 재고보다 주문량이 많은 주문들
 *       select o from Order o where o.orderAmount > ALL (select p.stockAmount from Product p)
 *   - 어떤 팀이든 팀에 소속된 회원 select m from Member m where m.team = ANY (select t from Team t)
 *
 * JPA 서브 쿼리 한계
 *   - JPA는 WHERE, HAVING 절에서만 서브 쿼리 사용 가능
 *   - SELECT 절도 가능(하이버네이트에서 지원)
 *   - FROM 절의 서브 쿼리는 현재 JPQL에서 불가능
 *   - 조인으로 풀 수 있으면 풀어서 해결
 *
 * JPQL 타입 표현
 *   - 문자: ‘HELLO’, ‘She’’s’
 *   - 숫자: 10L(Long), 10D(Double), 10F(Float)
 *   - Boolean: TRUE, FALSE
 *   - ENUM: jpabook.MemberType.Admin (패키지명 포함)
 *   - 엔티티 타입: TYPE(m) = Member (상속 관계에서 사용)
 */
public class JpaMain {

    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();



        tx.begin();

        try {

            for (int i = 0; i < 100; i++) {
                Member member = new Member();
                member.setUsername("member" + i);
                member.setAge(i);
                em.persist(member);
            }


            // 조회 후 수정 하면 영속성 컨텍스트에 포함 되어서 update 쿼리 날라간다.
            // 반환 타입이 명확할 떄
            TypedQuery<Member> query = em.createQuery("select m from Member m", Member.class);
            TypedQuery<String> query2 = em.createQuery("select m.username from Member m where m.id = 10", String.class);
            // 값이 여러개 일 떄 ( 결과가 없을 때 빈 배열로 떨어짐 )
            List<Member> resultList = query.getResultList();
            // 값이 한개 일 떄 ( 결과가 정확히 하나가 나와야 함, 결과가 없거나 두 개 이상이면 Exception 터짐)
            String singleResult = query2.getSingleResult();

            for (Member member1 : resultList) {
                System.out.println("member1 = " + member1);
            }

            // 반환 타입이 명확하지 않을 때
            Query query3 = em.createQuery("select m.username, m.age from Member m");

            // 파라미터 바인딩
            Member result4 = em.createQuery("select m from Member m where m.username = :username", Member.class)
                    .setParameter("username", "member1")
                    .getSingleResult();
            System.out.println("result4 = " + result4);

            em.flush();
            em.close();

            // 단순 값을 DTO로 단순 조회, 생성자를 통해 호출 된다.
            List<MemberDto> result5 = (List<MemberDto>) em.createQuery("select new jpql.MemberDto(m.username, m.age) from Member m", MemberDto.class);

            em.flush();
            em.close();

            // 페이징
            List<Member> result7 = em.createQuery("select m from Member m order by m.age desc", Member.class)
                    .setFirstResult(1)
                    .setMaxResults(10)
                    .getResultList();

            System.out.println("result7 = " + result7.size());

            for (Member member1 : result7) {
                System.out.println("member1 = " + member1);
            }


            tx.commit();
        } catch (Exception e) {
            tx.rollback();
        } finally {
            em.close();
        }
        emf.close();
    }
}
