package com.desertskyrangers.flightdeck.adapter.store.repo;

import com.desertskyrangers.flightdeck.BaseTest;
import com.desertskyrangers.flightdeck.adapter.store.entity.PreferencesProjection;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class PreferencesRepoTest extends BaseTest {

	@Autowired
	PreferencesRepo preferencesRepo;

	@Test
	void testSaveAndFind() {
		PreferencesProjection expected = new PreferencesProjection().setId( UUID.randomUUID() ).setJson( "{}" );
		preferencesRepo.save( expected );

		PreferencesProjection actual = preferencesRepo.findById( expected.getId() ).orElse( null );

		assertThat( actual ).isEqualTo( expected );
	}

}
