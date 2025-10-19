package com.sewage.auth.service;

import com.sewage.auth.dto.LoginRequest;
import com.sewage.auth.dto.LoginResponse;
import com.sewage.auth.dto.RegisterRequest;
import com.sewage.auth.mapper.UserMapper;
import com.sewage.common.entity.User;
import com.sewage.common.result.Result;
import com.sewage.common.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

/**
 * 认证服务业务层
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;

    /**
     * 用户登录
     */
    public Result<LoginResponse> login(LoginRequest loginRequest, HttpServletRequest request) {
        try {
            // 参数验证
            if (!StringUtils.hasText(loginRequest.getUsername()) ||
                    !StringUtils.hasText(loginRequest.getPassword())) {
                return Result.badRequest("用户名和密码不能为空");
            }

            // 查询用户
            User user = userMapper.findByUsername(loginRequest.getUsername());
            if (user == null) {
                return Result.failure("用户名或密码错误");
            }

            // 检查用户状态
            if (!user.isEnabled()) {
                return Result.failure("用户已被禁用，请联系管理员");
            }

            // 简单密码验证（实际项目中应该使用BCrypt等加密）
            if (!loginRequest.getPassword().equals("123456")) {
                return Result.failure("用户名或密码错误");
            }

            // 生成JWT令牌
            String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());

            // 更新最后登录信息
            updateLastLoginInfo(user.getId(), request);

            // 构建响应数据
            LoginResponse response = LoginResponse.builder()
                    .token(token)
                    .tokenType("Bearer")
                    .expiresIn(86400L) // 24小时
                    .userId(user.getId())
                    .username(user.getUsername())
                    .realName(user.getRealName())
                    .role(user.getRole())
                    .department(user.getDepartment())
                    .build();

            log.info("用户登录成功: {}", user.getUsername());
            return Result.success(response);

        } catch (Exception e) {
            log.error("登录失败: {}", e.getMessage(), e);
            return Result.failure("登录失败，请稍后重试");
        }
    }

    /**
     * 用户注册
     */
    public Result<String> register(RegisterRequest registerRequest) {
        try {
            // 参数验证
            if (!StringUtils.hasText(registerRequest.getUsername()) ||
                    !StringUtils.hasText(registerRequest.getPassword()) ||
                    !StringUtils.hasText(registerRequest.getRealName())) {
                return Result.badRequest("用户名、密码和真实姓名不能为空");
            }

            // 检查密码确认
            if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
                return Result.badRequest("密码和确认密码不匹配");
            }

            // 检查用户名是否已存在
            if (userMapper.countByUsername(registerRequest.getUsername()) > 0) {
                return Result.failure("用户名已存在");
            }

            // 检查手机号是否已存在
            if (StringUtils.hasText(registerRequest.getPhone()) &&
                    userMapper.countByPhone(registerRequest.getPhone()) > 0) {
                return Result.failure("手机号已存在");
            }

            // 创建用户
            User user = new User();
            user.setUsername(registerRequest.getUsername());
            user.setPassword(registerRequest.getPassword()); // 实际项目中应该加密
            user.setRealName(registerRequest.getRealName());
            user.setPhone(registerRequest.getPhone());
            user.setEmail(registerRequest.getEmail());
            user.setDepartment(registerRequest.getDepartment());
            user.setRole(StringUtils.hasText(registerRequest.getRole()) ? registerRequest.getRole() : "OPERATOR");
            user.setStatus(1); // 默认启用
            user.setCreatedTime(LocalDateTime.now());
            user.setUpdatedTime(LocalDateTime.now());

            int result = userMapper.insert(user);
            if (result > 0) {
                log.info("用户注册成功: {}", user.getUsername());
                return Result.success("注册成功");
            } else {
                return Result.failure("注册失败，请稍后重试");
            }

        } catch (Exception e) {
            log.error("注册失败: {}", e.getMessage(), e);
            return Result.failure("注册失败，请稍后重试");
        }
    }

    /**
     * 验证令牌
     */
    public Result<User> validateToken(String token) {
        try {
            if (!StringUtils.hasText(token)) {
                return Result.failure("令牌不能为空");
            }

            // 从令牌中获取用户名
            String username = jwtUtil.getUsernameFromToken(token);

            // 查询用户
            User user = userMapper.findByUsername(username);
            if (user == null) {
                return Result.failure("用户不存在");
            }

            // 验证令牌
            if (!jwtUtil.validateToken(token, username)) {
                return Result.failure("令牌无效或已过期");
            }

            // 检查用户状态
            if (!user.isEnabled()) {
                return Result.failure("用户已被禁用");
            }

            return Result.success(user);

        } catch (Exception e) {
            log.error("令牌验证失败: {}", e.getMessage(), e);
            return Result.failure("令牌验证失败");
        }
    }

    /**
     * 更新最后登录信息
     */
    private void updateLastLoginInfo(Long userId, HttpServletRequest request) {
        try {
            String clientIp = getClientIp(request);
            userMapper.updateLastLogin(userId, LocalDateTime.now(), clientIp);
        } catch (Exception e) {
            log.warn("更新登录信息失败: {}", e.getMessage());
        }
    }

    /**
     * 获取客户端IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}