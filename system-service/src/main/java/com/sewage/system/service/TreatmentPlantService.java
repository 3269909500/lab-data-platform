package com.sewage.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sewage.system.entity.TreatmentPlant;
import com.sewage.system.mapper.TreatmentPlantMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TreatmentPlantService {

    private final TreatmentPlantMapper plantMapper;

    /**
     * 分页查询处理厂
     */
    public Page<TreatmentPlant> getPageData(int current, int size, String name, Integer status) {
        Page<TreatmentPlant> page = new Page<>(current, size);
        LambdaQueryWrapper<TreatmentPlant> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(name)) {
            wrapper.like(TreatmentPlant::getPlantName, name);
        }
        if (status != null) {
            wrapper.eq(TreatmentPlant::getStatus, status);
        }
        wrapper.orderByDesc(TreatmentPlant::getCreatedTime);

        return plantMapper.selectPage(page, wrapper);
    }

    /**
     * 新增处理厂
     */
    public void addPlant(TreatmentPlant plant) {
        plant.setCreatedTime(LocalDateTime.now());
        plant.setUpdatedTime(LocalDateTime.now());
        plantMapper.insert(plant);
        log.info("新增处理厂成功: {}", plant.getPlantName());
    }

    /**
     * 更新处理厂
     */
    public void updatePlant(TreatmentPlant plant) {
        plant.setUpdatedTime(LocalDateTime.now());
        plantMapper.updateById(plant);
        log.info("更新处理厂成功: {}", plant.getPlantName());
    }

    /**
     * 删除处理厂
     */
    public void deletePlant(Long id) {
        plantMapper.deleteById(id);
        log.info("删除处理厂成功: {}", id);
    }

    /**
     * 根据ID查询
     */
    public TreatmentPlant getById(Long id) {
        return plantMapper.selectById(id);
    }

    /**
     * 获取所有运行中的处理厂
     */
    public List<TreatmentPlant> getRunningPlants() {
        return plantMapper.getByStatus(1);
    }
}