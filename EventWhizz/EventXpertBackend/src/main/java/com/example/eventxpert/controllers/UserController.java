package com.example.eventxpert.controllers;

import com.example.eventxpert.annotations.UnprotectedEndpoint;
import com.example.eventxpert.dao.Userdao;
import com.example.eventxpert.pojo.User;
import com.password4j.Hash;
import com.password4j.Password;
import org.springframework.web.bind.annotation.*;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.HashMap;
import java.util.Date;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jakarta.persistence.NoResultException;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "http://localhost:3000")
public class UserController {
    @UnprotectedEndpoint
    @PostMapping("/signup")
    public ResponseEntity<Map<String, String>> addUser(final @RequestBody User user, Userdao userdao) {
        Map<String, String> jsonResponse = new HashMap<>();
        try {
            Pattern pattern = Pattern.compile("^(.+)@(.+)$");
            Matcher matcher = pattern.matcher(user.getEmail());

            if (!matcher.matches()) {
                jsonResponse.put("message", "Email address invalid");
                return ResponseEntity.status(400).body(jsonResponse);
            }

            Hash encodedPassword = Password.hash(user.getPassword()).withBcrypt();
            user.setPassword(encodedPassword.getResult());

            boolean isUserExisting = userdao.isUserExisting(user.getEmail(), user.getUsername());

            if (isUserExisting) {
                jsonResponse.put("message", "User already exists");
                return ResponseEntity.status(400).body(jsonResponse);
            }

            userdao.addUser(user);
            jsonResponse.put("message", "Successfully added user");
            return ResponseEntity.status(200).body(jsonResponse);
        }
        catch(Exception e) {
            System.out.println(e);
            jsonResponse.put("message", "Something went wrong");
            return ResponseEntity.status(500).body(jsonResponse);
        }
    }

    @UnprotectedEndpoint
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(final @RequestBody User user, Userdao userdao) {
        Map<String, Object> jsonResponse = new HashMap<>();
        try {
            User userDetails = userdao.getUserByUsername(user.getUsername());

            if (userDetails == null) {
                jsonResponse.put("message", "User not found");
                return ResponseEntity.status(404).body(jsonResponse);
            }

            boolean verified = Password.check(user.getPassword(), userDetails.getPassword()).withBcrypt();

            if (!verified) {
                jsonResponse.put("message", "Invalid credentials");
                return ResponseEntity.status(403).body(jsonResponse);
            }

            String jwt = Jwts.builder().setId(Integer.toString(user.getUserId()))
                    .setIssuedAt(new Date(System.currentTimeMillis()))
                    .setExpiration(new Date(System.currentTimeMillis() + 180 * 24 * 60 * 60 * 10000))
                    .setIssuer(Integer.toString(userDetails.getUserId()))
                    .setSubject(userDetails.getRole())
                    .signWith(SignatureAlgorithm.HS256, "secretkeyforeventxpertproject12345678901234567890!!!!!!!!!!!!!!!!!!!!!!!!!!")
                    .compact();


            jsonResponse.put("message", "Successfully logged in");
            jsonResponse.put("token", jwt);
            jsonResponse.put("userDetails", userDetails);
            return ResponseEntity.status(200).body(jsonResponse);
        }
        catch (NoResultException e) {
            jsonResponse.put("message", "User not found");
            return ResponseEntity.status(404).body(jsonResponse);
        }
        catch(Exception e) {
            System.out.println(e);
            jsonResponse.put("message", "Something went wrong");
            return ResponseEntity.status(500).body(jsonResponse);
        }
    }


}
