package com.sewage.monitor.interceptor;

import com.sewage.common.context.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 用户信息拦截器
 *
 * 作用：
 * 1. 在请求到达Controller之前，从请求头中提取用户信息
 * 2. 将用户信息封装成UserInfo对象，存入ThreadLocal
 * 3. 在请求处理完成后，清理ThreadLocal，防止内存泄漏
 *
 * 原理：
 * - Gateway已经验证了Token的有效性
 * - Gateway把用户信息（userId、username、role）放到了请求头中
 * - 我们只需要从请求头中取出来，存到ThreadLocal即可
 * - 这样在Service层就可以直接调用 UserContext.getUserId() 获取当前用户
 */
@Slf4j
@Component
public class UserInterceptor implements HandlerInterceptor {

    /**
     * 前置处理：在Controller方法执行之前调用
     *
     * @param request  HTTP请求对象
     * @param response HTTP响应对象
     * @param handler  处理器（Controller方法）
     * @return true表示继续执行后续流程，false表示中断请求
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 步骤1：从请求头中获取Gateway传递过来的用户信息
        // Gateway会在请求头中添加以下字段：
        // - X-User-Id: 用户ID
        // - X-Username: 用户名
        // - X-User-Role: 用户角色
        String userIdStr = request.getHeader("X-User-Id");
        String username = request.getHeader("X-Username");
        String role = request.getHeader("X-User-Role");

        // 步骤2：判断是否有用户信息
        // 如果请求头中没有用户信息，说明可能是：
        // 1. 白名单接口（如健康检查、测试接口）
        // 2. Gateway配置错误，没有正确传递用户信息
        if (userIdStr != null && username != null) {
            try {
                // 步骤3：将字符串类型的userId转换为Long类型
                Long userId = Long.parseLong(userIdStr);

                // 步骤4：创建UserInfo对象
                UserContext.UserInfo userInfo = new UserContext.UserInfo(userId, username, role);

                // 步骤5：存入ThreadLocal
                // 这样在Controller、Service、Mapper的任何地方都可以通过 UserContext.getUserId() 获取
                UserContext.setUser(userInfo);

                // 步骤6：记录日志（调试用）
                log.debug("拦截器提取用户信息成功：userId={}, username={}, role={}",
                        userId, username, role);

            } catch (NumberFormatException e) {
                // 如果userId格式不正确（不是数字），记录错误日志
                log.error("用户ID格式错误：{}", userIdStr, e);
            }
        } else {
            // 如果请求头中没有用户信息，记录警告日志
            log.warn("请求头中没有用户信息，请求路径：{}", request.getRequestURI());
        }

        // 步骤7：返回true，表示继续执行后续流程
        // 注意：即使没有提取到用户信息，我们也返回true
        // 因为有些接口（如测试接口）可能不需要用户信息
        return true;
    }

    /**
     * 后置处理：在Controller方法执行之后、视图渲染之后调用
     *
     * 作用：清理ThreadLocal，防止内存泄漏
     *
     * 为什么要清理ThreadLocal？
     * - Web容器（如Tomcat）使用线程池处理请求
     * - 线程处理完一个请求后，不会销毁，而是放回线程池
     * - 如果不清理ThreadLocal，下一个请求复用这个线程时，可能会读到上一个请求的用户信息
     * - 长期不清理会导致内存泄漏
     *
     * @param request  HTTP请求对象
     * @param response HTTP响应对象
     * @param handler  处理器
     * @param ex       异常对象（如果有）
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        // 清理ThreadLocal
        UserContext.clear();

        // 记录日志
        log.debug("清理ThreadLocal用户上下文，请求路径：{}", request.getRequestURI());
    }
}