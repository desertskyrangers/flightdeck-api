package com.desertskyrangers.flightdeck.core.service;

import com.desertskyrangers.flightdeck.core.model.Battery;
import com.desertskyrangers.flightdeck.port.BatteryService;
import com.desertskyrangers.flightdeck.port.StatePersisting;
import com.desertskyrangers.flightdeck.port.StateRetrieving;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class BatteryServiceImpl implements BatteryService {

	private final StatePersisting statePersisting;

	private final StateRetrieving stateRetrieving;

	public BatteryServiceImpl( StatePersisting statePersisting, StateRetrieving stateRetrieving ) {
		this.statePersisting = statePersisting;
		this.stateRetrieving = stateRetrieving;
	}

	@Override
	public Optional<Battery> find( UUID id ) {
		return stateRetrieving.findBattery( id );
	}

	@Override
	public List<Battery> findByOwner( UUID owner ) {
		return stateRetrieving.findBatteriesByOwner( owner );
	}

	@Override
	public void upsert( Battery battery ) {
		statePersisting.upsert( battery );
	}

	@Override
	public void remove( Battery battery ) {
		statePersisting.remove( battery );
	}
}
