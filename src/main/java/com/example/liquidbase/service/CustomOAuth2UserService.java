package com.example.liquidbase.service;

import com.example.liquidbase.constant.UserRole;
import com.example.liquidbase.model.User;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserService userService;

    public CustomOAuth2UserService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 1. Gọi service mặc định để lấy thông tin từ Google
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        // 2. Lấy thông tin cần thiết
        String email = oAuth2User.getAttribute("email");
        Optional<User> optionalUser = userService.findByEmail(email);

        // 3. Tìm hoặc tạo mới User trong DB
        User user = optionalUser.orElseGet(() -> {
                    // Nếu chưa có, tạo mới user với role mặc định
                    User newUser = new User();
                    newUser.setEmail(email);
                    newUser.setRole(UserRole.ROLE_USER);   // Role mặc định
                    return userService.save(newUser); // Lưu vào DB và trả về user đã lưu
                });

        // 4. Tạo Authorities từ Role trong DB
        // Spring Security yêu cầu prefix "ROLE_" cho hasRole()
        var authorities = Collections.singletonList(
                new SimpleGrantedAuthority(user.getRole().toString())
        );

        // 5. Trả về DefaultOAuth2User với authorities đã được gán
        // Tham số thứ 3 ("email") là key dùng làm "name attribute" (principal name)
        return new DefaultOAuth2User(
                authorities,
                oAuth2User.getAttributes(),
                "email"
        );
    }
}
