package com.sewage.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sewage.system.entity.Laboratory;
import com.sewage.system.mapper.LaboratoryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LaboratoryService {

    private final LaboratoryMapper plantMapper;

    /**
     * 分页查询处理厂
     */
    public Page<Laboratory> getPageData(int current, int size, String name, Integer status) {
        Page<Laboratory> page = new Page<>(current, size);
        LambdaQueryWrapper<Laboratory> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(name)) {
            wrapper.like(Laboratory::getLabName, name);
        }
        if (status != null) {
            wrapper.eq(Laboratory::getStatus, status);
        }
        wrapper.orderByDesc(Laboratory::getCreatedTime);

        return plantMapper.selectPage(page, wrapper);
    }

    /**
     * 新增处理厂
     */
    public void addPlant(Laboratory plant) {
        plant.setCreatedTime(LocalDateTime.now());
        plant.setUpdateTime(LocalDateTime.now());
        plantMapper.insert(plant);
        log.info("新增实验室成功: {}", plant.getLabName());
    }

    /**
     * 更新处理厂
     */
    public void updatePlant(Laboratory plant) {
        plant.setUpdateTime(LocalDateTime.now());
        plantMapper.updateById(plant);
        log.info("更新实验室成功: {}", plant.getLabName());
    }

    /**
     * 删除处理厂
     */
    public void deletePlant(Long id) {
        plantMapper.deleteById(id);
        log.info("删除实验室成功: {}", id);
    }

    /**
     * 根据ID查询
     */
    public Laboratory getById(Long id) {
        return plantMapper.selectById(id);
    }

    /**
     * 获取所有运行中的处理厂
     */
    public List<Laboratory> getRunningPlants() {
        return plantMapper.getByStatus(1);
    }
}