package com.desertskyrangers.flightdeck.adapter.state;

import com.desertskyrangers.flightdeck.adapter.state.entity.*;
import com.desertskyrangers.flightdeck.adapter.state.repo.*;
import com.desertskyrangers.flightdeck.core.model.*;
import com.desertskyrangers.flightdeck.port.StateRetrieving;
import com.desertskyrangers.flightdeck.util.Json;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@Slf4j
public class StateRetrievingService implements StateRetrieving {

	private final AircraftRepo aircraftRepo;

	private final BatteryRepo batteryRepo;

	private final DashboardRepo dashboardRepo;

	private final FlightRepo flightRepo;

	private final GroupRepo groupRepo;

	private final MemberRepo memberRepo;

	private final PreferencesRepo preferencesRepo;

	private final PublicDashboardRepo publicDashboardRepo;

	private final UserRepo userRepo;

	private final TokenRepo tokenRepo;

	private final VerificationRepo verificationRepo;

	public StateRetrievingService(
		AircraftRepo aircraftRepo,
		BatteryRepo batteryRepo,
		DashboardRepo dashboardRepo,
		FlightRepo flightRepo,
		GroupRepo groupRepo,
		MemberRepo memberRepo,
		PreferencesRepo preferencesRepo,
		PublicDashboardRepo publicDashboardRepo,
		UserRepo userRepo,
		TokenRepo tokenRepo,
		VerificationRepo verificationRepo
	) {
		this.aircraftRepo = aircraftRepo;
		this.batteryRepo = batteryRepo;
		this.dashboardRepo = dashboardRepo;
		this.flightRepo = flightRepo;
		this.groupRepo = groupRepo;
		this.memberRepo = memberRepo;
		this.preferencesRepo = preferencesRepo;
		this.publicDashboardRepo = publicDashboardRepo;
		this.userRepo = userRepo;
		this.tokenRepo = tokenRepo;
		this.verificationRepo = verificationRepo;
	}

	@Override
	public Optional<Aircraft> findAircraft( UUID id ) {
		return aircraftRepo.findById( id ).map( AircraftEntity::toAircraft );
	}

	@Override
	public List<Aircraft> findAircraftByOwner( UUID owner ) {
		return convertAircraft( aircraftRepo.findAircraftByOwnerOrderByName( owner ) );
	}

	@Override
	public List<Aircraft> findAircraftByOwnerAndStatus( UUID id, AircraftStatus status ) {
		return convertAircraft( aircraftRepo.findAircraftByOwnerAndStatusOrderByName( id, status.name().toLowerCase() ) );
	}

	@Override
	public List<Aircraft> findAllAircraft() {
		return convertAircraft( aircraftRepo.findAll() );
	}

	@Override
	public Optional<Battery> findBattery( UUID id ) {
		return batteryRepo.findById( id ).map( BatteryEntity::toBattery );
	}

	@Override
	public List<Battery> findBatteriesByOwner( UUID owner ) {
		return convertBatteries( batteryRepo.findBatteryEntitiesByOwnerOrderByName( owner ) );
	}

	@Override
	public List<Battery> findAllBatteries() {
		return convertBatteries( batteryRepo.findAll() );
	}

	@Override
	public Optional<Flight> findFlight( UUID id ) {
		return flightRepo.findById( id ).map( FlightEntity::toFlight );
	}

	@Override
	public List<Flight> findFlightsByPilot( UUID id ) {
		return flightRepo.findFlightEntitiesByPilot_IdOrderByTimestampDesc( id ).stream().map( FlightEntity::toFlight ).toList();
	}

	// Pilot

	public List<Flight> findFlightsByPilotAndTimestampAfter( UUID id, long timestamp ) {
		return convertFlights( flightRepo.findFlightEntitiesByPilotIdAndTimestampAfterOrderByTimestampDesc( id, timestamp ) );
	}

	public List<Flight> findFlightsByPilotAndCount( UUID id, int count ) {
		return convertFlights( new ArrayList<>( flightRepo.findAllByPilotId( id, PageRequest.of( 0, count, Sort.Direction.DESC, "timestamp" ) ) ) );
	}

	// Observer

	public List<Flight> findFlightsByObserverAndTimestampAfter( UUID id, long timestamp ) {
		return convertFlights( flightRepo.findFlightEntitiesByObserverIdAndTimestampAfterOrderByTimestampDesc( id, timestamp ) );
	}

