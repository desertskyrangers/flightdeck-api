package com.desertskyrangers.flightdeck.adapter.web.model;

import com.desertskyrangers.flightdeck.core.model.Location;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors( chain = true )
@JsonInclude( JsonInclude.Include.NON_NULL )
public class ReactLocation {

	private String id;

	private double latitude;

	private double longitude;

	private double altitude;

	private ReactUser user;

	private String name;

	private double size;

	private String status;

	public static ReactLocation from( Location location ) {
		ReactLocation result = new ReactLocation();

		result.setId( location.id().toString() );
		result.setLatitude( location.latitude() );
		result.setLongitude( location.longitude() );
		result.setAltitude( location.altitude() );
		result.setUser( ReactUser.from( location.user() ) );
		result.setName( location.name() );
		result.setSize( location.size() );
		result.setStatus( location.status().name().toLowerCase() );

		return result;
	}

	public static Location toLocation( ReactLocation location ) {
		Location result = new Location();

		if( "custom".equals( location.getId() ) ) {
			result.id( Location.CUSTOM_LOCATION_ID );
		} else if( "device".equals( location.getId() ) ) {
			result.id( Location.DEVICE_LOCATION_ID );
		} else if( "".equals( location.getId() ) ) {
			result.id( Location.NO_LOCATION_ID );
		} else {
			result.id( UUID.fromString( location.getId() ) );
		}

		result.latitude( location.getLatitude() );
		result.longitude( location.getLongitude() );
		result.altitude( location.getAltitude() );
		result.user( ReactUser.to( location.getUser() ) );
		result.name( location.getName() );
		result.size( location.getSize() );
		result.status( Location.Status.valueOf( location.getStatus().toUpperCase() ) );

		return result;
	}

}
