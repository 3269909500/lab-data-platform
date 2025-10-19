package com.sewage.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sewage.common.result.Result;
import com.sewage.system.entity.TreatmentPlant;
import com.sewage.system.service.TreatmentPlantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/plant")
@RequiredArgsConstructor
public class TreatmentPlantController {

    private final TreatmentPlantService plantService;

    /**
     * 分页查询处理厂
     */
    @GetMapping("/page")
    public Result<Page<TreatmentPlant>> getPageData(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer status) {
        Page<TreatmentPlant> page = plantService.getPageData(current, size, name, status);
        return Result.success(page);
    }

    /**
     * 新增处理厂
     */
    @PostMapping
    public Result<String> addPlant(@RequestBody TreatmentPlant plant) {
        plantService.addPlant(plant);
        return Result.success("新增成功");
    }

    /**
     * 更新处理厂
     */
    @PutMapping
    public Result<String> updatePlant(@RequestBody TreatmentPlant plant) {
        plantService.updatePlant(plant);
        return Result.success("更新成功");
    }

    /**
     * 删除处理厂
     */
    @DeleteMapping("/{id}")
    public Result<String> deletePlant(@PathVariable Long id) {
        plantService.deletePlant(id);
        return Result.success("删除成功");
    }

    /**
     * 根据ID查询
     */
    @GetMapping("/{id}")
    public Result<TreatmentPlant> getById(@PathVariable Long id) {
        TreatmentPlant plant = plantService.getById(id);
        return Result.success(plant);
    }

    /**
     * 获取所有运行中的处理厂
     */
    @GetMapping("/running")
    public Result<List<TreatmentPlant>> getRunningPlants() {
        List<TreatmentPlant> plants = plantService.getRunningPlants();
        return Result.success(plants);
    }
}