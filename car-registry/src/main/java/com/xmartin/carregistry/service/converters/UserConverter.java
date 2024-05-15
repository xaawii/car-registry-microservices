package com.xmartin.carregistry.service.converters;


import com.xmartin.carregistry.domain.User;
import com.xmartin.carregistry.entity.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class UserConverter {

    public User toModel(UserEntity userEntity){

        return User.builder()
                .id(userEntity.getId())
                .email(userEntity.getEmail())
                .password(userEntity.getPassword())
                .name(userEntity.getName())
                .role(userEntity.getRole())
                .build();

    }
}
