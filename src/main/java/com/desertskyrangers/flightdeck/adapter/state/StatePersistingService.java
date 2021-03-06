package com.desertskyrangers.flightdeck.adapter.state;

import com.desertskyrangers.flightdeck.adapter.state.entity.*;
import com.desertskyrangers.flightdeck.adapter.state.repo.*;
import com.desertskyrangers.flightdeck.core.model.*;
import com.desertskyrangers.flightdeck.port.StatePersisting;
import com.desertskyrangers.flightdeck.util.Json;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class StatePersistingService implements StatePersisting {

	private final AircraftRepo aircraftRepo;

	private final BatteryRepo batteryRepo;

	private final DashboardRepo dashboardRepo;

	private final FlightRepo flightRepo;

	private final GroupRepo groupRepo;

	private final MemberRepo memberRepo;

	private final PreferencesRepo preferencesRepo;

	private final PublicDashboardRepo publicDashboardRepo;

	private final TokenRepo tokenRepo;

	private final UserRepo userRepo;

	private final VerificationRepo verificationRepo;

	public StatePersistingService(
		AircraftRepo aircraftRepo,
		BatteryRepo batteryRepo,
		DashboardRepo dashboardRepo,
		FlightRepo flightRepo,
		GroupRepo groupRepo,
		MemberRepo memberRepo,
		PreferencesRepo preferencesRepo,
		PublicDashboardRepo publicDashboardRepo,
		TokenRepo tokenRepo,
		UserRepo userRepo,
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
		this.tokenRepo = tokenRepo;
		this.userRepo = userRepo;
		this.verificationRepo = verificationRepo;
	}

	@Override
	public Aircraft upsert( Aircraft aircraft ) {
		return AircraftEntity.toAircraft( aircraftRepo.save( AircraftEntity.from( aircraft ) ) );
	}

	@Override
	public void remove( Aircraft aircraft ) {
		aircraftRepo.deleteById( aircraft.id() );
	}

	@Override
	public Battery upsert( Battery battery ) {
		return BatteryEntity.toBattery( batteryRepo.save( BatteryEntity.from( battery ) ) );
	}

	@Override
	public void remove( Battery battery ) {
		batteryRepo.deleteById( battery.id() );
	}

	@Override
	public Flight upsert( Flight flight ) {
		return FlightEntity.toFlight( flightRepo.save( FlightEntity.from( flight ) ) );
	}

	@Override
	public void remove( Flight flight ) {
		flightRepo.deleteById( flight.id() );
	}

	@Override
	public void removeAllFlights() {
		flightRepo.deleteAll();
	}

	@Override
	public Group upsert( Group group ) {
		return GroupEntity.toGroup( groupRepo.save( GroupEntity.from( group ) ) );
	}

	@Override
	public void remove( Group group ) {
		groupRepo.deleteById( group.id() );
	}

	@Override
	public void removeAllGroups() {
		groupRepo.deleteAll();
	}

	@Override
	public Member upsert( Member member ) {
		return MemberEntity.toMember( memberRepo.save( MemberEntity.from( member ) ) );
	}

	@Override
	public Member remove( Member member ) {
		memberRepo.deleteById( member.id() );
		return member;
	}

	@Override
	public void removeAllMembers() {
		memberRepo.deleteAll();
	}

	@Override
	public Map<String, Object> upsertPreferences( User user, Map<String, Object> preferences ) {
		return Json.asMap( preferencesRepo.save( PreferencesEntity.from( user, preferences ) ).getJson() );
	}

	@Override
	public Map<String, Object> removePreferences( User user ) {
		Map<String, Object> preferences = Json.asMap( preferencesRepo.findById( user.id() ).orElse( new PreferencesProjection().setJson( "{}" ) ).getJson() );
		preferencesRepo.deleteById( user.id() );
		return preferences;
	}

	@Override
	public void upsert( UserToken token ) {
		tokenRepo.save( TokenEntity.from( token ) );
	}

	@Override
	public User upsert( User account ) {
		return UserEntity.toUser( userRepo.save( UserEntity.from( account ) ) );
	}

	@Override
	public void remove( User account ) {
		userRepo.deleteById( account.id() );
	}

	@Override
	public Verification upsert( Verification verification ) {
		verificationRepo.save( VerificationEntity.from( verification ) );
		return verification;
	}

	@Override
	public Verification remove( Verification verification ) {
		verificationRepo.deleteById( verification.id() );
		return verification;
	}

	@Override
	public Dashboard upsertDashboard( User user, Dashboard dashboard ) {
		dashboardRepo.save( DashboardEntity.from( user, dashboard ) );
		return dashboard;
	}

	@Override
	public Dashboard removeDashboard( User user ) {
		DashboardProjection projection = dashboardRepo.getById( user.id() );
		Dashboard dashboard = DashboardEntity.toDashboard( projection );
		dashboardRepo.deleteById( user.id() );
		return dashboard;
	}

	@Override
	public PublicDashboard upsertPublicDashboard( User user, PublicDashboard dashboard ) {
		publicDashboardRepo.save( PublicDashboardEntity.from( user, dashboard ) );
		return dashboard;
	}

	@Override
	public PublicDashboard removePublicDashboard( User user ) {
		PublicDashboardProjection projection = publicDashboardRepo.getById( user.id() );
		PublicDashboard dashboard = PublicDashboardEntity.toDashboard( projection );
		publicDashboardRepo.deleteById( user.id() );
		return dashboard;
	}

}
