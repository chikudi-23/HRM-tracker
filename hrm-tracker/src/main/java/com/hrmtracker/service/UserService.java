package com.hrmtracker.service;

import com.hrmtracker.dto.UserRegistrationDto;
import com.hrmtracker.entity.User;

public interface UserService {
    User registerUser(UserRegistrationDto dto);
}
