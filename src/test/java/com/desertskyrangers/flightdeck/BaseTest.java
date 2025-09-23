package com.desertskyrangers.flightdeck;

import com.desertskyrangers.flightdeck.adapter.store.entity.GroupEntity;
import com.desertskyrangers.flightdeck.adapter.store.entity.LocationEntity;
import com.desertskyrangers.flightdeck.adapter.store.entity.MemberEntity;
import com.desertskyrangers.flightdeck.adapter.store.entity.UserEntity;
import com.desertskyrangers.flightdeck.core.model.*;
import com.desertskyrangers.flightdeck.port.LocationServices;
import com.desertskyrangers.flightdeck.port.StatePersisting;
import com.desertskyrangers.flightdeck.port.StateRetrieving;
import com.desertskyrangers.flightdeck.port.UserServices;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;
import java.util.UUID;

@SpringBootTest
public class BaseTest {

	@Autowired
	protected StatePersisting statePersisting;

	@Autowired
	protected StateRetrieving stateRetrieving;

	@Autowired
	protected PasswordEncoder passwordEncoder;

	@Autowired
	protected LocationServices locationServices;

	@Autowired
	protected UserServices userServices;

	@Autowired
	protected User unlistedUser;

	@BeforeEach
	protected void setup() {
		// Clean old members
		statePersisting.removeAllMembers();
		// Clean old groups
		statePersisting.removeAllGroups();
		// Clean old flights
		statePersisting.removeAllFlights();
	}

	protected User getUnlistedUser() {
		return unlistedUser;
	}

	protected User createTestUser() {
		return createTestUser( null, null );
	}

	protected User createTestUser( String username, String email ) {
		User user = new User();
		user.username( username );
		user.email( email );
		statePersisting.upsert( user );
		return user;
	}

	protected UserEntity createTestUserEntity( String username, String email ) {
		return UserEntity.from( createTestUser( username, email ) );
	}

	protected UserToken createTestToken( User user, String principal, String password ) {
		return new UserToken().user( user ).principal( principal ).credential( passwordEncoder.encode( password ) );
	}

	protected Aircraft createTestAircraft( User owner ) {
		Aircraft aircraft = new Aircraft();
		aircraft.id( UUID.randomUUID() );
		aircraft.name( "Aftyn" );
		aircraft.make( "Hobby King" );
		aircraft.model( "Bixler 2" );
		aircraft.type( AircraftType.FIXEDWING );
		aircraft.status( Aircraft.Status.DESTROYED );
		aircraft.owner( owner.id() );
		aircraft.ownerType( OwnerType.USER );
		statePersisting.upsert( aircraft );
		return aircraft;
	}

	protected Battery createTestBattery( User owner ) {
		Battery battery = new Battery();
		battery.name( "C 4S 2650 Turnigy" );
		battery.make( "Hobby King" );
		battery.model( "Turnigy 2650 4S" );
		battery.connector( Battery.Connector.XT60 );
		battery.status( Battery.Status.DESTROYED );

		battery.chemistry( Battery.Chemistry.LIPO );
		battery.cells( 4 );
		battery.initialCycles( 23 );
		battery.cycles( 57 );
		battery.capacity( 2650 );
		battery.dischargeRating( 20 );

		battery.owner( owner.id() );
		battery.ownerType( OwnerType.USER );
		statePersisting.upsert( battery );
		return battery;
	}

	protected Flight createTestFlight( User pilot ) {
		Aircraft aircraft = createTestAircraft( pilot );
		statePersisting.upsert( aircraft );
		Battery battery = createTestBattery( pilot );
		statePersisting.upsert( battery );
		Flight flight = new Flight();
		flight.pilot( pilot );
		flight.observer( getUnlistedUser() );
		flight.unlistedObserver( "Oscar Observer" );
		flight.aircraft( aircraft );
		flight.batteries( Set.of( battery ) );
		flight.timestamp( System.currentTimeMillis() );
		flight.duration( 1000 );
		flight.location( createTestLocation( pilot ) );
		flight.notes( "Just a test flight" );
		statePersisting.upsert( flight );
		return flight;
	}

	protected Location createTestLocation( User user ) {
		Location location = new Location();

		location.latitude( 40.50339561398912 );
		location.longitude( -112.01445893299925 );
		location.altitude( 1478 );
		location.user( user );
		location.name( "Morning Cloak Park" );
		location.size( 150 );

		locationServices.upsert( location );
		return location;
	}

	protected LocationEntity createTestLocationEntity() {
		User user = statePersisting.upsert( createTestUser() );
		return LocationEntity.from( createTestLocation( user ) );
	}

	protected Group createTestGroup() {
		return createTestGroup( "Test Group", Group.Type.GROUP );
	}

	protected Group createTestGroup( String name, Group.Type type ) {
		return new Group().name( name ).type( type );
	}

	protected GroupEntity createTestGroupEntity( String name, Group.Type type ) {
		return GroupEntity.from( createTestGroup( name, type ) ).setId( UUID.randomUUID() );
	}

	protected Member createTestMember( User user, Group group, Member.Status status ) {
		return new Member().user( user ).group( group ).status( status );
	}

	protected MemberEntity createTestMemberEntity( UserEntity user, GroupEntity group, Member.Status status ) {
		return MemberEntity.from( createTestMember( UserEntity.toUser( user ), GroupEntity.toGroup( group ), status ) ).setId( UUID.randomUUID() );
	}

}
