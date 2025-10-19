package com.sewage.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sewage.system.entity.TreatmentPlant;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface TreatmentPlantMapper extends BaseMapper<TreatmentPlant> {

    // 根据状态查询处理厂
    @Select("SELECT * FROM treatment_plant WHERE status = #{status}")
    List<TreatmentPlant> getByStatus(@Param("status") Integer status);

    // 模糊查询处理厂名称
    @Select("SELECT * FROM treatment_plant WHERE plant_name LIKE CONCAT('%', #{name}, '%')")
    List<TreatmentPlant> searchByName(@Param("name") String name);
}