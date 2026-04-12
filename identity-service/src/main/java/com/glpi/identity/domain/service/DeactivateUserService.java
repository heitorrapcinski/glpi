package com.glpi.identity.domain.service;

import com.glpi.identity.domain.model.User;
import com.glpi.identity.domain.model.UserNotFoundException;
import com.glpi.identity.domain.port.in.DeactivateUserUseCase;
import com.glpi.identity.domain.port.out.UserRepository;
import org.springframework.stereotype.Service;

/**
 * Domain service implementing DeactivateUserUseCase.
 * Sets isActive=false on the user.
 */
@Service
public class DeactivateUserService implements DeactivateUserUseCase {

    private final UserRepository userRepository;

    public DeactivateUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void deactivateUser(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        user.deactivate();
        userRepository.save(user);
    }
}
