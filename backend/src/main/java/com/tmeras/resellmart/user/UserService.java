package com.tmeras.resellmart.user;

import com.tmeras.resellmart.common.PageResponse;
import com.tmeras.resellmart.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

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
}
