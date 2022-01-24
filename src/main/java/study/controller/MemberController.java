package study.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import study.dto.MemberSearchCondition;
import study.dto.MemberTeamDto;
import study.repository.MemberJpaRepository;

@RestController
@RequiredArgsConstructor
public class MemberController {
	
	private final MemberJpaRepository memberJpaRepository;
	
	@GetMapping("/v1/members")
	public List<MemberTeamDto> searchMember1(MemberSearchCondition cond){
		return memberJpaRepository.search(cond);
	}

}
