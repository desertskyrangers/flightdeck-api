package com.desertskyrangers.flightdeck.adapter.state.repo;

import com.desertskyrangers.flightdeck.adapter.state.entity.AircraftEntity;
import com.desertskyrangers.flightdeck.core.model.AircraftStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AircraftRepo extends JpaRepository<AircraftEntity, UUID> {

	List<AircraftEntity> findAircraftByOwner( UUID owner );

	List<AircraftEntity> findAircraftByOwnerOrderByName( UUID owner );

	List<AircraftEntity> findAircraftByOwnerAndStatusOrderByName( UUID owner, String status );

}
