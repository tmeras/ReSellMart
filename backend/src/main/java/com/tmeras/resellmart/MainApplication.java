package com.tmeras.resellmart;

import com.tmeras.resellmart.category.Category;
import com.tmeras.resellmart.category.CategoryRepository;
import com.tmeras.resellmart.role.Role;
import com.tmeras.resellmart.role.RoleRepository;
import com.tmeras.resellmart.user.User;
import com.tmeras.resellmart.user.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Set;

@SpringBootApplication
@EnableAsync
public class MainApplication {

	public static void main(String[] args) {
		SpringApplication.run(MainApplication.class, args);
	}

	// TODO: Disable during testing
	@Bean
	public CommandLineRunner runner(
			RoleRepository roleRepository,
			CategoryRepository categoryRepository,
			UserRepository userRepository,
			PasswordEncoder passwordEncoder
	) {
		return args -> {
			if (roleRepository.findByName("USER").isEmpty()) {
				Role userRole = roleRepository.save(
						Role.builder()
								.name("USER")
								.build()
				);
				Role adminRole = roleRepository.save(
						Role.builder()
								.name("ADMIN")
								.build()
				);
				userRepository.save(
						User.builder()
								.email("test@test.com")
								.name("test user")
								.dob(LocalDate.of(2001, 7, 30))
								.roles(Set.of(userRole))
								.mfaEnabled(false)
								.homeCountry("Greece")
								.enabled(true)
								.password(passwordEncoder.encode("Bacon12!"))
								.build()
				);
				userRepository.save(
						User.builder()
								.email("admin@test.com")
								.name("test admin user")
								.dob(LocalDate.of(1996, 2, 18))
								.roles(Set.of(adminRole))
								.mfaEnabled(false)
								.homeCountry("Greece")
								.enabled(true)
								.password(passwordEncoder.encode("Bacon12!"))
								.build()
				);				categoryRepository.save(
						Category.builder()
								.name("First category")
								.build()
				);
			}
		};
	}
}
