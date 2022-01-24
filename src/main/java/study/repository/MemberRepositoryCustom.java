package study.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import study.dto.MemberSearchCondition;
import study.dto.MemberTeamDto;

public interface MemberRepositoryCustom {

	List<MemberTeamDto> search(MemberSearchCondition cond);
	Page<MemberTeamDto> searchPageSimple(MemberSearchCondition cond,Pageable pageable);
	Page<MemberTeamDto> searchPageComplex(MemberSearchCondition cond,Pageable pageable);
}
