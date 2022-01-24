package study;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.from;
import static study.entity.QMember.*;
import static study.entity.QTeam.*;

import java.util.List;

import javax.persistence.EntityManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;

import study.dto.MemberDto;
import study.dto.QMemberDto;
import study.entity.Member;
import study.entity.QMember;
import study.entity.Team;

/**
 * @author LEE HYO JIN
 *
 */
/**
 * @author LEE HYO JIN
 *
 */
@SpringBootTest
@Transactional
@Commit
public class QuerydslBasicTest {
	
	@Autowired EntityManager em;
	JPAQueryFactory queryFactory;
	
	@BeforeEach
	public void before() {
		
		queryFactory = new JPAQueryFactory(em);
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

		em.flush();
		em.clear();
	}
	
	@Test
	public void startJPQL() {
		Member findMember = em.createQuery("select m from Member m where m.username = :username",Member.class)
		.setParameter("username", "member1")
		.getSingleResult();
		
		assertThat(findMember.getUsername()).isEqualTo("member1");
	}
	
	@Test
	public void startQuerydsl() {
		
//		QMember m = new QMember("m"); //별칭을 사용하는 방법, 같은테이블 조인할 경우 구분을 위해 사용권장
//		QMember m = QMember.member; // 인스턴스 사용
		
		Member findMember = queryFactory
			.select(member) //static import사용
			.from(member)
			.where(member.username.eq("member1")) //파라미터 바인딩
			.fetchOne();
		
		assertThat(findMember.getUsername()).isEqualTo("member1");
		
	}
	
	@Test
	public void search() {
		
		Member findMember = queryFactory
			.select(member) //static import사용
			.from(member)
			.where(member.username.eq("member1")
					.and(member.age.between(10,30))) //파라미터 바인딩
			.fetchOne();
		
		/*검색조건 제공
		 * eq
		 * ne
		 * isNotNull
		 * in
		 * notIn
		 * between
		 * age.goe(30)  age>=30
		 * gt(>)
		 * loe(<=)
		 * lt(<)
		 * like
		 * startWith 'member%'
		 * Endwith '%member'
		 * 
		 * 
		 * */
		
		assertThat(findMember.getUsername()).isEqualTo("member1");
		
	}
	
	@Test
	public void searchAndParam() {
		
		Member findMember = queryFactory
			.select(member) //static import사용
			.from(member)
			.where(member.username.eq("member1"), //and나 (,)로 체인을 이어갈 수 있다
					member.age.between(10,30)) //파라미터 바인딩
			.fetchOne();
	}
	
	@Test
	public void resultFetch() {
		
//		List<Member> fetch = queryFactory	
//						.selectFrom(member)
//						.fetch(); //list 반환
//		
//		Member fetchOne = queryFactory	
//				.selectFrom(member)
//				.fetchOne(); // 단건 조회
//		
//		Member fetchFirst = queryFactory
//				.selectFrom(member)
//				.fetchFirst(); 첫번째 값 단건조회
		
//		QueryResults<Member> result = queryFactory
//			.selectFrom(member)
//			.fetchResults(); //페이징 정보 포함
//		
//		result.getTotal();
//		result.getResults();
		
		Long total = queryFactory.selectFrom(member).fetchCount();
	}
	
	@Test
	public void sort() {
		
		/*
		 * 나이 내림차순
		 * 이름 올림차순
		 * 이름 없으면 마지막
		 * */
		
		em.persist(new Member(null, 100));
		em.persist(new Member("member5", 100));
		em.persist(new Member("member6", 100));
		
		List<Member> result = queryFactory
			.selectFrom(member)
			.where(member.age.eq(100))
			.orderBy(member.age.desc(),member.username.asc().nullsLast())
			.fetch();
		
		 Member member5 = result.get(0);
		 Member member6 = result.get(1);
		 Member memberNull = result.get(2);
		 assertThat(member5.getUsername()).isEqualTo("member5");
		 assertThat(member6.getUsername()).isEqualTo("member6");
		 assertThat(memberNull.getUsername()).isNull();
	}
	
	@Test
	public void paging() {
		
		List<Member> findMember = queryFactory
			.selectFrom(member) 
			.orderBy(member.username.desc())
			.offset(1)
			.limit(2)
			.fetch();
		
		System.out.println(findMember.toString());
	}
	@Test
	public void aggregation() {
		
		List<Tuple> result = queryFactory
			.select(member.count(),
					member.age.sum(),
					member.age.avg(),
					member.age.max(),
					member.age.min()
					) 
			.from(member)
			.fetch();
		
		Tuple tuple = result.get(0);
		assertThat(tuple.get(member.count())).isEqualTo(4);
		assertThat(tuple.get(member.age.sum())).isEqualTo(100);
		assertThat(tuple.get(member.age.avg())).isEqualTo(25);
		assertThat(tuple.get(member.age.max())).isEqualTo(40);


	}
	
	
	//팀의 이름과 각팀의 평균연령을 구해라
	@Test
	public void groupBy() {
		
		List<Tuple> result = queryFactory
			.select(team.name,member.age.avg()) 
			.from(member)
			.join(member.team, team)
			.groupBy(team.name)
			.fetch();
		
		Tuple teamA = result.get(0);
		Tuple teamB = result.get(1);
		
		assertThat(teamA.get(team.name)).isEqualTo("teamA");
		assertThat(teamA.get(member.age.avg())).isEqualTo(15);
		assertThat(teamB.get(team.name)).isEqualTo("teamB");
		assertThat(teamB.get(member.age.avg())).isEqualTo(35);
		
	}


