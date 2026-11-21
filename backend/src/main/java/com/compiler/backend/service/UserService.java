package com.compiler.backend.service;

import com.compiler.backend.config.MyJWT;
import com.compiler.backend.dto.Res.UserFolderResponse;
import com.compiler.backend.dto.Res.UserRes;
import com.compiler.backend.dto.req.LoginReq;
import com.compiler.backend.dto.req.UserReq;
import com.compiler.backend.entity.User;
import com.compiler.backend.entity.UserFolder;
import com.compiler.backend.exception.InvalidUsernameAndPassword;
import com.compiler.backend.exception.UserAlreadyExistsException;
import com.compiler.backend.repo.UserFileRepository;
import com.compiler.backend.repo.UserFolderRepository;
import com.compiler.backend.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserFolderRepository userFolderRepository;
    private final UserFileRepository userFileRepository;
    private final PasswordEncoder passwordEncoder;
    private final MyJWT myJWT;


    public void logout(HttpServletResponse response) {

        Cookie cookie = new Cookie("accessToken", null);

        cookie.setHttpOnly(true);
        cookie.setSecure(false); // Localhost HTTP
        cookie.setPath("/");
        cookie.setMaxAge(0);

        response.addCookie(cookie);
    }
    @Transactional
    public UserRes registerUser(UserReq userReq, HttpServletResponse response) {

        if (userRepository.existsByEmail(userReq.getEmail())) {
            throw new UserAlreadyExistsException("User already exists");
        }




        User savedUser = userRepository.save(User.builder()
                        .email(userReq.getEmail())
                        .password(passwordEncoder.encode(userReq.getPassword()))
                        .name(userReq.getName())
                        .gender(userReq.getGender())
                        .build());


        UserFolder userFolder = UserFolder.builder()
                .folderName("MY-WORKPLACE")
                .user(savedUser)
                .parentFolder(null)
                .build();

        UserFolder savedFolder = userFolderRepository.save(userFolder);

        String token = myJWT.generateToken(
                savedUser.getId(),
                savedUser.getEmail()
        );

        Cookie cookie = new Cookie("accessToken", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(24 * 60 * 60);
        response.addCookie(cookie);


        return UserRes.builder()
                .userFolder(UserFolderResponse
                        .builder()
                        .id(savedFolder.getId())
                        .folderName(savedFolder.getFolderName())
                        .parentFolderId(null)
                        .build())
                .id(savedUser.getId())
                .email(savedUser.getEmail())
                .name(savedUser.getName())
                .build();
    }

    @Transactional(readOnly = true)
    public UserRes login(LoginReq loginReq, HttpServletResponse response) {

        User user = userRepository.findByEmail(loginReq.getEmail())
                .orElseThrow(() ->
                        new InvalidUsernameAndPassword("Invalid email or password"));

        if (!passwordEncoder.matches(
                loginReq.getPassword(),
                user.getPassword())) {

            throw new InvalidUsernameAndPassword("Invalid email or password");
        }


        UserFolder rootFolder = userFolderRepository.findByUserId(user.getId())
                .orElse(null);
if(rootFolder==null){
    rootFolder=userFolderRepository.save(    UserFolder.builder()
                    .user(user)
                    .folderName("MY-WORKPLACE")
                    .parentFolder(null)
            .build());


}

        String token = myJWT.generateToken(
                user.getId(),
                user.getEmail()
        );

        Cookie cookie = new Cookie("accessToken", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(24 * 60 * 60);

        response.addCookie(cookie);

        return UserRes.builder()
                .userFolder(UserFolderResponse.builder().id(rootFolder.getId()).folderName(rootFolder.getFolderName()).parentFolderId(null).build())
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .build();
    }

    public UserRes getMe(Integer userId) {

        User user = userRepository.findById(userId).orElseThrow(()->new UsernameNotFoundException("User not found"));
      return   UserRes.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .userFolder(UserFolderResponse.builder()
                        .id(user.getRootFolder().getId())
                        .folderName(user.getRootFolder().getFolderName())
                        .parentFolderId(null)
                        .build())
                .build();
    }
}