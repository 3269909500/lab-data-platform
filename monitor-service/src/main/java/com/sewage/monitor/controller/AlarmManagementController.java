package com.sewage.monitor.controller;

import com.sewage.common.context.UserContext;
import com.sewage.common.result.Result;
import com.sewage.monitor.entity.LabAlarm;
import com.sewage.monitor.mapper.LabAlarmMapper;
import com.sewage.monitor.service.AlarmService;
import com.sewage.monitor.service.WebSocketPushService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 告警管理控制器
 * 提供告警的确认、解决、历史查询等功能
 *
 * @author system
 */
@Slf4j
@RestController
@RequestMapping("/alarm-management")
@RequiredArgsConstructor
public class AlarmManagementController {

    private final LabAlarmMapper labAlarmMapper;
    private final WebSocketPushService webSocketPushService;

    /**
     * 确认告警
     *
     * PUT http://localhost:8083/alarm-management/alarms/{id}/confirm
     */
    @PutMapping("/alarms/{id}/confirm")
    public Result<String> confirmAlarm(@PathVariable Long id) {
        try {
            // 获取当前用户信息
            UserContext.UserInfo userInfo = UserContext.getUser();
            if (userInfo == null) {
                return Result.failure("用户未登录或权限不足");
            }

            // 查询告警
            LabAlarm alarm = labAlarmMapper.selectById(id);
            if (alarm == null) {
                return Result.failure("告警不存在");
            }

            // 检查告警状态
            if (alarm.getStatus() == LabAlarm.HandleStatus.CONFIRMED) {
                return Result.failure("告警已确认，无需重复操作");
            }

            if (alarm.getStatus() == LabAlarm.HandleStatus.RESOLVED) {
                return Result.failure("告警已解决，无需确认");
            }

            // 更新告警状态
            alarm.setStatus(LabAlarm.HandleStatus.CONFIRMED);
            alarm.setConfirmedAt(LocalDateTime.now());
            alarm.setConfirmedBy(userInfo.getUsername());

            int updated = labAlarmMapper.updateById(alarm);
            if (updated > 0) {
                log.info("✅ 告警确认成功 - 告警ID: {}, 操作人: {}", id, userInfo.getUsername());

                // 推送WebSocket通知
                Map<String, Object> notification = new HashMap<>();
                notification.put("type", "ALARM_CONFIRMED");
                notification.put("alarmId", id);
                notification.put("confirmedBy", userInfo.getUsername());
                notification.put("confirmedAt", alarm.getConfirmedAt());
                notification.put("labId", alarm.getLabId());

                webSocketPushService.pushStatistics(alarm.getLabId(), notification);

                return Result.success("告警确认成功");
            } else {
                return Result.failure("告警确认失败");
            }
        } catch (Exception e) {
            log.error("❌ 告警确认失败 - 告警ID: {}", id, e);
            return Result.failure("告警确认失败: " + e.getMessage());
        }
    }

    /**
     * 解决告警
     *
     * PUT http://localhost:8083/alarm-management/alarms/{id}/resolve
     */
    @PutMapping("/alarms/{id}/resolve")
    public Result<String> resolveAlarm(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> request) {
        try {
            // 获取当前用户信息
            UserContext.UserInfo userInfo = UserContext.getUser();
            if (userInfo == null) {
                return Result.failure("用户未登录或权限不足");
            }

            // 查询告警
            LabAlarm alarm = labAlarmMapper.selectById(id);
            if (alarm == null) {
                return Result.failure("告警不存在");
            }

            // 检查告警状态
            if (alarm.getStatus() == LabAlarm.HandleStatus.RESOLVED) {
                return Result.failure("告警已解决，无需重复操作");
            }

            // 获取解决备注
            String remark = request != null ? request.get("remark") : "";

            // 更新告警状态
            alarm.setStatus(LabAlarm.HandleStatus.RESOLVED);
            alarm.setResolvedAt(LocalDateTime.now());
            alarm.setResolvedBy(userInfo.getUsername());
            alarm.setRemark(remark);

            int updated = labAlarmMapper.updateById(alarm);
            if (updated > 0) {
                log.info("✅ 告警解决成功 - 告警ID: {}, 操作人: {}, 备注: {}", id, userInfo.getUsername(), remark);

                // 推送WebSocket通知
                Map<String, Object> notification = new HashMap<>();
                notification.put("type", "ALARM_RESOLVED");
                notification.put("alarmId", id);
                notification.put("resolvedBy", userInfo.getUsername());
                notification.put("resolvedAt", alarm.getResolvedAt());
                notification.put("remark", remark);
                notification.put("labId", alarm.getLabId());

                webSocketPushService.pushStatistics(alarm.getLabId(), notification);

                return Result.success("告警解决成功");
            } else {
                return Result.failure("告警解决失败");
            }
        } catch (Exception e) {
            log.error("❌ 告警解决失败 - 告警ID: {}", id, e);
            return Result.failure("告警解决失败: " + e.getMessage());
        }
    }

