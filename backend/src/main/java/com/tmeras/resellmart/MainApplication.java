package com.tmeras.resellmart;

import com.tmeras.resellmart.category.Category;
import com.tmeras.resellmart.category.CategoryRepository;
import com.tmeras.resellmart.product.Product;
import com.tmeras.resellmart.product.ProductCondition;
import com.tmeras.resellmart.product.ProductRepository;
import com.tmeras.resellmart.role.Role;
import com.tmeras.resellmart.role.RoleRepository;
import com.tmeras.resellmart.user.User;
import com.tmeras.resellmart.user.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.Set;

@SpringBootApplication
@EnableAsync
public class MainApplication {

	public static void main(String[] args) {
		SpringApplication.run(MainApplication.class, args);
	}

	@Bean
	public CommandLineRunner runner(
			RoleRepository roleRepository,
			CategoryRepository categoryRepository,
			ProductRepository productRepository,
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
				User user = userRepository.save(
						User.builder()
								.email("test@test.com")
								.name("test user")
								.roles(Set.of(userRole))
								.mfaEnabled(false)
								.homeCountry("Greece")
								.enabled(true)
								.password(passwordEncoder.encode("Bacon12!"))
								.build()
				);
				User adminUser = userRepository.save(
						User.builder()
								.email("admin@test.com")
								.name("test admin user")
								.roles(Set.of(adminRole))
								.mfaEnabled(false)
								.homeCountry("USA")
								.enabled(true)
								.password(passwordEncoder.encode("Bacon12!"))
								.build()
				);
				Category categoryA = categoryRepository.save(
						Category.builder()
								.name("First category")
								.build()
				);
				Category categoryB = categoryRepository.save(
						Category.builder()
								.name("Second category")
								.parentCategory(categoryA)
								.build()
				);
				Product productA = productRepository.save(
					Product.builder()
							.name("Product A")
							.description("Description A")
							.price(10.0)
							.discountedPrice(5.0)
							.productCondition(ProductCondition.NEW)
							.availableQuantity(2)
							.available(true)
							.category(categoryA)
							.seller(adminUser)
							.images(new ArrayList<>())
							.build()
				);
				Product productB = productRepository.save(
						Product.builder()
								.name("Product B")
								.description("Description B")
								.price(20.0)
								.discountedPrice(10.0)
								.productCondition(ProductCondition.FAIR)
								.availableQuantity(1)
								.available(true)
								.category(categoryB)
								.seller(adminUser)
								.images(new ArrayList<>())
								.build()
				);
				Product productC = productRepository.save(
						Product.builder()
								.name("Product C")
								.description("Description C")
								.price(30.0)
								.discountedPrice(15.0)
								.productCondition(ProductCondition.LIKE_NEW)
								.availableQuantity(5)
								.available(true)
								.category(categoryB)
								.seller(user)
								.images(new ArrayList<>())
								.build()
				);
			}
		};
	}
}
