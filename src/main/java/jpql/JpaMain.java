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
 *   - SELECT m FROM Member m -> 엔티티 프로젝션 SELECT m.team FROM Member m -> 엔티티 프로젝션
 *   - SELECT m.address FROM Member m -> 임베디드 타입 프로젝션
 *   - SELECT m.username, m.age FROM Member m -> 스칼라 타입 프로젝션 DISTINCT로 중복 제거
 */
public class JpaMain {

    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();



        tx.begin();

        try {

            Member member = new Member();
            member.setUsername("member1");
            member.setAge(10);
            em.persist(member);

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




            tx.commit();
        } catch (Exception e) {
            tx.rollback();
        } finally {
            em.close();
        }
        emf.close();
    }
}
