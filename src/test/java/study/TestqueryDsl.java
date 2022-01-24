package study;

import java.util.List;

import javax.persistence.EntityManager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import study.dto.MemberSearchCondition;
import study.dto.MemberTeamDto;
import study.entity.Member;
import study.entity.Team;
import study.repository.MemberJpaRepository;
import study.repository.MemberRepository;


@SpringBootTest
@Transactional

public class TestqueryDsl {
	
	@Autowired EntityManager em;
	@Autowired MemberJpaRepository memberJpaRepository;
	@Autowired MemberRepository memberRepository;
	
	@Test 
	public void searchTest() {
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
		
		MemberSearchCondition cond = new MemberSearchCondition();
		cond.setAgeGeo(35);
		cond.setAgeLoe(40);
		cond.setTeamName("teamB");
		
		List<MemberTeamDto> result = memberJpaRepository.search(cond);
		System.out.println(result.get(0).getUsername());
	}
	
	@Test
	public void springDataTest() {
		memberRepository.findAll();
	}

}
