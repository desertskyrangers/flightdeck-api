package com.desertskyrangers.flightdeck.adapter.web.model;

import com.desertskyrangers.flightdeck.core.model.*;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors( chain = true )
@JsonInclude( JsonInclude.Include.NON_NULL )
public class ReactBattery {

	private String id;

	private String name;

	private String status;

	private String make;

	private String model;

	private String connector;

	private String unlistedConnector;

	private String type;

	private int cells;

	private int cycles;

	private int capacity;

	private int dischargeRating;

	private String owner;

	private String ownerType;

	private int life;

	private int flightCount;

	private long flightTime;

	public static ReactBattery from( Battery battery ) {
		ReactBattery result = new ReactBattery();

		result.setId( battery.id().toString() );
		result.setName( battery.name() );
		if( battery.status() != null ) result.setStatus( battery.status().name().toLowerCase() );
		result.setMake( battery.make() );
		result.setModel( battery.model() );
		if( battery.connector() != null ) result.setConnector( battery.connector().name().toLowerCase() );
		if( battery.unlistedConnector() != null ) result.setUnlistedConnector( battery.unlistedConnector() );

		if( battery.chemistry() != null ) result.setType( battery.chemistry().name().toLowerCase() );
		result.setCells( battery.cells() );
		result.setCycles( battery.cycles() );
		result.setCapacity( battery.capacity() );
		result.setDischargeRating( battery.dischargeRating() );

		result.setOwner( battery.owner().toString() );
		result.setOwnerType( battery.ownerType().name().toLowerCase() );

		result.setLife( battery.life() );
		result.setFlightCount( battery.flightCount() );
		result.setFlightTime( battery.flightTime() );

		return result;
	}

	public static Battery toBattery( ReactBattery reactBattery ) {
		Battery battery = new Battery();

		battery.id( UUID.fromString( reactBattery.getId() ) );
		battery.name( reactBattery.getName() );
		if( reactBattery.getStatus() != null ) battery.status( Battery.Status.valueOf( reactBattery.getStatus().toUpperCase() ) );
		battery.make( reactBattery.getMake() );
		battery.model( reactBattery.getModel() );
		if( reactBattery.getConnector() != null ) battery.connector( Battery.Connector.valueOf( reactBattery.getConnector().toUpperCase() ) );
		battery.unlistedConnector( reactBattery.getUnlistedConnector() );

		if( reactBattery.getType() != null ) battery.chemistry( Battery.Chemistry.valueOf( reactBattery.getType().toUpperCase() ) );
		battery.cells( reactBattery.getCells() );
		battery.cycles( reactBattery.getCycles() );
		battery.capacity( reactBattery.getCapacity() );
		battery.dischargeRating( reactBattery.getDischargeRating() );

		if( reactBattery.getOwner() != null ) battery.owner( UUID.fromString( reactBattery.getOwner() ) );
		if( reactBattery.getOwnerType() != null ) battery.ownerType( OwnerType.valueOf( reactBattery.getOwnerType().toUpperCase() ) );

		// Life is a derived value
		// Flight count is a derived value
		// Flight time is a derived value

		return battery;
	}

}
