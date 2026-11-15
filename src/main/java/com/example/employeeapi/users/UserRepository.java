package com.example.employeeapi.users;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

/** Accès aux utilisateurs en base. */
public interface UserRepository extends MongoRepository<User, String> {

    Optional<User> findByEmail(String email);
}
