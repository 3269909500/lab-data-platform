package com.sewage.auth.service;

import com.sewage.auth.dto.LoginRequest;
import com.sewage.auth.dto.LoginResponse;
import com.sewage.auth.dto.RegisterRequest;
import com.sewage.auth.mapper.UserMapper;
import com.sewage.common.entity.User;
import com.sewage.common.result.Result;
import com.sewage.common.util.JwtUtil;
import com.sewage.common.util.PasswordEncoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 认证服务业务层 - 简化版本
 * 用于临时解决数据库连接问题，使用内存存储用户数据
 */
@Slf4j
@Service("authServiceSimple")
@RequiredArgsConstructor
public class AuthServiceSimple {

    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final StringRedisTemplate stringRedisTemplate;

    // 临时内存用户存储（用于测试）
    private static final Map<String, User> MOCK_USERS = new HashMap<>();

    static {
        // 初始化测试用户 - 密码都是明文，用于测试
        User admin = new User();
        admin.setId(1L);
        admin.setUsername("admin");
        admin.setPassword("admin123"); // 临时使用明文密码
        admin.setRealName("系统管理员");
        admin.setEmail("admin@example.com");
        admin.setPhone("13800138000");
        admin.setDepartment("IT部门");
        admin.setRole("ADMIN");
        admin.setStatus(1);
        admin.setCreatedTime(LocalDateTime.now());
        MOCK_USERS.put("admin", admin);

        User operator = new User();
        operator.setId(2L);
        operator.setUsername("operator");
        operator.setPassword("operator123"); // 临时使用明文密码
        operator.setRealName("操作员");
        operator.setEmail("operator@example.com");
        operator.setPhone("13800138001");
        operator.setDepartment("运营部门");
        operator.setRole("OPERATOR");
        operator.setStatus(1);
        operator.setCreatedTime(LocalDateTime.now());
        MOCK_USERS.put("operator", operator);

        User user = new User();
        user.setId(3L);
        user.setUsername("user");
        user.setPassword("user123"); // 临时使用明文密码
        user.setRealName("普通用户");
        user.setEmail("user@example.com");
        user.setPhone("13800138002");
        user.setDepartment("测试部门");
        user.setRole("USER");
        user.setStatus(1);
        user.setCreatedTime(LocalDateTime.now());
        MOCK_USERS.put("user", user);
    }

    private final UserMapper userMapper;

    /**
     * 用户登录 - 简化版本（使用内存用户）
     */
    public Result<LoginResponse> login(LoginRequest loginRequest, HttpServletRequest request) {
        try {
            log.info("简化版用户登录请求: username={}, ip={}",
                    loginRequest.getUsername(), getClientIp(request));

            // 1. 参数验证
            if (!StringUtils.hasText(loginRequest.getUsername()) ||
                !StringUtils.hasText(loginRequest.getPassword())) {
                return Result.failure("用户名和密码不能为空");
            }

            // 2. 从内存中查询用户
            User user = MOCK_USERS.get(loginRequest.getUsername());
            if (user == null) {
                log.warn("用户不存在: username={}", loginRequest.getUsername());
                return Result.failure("用户名或密码错误");
            }

            // 3. 验证密码（简化版使用明文比较）
            if (!loginRequest.getPassword().equals(user.getPassword())) {
                log.warn("密码错误: username={}, ip={}",
                        loginRequest.getUsername(), getClientIp(request));
                return Result.failure("用户名或密码错误");
            }

            // 4. 检查用户状态
            if (user.getStatus() != null && user.getStatus() == 0) {
                log.warn("用户已被禁用: username={}", loginRequest.getUsername());
                return Result.failure("账号已被禁用，请联系管理员");
            }

            // 5. 生成JWT令牌
            String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
            Duration expiresIn = Duration.ofHours(24); // 24小时过期

            // 6. 存储令牌到Redis
            String tokenKey = "auth:token:" + user.getId();
            stringRedisTemplate.opsForValue().set(tokenKey, token, expiresIn);

            // 7. 创建登录响应
            LoginResponse response = LoginResponse.builder()
                    .token(token)
                    .tokenType("Bearer")
                    .expiresIn(expiresIn.getSeconds())
                    .userId(user.getId())
                    .username(user.getUsername())
                    .realName(user.getRealName())
                    .role(user.getRole())
                    .department(user.getDepartment())
                    .email(user.getEmail())
                    .phone(user.getPhone())
                    .build();

            log.info("简化版用户登录成功: username={}, userId={}, role={}",
                    user.getUsername(), user.getId(), user.getRole());

            return Result.success(response);

        } catch (Exception e) {
            log.error("简化版登录失败: username={}, error={}",
                    loginRequest.getUsername(), e.getMessage(), e);
            return Result.failure("登录失败: " + e.getMessage());
        }
    }

