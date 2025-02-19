package com.tmeras.resellmart.address;

import com.tmeras.resellmart.TestDataUtils;
import com.tmeras.resellmart.common.PageResponse;
import com.tmeras.resellmart.exception.ExceptionResponse;
import com.tmeras.resellmart.role.Role;
import com.tmeras.resellmart.role.RoleRepository;
import com.tmeras.resellmart.token.JwtService;
import com.tmeras.resellmart.user.User;
import com.tmeras.resellmart.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public class AddressControllerIT {

    @Container
    @ServiceConnection
    private static MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:9.2.0");

    private final TestRestTemplate restTemplate;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AddressRepository addressRepository;

    // Used to add JWT in requests
    private HttpHeaders headers;

    private Address addressA;
    private Address addressB;
    private AddressRequest addressRequestA;

    @Autowired
    public AddressControllerIT(
            TestRestTemplate restTemplate, UserRepository userRepository,
            RoleRepository roleRepository, PasswordEncoder passwordEncoder,
            JwtService jwtService, AddressRepository addressRepository
    ) {
        this.restTemplate = restTemplate;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.addressRepository = addressRepository;
    }

    @BeforeEach
    public void setUp() {
        // Empty relevant database tables
        addressRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();

        // Save required entities (need to set IDs to null before inserting to avoid
        // errors related to MySQL's AUTO_INCREMENT counter not resetting between tests)
        Role adminRole = roleRepository.save(Role.builder().name("ADMIN").build());
        User userA = TestDataUtils.createUserA(Set.of(adminRole));
        userA.setId(null);
        userA.setPassword(passwordEncoder.encode(userA.getPassword()));
        userA = userRepository.save(userA);

        Role userRole = roleRepository.save(Role.builder().name("USER").build());
        User userB = TestDataUtils.createUserB(Set.of(userRole));
        userB.setId(null);
        userB.setPassword(passwordEncoder.encode(userB.getPassword()));
        userB = userRepository.save(userB);

        addressA = TestDataUtils.createAddressA(userA);
        addressA.setId(null);
        addressA.setMain(true);
        addressA = addressRepository.save(addressA);
        addressRequestA = TestDataUtils.createAddressRequestA();

        addressB = TestDataUtils.createAddressB(userB);
        addressB.setId(null);
        addressB = addressRepository.save(addressB);

        // Generate test JWT with admin user details to include in each authenticated request
        String testJwt = jwtService.generateAccessToken(new HashMap<>(), userA);
        headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + testJwt);
    }

    @Test
    public void shouldSaveAddressWhenValidRequest() {
        AddressRequest addressRequest = AddressRequest.builder()
                .country("UK")
                .street("High Street")
                .state("Greater London")
                .city("London")
                .postalCode("SW1A 1AA")
                .addressType("BILLING")
                .build();

        ResponseEntity<AddressResponse> response =
                restTemplate.exchange("/api/addresses", HttpMethod.POST,
                        new HttpEntity<>(addressRequest, headers), AddressResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCountry()).isEqualTo(addressRequest.getCountry());
        assertThat(response.getBody().getStreet()).isEqualTo(addressRequest.getStreet());
        assertThat(response.getBody().getState()).isEqualTo(addressRequest.getState());
        assertThat(response.getBody().getCity()).isEqualTo(addressRequest.getCity());
        assertThat(response.getBody().getPostalCode()).isEqualTo(addressRequest.getPostalCode());
        assertThat(response.getBody().getAddressType().toString()).isEqualTo(addressRequest.getAddressType());
        assertThat(response.getBody().isDeleted()).isFalse();
        assertThat(response.getBody().isMain()).isFalse();
    }

    @Test
    public void shouldNotSaveAddressWhenInvalidRequest() {
        AddressRequest addressRequest = AddressRequest.builder()
                .country(null)
                .street("High Street")
                .state("Greater London")
                .city("London")
                .postalCode("SW1A 1AA")
                .addressType("BILLING")
                .build();
        Map<String, String> expectedErrors = new HashMap<>();
        expectedErrors.put("country", "Country must not be empty");

        ResponseEntity<Map<String, String>> response =
                restTemplate.exchange("/api/addresses", HttpMethod.POST,
                        new HttpEntity<>(addressRequest, headers), new ParameterizedTypeReference<>() {});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isEqualTo(expectedErrors);
    }

    @Test
    public void shouldFindAllAddressesWhenValidRequest() {
        ResponseEntity<PageResponse<AddressResponse>> response =
                restTemplate.exchange("/api/addresses", HttpMethod.GET,
                        new HttpEntity<>(headers), new ParameterizedTypeReference<>() {});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(2);
        assertThat(response.getBody().getContent().get(0).getStreet()).isEqualTo(addressA.getStreet());
        assertThat(response.getBody().getContent().get(1).getStreet()).isEqualTo(addressB.getStreet());
    }

    @Test
    public void shouldNotFindAllAddressesWhenNonAdminUser() {
        String testJwt = jwtService.generateAccessToken(new HashMap<>(), addressB.getUser());
        headers.set("Authorization", "Bearer " + testJwt);

        ResponseEntity<ExceptionResponse> response =
                restTemplate.exchange("/api/addresses", HttpMethod.GET,
                        new HttpEntity<>(headers), new ParameterizedTypeReference<>() {});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    public void shouldFindAllAddressesByUserId() {
        ResponseEntity<List<AddressResponse>> response =
                restTemplate.exchange("/api/addresses/users/" + addressA.getUser().getId(), HttpMethod.GET,
                        new HttpEntity<>(headers), new ParameterizedTypeReference<>() {});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getStreet()).isEqualTo(addressA.getStreet());
    }

    @Test
    public void shouldNotFindAllAddressesByUserIdWhenUserIsNotLoggedIn() {
        ResponseEntity<ExceptionResponse> response =
                restTemplate.exchange("/api/addresses/users/" + addressB.getUser().getId(), HttpMethod.GET,
                        new HttpEntity<>(headers), ExceptionResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage())
                .isEqualTo("You do not have permission to view the addresses of this user");
    }

    @Test
    public void shouldFindAllNonDeletedAddressesByUserId() {
        // Save a deleted address for user A
        Address deletedAddress = TestDataUtils.createAddressA(addressA.getUser());
        deletedAddress.setId(null);
        deletedAddress.setDeleted(true);
        deletedAddress.setStreet("Deleted street");
        addressRepository.save(deletedAddress);

        ResponseEntity<List<AddressResponse>> response =
                restTemplate.exchange("/api/addresses/users/" + addressA.getUser().getId() + "/non-deleted",
                        HttpMethod.GET, new HttpEntity<>(headers), new ParameterizedTypeReference<>() {});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getStreet()).isEqualTo(addressA.getStreet());
    }

    @Test
    public void shouldNotFindAllNonDeletedAddressesByUserIdWhenAddressOwnerIsNotLoggedIn() {
        ResponseEntity<ExceptionResponse> response =
                restTemplate.exchange("/api/addresses/users/" + addressB.getUser().getId() + "/non-deleted",
                        HttpMethod.GET, new HttpEntity<>(headers), ExceptionResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage())
                .isEqualTo("You do not have permission to view the addresses of this user");
    }

    @Test
    public void shouldMakeAddressMainWhenValidRequest() {
        // Save a second address for user A
        Address newAddress = TestDataUtils.createAddressA(addressA.getUser());
        newAddress.setId(null);
        newAddress.setStreet("New street");
        newAddress = addressRepository.save(newAddress);

        ResponseEntity<AddressResponse> response =
                restTemplate.exchange("/api/addresses/" + newAddress.getId() + "/main",
                        HttpMethod.PATCH, new HttpEntity<>(headers), AddressResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStreet()).isEqualTo(newAddress.getStreet());
        assertThat(addressRepository.findById(addressA.getId()).get().isMain()).isFalse();
        assertThat(addressRepository.findById(newAddress.getId()).get().isMain()).isTrue();
    }

    @Test
    public void shouldNotMakeAddressMainWhenInvalidAddressId() {
        ResponseEntity<ExceptionResponse> response =
                restTemplate.exchange("/api/addresses/" + 99 + "/main",
                        HttpMethod.PATCH, new HttpEntity<>(headers), ExceptionResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage())
                .isEqualTo("No address found with ID: 99");
    }

    @Test
    public void shouldNotMakeAddressMainWhenAddressOwnerIsNotLoggedIn() {
        ResponseEntity<ExceptionResponse> response =
                restTemplate.exchange("/api/addresses/" + addressB.getId() + "/main",
                        HttpMethod.PATCH, new HttpEntity<>(headers), ExceptionResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage())
                .isEqualTo("The specified address is related to another user");
    }

    @Test
    public void shouldUpdateAddressWhenValidRequest() {
        addressRequestA.setCity("Updated city");
        addressRequestA.setCountry("Updated country");

        ResponseEntity<AddressResponse> response =
                restTemplate.exchange("/api/addresses/" + addressA.getId(), HttpMethod.PUT,
                        new HttpEntity<>(addressRequestA, headers), AddressResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStreet()).isEqualTo(addressRequestA.getStreet());
        assertThat(response.getBody().getCity()).isEqualTo(addressRequestA.getCity());
    }

    @Test
    public void shouldNotUpdateAddressWhenInvalidRequest() {
        addressRequestA.setCountry(null);
        Map<String, String> expectedErrors = new HashMap<>();
        expectedErrors.put("country", "Country must not be empty");

        ResponseEntity<Map<String, String>> response =
                restTemplate.exchange("/api/addresses/" + addressA.getId(), HttpMethod.PUT,
                    new HttpEntity<>(addressRequestA, headers), new ParameterizedTypeReference<>() {});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isEqualTo(expectedErrors);
    }

    @Test
    public void shouldNotUpdateAddressWhenInvalidAddressId() {
        ResponseEntity<ExceptionResponse> response =
                restTemplate.exchange("/api/addresses/" + 99, HttpMethod.PUT,
                        new HttpEntity<>(addressRequestA, headers), ExceptionResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage())
                .isEqualTo("No address found with ID: 99");
    }

    @Test
    public void shouldNotUpdateAddressWhenNotAdminAndAddressOwnerIsNotLoggedIn() {
        String testJwt = jwtService.generateAccessToken(new HashMap<>(), addressB.getUser());
        headers.set("Authorization", "Bearer " + testJwt);

        ResponseEntity<ExceptionResponse> response =
                restTemplate.exchange("/api/addresses/" + addressA.getId(), HttpMethod.PUT,
                        new HttpEntity<>(addressRequestA, headers), ExceptionResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage())
                .isEqualTo("You do not have permission to update the address of this user");
    }

    @Test
    public void shouldDeleteAddressWhenValidRequest() {
        ResponseEntity<?> response =
                restTemplate.exchange("/api/addresses/" + addressA.getId(), HttpMethod.DELETE,
                        new HttpEntity<>(headers), Object.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(addressRepository.findById(addressA.getId()).get().isDeleted()).isTrue();
    }

    @Test
    public void shouldNotDeleteAddressWhenInvalidAddressId() {
        ResponseEntity<ExceptionResponse> response =
                restTemplate.exchange("/api/addresses/" + 99, HttpMethod.DELETE,
                        new HttpEntity<>(headers), ExceptionResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage())
                .isEqualTo("No address found with ID: 99");
    }

    @Test
    public void shouldNotDeleteAddressWhenNotAdminAndAddressOwnerIsNotLoggedIn() {
        String testJwt = jwtService.generateAccessToken(new HashMap<>(), addressB.getUser());
        headers.set("Authorization", "Bearer " + testJwt);

        ResponseEntity<ExceptionResponse> response =
                restTemplate.exchange("/api/addresses/" + addressA.getId(), HttpMethod.DELETE,
                        new HttpEntity<>(headers), ExceptionResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage())
                .isEqualTo("You do not have permission to delete the address of this user");
    }

}
