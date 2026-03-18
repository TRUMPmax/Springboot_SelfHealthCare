package com.example.selfhealthcare.repository;

import com.example.selfhealthcare.domain.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
}
