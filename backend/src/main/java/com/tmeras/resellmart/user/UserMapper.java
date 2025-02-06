package com.tmeras.resellmart.user;

import com.tmeras.resellmart.file.FileUtilities;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserMapper {

    public User toUser(UserRequest userRequest) {
        return User.builder()
                .name(userRequest.getName())
                .homeCountry(userRequest.getHomeCountry())
                .mfaEnabled(userRequest.isMfaEnabled())
                .build();
    }

    public UserResponse toUserResponse(User user) {
        byte[] profileImage = (user.getImagePath() == null) ?
                null : FileUtilities.readFileFromPath(user.getImagePath());

        return UserResponse.builder()
                .name(user.getRealName())
                .email(user.getEmail())
                .homeCountry(user.getHomeCountry())
                .mfaEnabled(user.isMfaEnabled())
                .profileImage(profileImage)
                .roles(user.getRoles())
                .build();
    }
}
