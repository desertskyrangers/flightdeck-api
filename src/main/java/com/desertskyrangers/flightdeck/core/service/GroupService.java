package com.desertskyrangers.flightdeck.core.service;

import com.desertskyrangers.flightdeck.core.model.Group;
import com.desertskyrangers.flightdeck.core.model.Member;
import com.desertskyrangers.flightdeck.core.model.MemberStatus;
import com.desertskyrangers.flightdeck.core.model.User;
import com.desertskyrangers.flightdeck.port.GroupServices;
import com.desertskyrangers.flightdeck.port.StatePersisting;
import com.desertskyrangers.flightdeck.port.StateRetrieving;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
public class GroupService implements GroupServices {

	private final StatePersisting statePersisting;

	private final StateRetrieving stateRetrieving;

	public GroupService( StatePersisting statePersisting, StateRetrieving stateRetrieving ) {
		this.statePersisting = statePersisting;
		this.stateRetrieving = stateRetrieving;
	}

	@Override
	public Set<Group> findAllAvailable( User user ) {
		return stateRetrieving.findAllAvailableGroups( user );
	}

	@Override
	public Optional<Group> find( UUID id ) {
		return stateRetrieving.findGroup( id );
	}

	@Override
	public Group create( User requester, User owner, Group group ) {
		Group result = upsert( group );
		statePersisting.upsert( new Member().user( owner ).group( group ).status( MemberStatus.OWNER ) );
		return result;
	}

	@Override
	public Group upsert( Group group ) {
		return statePersisting.upsert( group );
	}

	@Override
	public Group remove( Group group ) {
		statePersisting.remove( group );
		return group;
	}

	@Override
	public Set<Group> findGroupsByUser( User user ) {
		return stateRetrieving.findGroupsByOwner( user );
	}

}
