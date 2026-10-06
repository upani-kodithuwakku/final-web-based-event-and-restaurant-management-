package com.group06.restaurantevent.users.repository;

import com.group06.restaurantevent.users.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByEmailAndIsActiveTrue(String email);
    boolean existsByEmail(String email);

    @Query("SELECT DISTINCT u FROM User u JOIN u.roles r WHERE r.name <> :excludeRole ORDER BY u.fullName ASC")
    List<User> findAllExcludingRole(@Param("excludeRole") String excludeRole);
}
