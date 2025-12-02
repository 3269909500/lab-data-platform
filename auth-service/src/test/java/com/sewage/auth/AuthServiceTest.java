package com.sewage.auth;

import com.sewage.auth.dto.LoginRequest;
import com.sewage.auth.dto.RegisterRequest;
import com.sewage.auth.service.AuthService;
import com.sewage.common.entity.User;
import com.sewage.common.result.Result;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AuthService测试类
 * 验证认证服务的核心功能
 */
@SpringBootTest
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Test
    void testUserLoginSuccess() {
        // 准备测试数据
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("127.0.0.1");

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("123456");

        // 执行登录
        Result<LoginResponse> result = authService.login(loginRequest, request);

        // 验证结果
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        assertNotNull(result.getData().getToken());
        assertEquals("Bearer", result.getData().getTokenType());
        assertEquals(86400L, result.getData().getExpiresIn());
        assertEquals("testuser", result.getData().getUsername());

        System.out.println("✅ 用户登录测试通过");
        System.out.println("Token: " + result.getData().getToken());
    }

    @Test
    void testUserLoginPasswordError() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("127.0.0.1");

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("wrongpassword");

        Result<LoginResponse> result = authService.login(loginRequest, request);

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("用户名或密码错误"));

        System.out.println("✅ 密码错误测试通过");
    }

    @Test
    void testUserLoginEmptyFields() {
        MockHttpServletRequest request = new MockHttpServletRequest();

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("");
        loginRequest.setPassword("");

        Result<LoginResponse> result = authService.login(loginRequest, request);

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("用户名和密码不能为空"));

        System.out.println("✅ 空字段验证测试通过");
    }

    @Test
    void testUserRegisterSuccess() {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("newuser");
        registerRequest.setPassword("123456");
        registerRequest.setEmail("newuser@test.com");
        registerRequest.setPhone("13800138000");
        registerRequest.setRealName("新用户");
        registerRequest.setDepartment("技术部");

        Result<String> result = authService.register(registerRequest);

        assertTrue(result.isSuccess());
        assertEquals("注册成功", result.getMessage());

        System.out.println("✅ 用户注册测试通过");
    }

    @Test
    void testTokenValidation() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("127.0.0.1");

        // 先登录获取token
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("123456");

        Result<LoginResponse> loginResult = authService.login(loginRequest, request);
        String token = loginResult.getData().getToken();
        assertNotNull(token);

        // 验证token
        Result<User> validateResult = authService.validateToken(token);
        assertTrue(validateResult.isSuccess());
        assertEquals("testuser", validateResult.getData().getUsername());

        System.out.println("✅ Token验证测试通过");
    }

    @Test
    void testTokenExpired() {
        // 模拟过期token
        String expiredToken = "expired.token.123";

        Result<User> result = authService.validateToken(expiredToken);
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("令牌格式无效") || result.getMessage().contains("令牌已过期"));

        System.out.println("✅ Token过期验证测试通过");
    }

    @Test
    void testUserLogout() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("127.0.0.1");

        // 先登录获取token
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("123456");

        Result<LoginResponse> loginResult = authService.login(loginRequest, request);
        String token = loginResult.getData().getToken();
        assertNotNull(token);

        // 登出
        Result<String> logoutResult = authService.logout(token);
        assertTrue(logoutResult.isSuccess());
        assertEquals("登出成功", logoutResult.getMessage());

        // 验证token应该失效
        Result<User> validateResult = authService.validateToken(token);
        assertFalse(validateResult.isSuccess());

        System.out.println("✅ 用户登出测试通过");
    }

    @Test
    void testGetClientIp() {
        MockHttpServletRequest request = new MockHttpServletRequest();

        // 模拟直接IP
        request.setRemoteAddr("192.168.1.100");
        // 调用getClientIp方法（需要通过反射测试私有方法）
        String ip = extractPrivateMethod(authService, "getClientIp", request);
        assertEquals("192.168.1.100", ip);

        System.out.println("✅ IP地址获取测试通过: " + ip);
    }

    @Test
    void testXForwardedFor() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Forwarded-For", "192.168.1.100, 10.0.0.1");

        String ip = extractPrivateMethod(authService, "getClientIp", request);
        assertEquals("192.168.1.100", ip);

        System.out.println("✅ X-Forwarded-For代理IP测试通过: " + ip);
    }

    /**
     * 通过反射调用私有方法进行测试
     */
    @SuppressWarnings("unchecked")
    private <T> T extractPrivateMethod(Object obj, String methodName, Object... args) {
        try {
            java.lang.reflect.Method method = obj.getClass().getDeclaredMethod(methodName,
                args.length > 0 ? args[0].getClass() : null);
            method.setAccessible(true);
            return (T) method.invoke(obj, args);
        } catch (Exception e) {
            throw new RuntimeException("Failed to access private method: " + methodName, e);
        }
    }
}