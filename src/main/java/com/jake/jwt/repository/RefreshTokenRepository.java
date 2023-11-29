package com.jake.jwt.repository;

import com.jake.jwt.common.repository.RefreshableCRUDRepository;
import com.jake.jwt.model.RefreshToken;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends RefreshableCRUDRepository<RefreshToken, Integer> {

    Optional<RefreshToken> findByToken(String token);
}
