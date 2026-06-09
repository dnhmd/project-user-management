package com.dnhmd.user_management.repository;

import com.dnhmd.user_management.entity.RefreshToken;
import com.dnhmd.user_management.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String refreshToken);
    List<RefreshToken> findByUser(User user);
}
