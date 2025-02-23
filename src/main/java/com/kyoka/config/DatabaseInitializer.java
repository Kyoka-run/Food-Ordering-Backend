package com.kyoka.config;

import com.kyoka.model.AppRole;
import com.kyoka.model.Role;
import com.kyoka.model.User;
import com.kyoka.repository.RoleRepository;
import com.kyoka.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;

    @Override
    public void run(String... args) throws Exception {
        // Initialize roles if they don't exist
        if (roleRepository.count() == 0) {
            // Create roles
            Role customerRole = new Role(AppRole.ROLE_CUSTOMER);
            Role ownerRole = new Role(AppRole.ROLE_RESTAURANT_OWNER);
            Role adminRole = new Role(AppRole.ROLE_ADMIN);

            roleRepository.save(customerRole);
            roleRepository.save(ownerRole);
            roleRepository.save(adminRole);
            System.out.println("Roles created");
        }
    }
}