package com.desertskyrangers.flightdeck.adapter.state.repo;

import com.desertskyrangers.flightdeck.adapter.state.entity.PublicDashboardProjection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PublicDashboardRepo extends JpaRepository<PublicDashboardProjection, UUID> {}
