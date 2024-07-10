package com.xmartin.userservice.controller;


import com.xmartin.userservice.controller.dtos.UserRequest;
import com.xmartin.userservice.controller.mappers.UserMapper;
import com.xmartin.userservice.exceptions.UserNotFoundException;
import com.xmartin.userservice.service.impl.UserServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    public final UserServiceImpl userService;
    public final UserMapper userMapper;


    @Operation(summary = "Log in", description = "Log in a user in the application")
    @PostMapping("/save")
    public ResponseEntity<?> saveUser(@Valid @RequestBody UserRequest userRequest) {


        try {
            return ResponseEntity.ok(userMapper.toResponse(userService.save(userMapper.toModel(userRequest))));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credentials are incorrect or don't exist");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

    }

    @Operation(summary = "Delete user by email", description = "Delete user by email")
    @DeleteMapping("/{email}")

    public ResponseEntity<?> deleteUser(@PathVariable String email) {

        try {
            userService.deleteUser(email);
            return ResponseEntity.ok("User deleted");
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User with email " + email + " not found.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

    }

    @Operation(summary = "Get user by email", description = "Get user by email")
    @GetMapping("/{email}")
    public ResponseEntity<?> getUser(@PathVariable String email) {

        try {
            return ResponseEntity.ok(userMapper.toResponse(userService.getUserByEmail(email)));
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User with email " + email + " not found.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

    }

    @Operation(summary = "Check if user exist by email", description = "Check if user exist by email")
    @GetMapping("/exist/{email}")
    public ResponseEntity<?> getUserExistByEmail(@PathVariable String email) {

        try {
            boolean exist = userService.userExists(email);
            return ResponseEntity.ok(exist);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

    }

    @Operation(summary = "Add image to user by ID", description = "Add an image to a user with specified ID")
    @PostMapping("userImage/{id}/add")

    public ResponseEntity<String> addImage(@PathVariable Integer id, @RequestParam("imageFile") MultipartFile imageFile) {
        try {
            if (imageFile.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing image file");
            }
            userService.addUserImage(id, imageFile);
            return ResponseEntity.ok().body("Image saved");
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Cannot save image");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Download image from user by ID", description = "Download the image from a user with specified ID")
    @GetMapping("userImage/{id}/download")
    public ResponseEntity<?> downloadImage(@PathVariable Integer id) {
        try {
            byte[] image = userService.getUserImage(id);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);
            return new ResponseEntity<>(image, headers, HttpStatus.OK);
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


}
