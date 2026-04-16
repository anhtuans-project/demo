package com.example.liquidbase.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@Slf4j
public class Auth {

    /**
     * Endpoint để frontend kiểm tra session và lấy thông tin user hiện tại.
     * Frontend gọi khi app khởi động để xác định trạng thái đăng nhập.
     * Trả về 200 + user info nếu đã login, 401 nếu chưa.
     */
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            log.debug("[Auth] /me request: User is not authenticated or anonymous.");
            return ResponseEntity.status(401).body(Map.of("error", "Chưa đăng nhập"));
        }

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("email", oAuth2User.getAttribute("email"));
        userInfo.put("name", oAuth2User.getAttribute("name"));
        userInfo.put("picture", oAuth2User.getAttribute("picture"));
        // Lấy role đầu tiên (VD: ROLE_USER hoặc ROLE_ADMIN)
        userInfo.put("role", authentication.getAuthorities().stream()
                .findFirst()
                .map(Object::toString)
                .orElse("ROLE_USER"));

        log.info("[Auth] /me request: User identified as {} with role {}", userInfo.get("email"), userInfo.get("role"));
        return ResponseEntity.ok(userInfo);
    }

    /**
     * Endpoint redirect sau khi OAuth2 login thành công.
     * SecurityConfig đã set defaultSuccessUrl("/home", true) nhưng với SPA (React),
     * ta nên redirect về frontend URL thay vì trả HTML.
     * Endpoint này sẽ redirect browser về trang chủ của frontend.
     */
    @GetMapping("/home")
    public ResponseEntity<Void> home(HttpServletResponse response) throws Exception {
        // Redirect về frontend (React app chạy ở port 3000 theo cấu hình hiện tại)
        log.info("[Auth] Login success. Redirecting back to frontend...");
        response.sendRedirect("http://localhost:3000/");
        return ResponseEntity.ok().build();
    }

    /**
     * Endpoint logout: invalidate session phía server và xóa cookie.
     * Frontend gọi POST /auth/logout, sau đó redirect về trang login.
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) {
        
        // 1. Xóa thông tin xác thực trong SecurityContext
        if (authentication != null) {
            new SecurityContextLogoutHandler().logout(request, response, authentication);
        }
        
        // 2. Hủy session hiện tại trên server
        var session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        
        // 3. Xóa SecurityContextHolder cho thread này
        SecurityContextHolder.clearContext();
        
        log.info("[Auth] Logout successful.");
        return ResponseEntity.ok(Map.of("message", "Đăng xuất thành công"));
    }

    // ---- Các endpoint theo role (giữ nguyên) ----

    @GetMapping("/admin/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminDashboard() {
        return "Đây là trang chỉ dành cho Admin";
    }

    @GetMapping("/user/profile")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public String userProfile(Authentication authentication) {
        OAuth2User user = (OAuth2User) authentication.getPrincipal();
        return "Xin chào: " + user.getAttribute("email");
    }
}
