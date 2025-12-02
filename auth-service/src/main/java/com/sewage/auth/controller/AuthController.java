package com.sewage.auth.controller;

import com.sewage.auth.dto.LoginRequest;
import com.sewage.auth.dto.LoginResponse;
import com.sewage.auth.dto.RegisterRequest;
import com.sewage.auth.mapper.UserMapper;
import com.sewage.auth.service.AuthServiceSimple;
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

    private final AuthServiceSimple authServiceSimple;
    private final JwtUtil jwtUtil;
    private final UserMapper userMapper;


    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody LoginRequest loginRequest,
                                       HttpServletRequest request) {
        log.info("用户登录请求: {}", loginRequest.getUsername());
        return authServiceSimple.login(loginRequest, request);
    }

    /**
     * 用户注册 - 简化版（暂时禁用）
     */
    @PostMapping("/register")
    public Result<String> register(@RequestBody RegisterRequest request) {
        return Result.failure("当前为简化模式，暂时不支持注册功能，请使用预设测试账号");
    }

    /**
     * 验证令牌 - 简化版
     */
    @PostMapping("/validate")
    public Result<User> validateToken(@RequestParam String token) {
        return authServiceSimple.validateToken(token);
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public Result<String> health() {
        return Result.success("认证服务运行正常");
    }

    /**
     * 根路径测试
     */
    @RequestMapping("/")
    public Result<String> home() {
        return Result.success("Auth服务首页测试成功");
    }

    /**
     * 简单测试接口
     */
    @GetMapping("/test")
    public Result<String> test() {
        return Result.success("Auth服务测试成功");
    }

    /**
     * 测试数据库连接
     */
    @GetMapping("/test-db")
    public Result<String> testDb() {
        try {
            Long count = userMapper.selectCount(null);
            return Result.success("数据库连接正常，用户数量：" + count);
        } catch (Exception e) {
            return Result.failure("数据库连接失败：" + e.getMessage());
        }
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
