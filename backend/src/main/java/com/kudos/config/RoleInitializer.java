package com.kudos.config;

import com.kudos.model.Role;
import com.kudos.repository.RoleRepository;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RoleInitializer {

    private final RoleRepository roleRepository;

    @PostConstruct
    @Transactional
    public void initRoles() {
        // Create ADMIN role if it doesn't exist
        if (roleRepository.findByName("ADMIN").isEmpty()) {
            roleRepository.save(new Role("ADMIN"));
        }

        // Create MODERATOR role if it doesn't exist
        if (roleRepository.findByName("MODERATOR").isEmpty()) {
            roleRepository.save(new Role("MODERATOR"));
        }

        // Create EMPLOYEE role if it doesn't exist
        if (roleRepository.findByName("EMPLOYEE").isEmpty()) {
            roleRepository.save(new Role("EMPLOYEE"));
        }
    }
}
