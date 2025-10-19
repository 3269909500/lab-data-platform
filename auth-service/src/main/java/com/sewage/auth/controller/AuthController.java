package com.sewage.auth.controller;

import com.sewage.auth.dto.LoginRequest;
import com.sewage.auth.dto.LoginResponse;
import com.sewage.auth.dto.RegisterRequest;
import com.sewage.auth.mapper.UserMapper;
import com.sewage.auth.service.AuthService;
import com.sewage.common.entity.User;
import com.sewage.common.result.Result;
import com.sewage.common.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

/**
 * 认证控制器
 * 提供用户登录、注册、令牌验证等功能
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;
    private final UserMapper userMapper;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody LoginRequest loginRequest,
                                       HttpServletRequest request) {
        log.info("用户登录请求: {}", loginRequest.getUsername());
        return authService.login(loginRequest, request);
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<String> register(@RequestBody RegisterRequest request) {
        // 验证密码一致性
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            return Result.badRequest("两次密码不一致");
        }

        // 检查用户名是否已存在
        if (userMapper.countByUsername(request.getUsername()) > 0) {
            return Result.failure("用户名已存在");
        }

        // 创建新用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword()); // 实际应该加密
        user.setRealName(request.getRealName());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setDepartment(request.getDepartment());
        user.setRole("OPERATOR"); // 默认角色
        user.setStatus(1);
        user.setCreatedTime(LocalDateTime.now());

        userMapper.insert(user);
        return Result.success("注册成功");
    }

    /**
     * 验证令牌
     */
    @PostMapping("/validate")
    public Result<User> validateToken(@RequestParam String token) {
        try {
            // 验证token并返回用户信息
            String username = jwtUtil.getUsernameFromToken(token);
            User user = userMapper.findByUsername(username);

            if (user == null) {
                return Result.failure("用户不存在");
            }

            if (!jwtUtil.validateToken(token, username)) {
                return Result.failure("Token无效或已过期");
            }

            user.setPassword(null); // 不返回密码
            return Result.success(user);
        } catch (Exception e) {
            return Result.failure("Token验证失败");
        }
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public Result<String> health() {
        return Result.success("认证服务运行正常");
    }

    /**
     * 获取服务信息
     */
    @GetMapping("/info")
    public Result<String> info() {
        return Result.success("污水处理系统认证服务 v1.0.0");
    }
    @GetMapping("/userinfo")
    public Result<User> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        try {
            // 从Authorization头获取token
            String token = authHeader.replace("Bearer ", "");
            String username = jwtUtil.getUsernameFromToken(token);

            User user = userMapper.findByUsername(username);
            if (user == null) {
                return Result.failure("用户不存在");
            }

            user.setPassword(null); // 不返回密码
            return Result.success(user);
        } catch (Exception e) {
            return Result.failure("获取用户信息失败");
        }
    }
    @GetMapping("/protected")
    public Result<String> protectedEndpoint() {
        return Result.success("这是受保护的接口");
    }
}