    /**
     * 忽略告警
     *
     * PUT http://localhost:8083/alarm-management/alarms/{id}/ignore
     */
    @PutMapping("/alarms/{id}/ignore")
    public Result<String> ignoreAlarm(@PathVariable Long id) {
        try {
            // 获取当前用户信息
            UserContext.UserInfo userInfo = UserContext.getUser();
            if (userInfo == null) {
                return Result.failure("用户未登录或权限不足");
            }

            // 查询告警
            LabAlarm alarm = labAlarmMapper.selectById(id);
            if (alarm == null) {
                return Result.failure("告警不存在");
            }

            // 检查告警状态
            if (alarm.getStatus() == LabAlarm.HandleStatus.IGNORED) {
                return Result.failure("告警已忽略，无需重复操作");
            }

            if (alarm.getStatus() == LabAlarm.HandleStatus.RESOLVED) {
                return Result.failure("告警已解决，无法忽略");
            }

            // 更新告警状态
            alarm.setStatus(LabAlarm.HandleStatus.IGNORED);
            alarm.setIgnoredAt(LocalDateTime.now());
            alarm.setIgnoredBy(userInfo.getUsername());

            int updated = labAlarmMapper.updateById(alarm);
            if (updated > 0) {
                log.info("✅ 告警忽略成功 - 告警ID: {}, 操作人: {}", id, userInfo.getUsername());

                // 推送WebSocket通知
                Map<String, Object> notification = new HashMap<>();
                notification.put("type", "ALARM_IGNORED");
                notification.put("alarmId", id);
                notification.put("ignoredBy", userInfo.getUsername());
                notification.put("ignoredAt", alarm.getIgnoredAt());
                notification.put("labId", alarm.getLabId());

                webSocketPushService.pushStatistics(alarm.getLabId(), notification);

                return Result.success("告警忽略成功");
            } else {
                return Result.failure("告警忽略失败");
            }
        } catch (Exception e) {
            log.error("❌ 告警忽略失败 - 告警ID: {}", id, e);
            return Result.failure("告警忽略失败: " + e.getMessage());
        }
    }

    /**
     * 查询告警历史记录
     *
     * GET http://localhost:8083/alarm-management/alarms/history?labId=1&days=7
     */
    @GetMapping("/alarms/history")
    public Result<List<LabAlarm>> getAlarmHistory(
            @RequestParam(required = false) Long labId,
            @RequestParam(defaultValue = "7") Integer days) {
        try {
            LocalDateTime endTime = LocalDateTime.now();
            LocalDateTime startTime = endTime.minusDays(days);

            List<LabAlarm> alarmList = labAlarmMapper.selectAlarmHistory(labId, startTime, endTime);

            log.info("📋 查询告警历史 - 实验室ID: {}, 天数: {}, 记录数: {}", labId, days, alarmList.size());

            return Result.success(alarmList);
        } catch (Exception e) {
            log.error("❌ 查询告警历史失败", e);
            return Result.failure("查询告警历史失败: " + e.getMessage());
        }
    }