	public List<Flight> findFlightsByObserverAndCount( UUID id, int count ) {
		return convertFlights( new ArrayList<>( flightRepo.findAllByObserverId( id, PageRequest.of( 0, count, Sort.Direction.DESC, "timestamp" ) ) ) );
	}

	// Owner

	public List<Flight> findFlightsByOwnerAndTimestampAfter( UUID id, long timestamp ) {
		return convertFlights( flightRepo.findFlightEntitiesByAircraft_OwnerAndTimestampAfterOrderByTimestampDesc( id, timestamp ) );
	}

	public List<Flight> findFlightsByOwnerAndCount( UUID id, int count ) {
		return convertFlights( new ArrayList<>( flightRepo.findAllByAircraft_Owner( id, PageRequest.of( 0, count, Sort.Direction.DESC, "timestamp" ) ) ) );
	}

	@Override
	public List<Flight> findAllFlights() {
		return convertFlights( flightRepo.findAll() );
	}

	private List<Aircraft> convertAircraft( List<AircraftEntity> entities ) {
		return entities.stream().map( AircraftEntity::toAircraft ).toList();
	}

	private List<Battery> convertBatteries( List<BatteryEntity> entities ) {
		return entities.stream().map( BatteryEntity::toBattery ).toList();
	}

	private List<Flight> convertFlights( List<FlightEntity> entities ) {
		return entities.stream().map( FlightEntity::toFlight ).toList();
	}

	@Override
	public List<Flight> findFlightsByAircraft( UUID id ) {
		return flightRepo.findFlightEntitiesByAircraft_IdOrderByTimestampDesc( id ).stream().map( FlightEntity::toFlight ).toList();
	}

	@Override
	public List<Flight> findFlightsByBattery( UUID id ) {
		return null;
	}

	/**
	 * This returns the list of flights where the user is a pilot, observer, or aircraft owner.
	 *
	 * @param id The user id
	 * @return The list of flights
	 */
	@Override
	public List<Flight> findFlightsByUser( UUID id ) {
		return flightRepo.findFlightEntitiesByPilot_IdOrObserver_IdOrAircraft_OwnerOrderByTimestampDesc( id, id, id ).stream().map( FlightEntity::toFlight ).toList();
	}

	@Override
	public Set<Group> findAllAvailableGroups( User user ) {
		Set<Group> groups = findAllGroups();
		groups.removeAll( memberRepo.findAllByUser( UserEntity.from( user ) ).stream().map( MemberEntity::getGroup ).map( GroupEntity::toGroup ).collect( Collectors.toSet() ) );
		return groups;
	}

	public Set<Group> findAllGroups() {
		return groupRepo.findAll().stream().map( GroupEntity::toGroup ).collect( Collectors.toSet() );
	}

	@Override
	public Optional<Group> findGroup( UUID id ) {
		return groupRepo.findById( id ).map( GroupEntity::toGroup );
	}

	@Override
	public Set<Group> findGroupsByOwner( User user ) {
		return memberRepo.findAllByUser_IdAndStatus( user.id(), MemberStatus.OWNER.name().toLowerCase() ).stream().map( MemberEntity::getGroup ).map( GroupEntity::toGroup ).collect( Collectors.toSet() );
	}

	@Override
	public Set<User> findGroupOwners( Group group ) {
		return memberRepo.findAllByGroup_IdAndStatus( group.id(), MemberStatus.OWNER.name().toLowerCase() ).stream().map( MemberEntity::getUser ).map( UserEntity::toUser ).collect( Collectors.toSet() );
	}

	@Override
	public Optional<Member> findMembership( UUID id ) {
		return memberRepo.findById( id ).map( MemberEntity::toMember );
	}

	@Override
	public Optional<Member> findMembership( Group group, User user ) {
		return memberRepo.findByGroupAndUser( GroupEntity.from( group ), UserEntity.from( user ) ).map( MemberEntity::toMember );
	}

	@Override
	public Set<Member> findMemberships( User user ) {
		return memberRepo.findAllByUser( UserEntity.from( user ) ).stream().map( MemberEntity::toMember ).collect( Collectors.toSet() );
	}

	@Override
	public Set<Member> findMemberships( Group group ) {
		return memberRepo.findAllByGroup( GroupEntity.from( group ) ).stream().map( MemberEntity::toMember ).collect( Collectors.toSet() );
	}

