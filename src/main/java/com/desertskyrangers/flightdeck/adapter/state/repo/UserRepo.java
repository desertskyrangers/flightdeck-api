package com.desertskyrangers.flightdeck.adapter.state.repo;

import com.desertskyrangers.flightdeck.adapter.state.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRepo extends JpaRepository<UserEntity, UUID> {}
