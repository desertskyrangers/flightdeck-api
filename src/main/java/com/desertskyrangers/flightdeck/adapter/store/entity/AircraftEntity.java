package com.desertskyrangers.flightdeck.adapter.store.entity;

import com.desertskyrangers.flightdeck.util.AppColor;
import com.desertskyrangers.flightdeck.core.model.Aircraft;
import com.desertskyrangers.flightdeck.core.model.AircraftType;
import com.desertskyrangers.flightdeck.core.model.OwnerType;
import com.desertskyrangers.flightdeck.util.AppColorConverter;
import lombok.Data;
import lombok.experimental.Accessors;

import jakarta.persistence.*;
import java.util.UUID;

@Data
@Entity
@Table( name = "aircraft" )
@Accessors( chain = true )
public class AircraftEntity {

	@Id
	private UUID id;

	@Column( nullable = false )
	private String name;

	private String type;

	private String make;

	private String model;

	private String status;

	private String connector;

	private Double wingspan;

	private Double length;

	private Double wingarea;

	private Double weight;

	@Column( name = "nightlights" )
	private Boolean nightLights;

	@Column( name = "flightcount" )
	private Integer flightCount;

	@Column( name = "flighttime" )
	private Long flightTime;

	@Column( nullable = false )
	private UUID owner;

	@Column( name = "ownertype", nullable = false )
	private String ownerType;

	@Column( name = "basecolor" )
	@Convert( converter = AppColorConverter.class )
	private AppColor baseColor;

	@Column( name = "trimcolor" )
	@Convert( converter = AppColorConverter.class )
	private AppColor trimColor;

	public static AircraftEntity from( Aircraft aircraft ) {
		AircraftEntity entity = new AircraftEntity();

		entity.setId( aircraft.id() );
		entity.setName( aircraft.name() );
		entity.setType( aircraft.type().name().toLowerCase() );
		entity.setMake( aircraft.make() );
		entity.setModel( aircraft.model() );
		entity.setStatus( aircraft.status().name().toLowerCase() );
		entity.setNightLights( aircraft.nightLights() );

		entity.setWingspan( aircraft.wingspan() );
		entity.setLength( aircraft.length() );
		entity.setWingarea( aircraft.wingarea() );
		entity.setWeight( aircraft.weight() );

		entity.setFlightCount( aircraft.flightCount() );
		entity.setFlightTime( aircraft.flightTime() );

		entity.setOwner( aircraft.owner() );
		if( aircraft.ownerType() != null ) entity.setOwnerType( aircraft.ownerType().name().toLowerCase() );

		entity.setBaseColor( aircraft.baseColor() );
		entity.setTrimColor( aircraft.trimColor() );

		return entity;
	}

	public static Aircraft toAircraft( AircraftEntity entity ) {
		Aircraft aircraft = new Aircraft();

		aircraft.id( entity.getId() );
		aircraft.name( entity.getName() );
		aircraft.type( AircraftType.valueOf( entity.getType().toUpperCase() ) );
		aircraft.make( entity.getMake() );
		aircraft.model( entity.getModel() );
		aircraft.status( Aircraft.Status.valueOf( entity.getStatus().toUpperCase() ) );
		aircraft.nightLights( entity.getNightLights() != null && entity.getNightLights() );

		aircraft.wingspan( entity.getWingspan() == null ? 0.0 : entity.getWingspan() );
		aircraft.length( entity.getLength() == null ? 0.0 : entity.getLength() );
		aircraft.wingarea( entity.getWingarea() == null ? 0.0 : entity.getWingarea() );
		aircraft.weight( entity.getWeight() == null ? 0.0 : entity.getWeight() );

		aircraft.flightCount( entity.getFlightCount() == null ? 0 : entity.getFlightCount() );
		aircraft.flightTime( entity.getFlightTime() == null ? 0 : entity.getFlightTime() );

		aircraft.owner( entity.getOwner() );
		if( entity.getOwnerType() != null ) aircraft.ownerType( OwnerType.valueOf( entity.getOwnerType().toUpperCase() ) );

		aircraft.baseColor( entity.getBaseColor() );
		aircraft.trimColor( entity.getTrimColor() );

		return aircraft;
	}

}