	@Override
	public Optional<UserToken> findUserToken( UUID id ) {
		return tokenRepo.findById( id ).map( TokenEntity::toUserToken );
	}

	public Map<String, Object> findPreferences( User user ) {
		return Json.asMap( preferencesRepo.findById( user.id() ).orElse( new PreferencesProjection().setJson( "{}" ) ).getJson() );
	}

	public boolean isPreferenceSet( User user, String key ) {
		return findPreferences( user ).containsKey( key );
	}

	public boolean isPreferenceSetTo( User user, String key, String value ) {
		return String.valueOf( findPreferences( user ).get( key ) ).equals( value );
	}

	public String getPreference( User user, String key ) {
		return getPreference( user, key, null );
	}

	public String getPreference( User user, String key, String defaultValue ) {
		Object result = findPreferences( user ).get( key );
		if( result == null ) result = defaultValue;
		return result == null ? null : String.valueOf( result );
	}

	@Override
	public Optional<UserToken> findUserTokenByPrincipal( String username ) {
		return tokenRepo.findByPrincipal( username ).map( TokenEntity::toUserTokenDeep );
	}

	@Override
	public List<User> findAllUserAccounts() {
		return userRepo.findAll().stream().map( UserEntity::toUser ).collect( Collectors.toList() );
	}

	@Override
	public Optional<User> findUser( UUID id ) {
		if( id == null ) return Optional.empty();
		return userRepo.findById( id ).map( UserEntity::toUser );
	}

	@Override
	public List<Verification> findAllVerifications() {
		return StreamSupport.stream( verificationRepo.findAll().spliterator(), false ).map( VerificationEntity::toVerification ).collect( Collectors.toList() );
	}

	@Override
	public Optional<Verification> findVerification( UUID id ) {
		return verificationRepo.findById( id ).map( VerificationEntity::toVerification );
	}

	@Override
	public int getPilotFlightCount( UUID id ) {
		Integer count = flightRepo.countByPilot_Id( id );
		return count == null ? 0 : count;
	}

	@Override
	public long getPilotFlightTime( UUID id ) {
		Long time = flightRepo.getFlightTimeByPilot_Id( id );
		return time == null ? 0 : time;
	}

	@Override
	public int getObserverFlightCount( UUID id ) {
		Integer count = flightRepo.getFlightCountByObserver( id );
		return count == null ? 0 : count;
	}

	@Override
	public long getObserverFlightTime( UUID id ) {
		Long time = flightRepo.getFlightTimeByObserver_Id( id );
		return time == null ? 0 : time;
	}

	@Override
	public Optional<Flight> getLastAircraftFlight( Aircraft aircraft ) {
		return flightRepo.findFirstByAircraftOrderByTimestampDesc( AircraftEntity.from( aircraft ) ).map( FlightEntity::toFlight );
	}

	@Override
	public Optional<Flight> getLastPilotFlight( User pilot ) {
		return flightRepo.findFirstByPilotOrderByTimestampDesc( UserEntity.from( pilot ) ).map( FlightEntity::toFlight );
	}

	@Override
	public int getAircraftFlightCount( Aircraft aircraft ) {
		Integer count = flightRepo.countByAircraft_Id( aircraft.id() );
		return count == null ? 0 : count;
	}

	@Override
	public long getAircraftFlightTime( Aircraft aircraft ) {
		Long time = flightRepo.getFlightTimeByAircraft_Id( aircraft.id() );
		return time == null ? 0 : time;
	}

	@Override
	public int getBatteryFlightCount( Battery battery ) {
		Integer count = flightRepo.countByBattery( BatteryEntity.from( battery ) );
		return count == null ? 0 : count;
	}

	@Override
	public long getBatteryFlightTime( Battery battery ) {
		Long time = flightRepo.getFlightTimeByBattery( BatteryEntity.from( battery ) );
		return time == null ? 0 : time;
	}

	@Override
	public Optional<Dashboard> findDashboard( User user ) {
		DashboardProjection entity = dashboardRepo.findById( user.id() ).orElse( null );
		return entity == null ? Optional.empty() : Optional.of( DashboardEntity.toDashboard( entity ) );
	}

	@Override
	public Optional<PublicDashboard> findPublicDashboard( User user ) {
		PublicDashboardProjection entity = publicDashboardRepo.findById( user.id() ).orElse( null );
		return entity == null ? Optional.empty() : Optional.of( PublicDashboardEntity.toDashboard( entity ) );
	}

}
