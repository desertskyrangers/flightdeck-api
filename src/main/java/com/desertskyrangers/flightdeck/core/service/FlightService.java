package com.desertskyrangers.flightdeck.core.service;

import com.desertskyrangers.flightdeck.core.model.*;
import com.desertskyrangers.flightdeck.port.FlightServices;
import com.desertskyrangers.flightdeck.port.StatePersisting;
import com.desertskyrangers.flightdeck.port.StateRetrieving;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FlightService implements FlightServices {

	private final StatePersisting statePersisting;

	private final StateRetrieving stateRetrieving;

	private final UserService userService;

	public FlightService( StatePersisting statePersisting, StateRetrieving stateRetrieving, UserService userService ) {
		this.statePersisting = statePersisting;
		this.stateRetrieving = stateRetrieving;
		this.userService = userService;
	}

	@Override
	public Optional<Flight> find( UUID id ) {
		return stateRetrieving.findFlight( id );
	}

	@Override
	public List<Flight> findByPilot( User pilot ) {
		return stateRetrieving.findFlightsByPilot( pilot.id() );
	}

	@Override
	public List<Flight> findFlightsByUser( User user ) {
		boolean showObserverFlights = stateRetrieving.isPreferenceSetTo( user, PreferenceKey.SHOW_OBSERVER_FLIGHTS, "true" );
		boolean showOwnerFlights = stateRetrieving.isPreferenceSetTo( user, PreferenceKey.SHOW_OWNER_FLIGHTS, "true" );

		Set<Flight> flights = new HashSet<>( stateRetrieving.findFlightsByPilot( user.id() ) );
		if( showObserverFlights ) flights.addAll( stateRetrieving.findFlightsByObserver( user.id() ) );
		if( showOwnerFlights ) flights.addAll( stateRetrieving.findFlightsByOwner( user.id() ) );

		List<Flight> orderedFlights = new ArrayList<>( flights );
		orderedFlights.sort( new FlightTimestampComparator().reversed() );

		return orderedFlights;
	}

	private List<Flight> findFlightsByUserAndCount( User user, int count ) {
		return List.of();
	}

	private List<Flight> findFlightsByUserAndTime( User user, long span ) {
		return List.of();
	}

	@Override
	public void upsert( FlightUpsertRequest request ) {
		User pilot = stateRetrieving.findUser( request.pilot() ).orElse( null );
		User observer = stateRetrieving.findUser( request.observer() ).orElse( null );
		Aircraft aircraft = stateRetrieving.findAircraft( request.aircraft() ).orElse( null );
		Set<Battery> batteries = request.batteries().stream().map( id -> stateRetrieving.findBattery( id ).orElse( null ) ).filter( Objects::nonNull ).collect( Collectors.toSet() );

		// Convert request to a core flight object
		Flight flight = new Flight();
		flight.id( request.id() );
		flight.pilot( pilot );
		flight.unlistedPilot( request.unlistedPilot() );
		flight.observer( observer );
		flight.unlistedObserver( request.unlistedObserver() );
		flight.aircraft( aircraft );
		flight.batteries( batteries );
		flight.timestamp( request.timestamp() );
		flight.duration( request.duration() );
		flight.notes( request.notes() );

		statePersisting.upsert( flight );
	}

	@Override
	public void remove( Flight flight ) {
		statePersisting.remove( flight );
	}

	@Override
	public int getPilotFlightCount( UUID user ) {
		return stateRetrieving.getPilotFlightCount( user );
	}

	@Override
	public long getPilotFlightTime( UUID user ) {
		return stateRetrieving.getPilotFlightTime( user );
	}

}
