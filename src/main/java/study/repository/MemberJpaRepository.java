package study.repository;

import static study.entity.QMember.*;
import static study.entity.QTeam.*;

import java.util.List;
import java.util.Optional;

import javax.persistence.EntityManager;

import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;

import ch.qos.logback.core.joran.conditional.Condition;
import study.dto.MemberSearchCondition;
import study.dto.MemberTeamDto;
import study.dto.QMemberTeamDto;
import study.entity.Member;
import study.entity.QMember;

@Repository
public class MemberJpaRepository {
	
	private final EntityManager em;
	private final JPAQueryFactory queryFactory;
	
	public MemberJpaRepository(EntityManager em) {
		this.em = em;
		this.queryFactory = new JPAQueryFactory(em);
	}
	
	public void save(Member member) {
		
		em.persist(member);
			
		
	}

	public Optional<Member> findById(Long id){
		Member members = em.find(Member.class, id);
		return Optional.ofNullable(members);
	}
	
	public List<Member> findAll(){
		return
				queryFactory.selectFrom(member).fetch();
	}
	
	public List<Member> findByUsername(String username){
		return queryFactory
				.selectFrom(member)
				.where(member.username.eq(username))
				.fetch();
	}
	
	public List<MemberTeamDto> searchByBuilder(MemberSearchCondition cond){
		BooleanBuilder builder = new BooleanBuilder();
		if(StringUtils.hasText(cond.getUsername())) {
			builder.and(member.username.eq(cond.getUsername()));
		}
		
		if(StringUtils.hasText(cond.getTeamName())) {
			builder.and(team.name.eq(cond.getTeamName()));
		}
		
		if(cond.getAgeGeo() != null) {
			builder.and(member.age.goe(cond.getAgeGeo()));
		}
		
		if(cond.getAgeLoe() != null) {
			builder.and(member.age.loe(cond.getAgeLoe()));
		}
		
		return queryFactory
				.select(new QMemberTeamDto(
						member.id.as("memberId"),
						member.username,
						member.age,
						team.id.as("teamId"),
						team.name.as("teamName")))
				.from(member)
				.leftJoin(member.team,team)
				.where(builder)
				.fetch();
				
	}
	
	public List<MemberTeamDto> search (MemberSearchCondition cond){
		return queryFactory
				.select(new QMemberTeamDto(
						member.id.as("memberId"),
						member.username,
						member.age,
						team.id.as("teamId"),
						team.name.as("teamName")))
				.from(member)
				.leftJoin(member.team,team)
				.where(
						usernameEq(cond.getUsername()),
						teamNmaeEq(cond.getTeamName()),
						ageGoe(cond.getAgeGeo()),
						ageLoe(cond.getAgeLoe()))
				.fetch();
	}

	private BooleanExpression ageLoe(Integer ageLoe) {
		// TODO Auto-generated method stub
		return ageLoe != null ? member.age.loe(ageLoe) : null;
	}

	private BooleanExpression ageGoe(Integer ageGoe) {
		// TODO Auto-generated method stub
		return ageGoe != null ? member.age.goe(ageGoe) : null;
	}

	private BooleanExpression teamNmaeEq(String teamName) {
		// TODO Auto-generated method stub
		return StringUtils.hasText(teamName) ? team.name.eq(teamName): null;
	}

	private BooleanExpression usernameEq(String username) {
		// TODO Auto-generated method stub
		return StringUtils.hasText(username) ? member.username.eq(username) : null;
	}
}