	@Test
	public void join() {
		
		List<Member> findMember = queryFactory
			.selectFrom(member) 
			.join(member.team,team)
			.where(team.name.eq("teamA"))
			.fetch();
	}
	
	//회원의 이름이 팀이름과 같은 회원 조회
	//연관관계 없은 조인
	@Test
	public void theta_join() {
		
		em.persist(new Member("teamA"));
		em.persist(new Member("teamB"));
		
		List<Member> findMember = queryFactory
			.select(member) 
			.from(member,team)
			.where(member.username.eq(team.name))
			.fetch();
	}
	
	/*
	 * 회원과 팀을 조인하면서 팀 이름이 teamA인 팀만 조인 , 회원은 모두조회
	 * select m,t from Member m left join m.team t on t.name = 'teamA'
	 * */
	@Test
	public void join_on_filter() {
		
		
		
		List<Tuple> findMember = queryFactory
			.select(member,team) 
			.from(member)
			.leftJoin(member.team,team).on(team.name.eq("teamA"))
			.fetch();
		
		for(Tuple tuple : findMember) {
			System.out.println("tuple: "+ tuple);
		}
	}
	/*
	 * 연관관계없는 엔티티 외부조인
	 * */
	@Test
	public void join_on_no_relate() {
		
		em.persist(new Member("teamA"));
		em.persist(new Member("teamB"));
		em.persist(new Member("teamC"));
		
		List<Tuple> findMember = queryFactory
			.select(member,team) 
			.from(member) //on 조인 할때는 이렇게
			.leftJoin(team).on(member.username.eq(team.name))
			.fetch();
		
		for(Tuple tuple : findMember) {
			System.out.println("tuple: "+ tuple);
		}
	}
	
	@Test
	public void no_fetch() {
		em.flush();
		em.clear();
		
		Member findMember = queryFactory
			.selectFrom(member)
			.where(member.username.eq("member1"))
			.fetchOne();// team은 없는 상태
		
		
	}
	
	@Test
	public void fetchJoinUse () {
		em.flush();
		em.clear();
		
		Member findMember = queryFactory
			.selectFrom(member)
			.join(member.team,team).fetchJoin()
			.where(member.username.eq("member1"))
			.fetchOne();// team도 함께 가져옴
		
		System.out.println(findMember.getTeam().getName());
		
	}
	
	/*
	 * 나이가 가장 많은 회원을 조회
	 * */
	@Test
	public void subQuery() {
		QMember memberSub = new QMember("memberSub");
		
		List<Member> result = queryFactory
			.selectFrom(member)
			.where(member.age.eq(
					JPAExpressions.select(memberSub.age.max()).from(memberSub)))
			.fetch();
		
		assertThat(result).extracting("age")
		 .containsExactly(40);
	}
	
	

	/*
	 * 나이가 평균이상인 회원 조회
	 * */
	@Test
	public void subQuery2() {
		QMember memberSub = new QMember("memberSub");
		
		List<Member> result = queryFactory
			.selectFrom(member)
			.where(member.age.goe(
					JPAExpressions.select(memberSub.age.avg()).from(memberSub)))
			.fetch();
		
		for(Member m : result) {
			System.out.println(m.toString());
		}
		
		
	}
	
	@Test
	public void selectSubQuery() {
		QMember memberSub = new QMember("memberSub");
		List<Tuple> result = queryFactory
			.select(member.username,
					JPAExpressions
					.select(memberSub.age.avg())
					.from(memberSub))
			.from(member)
			.fetch();
		
		for (Tuple tuple : result) {
			 System.out.println("username = " + tuple.get(member.username));
			 System.out.println("age = " +
			tuple.get(JPAExpressions.select(memberSub.age.avg())
			 .from(memberSub)));
			}
	}
	
	@Test
	public void basicCase() {
		List<String> result = queryFactory
			.select(member.age
					.when(10).then("열살")
					.when(20).then("스무살")
					.otherwise("기타")).from(member).fetch();
	}
	
	@Test
	public void complexCase() {
		List<String> result = queryFactory
				.select(new CaseBuilder()
						.when(member.age.between(0, 20)).then("0~20살")
						.when(member.age.between(21, 30)).then("21~30살")
						.otherwise("기타"))
				.from(member)
				.fetch();
	}
	
