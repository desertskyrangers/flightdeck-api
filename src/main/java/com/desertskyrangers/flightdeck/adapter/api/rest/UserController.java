package com.desertskyrangers.flightdeck.adapter.api.rest;

import com.desertskyrangers.flightdeck.adapter.api.ApiPath;
import com.desertskyrangers.flightdeck.adapter.api.model.*;
import com.desertskyrangers.flightdeck.core.model.*;
import com.desertskyrangers.flightdeck.port.*;
import com.desertskyrangers.flightdeck.util.Uuid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@Slf4j
public class UserController extends BaseController {

	private final AircraftService aircraftService;

	private final BatteryService batteryService;

	private final DashboardService dashboardService;

	private final FlightService flightService;

	private final GroupService groupService;

	private final UserService userService;

	private final MembershipService memberService;

	public UserController(
		AircraftService aircraftService,
		BatteryService batteryService,
		DashboardService dashboardService,
		FlightService flightService,
		GroupService groupService,
		MembershipService memberService,
		UserService userService
	) {
		this.aircraftService = aircraftService;
		this.batteryService = batteryService;
		this.dashboardService = dashboardService;
		this.flightService = flightService;
		this.groupService = groupService;
		this.memberService = memberService;
		this.userService = userService;
	}

	@GetMapping( path = ApiPath.DASHBOARD )
	ResponseEntity<ReactDashboardResponse> dashboard( Authentication authentication ) {
		try {
			return dashboardService
				.findByUser( findUser( authentication ) )
				.map( dashboard -> new ResponseEntity<>( new ReactDashboardResponse().setDashboard( ReactDashboard.from( dashboard ) ), HttpStatus.OK ) )
				.orElseGet( () -> new ResponseEntity<>( new ReactDashboardResponse().setMessages( List.of( "Dashboard not found" ) ), HttpStatus.BAD_REQUEST ) );
		} catch( Exception exception ) {
			log.error( "Error generating dashboard", exception );
			return new ResponseEntity<>( new ReactDashboardResponse().setMessages( List.of( "Error generating dashboard" ) ), HttpStatus.INTERNAL_SERVER_ERROR );
		}
	}

