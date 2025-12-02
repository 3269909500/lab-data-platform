package com.sewage.common.context;

import lombok.Data;

/**
 * 用户上下文持有者
 */
public class UserContext {

    private static final ThreadLocal<UserInfo> USER_THREAD_LOCAL = new ThreadLocal<>();

    /**
     * 设置用户信息
     */
    public static void setUser(UserInfo userInfo) {
        USER_THREAD_LOCAL.set(userInfo);
    }

    /**
     * 获取当前用户信息
     */
    public static UserInfo getUser() {
        return USER_THREAD_LOCAL.get();
    }

    /**
     * 获取当前用户ID
     */
    public static Long getUserId() {
        UserInfo userInfo = USER_THREAD_LOCAL.get();
        return userInfo != null ? userInfo.getUserId() : null;
    }

    /**
     * 获取当前用户名
     */
    public static String getUsername() {
        UserInfo userInfo = USER_THREAD_LOCAL.get();
        return userInfo != null ? userInfo.getUsername() : null;
    }

    /**
     * 清除用户信息
     */
    public static void clear() {
        USER_THREAD_LOCAL.remove();
    }

    /**
     * 用户信息类
     */
    @Data
    public static class UserInfo {
        private Long userId;
        private String username;
        private String realName;
        private String role;
        private String department;

        public UserInfo(Long userId, String username, String role) {
            this.userId = userId;
            this.username = username;
            this.role = role;
        }
    }
}