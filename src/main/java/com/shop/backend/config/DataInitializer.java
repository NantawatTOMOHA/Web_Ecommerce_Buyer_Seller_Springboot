package com.shop.backend.config;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import com.shop.backend.entity.Role;
import com.shop.backend.repository.RoleRepository;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) {
        if (roleRepository.count() == 0) {
            roleRepository.save(Role.builder().name("buyer").build());
            roleRepository.save(Role.builder().name("seller").build());
            System.out.println("Default roles added: buyer, seller");
        }
    }
}
