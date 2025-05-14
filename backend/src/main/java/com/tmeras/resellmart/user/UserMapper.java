package com.tmeras.resellmart.user;

import com.tmeras.resellmart.file.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserMapper {

    private final FileService fileService;

    public UserResponse toUserResponse(User user) {
        byte[] profileImage = (user.getImagePath() == null) ?
                null : fileService.readFileFromPath(user.getImagePath());

        return UserResponse.builder()
                .id(user.getId())
                .name(user.getRealName())
                .email(user.getEmail())
                .homeCountry(user.getHomeCountry())
                .registeredAt(user.getRegisteredAt())
                .isMfaEnabled(user.getIsMfaEnabled())
                .profileImage(profileImage)
                .roles(user.getRoles())
                .build();
    }
}