    /**
     * 查询未处理告警统计
     *
     * GET http://localhost:8083/alarm-management/alarms/unhandled-stats
     */
    @GetMapping("/alarms/unhandled-stats")
    public Result<Map<String, Object>> getUnhandledAlarmStats() {
        try {
            Map<String, Object> stats = new HashMap<>();

            // 待处理告警数量
            int pendingCount = Math.toIntExact(labAlarmMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<LabAlarm>()
                    .eq(LabAlarm::getStatus, LabAlarm.HandleStatus.PENDING)
            ));
            stats.put("pendingCount", pendingCount);

            // 已确认未解决数量
            int confirmedCount = Math.toIntExact(labAlarmMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<LabAlarm>()
                    .eq(LabAlarm::getStatus, LabAlarm.HandleStatus.CONFIRMED)
            ));
            stats.put("confirmedCount", confirmedCount);

            // 今日新增告警数量
            int todayCount = labAlarmMapper.selectTodayAlarmCount();
            stats.put("todayCount", todayCount);

            // 高级别告警数量
            int highLevelCount = Math.toIntExact(labAlarmMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<LabAlarm>()
                    .eq(LabAlarm::getAlarmLevel, LabAlarm.AlarmLevel.DANGER)
                    .in(LabAlarm::getStatus, LabAlarm.HandleStatus.PENDING, LabAlarm.HandleStatus.CONFIRMED)
            ));
            stats.put("highLevelCount", highLevelCount);

            return Result.success(stats);
        } catch (Exception e) {
            log.error("❌ 查询告警统计失败", e);
            return Result.failure("查询告警统计失败: " + e.getMessage());
        }
    }

    /**
     * 批量操作告警
     *
     * POST http://localhost:8083/alarm-management/alarms/batch-action
     */
    @PostMapping("/alarms/batch-action")
    public Result<String> batchAction(@RequestBody Map<String, Object> request) {
        try {
            // 获取当前用户信息
            UserContext.UserInfo userInfo = UserContext.getUser();
            if (userInfo == null) {
                return Result.failure("用户未登录或权限不足");
            }

            @SuppressWarnings("unchecked")
            List<Long> alarmIds = (List<Long>) request.get("alarmIds");
            String action = (String) request.get("action"); // "confirm", "resolve", "ignore"
            String remark = (String) request.get("remark");

            if (alarmIds == null || alarmIds.isEmpty()) {
                return Result.failure("请选择要操作的告警");
            }

            if (action == null || (!action.equals("confirm") && !action.equals("resolve") && !action.equals("ignore"))) {
                return Result.failure("操作类型无效");
            }

            int successCount = 0;
            for (Long alarmId : alarmIds) {
                try {
                    switch (action) {
                        case "confirm":
                            Result<String> confirmResult = confirmAlarm(alarmId);
                            if (confirmResult.getCode() == 200) successCount++;
                            break;
                        case "resolve":
                            Map<String, String> resolveRequest = new HashMap<>();
                            resolveRequest.put("remark", remark);
                            Result<String> resolveResult = resolveAlarm(alarmId, resolveRequest);
                            if (resolveResult.getCode() == 200) successCount++;
                            break;
                        case "ignore":
                            Result<String> ignoreResult = ignoreAlarm(alarmId);
                            if (ignoreResult.getCode() == 200) successCount++;
                            break;
                    }
                } catch (Exception e) {
                    log.warn("批量操作中单个告警处理失败 - 告警ID: {}", alarmId, e);
                }
            }

            log.info("📦 告警批量操作完成 - 操作: {}, 成功数: {}, 总数: {}", action, successCount, alarmIds.size());

            return Result.success(String.format("批量操作完成，成功处理 %d/%d 个告警", successCount, alarmIds.size()));
        } catch (Exception e) {
            log.error("❌ 告警批量操作失败", e);
            return Result.failure("告警批量操作失败: " + e.getMessage());
        }
    }
}