package com.xmartin.authservice.service.impl;

import com.xmartin.authservice.controller.dto.AuthUserDto;
import com.xmartin.authservice.controller.dto.TokenDto;
import com.xmartin.authservice.entity.AuthUser;

public interface AuthUserService {

    public AuthUser save(AuthUserDto authUserDto);

    public TokenDto login(AuthUserDto authUserDto);

    public TokenDto validate(String token);
}
