package com.xmartin.userservice.service;


import com.xmartin.userservice.domain.User;
import com.xmartin.userservice.exceptions.UserNotFoundException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface UserService {


    //guarda un nuevo usuario en la bbdd
    public User save(User newUser);

    boolean userExists(String email);

    public User getUserByEmail(String email) throws UserNotFoundException;

    public void deleteUser(String email) throws UserNotFoundException;

    public void addUserImage(Integer id, MultipartFile file) throws IOException, UserNotFoundException;

    public byte[] getUserImage(Integer id) throws UserNotFoundException;
}
