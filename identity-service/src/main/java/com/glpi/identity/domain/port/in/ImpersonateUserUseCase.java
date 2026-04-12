package com.glpi.identity.domain.port.in;

/**
 * Driving port: impersonate a target user (requires IMPERSONATE right = 32768 on "user" resource).
 */
public interface ImpersonateUserUseCase {
    AuthResponse impersonate(ImpersonateCommand command);
}
