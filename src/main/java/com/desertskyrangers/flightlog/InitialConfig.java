package com.desertskyrangers.flightlog;

import com.desertskyrangers.flightlog.core.model.UserAccount;
import com.desertskyrangers.flightlog.core.model.UserCredential;
import com.desertskyrangers.flightlog.port.StatePersisting;
import com.desertskyrangers.flightlog.port.StateRetrieving;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Set;

@Configuration
@Slf4j
public class InitialConfig {

	private final StatePersisting statePersisting;

	private final StateRetrieving stateRetrieving;

	public InitialConfig( StatePersisting statePersisting, StateRetrieving stateRetrieving ) {
		this.statePersisting = statePersisting;
		this.stateRetrieving = stateRetrieving;
	}

	void init() {
		UserCredential credential = new UserCredential();
		credential.username( "tester" );
		credential.password( new BCryptPasswordEncoder().encode( "tester" ) );
		UserAccount user = new UserAccount();
		user.credentials( Set.of( credential ) );
		user.firstName( "Test" );
		user.lastName( "user" );
		statePersisting.upsert( user );

		log.warn( "User created=" + stateRetrieving.findUserCredentialByUsername( "tester" ).get().username() );
	}

}