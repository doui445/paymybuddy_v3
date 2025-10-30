package com.paymybuddy.service;

import com.paymybuddy.model.User;

import java.util.Optional;

public interface UserService {

    Iterable<User> getUsers();

    Optional<User> getUserById(Integer id);

    Optional<User> getUserByEmail(String email);

    User saveUser(User user);

    void deleteUserById(Integer id);

    void connectUsers(Integer id1, Integer id2);
}
