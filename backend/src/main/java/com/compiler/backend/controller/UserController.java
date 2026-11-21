package com.compiler.backend.controller;
import com.compiler.backend.dto.Res.UserRes;
import com.compiler.backend.dto.req.UserReq;
import com.compiler.backend.dto.req.LoginReq;
import com.compiler.backend.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/v1/users")

@RequiredArgsConstructor
public class UserController {

    private final UserService userService;





    @GetMapping("/me")
    public ResponseEntity<UserRes> getCurrentUser(
            Authentication authentication) {

        Integer userId = (Integer) authentication.getPrincipal();


        return ResponseEntity.ok(userService.getMe(userId));
    }
    @PostMapping("/register")
    public ResponseEntity<UserRes> register(
            @RequestBody UserReq userReq,
            HttpServletResponse response) {
        return ResponseEntity.ok(
                userService.registerUser(userReq, response)
        );
    }
    @PostMapping("/logout")
    public ResponseEntity<String> logout(
            HttpServletResponse response) {

        userService.logout(response);

        return ResponseEntity.ok("Logout successful");
    }
    @PostMapping("/login")
    public ResponseEntity<UserRes> login(
            @RequestBody LoginReq loginReq,
            HttpServletResponse response) {

        return ResponseEntity.ok(
                userService.login(loginReq, response)
        );
    }
}