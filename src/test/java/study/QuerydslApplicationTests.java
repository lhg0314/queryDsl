package study;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import javax.persistence.EntityManager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

import com.querydsl.jpa.impl.JPAQueryFactory;

import study.entity.Hello;
import study.entity.Member;
import study.entity.QHello;
import study.entity.Team;

@SpringBootTest
@Transactional

class QuerydslApplicationTests {
	
	@Autowired EntityManager em;

	@Test
	void contextLoads() {
		
		Hello hello =new Hello();
		em.persist(hello);
		
		JPAQueryFactory query =new JPAQueryFactory(em);
		QHello qhello = new QHello("h");
		
		Hello result = query.selectFrom(qhello).fetchOne();
		assertThat(result).isEqualTo(hello);
	}
	
	
	@Test
	public void TestMember() {
		Team teamA = new Team("teamA");
		 Team teamB = new Team("teamB");
		 em.persist(teamA);
		 em.persist(teamB);
		 Member member1 = new Member("member1", 10, teamA);
		 Member member2 = new Member("member2", 20, teamA);
		 Member member3 = new Member("member3", 30, teamB);
		 Member member4 = new Member("member4", 40, teamB);
		 em.persist(member1);
		 em.persist(member2);
		 em.persist(member3);
		 em.persist(member4);
		 //초기화
		 em.flush();
		 em.clear();
		 //확인
		 List<Member> members = em.createQuery("select m from Member m",
		Member.class)
		 .getResultList();
		 for (Member member : members) {
		 System.out.println("member=" + member);
		 System.out.println("-> member.team=" + member.getTeam());
		 }
	}

}
