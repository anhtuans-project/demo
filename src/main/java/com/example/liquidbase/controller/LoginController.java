package com.example.liquidbase.controller;

import com.example.liquidbase.service.AuthService;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
public class LoginController {

    private final AuthService authService;

    @GetMapping("/login")
    public String loginPage() {
        log.info("[OAuth2] Serving custom login selection page.");
        return "login"; // Trả về file src/main/resources/templates/login.html
    }

    @GetMapping("/login/initiate")
    public String initiateLogin(HttpSession session) {
        // 1. Sinh State
        String state = java.util.UUID.randomUUID().toString();
        
        // 2. Lưu State vào Session
        session.setAttribute("oauth_state", state);
        
        // 3. Redirect sang Auth Server (Project 2)
        String loginUrl = authService.buildLoginUrl(state);
        log.info("[OAuth2] User clicked login. Redirecting to Auth Server: {}", loginUrl);
        return "redirect:" + loginUrl;
    }

    @GetMapping("/callback")
    public String callback(
            @RequestParam String code,
            @RequestParam String state,
            HttpSession session,
            Model model) {

        // 1. Kiểm tra State
        String savedState = (String) session.getAttribute("oauth_state");
        log.info("[OAuth2] Received callback with code: {}, state: {}", code, state);
        
        // Nếu savedState là null, nghĩa là người dùng đã bỏ qua bước trung gian (gọi trực tiếp từ Frontend)
        // Trong môi trường demo này, chúng ta cho phép tiếp tục nếu có state từ Request
        if (savedState != null && !savedState.equals(state)) {
            log.warn("[OAuth2] Invalid state parameter! Expected: {}, but received: {}", savedState, state);
            model.addAttribute("error", "Invalid state parameter!");
            return "error";
        }

        if (savedState == null) {
            log.info("[OAuth2] No saved state found (External flow). Proceeding with provided state.");
        }

        // Xóa state sau khi dùng
        session.removeAttribute("oauth_state");


        try {
            // 2. Đổi Code lấy Token
            log.info("[OAuth2] Exchanging code for token...");
            Map<String, Object> tokens = authService.exchangeCodeForToken(code);
            String accessToken = (String) tokens.get("access_token");
            String refreshToken = (String) tokens.get("refresh_token");

            // 3. Lấy thông tin User
            log.info("[OAuth2] Fetching user info with access token...");
            JsonNode userInfo = authService.getUserInfo(accessToken);
            String email = userInfo.has("email") ? userInfo.get("email").asText() : "user@example.com";
            String name = userInfo.has("name") ? userInfo.get("name").asText() : email;

            // 4. Manually Authenticate trong Spring Security Context
            // Để /auth/me hoạt động, chúng ta cần một Authentication object
            java.util.List<org.springframework.security.core.GrantedAuthority> authorities = 
                org.springframework.security.core.authority.AuthorityUtils.createAuthorityList("ROLE_USER");
            
            // Tạo principal tương thích với OAuth2User (để Auth.java không bị lỗi cast)
            Map<String, Object> attributes = new java.util.HashMap<>();
            attributes.put("email", email);
            attributes.put("name", name);
            attributes.put("sub", email); // sub là bắt buộc cho OAuth2User
            
            org.springframework.security.oauth2.core.user.OAuth2User principal = 
                new org.springframework.security.oauth2.core.user.DefaultOAuth2User(authorities, attributes, "email");

            org.springframework.security.authentication.UsernamePasswordAuthenticationToken auth = 
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(principal, null, authorities);
            
            org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth);
            
            // QUAN TRỌNG: Lưu SecurityContext vào Session để các request sau (như /auth/me) nhận diện được
            session.setAttribute(org.springframework.security.web.context.HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, 
                               org.springframework.security.core.context.SecurityContextHolder.getContext());

            log.info("[OAuth2] Login successful for user: {}. Session authenticated.", email);
            return "redirect:http://localhost:3000/"; // Chuyển hướng về Frontend

        } catch (Exception e) {
            log.error("[OAuth2] Login failed: {}", e.getMessage(), e);
            model.addAttribute("error", "Failed to login: " + e.getMessage());
            return "error";
        }
    }

    @GetMapping("/")
    public String home(HttpSession session, Model model) {
        String token = (String) session.getAttribute("access_token");
        if (token == null) {
            return "redirect:/login";
        }

        try {
            JsonNode userInfo = authService.getUserInfo(token);
            return "redirect:http://localhost:3000/";
        } catch (Exception e) {
            // Nếu token hết hạn, có thể xử lý refresh ở đây
            return "redirect:/login";
        }
    }
}
