package com.sewage.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sewage.common.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 用户数据访问层
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据用户名查询用户
     */
    @Select("SELECT * FROM sys_user WHERE username = #{username}")
    User findByUsername(@Param("username") String username);

    /**
     * 根据用户名检查用户是否存在
     */
    @Select("SELECT COUNT(*) FROM sys_user WHERE username = #{username}")
    int countByUsername(@Param("username") String username);

    /**
     * 根据手机号检查用户是否存在
     */
    @Select("SELECT COUNT(*) FROM sys_user WHERE phone = #{phone}")
    int countByPhone(@Param("phone") String phone);

    /**
     * 更新用户最后登录时间和IP
     */
    @Update("UPDATE sys_user SET last_login_time = #{loginTime}, last_login_ip = #{loginIp} WHERE id = #{userId}")
    int updateLastLogin(@Param("userId") Long userId,
                        @Param("loginTime") java.time.LocalDateTime loginTime,
                        @Param("loginIp") String loginIp);
}