    /**
     * 用户注册
     */
    public Result<String> register(RegisterRequest registerRequest) {
        try {
            log.info("用户注册请求: username={}, email={}",
                    registerRequest.getUsername(), registerRequest.getEmail());

            // 1. 参数验证
            if (!StringUtils.hasText(registerRequest.getUsername()) ||
                !StringUtils.hasText(registerRequest.getPassword())) {
                return Result.failure("用户名和密码不能为空");
            }

            if (registerRequest.getPassword().length() < 6) {
                return Result.failure("密码长度不能少于6位");
            }

            // 2. 检查用户名是否已存在
            if (userMapper.countByUsername(registerRequest.getUsername()) > 0) {
                log.warn("用户名已存在: username={}", registerRequest.getUsername());
                return Result.failure("用户名已存在，请选择其他用户名");
            }

            // 3. 检查邮箱是否已存在（如果提供邮箱）
            if (StringUtils.hasText(registerRequest.getEmail()) &&
                userMapper.countByPhone(registerRequest.getEmail()) > 0) {
                log.warn("邮箱已存在: email={}", registerRequest.getEmail());
                return Result.failure("邮箱已被使用，请选择其他邮箱");
            }

            // 4. 创建用户对象
            User user = new User();
            user.setUsername(registerRequest.getUsername());
            user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
            user.setEmail(registerRequest.getEmail());
            user.setPhone(registerRequest.getPhone());
            user.setRealName(registerRequest.getRealName());
            user.setDepartment(registerRequest.getDepartment());
            user.setRole("USER"); // 默认角色
            user.setStatus(1); // 默认启用

            // 5. 保存用户到数据库
            int result = userMapper.insert(user);
            if (result <= 0) {
                log.error("用户注册失败: username={}", registerRequest.getUsername());
                return Result.failure("注册失败，请稍后重试");
            }

            log.info("用户注册成功: username={}, userId={}",
                    user.getUsername(), user.getId());

            return Result.success("注册成功");

        } catch (Exception e) {
            log.error("注册失败: username={}, error={}",
                    registerRequest.getUsername(), e.getMessage(), e);
            return Result.failure("注册失败：" + e.getMessage());
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

            // 1. 检查token是否在黑名单中
            String blacklistKey = "auth:blacklist:" + token;
            if ("1".equals(stringRedisTemplate.opsForValue().get(blacklistKey))) {
                log.warn("令牌已在黑名单中: token={}", token);
                return Result.failure("令牌已失效，请重新登录");
            }

            // 2. 提取用户信息并验证token格式
            String username;
            Long userId;
            try {
                username = jwtUtil.getUsernameFromToken(token);
                userId = jwtUtil.getUserIdFromToken(token);
            } catch (Exception e) {
                log.warn("令牌格式无效: token={}, error={}", token, e.getMessage());
                return Result.failure("令牌格式无效");
            }

            // 3. 验证token有效性
            if (username == null || userId == null) {
                log.warn("无法从令牌提取用户信息: token={}", token);
                return Result.failure("令牌无效");
            }

            if (!jwtUtil.validateToken(token, username)) {
                log.warn("令牌验证失败: username={}", username);
                return Result.failure("令牌无效");
            }

  
            // 4. 验证token是否过期
            try {
                if (jwtUtil.isTokenExpired(token)) {
                    log.warn("令牌已过期: username={}", username);
                    return Result.failure("令牌已过期，请重新登录");
                }
            } catch (Exception e) {
                log.warn("令牌验证失败: username={}, error={}", username, e.getMessage());
                return Result.failure("令牌已过期，请重新登录");
            }

            // 5. 验证token是否在Redis中存在
            String tokenKey = "auth:token:" + userId;
            String storedToken = stringRedisTemplate.opsForValue().get(tokenKey);
            if (!token.equals(storedToken)) {
                log.warn("令牌不存在或已失效: userId={}", userId);
                return Result.failure("令牌已失效，请重新登录");
            }

            // 6. 查询用户信息
            User user = userMapper.findByUsername(username);
            if (user == null) {
                log.warn("用户不存在: username={}", username);
                return Result.failure("用户不存在");
            }

            // 7. 检查用户状态
            if (user.getStatus() != null && user.getStatus() == 0) {
                log.warn("用户已被禁用: username={}", username);
                return Result.failure("账号已被禁用，请联系管理员");
            }

            log.debug("令牌验证成功: username={}, role={}", username, user.getRole());

            return Result.success(user);

        } catch (Exception e) {
            log.error("令牌验证失败: token={}, error={}", token, e.getMessage(), e);
            return Result.failure("令牌验证失败");
        }
    }

