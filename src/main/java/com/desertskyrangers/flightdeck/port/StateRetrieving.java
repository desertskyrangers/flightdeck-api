package com.desertskyrangers.flightdeck.port;

import com.desertskyrangers.flightdeck.core.model.*;

import java.util.*;

public interface StateRetrieving {

	Optional<Aircraft> findAircraft( UUID id );

	List<Aircraft> findAircraftByOwner( UUID id );

	List<Aircraft> findAircraftByOwnerAndStatus( UUID id, AircraftStatus status );

	List<Aircraft> findAllAircraft();

	Optional<Battery> findBattery( UUID id );

	List<Battery> findBatteriesByOwner( UUID id );

	List<Battery> findAllBatteries();

	Optional<Flight> findFlight( UUID id );

	List<Flight> findFlightsByPilot( UUID id );

	List<Flight> findFlightsByPilotAndTimestampAfter( UUID id, long timestamp );

	List<Flight> findFlightsByObserverAndTimestampAfter( UUID id, long timestamp );

	List<Flight> findFlightsByOwnerAndTimestampAfter( UUID id, long timestamp );

	List<Flight> findFlightsByPilotAndCount( UUID id, int count );

	List<Flight> findFlightsByObserverAndCount( UUID id, int count );

	List<Flight> findFlightsByOwnerAndCount( UUID id, int count );

	List<Flight> findFlightsByAircraft( UUID id );

	List<Flight> findFlightsByBattery( UUID id );

	List<Flight> findFlightsByUser( UUID id );

	List<Flight> findAllFlights();

	Set<Group> findAllAvailableGroups( User user );

	Optional<Group> findGroup( UUID id );

	Set<Group> findGroupsByOwner( User user );

	Set<User> findGroupOwners( Group group );

	Optional<Member> findMembership( UUID id );

	Optional<Member> findMembership( Group group, User user );

	Set<Member> findMemberships( User user );

	Set<Member> findMemberships( Group group );

	Map<String, Object> findPreferences( User user );

	Optional<UserToken> findUserToken( UUID id );

	Optional<UserToken> findUserTokenByPrincipal( String username );

	List<User> findAllUserAccounts();

	Optional<User> findUser( UUID id );

	List<Verification> findAllVerifications();

	Optional<Verification> findVerification( UUID id );

	int getPilotFlightCount( UUID id );

	long getPilotFlightTime( UUID id );

	int getObserverFlightCount( UUID id );

	long getObserverFlightTime( UUID id );

	Optional<Flight> getLastAircraftFlight( Aircraft aircraft );

	Optional<Flight> getLastPilotFlight( User pilot );

	int getAircraftFlightCount( Aircraft aircraft );

	long getAircraftFlightTime( Aircraft aircraft );

	int getBatteryFlightCount( Battery battery );

	long getBatteryFlightTime( Battery battery );

	boolean isPreferenceSet( User user, String key );

	boolean isPreferenceSetTo( User user, String key, String value );

	String getPreference( User user, String key );

	String getPreference( User user, String key, String defaultValue );

	Optional<Dashboard> findDashboard( User user );

	Optional<PublicDashboard> findPublicDashboard( User user );

}
