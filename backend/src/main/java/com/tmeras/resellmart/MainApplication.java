package com.tmeras.resellmart;

import com.tmeras.resellmart.address.Address;
import com.tmeras.resellmart.address.AddressRepository;
import com.tmeras.resellmart.address.AddressType;
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

	/*// TODO: Replace with migration script
	@Bean
	public CommandLineRunner runner(
			RoleRepository roleRepository,
			CategoryRepository categoryRepository,
			ProductRepository productRepository,
			UserRepository userRepository,
			PasswordEncoder passwordEncoder,
			AddressRepository addressRepository
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
				User userA = userRepository.save(
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
				User userB = userRepository.save(
						User.builder()
								.email("admin@test.com")
								.name("test admin user")
								.roles(Set.of(adminRole))
								.mfaEnabled(false)
								.homeCountry("UK")
								.enabled(true)
								.password(passwordEncoder.encode("Bacon12!"))
								.build()
				);
				User userC = userRepository.save(
						User.builder()
								.email("test2@test.com")
								.name("test user 3")
								.roles(Set.of(userRole))
								.mfaEnabled(false)
								.homeCountry("UK")
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
							.isAvailable(true)
							.category(categoryA)
							.seller(userA)
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
								.isAvailable(true)
								.category(categoryB)
								.seller(userB)
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
								.isAvailable(true)
								.category(categoryB)
								.seller(userC)
								.images(new ArrayList<>())
								.build()
				);
				Product productD = productRepository.save(
						Product.builder()
								.name("Product D")
								.description("Description D")
								.price(40.0)
								.discountedPrice(null)
								.productCondition(ProductCondition.LIKE_NEW)
								.availableQuantity(3)
								.isAvailable(true)
								.category(categoryB)
								.seller(userC)
								.images(new ArrayList<>())
								.build()
				);
				Address addressA = addressRepository.save(
						Address.builder()
								.country("Greece")
								.street("Ermou Street")
								.state("Attica")
								.city("Athens")
								.postalCode("10563")
								.main(true)
								.deleted(false)
								.addressType(AddressType.HOME)
								.user(userA)
								.build()
				);
				Address addressB = addressRepository.save(
						Address.builder()
								.country("UK")
								.street("Mappin Street")
								.state("South Yorkshire")
								.city("Sheffield")
								.postalCode("S1 4DT")
								.main(true)
								.deleted(false)
								.addressType(AddressType.WORK)
								.user(userB)
								.build()
				);
				Address addressC = addressRepository.save(
						Address.builder()
								.country("UK")
								.street("Oxford Road")
								.state("Greater Manchester")
								.city("Manchester")
								.postalCode("M13 9PL")
								.main(true)
								.deleted(false)
								.addressType(AddressType.HOME)
								.user(userC)
								.build()
				);
			}
		};
	}*/
}
