package com.xmartin.userservice.service.impl;



import com.xmartin.userservice.domain.User;
import com.xmartin.userservice.entity.UserEntity;
import com.xmartin.userservice.exceptions.UserNotFoundException;
import com.xmartin.userservice.repository.UserRepository;
import com.xmartin.userservice.service.UserService;
import com.xmartin.userservice.service.converters.UserConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;

@Service
@RequiredArgsConstructor
@Slf4j

public class UserServiceImpl implements UserService {

    //proporciona una instancia de user repository
    private final UserRepository userRepository;
    private final UserConverter userConverter;

    //guarda un nuevo usuario en la bbdd
    @Override
    public User save(User newUser) {
        return userConverter.toModel(userRepository.save(userConverter.toEntity(newUser)));
    }

    @Override
    public User getUserByEmail(String email) throws UserNotFoundException {
        return userConverter.toModel(userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found")));
    }

    @Override
    public void deleteUser(String email) throws UserNotFoundException {
        UserEntity userEntity = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        userRepository.delete(userEntity);
    }

    @Override
    public void addUserImage(Integer id, MultipartFile file) throws IOException, UserNotFoundException {
        UserEntity userEntity = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User with ID " + id + " not found"));

        userEntity.setImage(Base64.getEncoder().encode(file.getBytes()));
        userRepository.save(userEntity);

    }

    @Override
    public byte[] getUserImage(Integer id) throws UserNotFoundException {
        UserEntity userEntity = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User with ID " + id + " not found"));
        return Base64.getDecoder().decode(userEntity.getImage());
    }


}