	@GetMapping( path = ApiPath.PROFILE )
	ResponseEntity<ReactProfileResponse> profile() {
		String username = ((org.springframework.security.core.userdetails.User)SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();

		Optional<ReactUser> optional = userService.findByPrincipal( username ).map( ReactUser::from );
		if( optional.isEmpty() ) return new ResponseEntity<>( new ReactProfileResponse().setAccount( new ReactUser() ), HttpStatus.BAD_REQUEST );

		return new ResponseEntity<>( new ReactProfileResponse().setAccount( optional.get() ), HttpStatus.OK );
	}

	//	@GetMapping
	//	public List<ReactRegisterRequest> findAll() {
	//		return service.find().stream().map( ReactRegisterRequest::from ).collect( Collectors.toList() );
	//	}

	@GetMapping( value = ApiPath.USER + "/{id}" )
	ResponseEntity<ReactProfileResponse> findById( @PathVariable( "id" ) UUID id ) {
		Optional<ReactUser> optional = userService.find( id ).map( ReactUser::from );
		if( optional.isEmpty() ) return new ResponseEntity<>( new ReactProfileResponse().setAccount( new ReactUser() ), HttpStatus.BAD_REQUEST );

		return new ResponseEntity<>( new ReactProfileResponse().setAccount( optional.get() ), HttpStatus.OK );
	}

	@PutMapping( value = ApiPath.USER + "/{id}" )
	@ResponseStatus( HttpStatus.OK )
	ResponseEntity<ReactProfileResponse> update( @PathVariable( "id" ) UUID id, @RequestBody ReactUser account ) {
		Optional<User> optional = userService.find( id );
		if( optional.isEmpty() ) return new ResponseEntity<>( new ReactProfileResponse().setAccount( new ReactUser() ), HttpStatus.BAD_REQUEST );

		User user = account.updateFrom( optional.get() );

		// Update the user account
		userService.upsert( user );

		return new ResponseEntity<>( new ReactProfileResponse().setAccount( ReactUser.from( user ) ), HttpStatus.OK );
	}

	@DeleteMapping( value = ApiPath.USER + "/{id}" )
	@ResponseStatus( HttpStatus.OK )
	void delete( @PathVariable( "id" ) UUID id ) {
		userService.find( id ).ifPresent( userService::remove );
	}

	@PutMapping( value = ApiPath.USER + "/{id}/password" )
	@ResponseStatus( HttpStatus.OK )
	ResponseEntity<Map<String, Object>> updatePassword( @PathVariable( "id" ) UUID id, @RequestBody ReactPasswordChangeRequest request ) {
		// Check that ids match
		if( !Objects.equals( id.toString(), request.getId() ) ) return new ResponseEntity<>( Map.of( "messages", List.of( "User id mismatch" ) ), HttpStatus.BAD_REQUEST );

		// Check the user exists
		Optional<User> optional = userService.find( id );
		if( optional.isEmpty() ) return new ResponseEntity<>( Map.of( "messages", List.of( "User not found: " + id ) ), HttpStatus.BAD_REQUEST );

		// Check that passwords match
		if( !userService.isCurrentPassword( optional.get(), request.getCurrentPassword() ) ) {
			return new ResponseEntity<>( Map.of( "messages", List.of( "Current password mismatch" ) ), HttpStatus.BAD_REQUEST );
		}

		// Update the user account
		userService.updatePassword( optional.get(), request.getPassword() );

		return new ResponseEntity<>( HttpStatus.OK );
	}

	@GetMapping( path = ApiPath.USER_AIRCRAFT + "/{page}" )
	ResponseEntity<ReactAircraftPageResponse> getAircraftPage( Authentication authentication, @PathVariable int page ) {
		List<String> messages = new ArrayList<>();

		try {
			User user = findUser( authentication );
			List<ReactAircraft> aircraftPage = aircraftService.findByOwner( user.id() ).stream().map( ReactAircraft::from ).toList();
			return new ResponseEntity<>( new ReactAircraftPageResponse().setAircraft( aircraftPage ), HttpStatus.OK );
		} catch( Exception exception ) {
			log.error( "Error creating new aircraft", exception );
			messages.add( exception.getMessage() );
		}

		return new ResponseEntity<>( new ReactAircraftPageResponse().setMessages( messages ), HttpStatus.INTERNAL_SERVER_ERROR );
	}

	@GetMapping( path = ApiPath.USER_BATTERY + "/{page}" )
	ResponseEntity<ReactBatteryPageResponse> getBatteryPage( Authentication authentication, @PathVariable int page ) {
		List<String> messages = new ArrayList<>();

		try {
			User user = findUser( authentication );
			List<ReactBattery> batteryPage = batteryService.findByOwner( user.id() ).stream().map( ReactBattery::from ).toList();
			return new ResponseEntity<>( new ReactBatteryPageResponse().setBatteries( batteryPage ), HttpStatus.OK );
		} catch( Exception exception ) {
			log.error( "Error creating new battery", exception );
			messages.add( exception.getMessage() );
		}

		return new ResponseEntity<>( new ReactBatteryPageResponse().setMessages( messages ), HttpStatus.INTERNAL_SERVER_ERROR );
	}

	@GetMapping( path = ApiPath.USER_FLIGHT + "/{page}" )
	ResponseEntity<ReactFlightPageResponse> getFlightPage( Authentication authentication, @PathVariable int page ) {
		List<String> messages = new ArrayList<>();

		try {
			User requester = findUser( authentication );
			List<ReactFlight> flightPage = flightService.findFlightsByUser( requester.id() ).stream().map( f -> ReactFlight.from( requester, f ) ).toList();
			return new ResponseEntity<>( new ReactFlightPageResponse().setFlights( flightPage ), HttpStatus.OK );
		} catch( Exception exception ) {
			log.error( "Error creating new battery", exception );
			messages.add( exception.getMessage() );
		}

		return new ResponseEntity<>( new ReactFlightPageResponse().setMessages( messages ), HttpStatus.INTERNAL_SERVER_ERROR );
	}

	@GetMapping( path = ApiPath.USER_GROUP + "/{page}" )
	ResponseEntity<ReactGroupPageResponse> getOrgPage( Authentication authentication, @PathVariable int page ) {
		List<String> messages = new ArrayList<>();

		try {
			User user = findUser( authentication );
			List<ReactGroup> groupPage = groupService.findGroupsByUser( user ).stream().map( ReactGroup::from ).toList();
			return new ResponseEntity<>( new ReactGroupPageResponse().setGroups( groupPage ), HttpStatus.OK );
		} catch( Exception exception ) {
			log.error( "Error creating new battery", exception );
			messages.add( exception.getMessage() );
		}

		return new ResponseEntity<>( new ReactGroupPageResponse().setMessages( messages ), HttpStatus.INTERNAL_SERVER_ERROR );
	}

	@GetMapping( path = ApiPath.USER_MEMBERSHIP )
	ResponseEntity<ReactMembershipPageResponse> getMembershipPage( Authentication authentication ) {
		List<String> messages = new ArrayList<>();

		try {
			User user = findUser( authentication );
			return new ResponseEntity<>( new ReactMembershipPageResponse().setMemberships( getMemberships( user ) ), HttpStatus.OK );
		} catch( Exception exception ) {
			log.error( "Error creating new battery", exception );
			messages.add( exception.getMessage() );
		}

		return new ResponseEntity<>( new ReactMembershipPageResponse().setMessages( messages ), HttpStatus.INTERNAL_SERVER_ERROR );
	}

	@GetMapping( path = ApiPath.USER_AIRCRAFT_LOOKUP )
	ResponseEntity<List<ReactOption>> getAircraftLookup( Authentication authentication ) {
		User user = findUser( authentication );
		List<Aircraft> objects = aircraftService.findByOwner( user.id() );
		return new ResponseEntity<>( objects.stream().map( c -> new ReactOption( c.id().toString(), c.name() ) ).toList(), HttpStatus.OK );
	}

	@GetMapping( path = ApiPath.USER_BATTERY_LOOKUP )
	ResponseEntity<List<ReactOption>> getBatteryLookup( Authentication authentication ) {
		User user = findUser( authentication );
		List<Battery> objects = batteryService.findByOwner( user.id() );
		List<ReactOption> options = new ArrayList<>( objects.stream().map( c -> new ReactOption( c.id().toString(), c.name() ) ).toList() );
		options.add( new ReactOption( "", "No battery specified" ) );
		return new ResponseEntity<>( options, HttpStatus.OK );
	}

	@GetMapping( path = ApiPath.USER_OBSERVER_LOOKUP )
	ResponseEntity<List<ReactOption>> getObserverLookup( Authentication authentication ) {
		return getPilotLookup( authentication );
	}

	@GetMapping( path = ApiPath.USER_PILOT_LOOKUP )
	ResponseEntity<List<ReactOption>> getPilotLookup( Authentication authentication ) {
		User user = findUser( authentication );

		List<User> users = new ArrayList<>( userService.findAllGroupPeers( user ) );
		users.sort( null );
		users.add( 0, user );
		users.add( unlistedUser() );

		return new ResponseEntity<>( users.stream().map( c -> new ReactOption( c.id().toString(), c.preferredName() ) ).toList(), HttpStatus.OK );
	}

	@PutMapping( path = ApiPath.USER_MEMBERSHIP )
	ResponseEntity<ReactMembershipPageResponse> requestGroupMembership( Authentication authentication, @RequestBody Map<String, String> request ) {
		String userId = request.get( "userid" );
		String groupId = request.get( "groupid" );
		String statusKey = request.get( "status" );

		List<String> messages = new ArrayList<>();
		if( Uuid.isNotValid( userId ) ) messages.add( "Invalid user ID" );
		if( Uuid.isNotValid( groupId ) ) messages.add( "Invalid group ID" );
		if( MemberStatus.isNotValid( statusKey ) ) messages.add( "Invalid membership status" );
		if( !messages.isEmpty() ) return new ResponseEntity<>( new ReactMembershipPageResponse().setMessages( messages ), HttpStatus.BAD_REQUEST );

		try {
			User requester = findUser( authentication );
			User user = userService.find( UUID.fromString( userId ) ).orElse( null );
			Group group = groupService.find( UUID.fromString( groupId ) ).orElse( null );
			MemberStatus status = MemberStatus.valueOf( statusKey.toUpperCase() );
			if( user == null ) {
				messages.add( "User not found" );
				log.warn( "User not found id=" + userId );
			}
			if( group == null ) {
				messages.add( "Group not found" );
				log.warn( "Group not found id=" + groupId );
			}
			if( !messages.isEmpty() ) return new ResponseEntity<>( new ReactMembershipPageResponse().setMessages( messages ), HttpStatus.BAD_REQUEST );

			memberService.requestMembership( requester, user, group, status );

			return new ResponseEntity<>( new ReactMembershipPageResponse().setMemberships( getMemberships( user ) ), HttpStatus.OK );
		} catch( Exception exception ) {
			log.error( "Error creating new battery", exception );
			messages.add( exception.getMessage() );
		}

		return new ResponseEntity<>( new ReactMembershipPageResponse().setMessages( messages ), HttpStatus.INTERNAL_SERVER_ERROR );
	}

	@DeleteMapping( path = ApiPath.USER_MEMBERSHIP )
	ResponseEntity<ReactMembershipPageResponse> cancelGroupMembership( Authentication authentication, @RequestBody Map<String, String> request ) {
		String userId = request.get( "membershipid" );

		List<String> messages = new ArrayList<>();
		if( Uuid.isNotValid( userId ) ) messages.add( "Invalid membership ID" );
		if( !messages.isEmpty() ) return new ResponseEntity<>( new ReactMembershipPageResponse().setMessages( messages ), HttpStatus.BAD_REQUEST );

		try {
			User requester = findUser( authentication );
			Member member = memberService.find( UUID.fromString( userId ) ).orElse( null );
			if( member == null ) {
				messages.add( "Membership not found" );
				log.warn( "Membership not found id=" + userId );
			}
			if( !messages.isEmpty() ) return new ResponseEntity<>( new ReactMembershipPageResponse().setMessages( messages ), HttpStatus.BAD_REQUEST );

			User user = member.user();
			memberService.cancelMembership( requester, member );

			return new ResponseEntity<>( new ReactMembershipPageResponse().setMemberships( getMemberships( user ) ), HttpStatus.OK );
		} catch( Exception exception ) {
			log.error( "Error creating new battery", exception );
			messages.add( exception.getMessage() );
		}

		return new ResponseEntity<>( new ReactMembershipPageResponse().setMessages( messages ), HttpStatus.INTERNAL_SERVER_ERROR );
	}

	private List<ReactMembership> getMemberships( User user ) {
		Set<Member> memberships = memberService.findMembershipsByUser( user );
		List<Member> objects = new ArrayList<>( memberships );
		Collections.sort( objects );
		return objects.stream().map( ReactMembership::from ).toList();
	}

}
