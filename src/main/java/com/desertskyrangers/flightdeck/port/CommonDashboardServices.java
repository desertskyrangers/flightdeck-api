package com.desertskyrangers.flightdeck.port;

import com.desertskyrangers.flightdeck.core.model.Group;
import com.desertskyrangers.flightdeck.core.model.User;

import java.util.Optional;
import java.util.UUID;

public interface CommonDashboardServices<T> {

	Optional<T> find( UUID uuid );

	Optional<T> findByUser( User user );

	T upsert( User user, T dashboard );

	String update( User user );

	String update( Group group );

}
