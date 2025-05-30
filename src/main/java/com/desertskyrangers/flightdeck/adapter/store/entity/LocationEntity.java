package com.desertskyrangers.flightdeck.adapter.store.entity;

import com.desertskyrangers.flightdeck.core.model.*;
import lombok.Data;
import lombok.experimental.Accessors;

import jakarta.persistence.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Data
@Entity
@Table( name = "location" )
@Accessors( chain = true )
public class LocationEntity {

	@Id
	private UUID id;

	private double latitude;

	private double longitude;

	private double altitude;

	@ManyToOne( optional = false, fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
	@JoinColumn( name = "userid", nullable = false, updatable = false )
	private UserEntity user;

	@Column( length = 160 )
	private String name;

	private double size;

	private String status;

	public static LocationEntity from( Location location ) {
		LocationEntity entity = new LocationEntity();

		entity.setId( location.id() );
		entity.setLatitude( location.latitude() );
		entity.setLongitude( location.longitude() );
		entity.setAltitude( location.altitude() );
		entity.setUser( UserEntity.from( location.user() ) );
		entity.setName( location.name() );
		entity.setSize( location.size() );
		entity.setStatus( location.status().name().toLowerCase() );

		return entity;
	}

	public static Location toLocation( LocationEntity entity ) {
		if( entity == null ) return null;

		Location location = toLocationShallow( entity );

		final Map<UUID, Group> groups = new HashMap<>();
		final Map<UUID, Location> locations = new HashMap<>();
		final Map<UUID, Member> members = new HashMap<>();
		final Map<UUID, User> users = new HashMap<>();
		locations.put( entity.getId(), location );

		location.user( UserEntity.toUserFromRelated( entity.getUser(), users, groups, locations, members ) );

		return location;
	}

	private static Location toLocationFromRelated( LocationEntity entity, Map<UUID, Member> members, Map<UUID, Group> groups, Map<UUID, Location> locations, Map<UUID, User> users ) {
		// If the user already exists, just return it
		Location location = locations.get( entity.getId() );
		if( location != null ) return location;

		// Create the shallow version of the location and put it in the locations map
		location = toLocationShallow( entity );
		locations.put( entity.getId(), location );

		// Link the location to related entities
		location.user( UserEntity.toUserFromRelated( entity.getUser(), users, groups, locations, members ) );

		return location;
	}

	private static Location toLocationShallow( LocationEntity entity ) {
		Location location = new Location();

		location.id( entity.getId() );
		location.latitude( entity.getLatitude() );
		location.longitude( entity.getLongitude() );
		location.altitude( entity.getAltitude() );
		location.name( entity.getName() );
		location.size( entity.getSize() );
		location.status( Location.Status.valueOf( entity.getStatus().toUpperCase() ) );

		return location;
	}

}
