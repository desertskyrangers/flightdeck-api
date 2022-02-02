package com.desertskyrangers.flightdeck.adapter.state.repo;

import com.desertskyrangers.flightdeck.BaseTest;
import com.desertskyrangers.flightdeck.adapter.state.entity.GroupEntity;
import com.desertskyrangers.flightdeck.adapter.state.entity.MemberEntity;
import com.desertskyrangers.flightdeck.adapter.state.entity.UserEntity;
import com.desertskyrangers.flightdeck.core.model.GroupType;
import com.desertskyrangers.flightdeck.core.model.MemberStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

public class MemberRepoTest extends BaseTest {

	@Autowired
	private UserRepo userRepo;

	@Autowired
	private GroupRepo groupRepo;

	@Autowired
	private MemberRepo memberRepo;

	@Test
	void testCreateAndRetrieve() {
		// given
		UserEntity user = userRepo.save( createTestUserEntity( "antonio", "antonio@example.com" ) );
		GroupEntity group = groupRepo.save( createTestGroupEntity( "Test Group", GroupType.CLUB ) );
		MemberEntity member = memberRepo.save( createTestMemberEntity( user, group, MemberStatus.ACCEPTED ) );

		// when
		MemberEntity actual = memberRepo.findById( member.getId() ).orElse( null );

		// then
		assertThat( actual ).isEqualTo( member );
	}

}
