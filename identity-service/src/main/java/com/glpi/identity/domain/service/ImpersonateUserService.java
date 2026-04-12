package com.glpi.identity.domain.service;

import com.glpi.identity.domain.model.InsufficientRightsException;
import com.glpi.identity.domain.model.UserNotFoundException;
import com.glpi.identity.domain.port.in.AuthResponse;
import com.glpi.identity.domain.port.in.ImpersonateCommand;
import com.glpi.identity.domain.port.in.ImpersonateUserUseCase;
import com.glpi.identity.domain.port.out.ProfileRepository;
import com.glpi.identity.domain.port.out.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Implements user impersonation.
 * Requires the requesting user to hold the IMPERSONATE right (bit 32768) on the "user" resource.
 * Issues an impersonation JWT with the {@code impersonated_by} claim set.
 */
@Service
public class ImpersonateUserService implements ImpersonateUserUseCase {

    /** IMPERSONATE right bit value on the "user" resource. */
    private static final int IMPERSONATE_RIGHT = 32768;
    private static final long ACCESS_TOKEN_EXPIRES_IN = 3600L;

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final JwtTokenService jwtTokenService;

    public ImpersonateUserService(UserRepository userRepository,
                                  ProfileRepository profileRepository,
                                  JwtTokenService jwtTokenService) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.jwtTokenService = jwtTokenService;
    }

    @Override
    public AuthResponse impersonate(ImpersonateCommand command) {
        // Load requesting user and verify IMPERSONATE right
        var requestingUser = userRepository.findById(command.requestingUserId())
                .orElseThrow(() -> new UserNotFoundException(command.requestingUserId()));

        var profile = profileRepository.findById(requestingUser.getProfileId())
                .orElseThrow(() -> new InsufficientRightsException("No profile assigned"));

        int userRights = profile.getRights().getOrDefault("user", 0);
        if ((userRights & IMPERSONATE_RIGHT) == 0) {
            throw new InsufficientRightsException("IMPERSONATE right required on 'user' resource");
        }

        // Load target user
        var targetUser = userRepository.findById(command.targetUserId())
                .orElseThrow(() -> new UserNotFoundException(command.targetUserId()));

        // Resolve target user's rights
        Map<String, Integer> targetRights = profileRepository.findById(targetUser.getProfileId())
                .map(p -> p.getRights())
                .orElse(Map.of());

        // Issue impersonation JWT with impersonated_by claim
        String accessToken = jwtTokenService.issueAccessTokenWithRights(
                targetUser, targetRights, command.requestingUserId());
        String refreshToken = jwtTokenService.issueRefreshToken(targetUser, null);

        return new AuthResponse(accessToken, refreshToken, ACCESS_TOKEN_EXPIRES_IN);
    }
}
