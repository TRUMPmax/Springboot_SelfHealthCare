package com.example.selfhealthcare.service;

import com.example.selfhealthcare.domain.AppUser;
import com.example.selfhealthcare.dto.AuthLoginRequest;
import com.example.selfhealthcare.dto.AuthRegisterRequest;
import com.example.selfhealthcare.dto.AuthSessionResponse;
import com.example.selfhealthcare.exception.BadRequestException;
import com.example.selfhealthcare.exception.UnauthorizedException;
import com.example.selfhealthcare.repository.AppUserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.Optional;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
public class AuthService {

    public static final String SESSION_USER_ID = "currentUserId";

    private final AppUserRepository appUserRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    @Transactional
    public AuthSessionResponse register(AuthRegisterRequest request) {
        if (!request.password().equals(request.confirmPassword())) {
            throw new BadRequestException("两次输入的密码不一致");
        }
        if (appUserRepository.existsByUsername(request.username())) {
            throw new BadRequestException("用户名已存在，请更换后重试");
        }

        AppUser user = new AppUser();
        user.setUsername(request.username().trim());
        user.setDisplayName(request.displayName().trim());
        user.setPasswordHash(passwordEncoder.encode(request.password()));

        AppUser savedUser = appUserRepository.save(user);
        currentRequest().getSession(true).setAttribute(SESSION_USER_ID, savedUser.getId());
        return toSessionResponse(savedUser);
    }

    @Transactional(readOnly = true)
    public AuthSessionResponse login(AuthLoginRequest request) {
        AppUser user = appUserRepository.findByUsername(request.username().trim())
                .orElseThrow(() -> new UnauthorizedException("用户名或密码不正确"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new UnauthorizedException("用户名或密码不正确");
        }

        currentRequest().getSession(true).setAttribute(SESSION_USER_ID, user.getId());
        return toSessionResponse(user);
    }

    @Transactional(readOnly = true)
    public AuthSessionResponse currentSession() {
        return resolveAuthenticatedUser()
                .map(this::toSessionResponse)
                .orElse(new AuthSessionResponse(null, null, null, false));
    }

    public void logout() {
        HttpSession session = currentRequest().getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }

    @Transactional(readOnly = true)
    public AppUser requireAuthenticatedUser() {
        Long userId = resolveCurrentUserId()
                .orElseThrow(() -> new UnauthorizedException("请先登录后再继续操作"));
        return appUserRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("登录状态已失效，请重新登录"));
    }

    @Transactional(readOnly = true)
    public Optional<AppUser> resolveAuthenticatedUser() {
        return resolveCurrentUserId().flatMap(appUserRepository::findById);
    }

    private Optional<Long> resolveCurrentUserId() {
        HttpSession session = currentRequest().getSession(false);
        if (session == null) {
            return Optional.empty();
        }
        Object userId = session.getAttribute(SESSION_USER_ID);
        if (userId instanceof Long value) {
            return Optional.of(value);
        }
        return Optional.empty();
    }

    private HttpServletRequest currentRequest() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw new UnauthorizedException("当前请求上下文不可用");
        }
        return attributes.getRequest();
    }

    private AuthSessionResponse toSessionResponse(AppUser user) {
        return new AuthSessionResponse(user.getId(), user.getUsername(), user.getDisplayName(), true);
    }
}
