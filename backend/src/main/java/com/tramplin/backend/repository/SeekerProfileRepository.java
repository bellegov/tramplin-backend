package com.tramplin.backend.repository;

import com.tramplin.backend.model.SeekerProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeekerProfileRepository extends JpaRepository<SeekerProfile, Long> {}