    /**
     * 用户登出
     */
    public Result<String> logout(String token) {
        try {
            if (!StringUtils.hasText(token)) {
                return Result.failure("令牌不能为空");
            }

            // 1. 提取用户信息
            Long userId = jwtUtil.getUserIdFromToken(token);
            if (userId != null) {
                log.info("用户登出: userId={}", userId);

                // 2. 删除Redis中的token
                String tokenKey = "auth:token:" + userId;
                stringRedisTemplate.delete(tokenKey);

                // 3. 将token加入黑名单（防止重复使用）
                String blacklistKey = "auth:blacklist:" + token;
                Duration ttl = Duration.ofHours(24); // 黑名单24小时
                stringRedisTemplate.opsForValue().set(blacklistKey, "1", ttl);
            }

            return Result.success("登出成功");

        } catch (Exception e) {
            log.error("登出失败: token={}, error={}", token, e.getMessage(), e);
            return Result.failure("登出失败");
        }
    }

    /**
     * 更新最后登录信息
     */
    private void updateLastLoginInfo(Long userId, HttpServletRequest request) {
        try {
            String lastLoginIp = getClientIp(request);
            LocalDateTime lastLoginTime = LocalDateTime.now();

            // 更新用户的最后登录信息
            userMapper.updateLastLogin(userId, lastLoginTime, lastLoginIp);

            log.info("更新用户最后登录信息: userId={}, ip={}, time={}",
                    userId, lastLoginIp, lastLoginTime);

        } catch (Exception e) {
            log.error("更新登录信息失败: userId={}", userId, e);
        }
    }

    /**
     * 获取客户端真实IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");

        // X-Forwarded-For可能包含多个IP，取第一个
        if (StringUtils.hasText(ip) && !"unknown".equalsIgnoreCase(ip)) {
            String[] ips = ip.split(",");
            return ips[0].trim();
        }

        ip = request.getHeader("Proxy-Client-IP");
        if (StringUtils.hasText(ip) && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }

        ip = request.getHeader("WL-Proxy-Client-IP");
        if (StringUtils.hasText(ip) && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }

        ip = request.getHeader("HTTP_CLIENT_IP");
        if (StringUtils.hasText(ip) && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }

        ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        if (StringUtils.hasText(ip) && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }

        // 最后使用request.getRemoteAddr()
        ip = request.getRemoteAddr();
        return "0:0:0:0:0:0:0:1".equals(ip) ? "127.0.0.1" : ip;
    }

    /**
     * 生成随机验证码
     */
    public Result<String> generateVerificationCode(String email) {
        try {
            if (!StringUtils.hasText(email)) {
                return Result.failure("邮箱不能为空");
            }

            // 生成6位数字验证码
            String code = String.format("%06d",
                    (int) (Math.random() * 900000) + 100000);

            // 存储验证码到Redis（5分钟过期）
            String codeKey = "auth:verify:" + email;
            stringRedisTemplate.opsForValue().set(codeKey, code, Duration.ofMinutes(5));

            log.info("生成验证码: email={}, code={}", email, code);

            // TODO: 这里应该发送邮件，暂时直接返回验证码用于测试
            return Result.success("验证码已发送到邮箱，测试环境返回: " + code);

        } catch (Exception e) {
            log.error("生成验证码失败: email={}, error={}", email, e.getMessage(), e);
            return Result.failure("验证码生成失败");
        }
    }
}