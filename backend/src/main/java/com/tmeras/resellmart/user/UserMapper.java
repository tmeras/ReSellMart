package com.tmeras.resellmart.user;

import com.tmeras.resellmart.file.FileUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserMapper {

    public User toUser(UserRequest userRequest) {
        return User.builder()
                .name(userRequest.getName())
                .email(userRequest.getEmail())
                .homeCountry(userRequest.getHomeCountry())
                .mfaEnabled(userRequest.isMfaEnabled())
                .build();
    }

    public UserResponse toUserResponse(User user) {
        byte[] profileImage = (user.getImagePath() == null) ?
                null : FileUtils.readFileFromPath(user.getImagePath());

        return UserResponse.builder()
                .name(user.getRealName())
                .email(user.getEmail())
                .homeCountry(user.getHomeCountry())
                .profileImage(profileImage)
                .roles(user.getRoles())
                .build();
    }
}
