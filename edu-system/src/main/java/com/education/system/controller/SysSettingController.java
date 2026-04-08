package com.education.system.controller;

import com.education.common.result.JsonResult;
import com.education.system.dto.SettingGroupVO;
import com.education.system.entity.SysSetting;
import com.education.system.service.EmailService;
import com.education.system.service.ISysSettingService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 系统设置Controller
 */
@Api(tags = "系统设置")
@RestController
@RequestMapping("/system/setting")
@RequiredArgsConstructor
public class SysSettingController {

    private final ISysSettingService settingService;
    private final EmailService emailService;

    @ApiOperation("查询所有设置（按分组）")
    @GetMapping("/all")
    public JsonResult<List<SettingGroupVO>> listAll() {
        return JsonResult.success(settingService.listAllGrouped());
    }

    @ApiOperation("按分组查询设置")
    @GetMapping("/group/{groupCode}")
    public JsonResult<List<SysSetting>> listByGroup(@PathVariable String groupCode) {
        return JsonResult.success(settingService.listByGroup(groupCode));
    }

    @ApiOperation("批量保存某分组设置")
    @PostMapping("/group/{groupCode}")
    public JsonResult<Void> saveBatch(@PathVariable String groupCode,
                                      @RequestBody Map<String, String> kvMap) {
        settingService.saveBatch(groupCode, kvMap);
        // 如果是通知设置，重新加载邮件配置
        if ("notify".equals(groupCode)) {
            emailService.reloadMailConfig();
        }
        return JsonResult.success("保存成功");
    }
}
