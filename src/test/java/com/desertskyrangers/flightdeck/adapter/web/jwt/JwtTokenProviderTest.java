package com.desertskyrangers.flightdeck.adapter.web.jwt;

import com.desertskyrangers.flightdeck.core.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class JwtTokenProviderTest {

	@Autowired
	private JwtTokenProvider tokenProvider;

	@Test
	void testCreateToken() {
		// given
		User account = new User();
		Authentication authentication = new TestingAuthenticationToken( "username", "password", "TESTER" );
		boolean remember = false;
		long timestamp = System.currentTimeMillis();
		long expiration = timestamp / 1000 + tokenProvider.getJwtValidityInSeconds();

		// when
		String token = tokenProvider.createToken( account, authentication, remember, timestamp );

		// then
		Map<String, Object> claims = tokenProvider.parse( token );
		assertThat( claims ).containsEntry( JwtToken.USER_ID_CLAIM_KEY, account.id().toString() );
		assertThat( claims ).containsEntry( JwtToken.SUBJECT_CLAIM_KEY, "username" );
		assertThat( claims ).containsEntry( JwtToken.AUTHORITIES_CLAIM_KEY, "TESTER" );
		assertThat( claims ).containsEntry( JwtToken.EXPIRES_CLAIM_KEY, expiration );
	}

	@Test
	void testCreateRememberedToken() {
		// given
		User account = new User();
		Authentication authentication = new TestingAuthenticationToken( "username", "password", "TESTER" );
		boolean remember = true;
		long timestamp = System.currentTimeMillis();
		long expiration = timestamp / 1000 + tokenProvider.getRememberedJwtValidityInSeconds();

		// when
		String token = tokenProvider.createToken( account, authentication, remember, timestamp );

		// then
		Map<String, Object> claims = tokenProvider.parse( token );
		assertThat( claims ).containsEntry( JwtToken.USER_ID_CLAIM_KEY, account.id().toString() );
		assertThat( claims ).containsEntry( JwtToken.SUBJECT_CLAIM_KEY, "username" );
		assertThat( claims ).containsEntry( JwtToken.AUTHORITIES_CLAIM_KEY, "TESTER" );
		assertThat( claims ).containsEntry( JwtToken.EXPIRES_CLAIM_KEY, expiration );
	}

}
