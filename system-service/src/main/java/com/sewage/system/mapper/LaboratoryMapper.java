package com.sewage.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sewage.system.entity.Laboratory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface LaboratoryMapper extends BaseMapper<Laboratory> {

    // 根据状态查询实验室
    @Select("SELECT * FROM laboratory WHERE status = #{status}")
    List<Laboratory> getByStatus(@Param("status") Integer status);

    // 模糊查询实验室名称
    @Select("SELECT * FROM laboratory WHERE lab_name LIKE CONCAT('%', #{name}, '%')")
    List<Laboratory> searchByName(@Param("name") String name);
}