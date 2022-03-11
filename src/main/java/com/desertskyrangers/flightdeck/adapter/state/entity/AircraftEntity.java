package com.desertskyrangers.flightdeck.adapter.state.entity;

import com.desertskyrangers.flightdeck.core.model.Aircraft;
import com.desertskyrangers.flightdeck.core.model.AircraftStatus;
import com.desertskyrangers.flightdeck.core.model.AircraftType;
import com.desertskyrangers.flightdeck.core.model.OwnerType;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

@Data
@Entity
@Table( name = "aircraft" )
@Accessors( chain = true )
public class AircraftEntity {

	@Id
	@Column( columnDefinition = "BINARY(16)" )
	private UUID id;

	@Column( nullable = false )
	private String name;

	private String type;

	private String make;

	private String model;

	private String status;

	private String connector;

	private double wingspan;

	private double length;

	private double wingarea;

	private double weight;

	@Column( name = "nightlights" )
	private Boolean nightLights;

	@Column( nullable = false, columnDefinition = "BINARY(16)" )
	private UUID owner;

	@Column( name = "ownertype", nullable = false )
	private String ownerType;

	public static AircraftEntity from( Aircraft aircraft ) {
		AircraftEntity entity = new AircraftEntity();

		entity.setId( aircraft.id() );
		entity.setName( aircraft.name() );
		entity.setType( aircraft.type().name().toLowerCase() );
		entity.setMake( aircraft.make() );
		entity.setModel( aircraft.model() );
		entity.setStatus( aircraft.status().name().toLowerCase() );
		entity.setOwner( aircraft.owner() );
		if( aircraft.ownerType() != null ) entity.setOwnerType( aircraft.ownerType().name().toLowerCase() );

		entity.setWingspan( aircraft.wingspan() );
		entity.setLength( aircraft.length() );
		entity.setWingarea( aircraft.wingarea() );
		entity.setWeight( aircraft.weight() );

		entity.setNightLights( aircraft.nightLights() );

		return entity;
	}

	public static Aircraft toAircraft( AircraftEntity entity ) {
		Aircraft aircraft = new Aircraft();

		aircraft.id( entity.getId() );
		aircraft.name( entity.getName() );
		aircraft.type( AircraftType.valueOf( entity.getType().toUpperCase() ) );
		aircraft.make( entity.getMake() );
		aircraft.model( entity.getModel() );
		aircraft.status( AircraftStatus.valueOf( entity.getStatus().toUpperCase() ) );
		aircraft.owner( entity.getOwner() );
		if( entity.getOwnerType() != null ) aircraft.ownerType( OwnerType.valueOf( entity.getOwnerType().toUpperCase() ) );

		aircraft.wingspan( entity.getWingspan() );
		aircraft.length( entity.getLength() );
		aircraft.wingarea( entity.getWingarea() );
		aircraft.weight( entity.getWeight() );

		aircraft.nightLights( entity.getNightLights() != null && entity.getNightLights() );

		return aircraft;
	}

}
