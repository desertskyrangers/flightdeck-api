package com.desertskyrangers.flightdeck.core.service;

import com.desertskyrangers.flightdeck.core.model.User;
import com.desertskyrangers.flightdeck.core.model.UserToken;
import com.desertskyrangers.flightdeck.port.StatePersisting;
import com.desertskyrangers.flightdeck.port.StateRetrieving;
import com.desertskyrangers.flightdeck.port.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

	private final StateRetrieving stateRetrieving;

	private final StatePersisting statePersisting;

	private final PasswordEncoder passwordEncoder;

	public UserServiceImpl( StateRetrieving stateRetrieving, StatePersisting statePersisting, PasswordEncoder passwordEncoder ) {
		this.stateRetrieving = stateRetrieving;
		this.statePersisting = statePersisting;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public List<User> find() {
		return stateRetrieving.findAllUserAccounts();
	}

	@Override
	public Optional<User> find( UUID id ) {
		return stateRetrieving.findUserAccount( id );
	}

	@Override
	public Optional<User> findByPrincipal( String username ) {
		return stateRetrieving.findUserTokenByPrincipal( username ).map( UserToken::user );
	}

	@Override
	public void upsert( User user ) {
		// If the email address has changed, the email auth token needs to be updated also
		find( user.id() ).ifPresent( current -> {
			if( !Objects.equals( user.email(), current.email() ) ) {
				for( UserToken token : user.tokens() ) {
					if( token.principal().equals( current.email() ) ) token.principal( user.email() );
				}
			}
		} );

		statePersisting.upsert( user );
	}

	@Override
	public void remove( User account ) {
		statePersisting.remove( account );
	}

	@Override
	public boolean isCurrentPassword( User user, String password ) {
		Optional<User> optional = find( user.id() );
		if( optional.isEmpty() ) return false;
		for( UserToken token : optional.get().tokens() ) {
			if( passwordEncoder.matches( password, token.credential() ) ) return true;
		}
		return false;
	}

	@Override
	public void updatePassword( User user, String password ) {
		find( user.id() ).ifPresent( current -> {
			String encodedPassword = passwordEncoder.encode( password );
			for( UserToken token : user.tokens() ) {
				token.credential( encodedPassword );
				statePersisting.upsert( token );
			}
		} );
	}

	@Override
	public Optional<User> findVerificationUser( UUID verificationId ) {
		return stateRetrieving.findVerification( verificationId ).flatMap( v -> stateRetrieving.findUserAccount( v.userId() ) );
	}

}
