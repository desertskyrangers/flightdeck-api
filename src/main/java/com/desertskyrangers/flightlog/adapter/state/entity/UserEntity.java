package com.desertskyrangers.flightlog.adapter.state.entity;

import com.desertskyrangers.flightlog.core.model.SmsProvider;
import com.desertskyrangers.flightlog.core.model.UserAccount;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@Entity
@Table( name = "user" )
public class UserEntity {

	@Id
	@Column( columnDefinition = "BINARY(16)" )
	private UUID id;

	@Column( name = "firstname" )
	private String firstName;

	@Column( name = "lastname" )
	private String lastName;

	@Column( name = "preferredname" )
	private String preferredName;

	private String email;

	@Column( name = "emailverified" )
	private Boolean emailVerified;

	@Column( name = "smsnumber" )
	private String smsNumber;

	@Column( name = "smsprovider" )
	private String provider;

	@Column( name = "smsverified" )
	private Boolean smsVerified;

	@EqualsAndHashCode.Exclude
	@OneToMany( cascade = CascadeType.ALL, fetch = FetchType.EAGER )
	@CollectionTable( name = "usercredential", joinColumns = @JoinColumn( name = "userid" ) )
	private Set<CredentialEntity> credentials = new HashSet<>();

	@ElementCollection
	@Column( name = "roles", nullable = false )
	@CollectionTable( name = "userrole", joinColumns = @JoinColumn( name = "userid" ) )
	private Set<String> roles = new HashSet<>();

	public static UserEntity from( UserAccount user ) {
		return fromUserAccount( user, true );
	}

	public static UserAccount toUserAccount( UserEntity entity ) {
		UserAccount user = new UserAccount();

		user.id( entity.getId() );
		user.firstName( entity.getFirstName() );
		user.lastName( entity.getLastName() );
		user.preferredName( entity.getPreferredName() );
		user.email( entity.getEmail() );
		user.emailVerified( entity.getEmailVerified() != null && entity.getEmailVerified() );
		user.smsNumber( entity.getSmsNumber() );
		if( entity.getSmsNumber() != null ) user.smsProvider( SmsProvider.valueOf( entity.getSmsNumber().toUpperCase() ) );
		user.smsVerified( entity.getSmsVerified() != null && entity.getSmsVerified() );
		user.credentials( entity.getCredentials().stream().map( c -> CredentialEntity.toUserCredential( user, c ) ).collect( Collectors.toSet() ) );
		user.roles( entity.getRoles() );

		return user;
	}

	static UserEntity fromWithoutCredential( UserAccount user ) {
		return fromUserAccount( user, false );
	}

	private static UserEntity fromUserAccount( UserAccount user, boolean includeCredential ) {
		UserEntity entity = new UserEntity();

		entity.setId( user.id() );
		entity.setFirstName( user.firstName() );
		entity.setLastName( user.lastName() );
		entity.setPreferredName( user.preferredName() );
		entity.setEmail( user.email() );
		entity.setEmailVerified( user.emailVerified() );
		entity.setSmsNumber( user.smsNumber() );
		if( user.smsProvider() != null ) entity.setSmsNumber( user.smsProvider().name().toLowerCase() );
		entity.setSmsVerified( user.smsVerified() );
		if( includeCredential ) entity.setCredentials( user.credentials().stream().map( CredentialEntity::from ).peek( c -> c.setUserAccount( entity ) ).collect( Collectors.toSet() ) );
		entity.setRoles( user.roles() );

		return entity;
	}

}
