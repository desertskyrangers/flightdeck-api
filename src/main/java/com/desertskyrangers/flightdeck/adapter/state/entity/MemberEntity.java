package com.desertskyrangers.flightdeck.adapter.state.entity;

import com.desertskyrangers.flightdeck.core.model.Group;
import com.desertskyrangers.flightdeck.core.model.Member;
import com.desertskyrangers.flightdeck.core.model.MemberStatus;
import com.desertskyrangers.flightdeck.core.model.User;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Data
@Entity
@Table( name = "member" )
@Accessors( chain = true )
public class MemberEntity {

	@Id
	@Column( columnDefinition = "BINARY(16)" )
	private UUID id;

	@ManyToOne( optional = false, fetch = FetchType.EAGER )
	@JoinColumn( name = "userid", nullable = false, updatable = false, columnDefinition = "BINARY(16)" )
	private UserEntity user;

	@ManyToOne( optional = false, fetch = FetchType.EAGER )
	@JoinColumn( name = "orgid", nullable = false, updatable = false, columnDefinition = "BINARY(16)" )
	private GroupEntity group;

	private String status;

	public static MemberEntity from( Member member ) {
		MemberEntity entity = new MemberEntity();
		entity.setId( member.id() );
		entity.setUser( UserEntity.from( member.user() ) );
		entity.setGroup( GroupEntity.from( member.group() ) );
		entity.setStatus( member.status().name().toLowerCase() );
		return entity;
	}

	public static Member toMember( MemberEntity entity ) {
		Member member = toMemberShallow( entity );

		final Map<UUID, Group> groups = new HashMap<>();
		final Map<UUID, Member> members = new HashMap<>();
		final Map<UUID, User> users = new HashMap<>();
		members.put( entity.getId(), member );

		member.group( GroupEntity.toGroupFromRelated( entity.getGroup(), groups, members, users ) );
		member.user( UserEntity.toUserFromRelated( entity.getUser(), users, groups, members ) );

		return member;
	}

	/**
	 * This method is specifically built to avoid a stack overflow when converting
	 * a membership record from the {@link GroupEntity#toGroup(GroupEntity)} method.
	 *
	 * @param entity
	 * @param groups
	 * @param members
	 * @return
	 */
	static Member toMemberFromRelated( MemberEntity entity, Map<UUID, Member> members, Map<UUID, Group> groups, Map<UUID, User> users ) {
		// If the member already exists, just return it
		Member member = members.get( entity.getId() );
		if( member != null ) return member;

		// Create the shallow version of the member and put it in the members map
		member = toMemberShallow( entity );
		members.put( entity.getId(), member );

		// Link the member to related entities
		member.user( UserEntity.toUserFromRelated( entity.getUser(), users, groups, members ) );
		member.group( GroupEntity.toGroupFromRelated( entity.getGroup(), groups, members, users ) );

		return member;
	}

	private static Member toMemberShallow( MemberEntity entity ) {
		Member member = new Member();

		member.id( entity.getId() );
		member.status( MemberStatus.valueOf( entity.getStatus().toUpperCase() ) );

		return member;
	}

}
