package com.desertskyrangers.flightdeck.util;

import com.desertskyrangers.flightdeck.adapter.web.model.ReactRegisterRequest;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class JsonTest {

	@Test
	void asMapCreatesObjectMap() {
		UUID id = UUID.randomUUID();
		TestBean bean = new TestBean();
		bean.setId( id );
		bean.setName( "test bean" );

		Map<String, Object> map = Json.asMap( bean );
		assertThat( map).containsAllEntriesOf( Map.of("id", id.toString(), "name", "test bean") );
	}

	@Test
	void asMapCreatesObjectMapForReactUserAccount() {
		ReactRegisterRequest account = new ReactRegisterRequest();
		account.setId( UUID.randomUUID().toString() );
		account.setUsername( "mockuser" );
		account.setPassword( "password");

		Map<String, Object> map = Json.asMap( account );
		assertThat( map).containsAllEntriesOf( Map.of("id", account.getId(), "username", account.getUsername(), "password", account.getPassword()) );
	}

	private static class TestBean {

		private UUID id;

		private String name;

		public UUID getId() {
			return id;
		}

		public void setId( UUID id ) {
			this.id = id;
		}

		public String getName() {
			return name;
		}

		public void setName( String name ) {
			this.name = name;
		}

	}

}