	@Test
	public void contant() {
		List<Tuple> result = queryFactory
			.select(member.username,Expressions.constant("A"))
			.from(member)
			.fetch();
		
		for(Tuple tuple :result) {
			System.out.println("tuple:"+tuple);
		}
	}
	
	@Test
	public void concat() {
		List<String> result = queryFactory
			.select(member.username.concat("_").concat(member.age.stringValue()))
			.from(member)
			.where(member.username.eq("member1"))
			.fetch();
		
		for(String s : result) {
			System.out.println("result    "+s);
		}
	}
	
	//프로젝션 : select대상
	
	@Test
	public void findDtoBySetter() { //setter를 사용해서 조회
		List<MemberDto> result = queryFactory
			.select(Projections.bean(MemberDto.class,
					member.username,//.as("name")넣어야할 이름이 다를때 as로 지칭해준다, 서브쿼리의 경우 두번째 파라미터로 alias지정
					member.age))
			.from(member)
			.fetch();
		
		for(MemberDto md : result) {
			System.out.println("username   "+md.getUsername());
			System.out.println("age   "+md.getAge());
		}
	}
	
	
	@Test
	public void findDtoByField() { //필드에 직접 데이터 삽입
		List<MemberDto> result = queryFactory
			.select(Projections.fields(MemberDto.class,
					member.username,
					member.age))
			.from(member)
			.fetch();
		
		for(MemberDto md : result) {
			System.out.println("username   "+md.getUsername());
			System.out.println("age   "+md.getAge());
		}
	}
	
	@Test
	public void findDtoByConstructor() { // 생성자로 조회, 타입을 보고 들어간다
		List<MemberDto> result = queryFactory
			.select(Projections.constructor(MemberDto.class,
					member.username,
					member.age))
			.from(member)
			.fetch();
		
		for(MemberDto md : result) {
			System.out.println("username   "+md.getUsername());
			System.out.println("age   "+md.getAge());
		}
	}
	
	@Test
	public void findDtoByQueryProjection() { //dto가 querydsl에 의존하게 된다.
		List<MemberDto> result = queryFactory
			.select(new QMemberDto(member.username,member.age))
			.from(member)
			.fetch();
		
		for(MemberDto md : result) {
			System.out.println("username   "+md.getUsername());
			
		}
			
			
	}
	
	@Test
	public void dynamicQuery_BooleanBuilder() {
		String usernameParam = "member1";
		Integer ageParam = null;
		
		List<Member> result = searchMember1(usernameParam,ageParam);
	}

	private List<Member> searchMember1(String usernameParam, Integer ageParam) {
		
		BooleanBuilder builder = new BooleanBuilder();
		if(usernameParam != null) {
			builder.and(member.username.eq(usernameParam));
		}
		
		if(ageParam != null) {
			builder.and(member.age.eq(ageParam));
		}
		
		return queryFactory
			.selectFrom(member)
			.where(builder)
			.fetch();
	}
	
	@Test
	public void dynamicQuery_whereParam() {
		String usernameParam = "member1";
		Integer ageParam = null;
		
		List<Member> result = searchMember2(usernameParam,ageParam);
	}

	private List<Member> searchMember2(String usernameParam, Integer ageParam) {
		// TODO Auto-generated method stub
		return queryFactory
				.selectFrom(member)
				.where(usernameEq(usernameParam), ageEq(ageParam))
				.fetch();
	}

	private BooleanExpression ageEq(Integer ageParam) {
		return ageParam != null ? member.age.eq(ageParam) : null;
	}

	private BooleanExpression usernameEq(String usernameParam) {
		if(usernameParam == null) {
			return null;
		}
		return member.username.eq(usernameParam);
	}
	
	@Test
	public void bulkUpdate() {
		long count = queryFactory
			.update(member)
			.set(member.username, "비회원")
			.where(member.age.lt(28))
			.execute(); // 벌크연산은 영속성 컨텍스트를 무시하고 쿼리를 바로 실행한다. db와 영속성 컨텍스트가 안맞는다
		
		
		
	}
	
	@Test 
	public void bulkAdd() {
		queryFactory	
			.update(member)
			.set(member.age, member.age.add(1))
			.execute();
		
			
	}
	
	@Test
	public void bulkDelete() {
		queryFactory
			.delete(member)
			.where(member.age.gt(18))
			.execute();
		
	}
	
	@Test
	public void sqlFunction() {
		List<String> result = queryFactory
			.select(
					Expressions.stringTemplate("function('replace',{0},{1},{2})",
							member.username,"member","M"))
			.from(member)
			.fetch();
		
		for(String s : result) {
			System.out.println("result  : "+ s);
		}
	}
	
	@Test
	public void sqlFunction2() {
		List<String> result = queryFactory
				.select(member.username)
				.from(member)
				.where(member.username.eq(
						Expressions.stringTemplate("function('lower',{0})", member.username)))
						//member.username.lower() 도 같은 기능
				.fetch();
			
			for(String s : result) {
				System.out.println("result  : "+ s);
			}
	}
	

}
