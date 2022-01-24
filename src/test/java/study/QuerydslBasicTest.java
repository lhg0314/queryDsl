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
		
//		QMember m = new QMember("m"); //��Ī�� ����ϴ� ���, �������̺� ������ ��� ������ ���� ������
//		QMember m = QMember.member; // �ν��Ͻ� ���
		
		Member findMember = queryFactory
			.select(member) //static import���
			.from(member)
			.where(member.username.eq("member1")) //�Ķ���� ���ε�
			.fetchOne();
		
		assertThat(findMember.getUsername()).isEqualTo("member1");
		
	}
	
	@Test
	public void search() {
		
		Member findMember = queryFactory
			.select(member) //static import���
			.from(member)
			.where(member.username.eq("member1")
					.and(member.age.between(10,30))) //�Ķ���� ���ε�
			.fetchOne();
		
		/*�˻����� ����
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
			.select(member) //static import���
			.from(member)
			.where(member.username.eq("member1"), //and�� (,)�� ü���� �̾ �� �ִ�
					member.age.between(10,30)) //�Ķ���� ���ε�
			.fetchOne();
	}
	
	@Test
	public void resultFetch() {
		
//		List<Member> fetch = queryFactory	
//						.selectFrom(member)
//						.fetch(); //list ��ȯ
//		
//		Member fetchOne = queryFactory	
//				.selectFrom(member)
//				.fetchOne(); // �ܰ� ��ȸ
//		
//		Member fetchFirst = queryFactory
//				.selectFrom(member)
//				.fetchFirst(); ù��° �� �ܰ���ȸ
		
//		QueryResults<Member> result = queryFactory
//			.selectFrom(member)
//			.fetchResults(); //����¡ ���� ����
//		
//		result.getTotal();
//		result.getResults();
		
		Long total = queryFactory.selectFrom(member).fetchCount();
	}
	
	@Test
	public void sort() {
		
		/*
		 * ���� ��������
		 * �̸� �ø�����
		 * �̸� ������ ������
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
	
	
	//���� �̸��� ������ ��տ����� ���ض�
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
	
	//ȸ���� �̸��� ���̸��� ���� ȸ�� ��ȸ
	//�������� ���� ����
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
	 * ȸ���� ���� �����ϸ鼭 �� �̸��� teamA�� ���� ���� , ȸ���� �����ȸ
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
	 * ����������� ��ƼƼ �ܺ�����
	 * */
	@Test
	public void join_on_no_relate() {
		
		em.persist(new Member("teamA"));
		em.persist(new Member("teamB"));
		em.persist(new Member("teamC"));
		
		List<Tuple> findMember = queryFactory
			.select(member,team) 
			.from(member) //on ���� �Ҷ��� �̷���
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
			.fetchOne();// team�� ���� ����
		
		
	}
	
	@Test
	public void fetchJoinUse () {
		em.flush();
		em.clear();
		
		Member findMember = queryFactory
			.selectFrom(member)
			.join(member.team,team).fetchJoin()
			.where(member.username.eq("member1"))
			.fetchOne();// team�� �Բ� ������
		
		System.out.println(findMember.getTeam().getName());
		
	}
	
	/*
	 * ���̰� ���� ���� ȸ���� ��ȸ
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
	 * ���̰� ����̻��� ȸ�� ��ȸ
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
					.when(10).then("����")
					.when(20).then("������")
					.otherwise("��Ÿ")).from(member).fetch();
	}
	
	@Test
	public void complexCase() {
		List<String> result = queryFactory
				.select(new CaseBuilder()
						.when(member.age.between(0, 20)).then("0~20��")
						.when(member.age.between(21, 30)).then("21~30��")
						.otherwise("��Ÿ"))
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
	
	//�������� : select���
	
	@Test
	public void findDtoBySetter() { //setter�� ����ؼ� ��ȸ
		List<MemberDto> result = queryFactory
			.select(Projections.bean(MemberDto.class,
					member.username,//.as("name")�־���� �̸��� �ٸ��� as�� ��Ī���ش�, ���������� ��� �ι�° �Ķ���ͷ� alias����
					member.age))
			.from(member)
			.fetch();
		
		for(MemberDto md : result) {
			System.out.println("username   "+md.getUsername());
			System.out.println("age   "+md.getAge());
		}
	}
	
	
	@Test
	public void findDtoByField() { //�ʵ忡 ���� ������ ����
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
	public void findDtoByConstructor() { // �����ڷ� ��ȸ, Ÿ���� ���� ����
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
	public void findDtoByQueryProjection() { //dto�� querydsl�� �����ϰ� �ȴ�.
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
			.set(member.username, "��ȸ��")
			.where(member.age.lt(28))
			.execute(); // ��ũ������ ���Ӽ� ���ؽ�Ʈ�� �����ϰ� ������ �ٷ� �����Ѵ�. db�� ���Ӽ� ���ؽ�Ʈ�� �ȸ´´�
		
		
		
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
						//member.username.lower() �� ���� ���
				.fetch();
			
			for(String s : result) {
				System.out.println("result  : "+ s);
			}
	}
	

}
