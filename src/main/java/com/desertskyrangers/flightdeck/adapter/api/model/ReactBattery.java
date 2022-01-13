package com.desertskyrangers.flightdeck.adapter.api.model;

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

	private String type;

	private int cells;

	private int cycles;

	private int capacity;

	private int chargeRating;

	private int dischargeRating;

	private String owner;

	private String ownerType;

	public static ReactBattery from( Battery battery ) {
		ReactBattery result = new ReactBattery();

		result.setId( battery.id().toString() );
		result.setName( battery.name() );
		if( battery.status() != null ) result.setStatus( battery.status().name().toLowerCase() );
		result.setMake( battery.make() );
		result.setModel( battery.model() );
		if( battery.connector() != null ) result.setConnector( battery.connector().name().toLowerCase() );

		if( battery.type() != null ) result.setType( battery.type().name().toLowerCase() );
		result.setCells( battery.cells() );
		result.setCycles( battery.cycles() );
		result.setCapacity( battery.capacity() );
		result.setChargeRating( battery.chargeRating() );
		result.setDischargeRating( battery.dischargeRating() );

		result.setOwner( battery.owner().toString() );
		result.setOwnerType( battery.ownerType().name().toLowerCase() );

		return result;
	}

	public static Battery toBattery( ReactBattery reactBattery ) {
		Battery battery = new Battery();

		battery.id( UUID.fromString( reactBattery.getId() ) );
		battery.name( reactBattery.getName() );
		if( reactBattery.getStatus() != null ) battery.status( BatteryStatus.valueOf( reactBattery.getStatus().toUpperCase() ) );
		battery.make( reactBattery.getMake() );
		battery.model( reactBattery.getModel() );
		if( reactBattery.getConnector() != null ) battery.connector( BatteryConnector.valueOf( reactBattery.getConnector().toUpperCase() ) );

		if( reactBattery.getType() != null ) battery.type( BatteryType.valueOf( reactBattery.getType().toUpperCase() ) );
		battery.cells( reactBattery.getCells() );
		battery.cycles( reactBattery.getCycles() );
		battery.capacity( reactBattery.getCapacity() );
		battery.chargeRating( reactBattery.getChargeRating() );
		battery.dischargeRating( reactBattery.getDischargeRating() );

		if( reactBattery.getOwner() != null ) battery.owner( UUID.fromString( reactBattery.getOwner() ) );
		if( reactBattery.getOwnerType() != null ) battery.ownerType( OwnerType.valueOf( reactBattery.getOwnerType().toUpperCase() ) );

		return battery;
	}

}
