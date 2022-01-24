package study.repository;

import static study.entity.QMember.member;
import static study.entity.QTeam.team;

import java.util.List;

import javax.persistence.EntityManager;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.data.repository.support.PageableExecutionUtils;
import org.springframework.util.StringUtils;

import com.querydsl.core.QueryResults;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import study.dto.MemberSearchCondition;
import study.dto.MemberTeamDto;
import study.dto.QMemberTeamDto;
import study.entity.Member;

public class MemberRepositoryImpl 
//extends QuerydslRepositorySupport 
implements MemberRepositoryCustom{

	

	private final JPAQueryFactory queryFactory;
	
	public MemberRepositoryImpl(EntityManager em) {
		this.queryFactory = new JPAQueryFactory(em);
	}
	
//	public MemberRepositoryImpl() {
//		super(Member.class);
//	}
	
	@Override
	public List<MemberTeamDto> search(MemberSearchCondition cond) {
		
//		from(member)
//		.leftJoin(member.team,team)
//		.where(
//				usernameEq(cond.getUsername()),
//				teamNmaeEq(cond.getTeamName()),
//				ageGoe(cond.getAgeGeo()),
//				ageLoe(cond.getAgeLoe()))
//		.select(
//				new QMemberTeamDto(
//						member.id.as("memberId"),
//						member.username,
//						member.age,
//						team.id.as("teamId"),
//						team.name.as("teamName"))
//				)
//		.fetch();
		
		
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

	@Override
	public Page<MemberTeamDto> searchPageSimple(MemberSearchCondition cond, Pageable pageable) {
		QueryResults<MemberTeamDto> result = queryFactory
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
				.offset(pageable.getOffset())
				.limit(pageable.getPageSize())
				.fetchResults();
		
		List<MemberTeamDto> content = result.getResults();
		long total = result.getTotal();
		
		return new PageImpl<>(content,pageable,total);
		
		
	}

	@Override
	public Page<MemberTeamDto> searchPageComplex(MemberSearchCondition cond, Pageable pageable) {
		List<MemberTeamDto> content = queryFactory
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
				.offset(pageable.getOffset())
				.limit(pageable.getPageSize())
				.fetch();
		
		
		/*long total = queryFactory
				.select(member)
				.from(member)
				.leftJoin(member.team,team)
				.where(
						usernameEq(cond.getUsername()),
						teamNmaeEq(cond.getTeamName()),
						ageGoe(cond.getAgeGeo()),
						ageLoe(cond.getAgeLoe()))
				.fetchCount(); //countQuery최적화가 가능하다.
*/		
		
		JPAQuery<Member> countQuery = queryFactory
				.select(member)
				.from(member)
				.leftJoin(member.team,team)
				.where(
						usernameEq(cond.getUsername()),
						teamNmaeEq(cond.getTeamName()),
						ageGoe(cond.getAgeGeo()),
						ageLoe(cond.getAgeLoe()));
				 
		
		
		return PageableExecutionUtils.getPage(content, pageable, () -> countQuery.fetchCount());
		
		//return new PageImpl<>(content,pageable,total);
	}

}
