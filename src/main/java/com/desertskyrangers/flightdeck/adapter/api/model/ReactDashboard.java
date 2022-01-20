package com.desertskyrangers.flightdeck.adapter.api.model;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors( chain = true )
public class ReactDashboard {

	private int pilotFlightCount;

	private long pilotFlightTime;

}