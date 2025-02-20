package com.tmeras.resellmart.address;

import com.tmeras.resellmart.TestDataUtils;
import com.tmeras.resellmart.role.Role;
import com.tmeras.resellmart.role.RoleRepository;
import com.tmeras.resellmart.user.User;
import com.tmeras.resellmart.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
public class AddressRepositoryTests {

    @Container
    @ServiceConnection
    private static MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:9.2.0");

    private final AddressRepository addressRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    private Address addressA;
    private Address addressB;

    @Autowired
    public AddressRepositoryTests(
            AddressRepository addressRepository, RoleRepository roleRepository,
            UserRepository userRepository
    ) {
        this.addressRepository = addressRepository;
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
    }

    @BeforeEach
    public void setUp() {
        // Save required entities (need to set IDs to null before inserting to avoid
        // errors related to MySQL's AUTO_INCREMENT counter not resetting between tests)
        Role adminRole = new Role(null, "ADMIN");
        adminRole = roleRepository.save(adminRole);

        User userA = TestDataUtils.createUserA(Set.of(adminRole));
        userA.setId(null);
        userA = userRepository.save(userA);

        addressA = TestDataUtils.createAddressA(userA);
        addressA.setId(null);
        addressA = addressRepository.save(addressA);
        addressB = TestDataUtils.createAddressB(userA);
        addressB.setId(null);
        addressB.setDeleted(true);
        addressB = addressRepository.save(addressB);
    }

    @Test
    public void shouldFindAllNonDeletedWithAssociationsByUserId() {
        List<Address> addresses =
                addressRepository.findAllNonDeletedWithAssociationsByUserId(addressA.getUser().getId());

        assertThat(addresses.size()).isEqualTo(1);
        assertThat(addresses.get(0)).isEqualTo(addressA);
    }
}
