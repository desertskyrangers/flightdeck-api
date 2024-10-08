package com.desertskyrangers.flightdeck.adapter.web.rest;

import com.desertskyrangers.flightdeck.adapter.web.ApiPath;
import com.desertskyrangers.flightdeck.core.model.Group;
import com.desertskyrangers.flightdeck.core.model.Member;
import com.desertskyrangers.flightdeck.core.model.User;
import com.desertskyrangers.flightdeck.port.StatePersisting;
import com.desertskyrangers.flightdeck.port.StateRetrieving;
import com.desertskyrangers.flightdeck.util.Json;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class MembershipControllerTest extends BaseControllerTest {

	@Autowired
	private StatePersisting statePersisting;

	@Autowired
	private StateRetrieving stateRetrieving;

	@Autowired
	private MockMvc mockMvc;

	@Test
	void testUpdateMembership() throws Exception {
		// given
		User user = statePersisting.upsert( createTestUser() );
		Group group = statePersisting.upsert( createTestGroup( "Group A", Group.Type.CLUB ) );
		statePersisting.upsert( new Member().user( getMockUser() ).group( group ).status( Member.Status.OWNER ) );
		Member membership = statePersisting.upsert( new Member().user( user ).group( group ).status( Member.Status.REQUESTED ) );

		// when
		Map<String, String> request = Map.of( "id", membership.id().toString(), "status", Member.Status.ACCEPTED.title().toLowerCase() );
		this.mockMvc.perform( put( ApiPath.MEMBERSHIP ).with( jwt() ).content( Json.stringify( request ) ).contentType( MediaType.APPLICATION_JSON ) ).andExpect( status().isOk() ).andReturn();

		// then
		Optional<Member> optional = stateRetrieving.findMembership( membership.id() );
		assertThat( optional.isPresent() ).isTrue();
		assertThat( optional.get().status() ).isEqualTo( Member.Status.ACCEPTED );
	}

	@Test
	void testUpdateMembershipWithBadRequest() throws Exception {
		// given
		User user = getMockUser();
		Group group = statePersisting.upsert( createTestGroup( "Group A", Group.Type.CLUB ) );
		Member membership = statePersisting.upsert( new Member().user( user ).group( group ).status( Member.Status.REQUESTED ) );

		// when
		Map<String, String> request = new HashMap<>();
		request.put( "id", membership.id().toString() );
		request.put( "status", "invalid" );
		this.mockMvc.perform( put( ApiPath.MEMBERSHIP ).content( Json.stringify( request ) ).contentType( MediaType.APPLICATION_JSON ) ).andExpect( status().isBadRequest() ).andReturn();
	}

	@Test
	void testUpdateMembership_UserCannotAcceptTheirOwnMembershipRequest() throws Exception {
		// given
		User user = statePersisting.upsert( createTestUser() );
		Group group = statePersisting.upsert( createTestGroup( "Group A", Group.Type.CLUB ) );
		statePersisting.upsert( new Member().user( user ).group( group ).status( Member.Status.OWNER ) );
		Member membership = statePersisting.upsert( new Member().user( getMockUser() ).group( group ).status( Member.Status.REQUESTED ) );

		// when
		Map<String, String> request = Map.of( "id", membership.id().toString(), "status", Member.Status.ACCEPTED.title().toLowerCase() );
		this.mockMvc.perform( put( ApiPath.MEMBERSHIP ).with( jwt() ).content( Json.stringify( request ) ).contentType( MediaType.APPLICATION_JSON ) ).andExpect( status().isUnauthorized() ).andReturn();
	}

	@Test
	void testUpdateMembership_UserCanAcceptTheirOwnMembershipInvitation() throws Exception {
		// given
		User user = statePersisting.upsert( createTestUser() );
		Group group = statePersisting.upsert( createTestGroup( "Group A", Group.Type.CLUB ) );
		statePersisting.upsert( new Member().user( user ).group( group ).status( Member.Status.OWNER ) );
		Member membership = statePersisting.upsert( new Member().user( getMockUser() ).group( group ).status( Member.Status.INVITED ) );

		// when
		Map<String, String> request = Map.of( "id", membership.id().toString(), "status", Member.Status.ACCEPTED.title().toLowerCase() );
		this.mockMvc.perform( put( ApiPath.MEMBERSHIP ).with( jwt() ).content( Json.stringify( request ) ).contentType( MediaType.APPLICATION_JSON ) ).andExpect( status().isOk() ).andReturn();
	}


//	@Test
//	void testUpdateMembership_UserCanRequestMembership() throws Exception {
//		// given
//		User user = statePersisting.upsert( createTestUser() );
//		Group group = statePersisting.upsert( createTestGroup( "Group A", GroupType.CLUB ) );
//		statePersisting.upsert( new Member().user( user ).group( group ).status( MemberStatus.OWNER ) );
//		//Member membership = statePersisting.upsert( new Member().user( getMockUser() ).group( group ).status( MemberStatus.INVITED ) );
//
//		// when
//		Map<String, String> request = Map.of( "status", MemberStatus.REQUESTED.name().toLowerCase() );
//		this.mockMvc.perform( put( ApiPath.MEMBERSHIP ).with( jwt() ).content( Json.stringify( request ) ).contentType( MediaType.APPLICATION_JSON ) ).andExpect( status().isOk() ).andReturn();
//	}

	@Test
	void testCancelMembership() throws Exception {
		// given
		User user = getMockUser();
		Group group = statePersisting.upsert( createTestGroup( "Group A", Group.Type.CLUB ) );
		Member membership = statePersisting.upsert( new Member().user( user ).group( group ).status( Member.Status.REQUESTED ) );

		// when
		Map<String, String> request = new HashMap<>();
		request.put( "id", membership.id().toString() );
		//request.put( "status", MemberStatus.ACCEPTED.name().toLowerCase() );
		this.mockMvc.perform( delete( ApiPath.MEMBERSHIP ).with( jwt() ).content( Json.stringify( request ) ).contentType( MediaType.APPLICATION_JSON ) ).andExpect( status().isOk() ).andReturn();

		// then
		Optional<Member> optional = stateRetrieving.findMembership( membership.id() );
		assertThat( optional ).isEmpty();
	}

	@Test
	void testCancelMembershipWithBadRequest() throws Exception {
		// given
		User user = getMockUser();
		Group group = statePersisting.upsert( createTestGroup( "Group A", Group.Type.CLUB ) );
		statePersisting.upsert( new Member().user( user ).group( group ).status( Member.Status.REQUESTED ) );

		// when
		Map<String, String> request = new HashMap<>();
		request.put( "id", "invalid" );
		this.mockMvc.perform( delete( ApiPath.MEMBERSHIP ).with( jwt() ).content( Json.stringify( request ) ).contentType( MediaType.APPLICATION_JSON ) ).andExpect( status().isBadRequest() ).andReturn();
	}

}
