package com.desertskyrangers.flightdeck.port;

import com.desertskyrangers.flightdeck.core.model.*;

import java.util.Map;

public interface StatePersisting {

	Aircraft upsert( Aircraft aircraft );

	void remove( Aircraft aircraft );

	Battery upsert( Battery battery );

	void remove( Battery battery );

	Flight upsert( Flight flight );

	void remove( Flight flight );

	void removeAllFlights();

	Group upsert( Group group );

	void remove( Group group );

	void removeAllGroups();

	Member upsert( Member member );

	Member remove( Member member );

	void removeAllMembers();

	Map<String, Object> upsertPreferences( User user, Map<String, Object> preferences );

	Map<String, Object> removePreferences( User user );

	User upsert( User account );

	void remove( User account );

	void upsert( UserToken token );

	Verification upsert( Verification verification );

	Verification remove( Verification verification );

	Dashboard upsertDashboard( User user, Dashboard dashboard );

	Dashboard removeDashboard( User user );

	PublicDashboard upsertPublicDashboard( User user, PublicDashboard dashboard );

	PublicDashboard removePublicDashboard( User user );

}
