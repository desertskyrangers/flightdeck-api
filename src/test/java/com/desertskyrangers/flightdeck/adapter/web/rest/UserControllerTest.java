package com.desertskyrangers.flightdeck.adapter.web.rest;

import com.desertskyrangers.flightdeck.adapter.web.ApiPath;
import com.desertskyrangers.flightdeck.adapter.web.jwt.JwtToken;
import com.desertskyrangers.flightdeck.adapter.web.jwt.JwtTokenProvider;
import com.desertskyrangers.flightdeck.adapter.web.model.ReactProfileResponse;
import com.desertskyrangers.flightdeck.adapter.web.model.ReactUser;
import com.desertskyrangers.flightdeck.core.model.*;
import com.desertskyrangers.flightdeck.port.DashboardServices;
import com.desertskyrangers.flightdeck.port.StatePersisting;
import com.desertskyrangers.flightdeck.port.UserServices;
import com.desertskyrangers.flightdeck.util.Json;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith( MockitoExtension.class )
public class UserControllerTest extends BaseControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserServices userServices;

	@Autowired
	private StatePersisting statePersisting;

	@Autowired
	private DashboardServices dashboardServices;

	@Autowired
	private JwtTokenProvider tokenProvider;

	private HttpHeaders headers;

	@BeforeEach
	protected void setup() {
		statePersisting.removeAllFlights();

		super.setup();

		userServices.setDashboardServices( dashboardServices );
		//userServices.setPublicDashboardServices( publicDashboardServices );

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String jwtToken = tokenProvider.createToken( getMockUser(), authentication, false );
		headers = new HttpHeaders();
		headers.add( JwtToken.AUTHORIZATION_HEADER, JwtToken.AUTHORIZATION_TYPE + " " + jwtToken );
	}

	@Test
	void testGetProfile() throws Exception {
		// when
		MvcResult result = this.mockMvc.perform( MockMvcRequestBuilders.get( ApiPath.PROFILE ).with( jwt() ).headers( headers ) ).andExpect( status().isOk() ).andReturn();

		// then
		String accountJson = Json.stringify( new ReactProfileResponse().setAccount( ReactUser.from( getMockUser() ) ) );
		assertThat( result.getResponse().getContentAsString() ).isEqualTo( accountJson );
	}

	@Test
	void testGetAccount() throws Exception {
		// when
		MvcResult result = this.mockMvc.perform( get( ApiPath.USER + "/" + getMockUser().id() ).with( jwt() ).headers( headers ) ).andExpect( status().isOk() ).andReturn();

		// then
		String accountJson = Json.stringify( new ReactProfileResponse().setAccount( ReactUser.from( getMockUser() ) ) );
		assertThat( result.getResponse().getContentAsString() ).isEqualTo( accountJson );
	}

	@Test
	void testUpdateAccount() throws Exception {
		// given
		ReactUser reactAccount = ReactUser.from( getMockUser() );
		reactAccount.setFirstName( "Anton" );
		// ReactUser does not update the name on its own...so it needs to be set as well
		reactAccount.setName( "Anton User" );

		// when
		String content = Json.stringify( reactAccount );
		MvcResult result = this.mockMvc
			.perform( put( ApiPath.USER + "/" + getMockUser().id() ).content( content ).contentType( "application/json" ).with( jwt() ).headers( headers ) )
			.andExpect( status().isOk() )
			.andReturn();

		// then
		String accountJson = Json.stringify( new ReactProfileResponse().setAccount( reactAccount ) );
		String resultContent = result.getResponse().getContentAsString();
		assertThat( resultContent ).isEqualTo( accountJson );
		Map<?, ?> map = Json.asMap( resultContent );
		Map<?, ?> account = (Map<?, ?>)map.get( "account" );
		assertThat( account.get( "firstName" ) ).isEqualTo( "Anton" );
	}

	@Test
	void testUpdatePassword() throws Exception {
		// given
		ReactUser reactAccount = ReactUser.from( getMockUser() );

		// when
		String content = Json.stringify( Map.of( "id", reactAccount.getId(), "currentPassword", "password", "password", "newmockpassword" ) );
		MvcResult result = this.mockMvc
			.perform( put( ApiPath.USER + "/" + getMockUser().id() + "/password" ).content( content ).contentType( "application/json" ).with( jwt() ).headers( headers ) )
			.andExpect( status().isOk() )
			.andReturn();

		// then
	}

	@Test
	void testGetAircraftPage() throws Exception {
		// given
		Aircraft aftyn = new Aircraft().name( "AFTYN" ).type( AircraftType.FIXEDWING ).status( Aircraft.Status.AIRWORTHY ).owner( getMockUser().id() ).ownerType( OwnerType.USER );
		Aircraft bianca = new Aircraft().name( "BIANCA" ).type( AircraftType.FIXEDWING ).status( Aircraft.Status.AIRWORTHY ).owner( getMockUser().id() ).ownerType( OwnerType.USER );
		statePersisting.upsert( aftyn );
		statePersisting.upsert( bianca );

		// when
		MvcResult result = this.mockMvc.perform( get( ApiPath.USER_AIRCRAFT ).param( "pg", "0" ).with( jwt() ) ).andExpect( status().isOk() ).andReturn();

		// then
		Map<String, Object> map = Json.asMap( result.getResponse().getContentAsString() );
		List<?> aircraftList = (List<?>)((Map<?, ?>)map.get( "page" )).get( "content" );
		Map<?, ?> messagesMap = (Map<?, ?>)map.get( "messages" );

		assertThat( aircraftList.size() ).isEqualTo( 2 );
		assertThat( messagesMap ).isNull();

		Map<?, ?> aircraft0 = (Map<?, ?>)aircraftList.get( 0 );
		Map<?, ?> aircraft1 = (Map<?, ?>)aircraftList.get( 1 );
		assertThat( aircraft0.get( "name" ) ).isEqualTo( "AFTYN" );
		assertThat( aircraft1.get( "name" ) ).isEqualTo( "BIANCA" );
	}

	@Test
	void testGetAircraftPageWithSize() throws Exception {
		// given
		Aircraft aftyn = new Aircraft().name( "AFTYN" ).type( AircraftType.FIXEDWING ).status( Aircraft.Status.AIRWORTHY ).owner( getMockUser().id() ).ownerType( OwnerType.USER );
		Aircraft bianca = new Aircraft().name( "BIANCA" ).type( AircraftType.FIXEDWING ).status( Aircraft.Status.AIRWORTHY ).owner( getMockUser().id() ).ownerType( OwnerType.USER );
		statePersisting.upsert( aftyn );
		statePersisting.upsert( bianca );

		// when
		MvcResult result = this.mockMvc.perform( get( ApiPath.USER_AIRCRAFT ).param( "pg", "0" ).param( "pz", "1" ).with( jwt() ) ).andExpect( status().isOk() ).andReturn();

		// then
		Map<String, Object> map = Json.asMap( result.getResponse().getContentAsString() );
		List<?> aircraftList = (List<?>)((Map<?, ?>)map.get( "page" )).get( "content" );
		Map<?, ?> messagesMap = (Map<?, ?>)map.get( "messages" );

		assertThat( aircraftList.size() ).isEqualTo( 1 );
		assertThat( messagesMap ).isNull();

		Map<?, ?> aircraft0 = (Map<?, ?>)aircraftList.get( 0 );
		assertThat( aircraft0.get( "name" ) ).isEqualTo( "AFTYN" );
	}

	@Test
	void testGetAircraftPageWithStatus() throws Exception {
		// given
		Aircraft aftyn = new Aircraft().name( "AFTYN" ).type( AircraftType.FIXEDWING ).status( Aircraft.Status.AIRWORTHY ).owner( getMockUser().id() ).ownerType( OwnerType.USER );
		Aircraft bianca = new Aircraft().name( "BIANCA" ).type( AircraftType.FIXEDWING ).status( Aircraft.Status.DESTROYED ).owner( getMockUser().id() ).ownerType( OwnerType.USER );
		statePersisting.upsert( aftyn );
		statePersisting.upsert( bianca );

		// when
		MvcResult result = this.mockMvc.perform( get( ApiPath.USER_AIRCRAFT ).param( "status", "available" ).param( "pg", "0" ).with( jwt() ) ).andExpect( status().isOk() ).andReturn();

		// then
		Map<String, Object> map = Json.asMap( result.getResponse().getContentAsString() );
		List<?> aircraftList = (List<?>)((Map<?, ?>)map.get( "page" )).get( "content" );
		Map<?, ?> messagesMap = (Map<?, ?>)map.get( "messages" );

		assertThat( aircraftList.size() ).isEqualTo( 1 );
		assertThat( messagesMap ).isNull();

		Map<?, ?> aircraft0 = (Map<?, ?>)aircraftList.get( 0 );
		assertThat( aircraft0.get( "name" ) ).isEqualTo( "AFTYN" );
	}

	@Test
	void testGetBatteryPage() throws Exception {
		// given
		Battery a = new Battery().name( "A" ).status( Battery.Status.AVAILABLE ).owner( getMockUser().id() ).ownerType( OwnerType.USER );
		Battery b = new Battery().name( "B" ).status( Battery.Status.NEW ).owner( getMockUser().id() ).ownerType( OwnerType.USER );
		statePersisting.upsert( a );
		statePersisting.upsert( b );

		// when
		MvcResult result = this.mockMvc.perform( get( ApiPath.USER_BATTERY ).param( "pg", "0" ).with( jwt() ) ).andExpect( status().isOk() ).andReturn();

		// then
		Map<String, Object> map = Json.asMap( result.getResponse().getContentAsString() );
		List<?> batteryList = (List<?>)((Map<?, ?>)map.get( "page" )).get( "content" );
		Map<?, ?> messagesMap = (Map<?, ?>)map.get( "messages" );

		assertThat( batteryList.size() ).isEqualTo( 2 );
		assertThat( messagesMap ).isNull();

		Map<?, ?> battery0 = (Map<?, ?>)batteryList.get( 0 );
		Map<?, ?> battery1 = (Map<?, ?>)batteryList.get( 1 );
		assertThat( battery0.get( "name" ) ).isEqualTo( "A" );
		assertThat( battery1.get( "name" ) ).isEqualTo( "B" );
	}

	@Test
	void testGetBatteryPageWithSize() throws Exception {
		// given
		Battery a = new Battery().name( "A" ).status( Battery.Status.AVAILABLE ).owner( getMockUser().id() ).ownerType( OwnerType.USER );
		Battery b = new Battery().name( "B" ).status( Battery.Status.NEW ).owner( getMockUser().id() ).ownerType( OwnerType.USER );
		statePersisting.upsert( a );
		statePersisting.upsert( b );

		// when
		MvcResult result0 = this.mockMvc.perform( get( ApiPath.USER_BATTERY ).param( "pg", "0" ).param( "pz", "1" ).with( jwt() ) ).andExpect( status().isOk() ).andReturn();
		MvcResult result1 = this.mockMvc.perform( get( ApiPath.USER_BATTERY ).param( "pg", "1" ).param( "pz", "1" ).with( jwt() ) ).andExpect( status().isOk() ).andReturn();

		// then
		Map<String, Object> map = Json.asMap( result0.getResponse().getContentAsString() );
		Map<?, ?> page = ((Map<?, ?>)map.get( "page" ));
		List<?> batteryList = (List<?>)page.get( "content" );
		Map<?, ?> messagesMap = (Map<?, ?>)map.get( "messages" );

		int totalPages = Integer.parseInt( String.valueOf( page.get( "totalPages" ) ) );

		assertThat( batteryList.size() ).isEqualTo( 1 );
		assertThat( messagesMap ).isNull();
		assertThat( totalPages ).isEqualTo( 2 );

		Map<?, ?> battery0 = (Map<?, ?>)batteryList.get( 0 );
		assertThat( battery0.get( "name" ) ).isEqualTo( "A" );
	}

	@Test
	void testGetBatteryPageWithStatus() throws Exception {
		// given
		Battery a = new Battery().name( "A" ).status( Battery.Status.AVAILABLE ).owner( getMockUser().id() ).ownerType( OwnerType.USER );
		Battery b = new Battery().name( "B" ).status( Battery.Status.DESTROYED ).owner( getMockUser().id() ).ownerType( OwnerType.USER );
		statePersisting.upsert( a );
		statePersisting.upsert( b );

		// when
		MvcResult result = this.mockMvc.perform( get( ApiPath.USER_BATTERY ).param( "status", "available" ).param( "pg", "0" ).with( jwt() ) ).andExpect( status().isOk() ).andReturn();

		// then
		Map<String, Object> map = Json.asMap( result.getResponse().getContentAsString() );
		List<?> batteryList = (List<?>)((Map<?, ?>)map.get( "page" )).get( "content" );
		Map<?, ?> messagesMap = (Map<?, ?>)map.get( "messages" );

		assertThat( batteryList.size() ).isEqualTo( 1 );
		assertThat( messagesMap ).isNull();

		Map<?, ?> battery0 = (Map<?, ?>)batteryList.get( 0 );
		assertThat( battery0.get( "name" ) ).isEqualTo( "A" );
	}

	@Test
	void testGetFlightPage() throws Exception {
		// given
		Aircraft aftyn = new Aircraft().name( "AFTYN" ).type( AircraftType.FIXEDWING ).status( Aircraft.Status.DESTROYED ).owner( getMockUser().id() ).ownerType( OwnerType.USER );
		Battery batteryA = new Battery().name( "A" ).status( Battery.Status.NEW ).owner( getMockUser().id() ).ownerType( OwnerType.USER );
		statePersisting.upsert( aftyn );
		statePersisting.upsert( batteryA );
		Flight flightA = new Flight().pilot( getMockUser() ).aircraft( aftyn ).batteries( Set.of( batteryA ) );
		Flight flightB = new Flight().pilot( getMockUser() ).aircraft( aftyn ).batteries( Set.of( batteryA ) );
		statePersisting.upsert( flightA );
		statePersisting.upsert( flightB );

		// when
		MvcResult result = this.mockMvc.perform( get( ApiPath.USER_FLIGHT ).param( "pg", "0" ).with( jwt() ) ).andExpect( status().isOk() ).andReturn();

		// then
		Map<String, Object> map = Json.asMap( result.getResponse().getContentAsString() );
		List<?> flightList = (List<?>)((Map<?, ?>)map.get( "page" )).get( "content" );
		Map<?, ?> messagesMap = (Map<?, ?>)map.get( "messages" );

		assertThat( flightList.size() ).isEqualTo( 2 );
		assertThat( messagesMap ).isNull();

		Map<?, ?> flight0 = (Map<?, ?>)flightList.get( 0 );
		Map<?, ?> flight1 = (Map<?, ?>)flightList.get( 1 );
		assertThat( flight0.get( "aircraft" ) ).isEqualTo( aftyn.id().toString() );
		assertThat( flight1.get( "aircraft" ) ).isEqualTo( aftyn.id().toString() );
	}

	@Test
	void testGetFlightPageWithSize() throws Exception {
		// given
		Aircraft aftyn = new Aircraft().name( "AFTYN" ).type( AircraftType.FIXEDWING ).status( Aircraft.Status.DESTROYED ).owner( getMockUser().id() ).ownerType( OwnerType.USER );
		Battery batteryA = new Battery().name( "A" ).status( Battery.Status.NEW ).owner( getMockUser().id() ).ownerType( OwnerType.USER );
		statePersisting.upsert( aftyn );
		statePersisting.upsert( batteryA );
		Flight flightA = new Flight().pilot( getMockUser() ).aircraft( aftyn ).batteries( Set.of( batteryA ) );
		Flight flightB = new Flight().pilot( getMockUser() ).aircraft( aftyn ).batteries( Set.of( batteryA ) );
		statePersisting.upsert( flightA );
		statePersisting.upsert( flightB );

		// when
		MvcResult result = this.mockMvc.perform( get( ApiPath.USER_FLIGHT ).param( "pg", "0" ).param( "pz", "1" ).with( jwt() ) ).andExpect( status().isOk() ).andReturn();

		// then
		Map<String, Object> map = Json.asMap( result.getResponse().getContentAsString() );
		Map<?, ?> page = ((Map<?, ?>)map.get( "page" ));
		List<?> flightList = (List<?>)page.get( "content" );
		Map<?, ?> messagesMap = (Map<?, ?>)map.get( "messages" );

		int totalPages = Integer.parseInt( String.valueOf( page.get( "totalPages" ) ) );

		assertThat( flightList.size() ).isEqualTo( 1 );
		assertThat( messagesMap ).isNull();
		//assertThat( totalPages ).isEqualTo( 2 );

		Map<?, ?> flight0 = (Map<?, ?>)flightList.get( 0 );
		assertThat( flight0.get( "aircraft" ) ).isEqualTo( aftyn.id().toString() );
	}

	@Test
	void testGetLocationPage() throws Exception {
		// given
		//		Aircraft aftyn = new Aircraft().name( "AFTYN" ).type( AircraftType.FIXEDWING ).status( AircraftStatus.DESTROYED ).owner( getMockUser().id() ).ownerType( OwnerType.USER );
		//		Battery batteryA = new Battery().name( "A" ).status( BatteryStatus.NEW ).owner( getMockUser().id() ).ownerType( OwnerType.USER );
		//		statePersisting.upsert( aftyn );
		//		statePersisting.upsert( batteryA );
		Location locationA = new Location().user( getMockUser() ).name( "Monarch Meadows Park" );
		Location locationB = new Location().user( getMockUser() ).name( "Morning Cloak Park" );
		statePersisting.upsert( locationA );
		statePersisting.upsert( locationB );

		// when
		MvcResult result = this.mockMvc.perform( get( ApiPath.USER_LOCATION ).param( "pg", "0" ).with( jwt() ) ).andExpect( status().isOk() ).andReturn();

		// then
		Map<String, Object> map = Json.asMap( result.getResponse().getContentAsString() );
		List<?> flightList = (List<?>)((Map<?, ?>)map.get( "page" )).get( "content" );
		Map<?, ?> messagesMap = (Map<?, ?>)map.get( "messages" );

		assertThat( flightList.size() ).isEqualTo( 2 );
		assertThat( messagesMap ).isNull();

		Map<?, ?> location0 = (Map<?, ?>)flightList.get( 0 );
		Map<?, ?> location1 = (Map<?, ?>)flightList.get( 1 );
		assertThat( location0.get( "name" ) ).isEqualTo( locationA.name() );
		assertThat( location1.get( "name" ) ).isEqualTo( locationB.name() );
	}

	@Test
	void testGetUserMemberships() throws Exception {
		User user = getMockUser();

		Group groupA = statePersisting.upsert( new Group().name( "Group A" ).type( Group.Type.CLUB ) );
		Group groupB = statePersisting.upsert( new Group().name( "Group B" ).type( Group.Type.GROUP ) );

		// given
		statePersisting.upsert( new Member().user( user ).group( groupA ).status( Member.Status.OWNER ) );
		statePersisting.upsert( new Member().user( user ).group( groupB ).status( Member.Status.ACCEPTED ) );

		// when
		MvcResult result = this.mockMvc.perform( get( ApiPath.USER_MEMBERSHIP ).with( jwt() ) ).andExpect( status().isOk() ).andReturn();

		// then
		Map<String, Object> map = Json.asMap( result.getResponse().getContentAsString() );
		List<?> memberships = (List<?>)map.get( "data" );
		Map<?, ?> messagesMap = (Map<?, ?>)map.get( "messages" );

		assertThat( memberships.size() ).isEqualTo( 2 );
		assertThat( messagesMap ).isNull();
	}

	@Test
	void testGetAircraftLookup() throws Exception {
		statePersisting.upsert( createTestAircraft( getMockUser() ).status( Aircraft.Status.PREFLIGHT ) );
		statePersisting.upsert( createTestAircraft( getMockUser() ).status( Aircraft.Status.AIRWORTHY ) );
		statePersisting.upsert( createTestAircraft( getMockUser() ).status( Aircraft.Status.INOPERATIVE ) );
		statePersisting.upsert( createTestAircraft( getMockUser() ).status( Aircraft.Status.DECOMMISSIONED ) );
		statePersisting.upsert( createTestAircraft( getMockUser() ).status( Aircraft.Status.DESTROYED ) );
		MvcResult result = this.mockMvc.perform( get( ApiPath.USER_AIRCRAFT_LOOKUP ).with( jwt() ) ).andExpect( status().isOk() ).andReturn();
		List<Object> list = Json.asList( result.getResponse().getContentAsString() );
		assertThat( list.size() ).isEqualTo( 2 );
	}

	@Test
	void testGetBatteryLookup() throws Exception {
		statePersisting.upsert( createTestBattery( getMockUser() ).status( Battery.Status.NEW ) );
		statePersisting.upsert( createTestBattery( getMockUser() ).status( Battery.Status.AVAILABLE ) );
		statePersisting.upsert( createTestBattery( getMockUser() ).status( Battery.Status.DESTROYED ) );
		// Plus the 'No battery specified' option
		MvcResult result = this.mockMvc.perform( get( ApiPath.USER_BATTERY_LOOKUP ).with( jwt() ) ).andExpect( status().isOk() ).andReturn();
		List<Object> list = Json.asList( result.getResponse().getContentAsString() );
		assertThat( list.size() ).isEqualTo( 3 );
	}

	@Test
	void testGetObserverLookup() throws Exception {
		MvcResult result = this.mockMvc.perform( get( ApiPath.USER_OBSERVER_LOOKUP ).with( jwt() ) ).andExpect( status().isOk() ).andReturn();
		List<Object> list = Json.asList( result.getResponse().getContentAsString() );
		assertThat( list.size() ).isEqualTo( 2 );
	}

	@Test
	void testGetPilotLookup() throws Exception {
		MvcResult result = this.mockMvc.perform( get( ApiPath.USER_PILOT_LOOKUP ).with( jwt() ) ).andExpect( status().isOk() ).andReturn();
		List<Object> list = Json.asList( result.getResponse().getContentAsString() );
		assertThat( list.size() ).isEqualTo( 2 );
	}

	@Test
	void testPutMembershipAsOwner() throws Exception {
		// given
		User user = statePersisting.upsert( createTestUser( "sammy", "sammy@example.com" ) );
		Group group = statePersisting.upsert( new Group().name( "Group A" ).type( Group.Type.CLUB ) );

		// Make the mock user the group owner
		statePersisting.upsert( new Member().user( getMockUser() ).group( group ).status( Member.Status.OWNER ) );

		// when
		// Because the mock user is making the request, this is an owner request
		Map<String, String> request = Map.of( "userid", user.id().toString(), "groupid", group.id().toString(), "status", "requested" );
		MvcResult result = this.mockMvc
			.perform( put( ApiPath.USER_MEMBERSHIP ).with( jwt() ).content( Json.stringify( request ) ).contentType( MediaType.APPLICATION_JSON ) )
			.andExpect( status().isOk() )
			.andReturn();

		// then
		Map<String, Object> map = Json.asMap( result.getResponse().getContentAsString() );
		List<?> memberships = (List<?>)map.get( "memberships" );
		List<?> messages = (List<?>)map.get( "messages" );

		assertThat( memberships.size() ).isEqualTo( 1 );
		assertThat( messages ).isNull();
	}

	@Test
	void testRequestMembership() throws Exception {
		// given
		User user = getMockUser();
		User owner = statePersisting.upsert( createTestUser().preferredName( "Tamara" ).email( "tammy@example.com" ) );
		Group group = statePersisting.upsert( new Group().name( "Group A" ).type( Group.Type.CLUB ) );
		statePersisting.upsert( new Member().user( owner ).group( group ).status( Member.Status.OWNER ) );

		// when
		// Because the requesting user is the mock user, this should fail
		Map<String, String> request = Map.of( "userid", user.id().toString(), "groupid", group.id().toString(), "status", "requested" );
		MvcResult result = this.mockMvc
			.perform( put( ApiPath.USER_MEMBERSHIP ).with( jwt() ).content( Json.stringify( request ) ).contentType( MediaType.APPLICATION_JSON ) )
			.andExpect( status().isOk() )
			.andReturn();

		// then
		Map<String, Object> map = Json.asMap( result.getResponse().getContentAsString() );
		List<?> memberships = (List<?>)map.get( "memberships" );
		List<?> messages = (List<?>)map.get( "messages" );

		assertThat( memberships.size() ).isEqualTo( 1 );
		assertThat( messages ).isNull();
	}

	@Test
	void testInviteMembershipWhenNotOwner() throws Exception {
		// given
		User user = getMockUser();
		User owner = statePersisting.upsert( createTestUser() );
		Group group = statePersisting.upsert( new Group().name( "Group A" ).type( Group.Type.CLUB ) );
		statePersisting.upsert( new Member().user( owner ).group( group ).status( Member.Status.OWNER ) );

		// when
		// Because the requesting user is the mock user, this should fail
		Map<String, String> request = Map.of( "userid", user.id().toString(), "groupid", group.id().toString(), "status", Member.Status.INVITED.title().toLowerCase() );
		MvcResult result = this.mockMvc
			.perform( put( ApiPath.USER_MEMBERSHIP ).with( jwt() ).content( Json.stringify( request ) ).contentType( MediaType.APPLICATION_JSON ) )
			.andExpect( status().isUnauthorized() )
			.andReturn();

		// then
		Map<String, Object> map = Json.asMap( result.getResponse().getContentAsString() );
		List<?> memberships = (List<?>)map.get( "memberships" );
		List<?> messages = (List<?>)map.get( "messages" );

		assertThat( memberships ).isNull();
		assertThat( messages.size() ).isEqualTo( 1 );
	}

	@Test
	void testAcceptMembershipWhenNotInvited() throws Exception {
		// given
		User user = getMockUser();
		User owner = statePersisting.upsert( createTestUser() );
		Group group = statePersisting.upsert( new Group().name( "Group A" ).type( Group.Type.CLUB ) );
		statePersisting.upsert( new Member().user( owner ).group( group ).status( Member.Status.OWNER ) );

		// when
		// Because the requesting user is the mock user, this should fail
		Map<String, String> request = Map.of( "userid", user.id().toString(), "groupid", group.id().toString(), "status", Member.Status.ACCEPTED.title().toLowerCase() );
		MvcResult result = this.mockMvc
			.perform( put( ApiPath.USER_MEMBERSHIP ).with( jwt() ).content( Json.stringify( request ) ).contentType( MediaType.APPLICATION_JSON ) )
			.andExpect( status().isUnauthorized() )
			.andReturn();

		// then
		Map<String, Object> map = Json.asMap( result.getResponse().getContentAsString() );
		List<?> memberships = (List<?>)map.get( "memberships" );
		List<?> messages = (List<?>)map.get( "messages" );

		assertThat( memberships ).isNull();
		assertThat( messages.size() ).isEqualTo( 1 );
	}

	@Test
	void testPutMembershipWithBadRequest() throws Exception {
		// given
		User user = getMockUser();
		Group group = statePersisting.upsert( new Group().name( "Group A" ).type( Group.Type.CLUB ) );
		Map<String, String> request = Map.of( "userid", user.id().toString(), "groupid", "invalid", "status", "requested" );

		// when
		MvcResult result = this.mockMvc
			.perform( put( ApiPath.USER_MEMBERSHIP ).content( Json.stringify( request ) ).contentType( MediaType.APPLICATION_JSON ) )
			.andExpect( status().isBadRequest() )
			.andReturn();

		// then
		Map<String, Object> map = Json.asMap( result.getResponse().getContentAsString() );
		List<?> memberships = (List<?>)map.get( "memberships" );
		List<?> messages = (List<?>)map.get( "messages" );

		assertThat( memberships ).isNull();
		assertThat( messages.size() ).isEqualTo( 1 );
	}

	@Test
	void testDeleteMembership() throws Exception {
		// given
		User user = getMockUser();
		Group group = statePersisting.upsert( new Group().name( "Group A" ).type( Group.Type.CLUB ) );
		Member member = statePersisting.upsert( new Member().user( user ).group( group ).status( Member.Status.ACCEPTED ) );

		Map<String, String> request = Map.of( "membershipid", member.id().toString() );

		// when
		MvcResult result = this.mockMvc
			.perform( delete( ApiPath.USER_MEMBERSHIP ).with( jwt() ).content( Json.stringify( request ) ).contentType( MediaType.APPLICATION_JSON ) )
			.andExpect( status().isOk() )
			.andReturn();

		// then
		Map<String, Object> map = Json.asMap( result.getResponse().getContentAsString() );
		List<?> memberships = (List<?>)map.get( "memberships" );
		List<?> messages = (List<?>)map.get( "messages" );

		assertThat( memberships ).isEmpty();
		assertThat( messages ).isNull();
	}

	@Test
	void testDeleteMembershipWithBadRequest() throws Exception {
		// given
		User user = getMockUser();
		Group group = statePersisting.upsert( new Group().name( "Group A" ).type( Group.Type.CLUB ) );
		statePersisting.upsert( new Member().user( user ).group( group ).status( Member.Status.ACCEPTED ) );

		Map<String, String> request = Map.of( "membershipid", "invalid" );

		// when
		MvcResult result = this.mockMvc
			.perform( delete( ApiPath.USER_MEMBERSHIP ).content( Json.stringify( request ) ).contentType( MediaType.APPLICATION_JSON ) )
			.andExpect( status().isBadRequest() )
			.andReturn();

		// then
		Map<String, Object> map = Json.asMap( result.getResponse().getContentAsString() );
		List<?> memberships = (List<?>)map.get( "memberships" );
		List<?> messages = (List<?>)map.get( "messages" );

		assertThat( memberships ).isNull();
		assertThat( messages.size() ).isEqualTo( 1 );
	}

	@Test
	void testDashboard() throws Exception {
		// given
		dashboardServices.update( getMockUser() ).get();

		// when
		MvcResult result = this.mockMvc.perform( get( ApiPath.DASHBOARD ).with( jwt() ) ).andExpect( status().isOk() ).andReturn();

		// then
		Map<String, Object> map = Json.asMap( result.getResponse().getContentAsString() );
		Map<?, ?> data = (Map<?, ?>)map.get( "data" );
		String displayName = (String)data.get( "displayName" );
		assertThat( displayName ).isNotNull();
		assertThat( displayName ).isEqualTo( "Mock User" );
	}

	@Test
	void testPublicDashboardWithId() throws Exception {
		// given
		userServices.setPreferences( getMockUser(), Map.of( PreferenceKey.ENABLE_PUBLIC_DASHBOARD, "true" ) );
		dashboardServices.update( getMockUser() ).get();

		// when
		// NOTE - Do not send the JWT with this request. It should be anonymous.
		MvcResult result = this.mockMvc.perform( get( ApiPath.PUBLIC_DASHBOARD + "/" + getMockUser().id() ).with( nojwt() ) ).andExpect( status().isOk() ).andReturn();

		// then
		Map<?, ?> map = Json.asMap( result.getResponse().getContentAsString() );
		Map<?, ?> data = (Map<?, ?>)map.get( "data" );
		String displayName = (String)data.get( "displayName" );
		assertThat( displayName ).isNotNull();
		assertThat( displayName ).isEqualTo( "Mock User" );
	}

	@Test
	void testPublicDashboardWithUsername() throws Exception {
		// given
		userServices.setPreferences( getMockUser(), Map.of( PreferenceKey.ENABLE_PUBLIC_DASHBOARD, "true" ) );
		dashboardServices.update( getMockUser() ).get();

		// when
		// NOTE - Do not send the JWT with this request. It should be anonymous.
		MvcResult result = this.mockMvc.perform( get( ApiPath.PUBLIC_DASHBOARD + "/" + getMockUser().username() ).with( nojwt() ) ).andExpect( status().isOk() ).andReturn();

		// then
		Map<?, ?> map = Json.asMap( result.getResponse().getContentAsString() );
		Map<?, ?> data = (Map<?, ?>)map.get( "data" );
		String displayName = (String)data.get( "displayName" );
		assertThat( displayName ).isNotNull();
		assertThat( displayName ).isEqualTo( "Mock User" );
	}

	@Test
	void testPublicDashboardWithMissingDashboard() throws Exception {
		// given

		// when
		// NOTE - Do not send the JWT with this request. It should be anonymous.
		MvcResult result = this.mockMvc.perform( get( ApiPath.PUBLIC_DASHBOARD + "/" + UUID.randomUUID() ).with( nojwt() ) ).andExpect( status().isBadRequest() ).andReturn();

		// then
		Map<String, Object> map = Json.asMap( result.getResponse().getContentAsString() );
		List<?> messages = (List<?>)map.get( "messages" );
		assertThat( messages ).isNotNull();
		assertThat( messages.size() ).isEqualTo( 1 );
		assertThat( messages.get( 0 ) ).isEqualTo( "Dashboard not found" );
	}

	@Test
	void testPublicDashboardWithInvalidId() throws Exception {
		// given

		// when
		// NOTE - Do not send the JWT with this request. It should be anonymous.
		MvcResult result = this.mockMvc.perform( get( ApiPath.PUBLIC_DASHBOARD + "/not-a-valid-id" ).with( nojwt() ) ).andExpect( status().isBadRequest() ).andReturn();

		// then
		Map<String, Object> map = Json.asMap( result.getResponse().getContentAsString() );
		List<?> messages = (List<?>)map.get( "messages" );
		assertThat( messages ).isNotNull();
		assertThat( messages.size() ).isEqualTo( 1 );
		assertThat( messages.get( 0 ) ).isEqualTo( "Dashboard not found" );
	}

	@Test
	void testGetPreferences() throws Exception {
		// given

		// when
		MvcResult result = this.mockMvc.perform( get( ApiPath.USER_PREFERENCES ).with( jwt() ) ).andExpect( status().isOk() ).andReturn();

		// then
		Map<String, Object> map = Json.asMap( result.getResponse().getContentAsString() );
		Map<?, ?> data = (Map<?, ?>)map.get( "data" );
		assertThat( data ).isNotNull();
	}

	@Test
	void testGetPreferencesWithNonAdminUser() throws Exception {
		// given
		User paula = statePersisting.upsert( createTestUser( "paula", "paula@example.com" ) );

		// when
		this.mockMvc.perform( get( ApiPath.USER_PREFERENCES + "/" + paula.id() ).with( jwt() ) ).andExpect( status().isForbidden() ).andReturn();
	}

	@Test
	@WithMockUser( authorities = "ADMIN" )
	void testGetPreferencesWithAdminUser() throws Exception {
		// given
		User quinn = statePersisting.upsert( createTestUser( "quinn", "quinn@example.com" ) );

		// when
		MvcResult result = this.mockMvc.perform( get( ApiPath.USER_PREFERENCES + "/" + quinn.id() ).with( jwt() ) ).andExpect( status().isOk() ).andReturn();

		// then
		Map<String, Object> map = Json.asMap( result.getResponse().getContentAsString() );
		Map<?, ?> data = (Map<?, ?>)map.get( "data" );
		assertThat( data ).isNotNull();
	}

	@Test
	void testSetPreferences() throws Exception {
		// given
		Map<String, Object> request = new HashMap<>();
		request.put( "id", getMockUser().id().toString() );
		request.put( "preferences", Map.of( "showAircraftStats", true ) );

		// then
		MvcResult result = this.mockMvc
			.perform( put( ApiPath.USER_PREFERENCES ).with( jwt() ).content( Json.stringify( request ) ).contentType( MediaType.APPLICATION_JSON ) )
			.andExpect( status().isOk() )
			.andReturn();

		// then
		Map<String, Object> map = Json.asMap( result.getResponse().getContentAsString() );
		Map<?, ?> data = (Map<?, ?>)map.get( "data" );
		assertThat( data ).isNotNull();
		assertThat( data.size() ).isEqualTo( 1 );
	}

}
