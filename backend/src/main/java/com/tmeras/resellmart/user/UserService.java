package com.tmeras.resellmart.user;

import com.tmeras.resellmart.common.PageResponse;
import com.tmeras.resellmart.exception.APIException;
import com.tmeras.resellmart.exception.OperationNotPermittedException;
import com.tmeras.resellmart.exception.ResourceNotFoundException;
import com.tmeras.resellmart.file.FileService;
import com.tmeras.resellmart.mfa.MfaService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final FileService fileService;
    private final MfaService mfaService;

    public UserResponse findById(Integer userId) {
        return userRepository.findById(userId)
                .map(userMapper::toUserResponse)
                .orElseThrow(() -> new ResourceNotFoundException("No user found with ID " + userId));
    }

    @PreAuthorize("hasRole('ADMIN')") // Only admins should be able to view all users
    public PageResponse<UserResponse> findAll(Integer pageNumber, Integer pageSize, String sortBy, String sortDirection) {
        Sort sort = sortDirection.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);

        Page<User> users = userRepository.findAll(pageable);
        List<UserResponse> userResponses = users.stream()
                .map(userMapper::toUserResponse)
                .toList();

        return new PageResponse<>(
                userResponses,
                users.getNumber(),
                users.getSize(),
                users.getTotalElements(),
                users.getTotalPages(),
                users.isFirst(),
                users.isLast()
        );
    }

    public UserResponse update(UserRequest userRequest, Integer userId, Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();

        if (!Objects.equals(currentUser.getId(), userId))
            throw new OperationNotPermittedException("You do not have permission to update the details of this user");

        // If enabling MFA, generate QR image
        String qrImageUri = null;
        if (!currentUser.isMfaEnabled() && userRequest.isMfaEnabled()) {
            currentUser.setSecret(mfaService.generateSecret());
            qrImageUri = mfaService.generateQrCodeImageUri(currentUser.getSecret(), currentUser.getEmail());
        }

        currentUser.setName(userRequest.getName());
        currentUser.setHomeCountry(userRequest.getHomeCountry());
        currentUser.setMfaEnabled(userRequest.isMfaEnabled());

        User updatedUser = userRepository.save(currentUser);
        UserResponse userResponse = userMapper.toUserResponse(updatedUser);
        userResponse.setQrImageUri(qrImageUri);
        return userResponse;
    }


    public void uploadUserImage(MultipartFile image, Integer userId, Authentication authentication) throws IOException {
        User currentUser = (User) authentication.getPrincipal();

        if (!Objects.equals(currentUser.getId(), userId))
            throw new OperationNotPermittedException("You do not have permission to update this user's profile image");

        // Delete previous user image, if it exists
        if (currentUser.getImagePath() != null)
            fileService.deleteFile(currentUser.getImagePath());

        String fileName = image.getOriginalFilename();
        String fileExtension = fileService.getFileExtension(fileName);
        Set<String> validImageExtensions = Set.of("jpg", "jpeg", "png", "gif", "bmp", "tiff");
        if (!validImageExtensions.contains(fileExtension))
            throw new APIException("Only images can be uploaded");

        String filePath = fileService.saveUserImage(image, userId);
        currentUser.setImagePath(filePath);
        userRepository.save(currentUser);
    }
}
