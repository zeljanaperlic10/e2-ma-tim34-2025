package com.example.myapplication.domain.service;

import com.example.myapplication.data.repository.FirebaseRepository;

public class UserService {
    private final FirebaseRepository repository = new FirebaseRepository();

    public void register(String email, String password, String username, String avatar,
                         FirebaseRepository.OnUserRegistered callback) {
        repository.registerUser(email, password, username, avatar, callback);
    }
}
