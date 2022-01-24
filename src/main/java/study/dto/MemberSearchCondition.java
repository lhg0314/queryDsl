package study.dto;

import lombok.Data;

@Data
public class MemberSearchCondition {

	private String username;
	private String teamName;
	private Integer ageGeo;
	private Integer ageLoe;
	
	
}
