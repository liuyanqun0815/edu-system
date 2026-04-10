package com.education.system.controller;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import com.education.common.constants.BusinessConstants;
import com.education.common.constants.RedisKeyConstants;
import com.education.common.constants.RoleCodeConstants;
import com.education.common.constants.StatusConstants;
import com.education.common.exception.BusinessException;
import com.education.common.result.JsonResult;
import com.education.common.utils.JwtUtilsProxy;
import com.education.common.utils.RedisUtils;
import com.education.common.utils.SecurityUtils;
import com.education.system.dto.ForgotPasswordDTO;
import com.education.system.dto.LoginDTO;
import com.education.system.dto.RegisterDTO;
import com.education.system.dto.ResetPasswordDTO;
import com.education.system.dto.CaptchaVO;
import com.education.system.dto.RefreshTokenVO;
import com.education.system.dto.UserRoleVO;
import com.education.system.dto.VerifyCodeDTO;
import com.education.system.entity.SysRole;
import com.education.system.entity.SysUser;
import com.education.system.entity.SysUserRole;
import com.education.system.entity.UserOnlineLog;
import com.education.system.mapper.SysRoleMapper;
import com.education.system.mapper.SysUserRoleMapper;
import com.education.system.mapper.UserOnlineLogMapper;
import com.education.system.service.DynamicMailService;
import com.education.system.service.ISysUserService;
import com.education.system.service.UserOnlineService;
import com.education.system.vo.LoginVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 认证Controller
 */
@Api(tags = "登录认证")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final ISysUserService userService;
    private final UserOnlineService userOnlineService;
    private final UserOnlineLogMapper userOnlineLogMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysRoleMapper roleMapper;
    private final DynamicMailService mailService;

    /** 登录验证码缓存(使用Redis) */
    private static final String CAPTCHA_CODE_PREFIX = RedisKeyConstants.CAPTCHA_LOGIN;
    
    /** 找回密码验证码缓存(使用Redis) */
    private static final String FORGOT_CODE_PREFIX = RedisKeyConstants.CAPTCHA_FORGOT;

    @ApiOperation("获取验证码")
    @GetMapping("/captcha")
    public JsonResult<CaptchaVO> captcha() {
        // 生成四位字符线条验证码，宿160x60
        LineCaptcha captcha = CaptchaUtil.createLineCaptcha(160, 60, 4, 80);
        String code = captcha.getCode().toLowerCase();
        String key = UUID.randomUUID().toString().replace("-", "");

        // 存入Redis,5分钟过期
        RedisUtils.set(CAPTCHA_CODE_PREFIX + key, code, BusinessConstants.CAPTCHA_EXPIRE_MINUTES, TimeUnit.MINUTES);

        CaptchaVO result = new CaptchaVO();
        result.setCaptchaKey(key);
        result.setCaptchaImg(captcha.getImageBase64Data()); // data:image/png;base64,...
        return JsonResult.success(result);
    }

    @ApiOperation("用户注册")
    @PostMapping("/register")
    public JsonResult<Void> register(@RequestBody @Validated RegisterDTO registerDTO) {
        // 检查用户名是否已存在
        SysUser existUser = userService.getByUsername(registerDTO.getUsername());
        if (existUser != null) {
            throw new BusinessException("用户名已存在");
        }

        // 检查邮箱是否已存在
        if (StringUtils.hasText(registerDTO.getEmail())) {
            LambdaQueryWrapper<SysUser> emailWrapper = new LambdaQueryWrapper<>();
            emailWrapper.eq(SysUser::getEmail, registerDTO.getEmail());
            if (userService.count(emailWrapper) > 0) {
                throw new BusinessException("邮箱已被注册");
            }
        }

        // 检查手机号是否已存在
        if (StringUtils.hasText(registerDTO.getPhone())) {
            LambdaQueryWrapper<SysUser> phoneWrapper = new LambdaQueryWrapper<>();
            phoneWrapper.eq(SysUser::getPhone, registerDTO.getPhone());
            if (userService.count(phoneWrapper) > 0) {
                throw new BusinessException("手机号已被注册");
            }
        }

        // 创建用户
        SysUser user = new SysUser();
        user.setUsername(registerDTO.getUsername());
        user.setPassword(SecurityUtils.encryptPassword(registerDTO.getPassword()));
        user.setNickname(StringUtils.hasText(registerDTO.getNickname()) ? registerDTO.getNickname() : registerDTO.getUsername());
        user.setEmail(registerDTO.getEmail());
        user.setPhone(registerDTO.getPhone());
        user.setStatus(StatusConstants.ENABLED); // 默认启用
        user.setSex(0); // 默认未知
        userService.save(user);

        // 分配默认角色(学生)
        try {
            LambdaQueryWrapper<SysRole> roleWrapper = new LambdaQueryWrapper<>();
            roleWrapper.eq(SysRole::getRoleCode, RoleCodeConstants.STUDENT);
            SysRole studentRole = roleMapper.selectOne(roleWrapper);
            
            if (studentRole != null) {
                SysUserRole userRole = new SysUserRole();
                userRole.setUserId(user.getId());
                userRole.setRoleId(studentRole.getId());
                userRoleMapper.insert(userRole);
            }
        } catch (Exception e) {
            // 角色分配失败不影响注册，可后续手动分配
        }

        return JsonResult.success("注册成功");
    }

    @ApiOperation("发送找回密码验证码")
    @PostMapping("/forgot/send-code")
    public JsonResult<Void> sendForgotCode(@RequestBody @Validated ForgotPasswordDTO dto) {
        // 查找用户
        SysUser user = null;
        
        // 先按用户名查找
        user = userService.getByUsername(dto.getUsernameOrEmail());
        
        // 如果没找到，按邮箱查找
        if (user == null) {
            LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SysUser::getEmail, dto.getUsernameOrEmail());
            user = userService.getOne(wrapper);
        }
        
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        if (!StringUtils.hasText(user.getEmail())) {
            throw new BusinessException("该用户未绑定邮箱，请联系管理员");
        }
        
        // 生成6位验证码
        String code = String.valueOf((int)((Math.random() * 9 + 1) * 100000));
        
        // 存储到Redis,5分钟过期
        RedisUtils.set(FORGOT_CODE_PREFIX + user.getEmail(), code, BusinessConstants.CAPTCHA_EXPIRE_MINUTES, TimeUnit.MINUTES);
        
        // 发送邮件
        try {
            String subject = "【智慧教培】密码重置验证码";
            String body = String.format(
                "<div style='font-family:Microsoft YaHei,sans-serif;max-width:600px;margin:0 auto;padding:20px;border:1px solid #e0e0e0;border-radius:8px;'>" +
                "<h2 style='color:#667eea;'>🔐 密码重置验证码</h2>" +
                "<p>您好，<strong>%s</strong></p>" +
                "<p>您正在重置密码，验证码为：</p>" +
                "<div style='background:#f5f5f5;padding:15px;text-align:center;font-size:28px;font-weight:bold;letter-spacing:5px;color:#667eea;border-radius:5px;margin:20px 0;'>%s</div>" +
                "<p style='color:#999;font-size:12px;'>验证码5分钟内有效，请及时使用。如非本人操作，请忽略此邮件。</p>" +
                "</div>",
                user.getNickname(), code
            );
            mailService.send(user.getEmail(), subject, body);
        } catch (Exception e) {
            RedisUtils.delete(FORGOT_CODE_PREFIX + user.getEmail());
            throw new BusinessException("验证码发送失败：" + e.getMessage());
        }
        
        return JsonResult.success("验证码已发送至邮箱");
    }

    @ApiOperation("验证找回密码验证码")
    @PostMapping("/forgot/verify-code")
    public JsonResult<Void> verifyForgotCode(@RequestBody @Validated VerifyCodeDTO dto) {
        String cachedCode = RedisUtils.get(FORGOT_CODE_PREFIX + dto.getEmail());
        if (cachedCode == null) {
            throw new BusinessException("验证码已过期，请重新获取");
        }
        if (!cachedCode.equals(dto.getVerifyCode())) {
            throw new BusinessException("验证码错误");
        }
        return JsonResult.success("验证成功");
    }

    @ApiOperation("重置密码")
    @PostMapping("/forgot/reset")
    public JsonResult<Void> resetPassword(@RequestBody @Validated ResetPasswordDTO dto) {
        // 验证验证码
        String cachedCode = RedisUtils.get(FORGOT_CODE_PREFIX + dto.getEmail());
        if (cachedCode == null) {
            throw new BusinessException("验证码已过期，请重新获取");
        }
        if (!cachedCode.equals(dto.getVerifyCode())) {
            throw new BusinessException("验证码错误");
        }
        
        // 查找用户
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getEmail, dto.getEmail());
        SysUser user = userService.getOne(wrapper);
        
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 更新密码
        SysUser update = new SysUser();
        update.setId(user.getId());
        update.setPassword(SecurityUtils.encryptPassword(dto.getNewPassword()));
        userService.updateById(update);
        
        // 清除验证码
        RedisUtils.delete(FORGOT_CODE_PREFIX + dto.getEmail());
        
        return JsonResult.success("密码重置成功");
    }

    @ApiOperation("修改密码")
    @PostMapping("/change-password")
    public JsonResult<Void> changePassword(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @RequestParam String oldPassword,
            @RequestParam String newPassword) {
        if (userId == null) {
            throw new BusinessException("未登录");
        }
        return JsonResult.success(userService.changePassword(userId, oldPassword, newPassword) ? "修改成功" : "修改失败");
    }

    @ApiOperation("刷新Token")
    @PostMapping("/refresh-token")
    public JsonResult<RefreshTokenVO> refreshToken(@RequestParam String refreshToken) {
        // 验证刷新Token
        if (!JwtUtilsProxy.validateToken(refreshToken)) {
            throw new BusinessException("刷新Token无效或已过期");
        }
        
        Long userId = JwtUtilsProxy.getUserIdFromToken(refreshToken);
        String username = JwtUtilsProxy.getUsernameFromToken(refreshToken);
        
        // 生成新Token
        String newToken = JwtUtilsProxy.generateToken(userId, username);
        String newRefreshToken = JwtUtilsProxy.generateRefreshToken(userId, username);
        
        RefreshTokenVO result = new RefreshTokenVO();
        result.setToken(newToken);
        result.setRefreshToken(newRefreshToken);
        
        return JsonResult.success(result);
    }

    @ApiOperation("用户登录")
    @PostMapping("/login")
    public JsonResult<LoginVO> login(@RequestBody @Validated LoginDTO loginDTO, HttpServletRequest request) {
        // 校验验证码
        String captchaKey = loginDTO.getCaptchaKey();
        String captchaCode = loginDTO.getCaptchaCode();
        if (!StringUtils.hasText(captchaKey) || !StringUtils.hasText(captchaCode)) {
            throw new BusinessException("请输入验证码");
        }
        String cachedCode = RedisUtils.get(CAPTCHA_CODE_PREFIX + captchaKey);
        if (cachedCode == null) {
            throw new BusinessException("验证码已过期，请刷新");
        }
        if (!cachedCode.equalsIgnoreCase(captchaCode.trim())) {
            RedisUtils.delete(CAPTCHA_CODE_PREFIX + captchaKey); // 验证失败立即删除
            throw new BusinessException("验证码错误");
        }
        RedisUtils.delete(CAPTCHA_CODE_PREFIX + captchaKey); // 校验成功后删除，一次性

        // 查询用户
        SysUser user = userService.getByUsername(loginDTO.getUsername());
        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }

        // 校验密码（BCrypt加密对比）
        if (!SecurityUtils.matchesPassword(loginDTO.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        // 检查用户状态
        if (user.getStatus() != 1) {
            throw new BusinessException("账号已被禁用");
        }

        // 生成JWT Token
        String token = JwtUtilsProxy.generateToken(user.getId(), user.getUsername());
        String refreshToken = JwtUtilsProxy.generateRefreshToken(user.getId(), user.getUsername());

        // 记录登录日志（包含更新用户在线状态、登录时间、登录IP）
        userOnlineService.recordLogin(user.getId(), request);

        // 组装返回数据
        LoginVO loginVO = new LoginVO();
        loginVO.setUserId(user.getId());
        loginVO.setUsername(user.getUsername());
        loginVO.setNickname(user.getNickname());
        loginVO.setAvatar(user.getAvatar());
        loginVO.setToken(token);
        loginVO.setRefreshToken(refreshToken);

        return JsonResult.success("登录成功", loginVO);
    }

    @ApiOperation("获取用户信息")
    @GetMapping("/info")
    public JsonResult<SysUser> info(@RequestParam String token) {
        return JsonResult.success(new SysUser());
    }

    @ApiOperation("退出登录")
    @PostMapping("/logout")
    public JsonResult<Void> logout(@RequestParam(required = false) Long userId, HttpServletRequest request) {
        // 1. 将当前Token加入黑名单(防止Token继续被使用)
        String token = request.getHeader("Authorization");
        if (StringUtils.hasText(token) && token.startsWith("Bearer ")) {
            String actualToken = token.substring(7);
            // Token剩余有效期作为黑名单过期时间
            Long expireSeconds = JwtUtilsProxy.getExpireTime(actualToken);
            if (expireSeconds != null && expireSeconds > 0) {
                RedisUtils.set(RedisKeyConstants.TOKEN_BLACKLIST + actualToken, "1", expireSeconds, TimeUnit.SECONDS);
            }
        }
        
        // 2. 记录登出日志（包含更新用户在线状态、累计在线时长）
        if (userId != null) {
            userOnlineService.recordLogout(userId);
        }
        
        return JsonResult.success("退出成功");
    }
    
    @ApiOperation("登录日志查询")
    @GetMapping("/login-logs")
    public JsonResult<IPage<Map<String, Object>>> loginLogs(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String username,
            HttpServletRequest request) {
        
        // 获取当前登录用户ID（从请求头获取）
        String currentUserIdStr = request.getHeader("X-User-Id");
        Long currentUserId = currentUserIdStr != null ? Long.parseLong(currentUserIdStr) : null;
        
        // 获取当前用户角色信息
        boolean isSuperAdmin = false;
        boolean isAdmin = false;
        boolean isTeacher = false;
        List<Long> manageableUserIds = new ArrayList<>();
        
        if (currentUserId != null) {
            SysUser currentUser = userService.getById(currentUserId);
            if (currentUser != null) {
                // 检查用户角色
                List<UserRoleVO> roles = userService.getUserRoles(currentUserId);
                for (UserRoleVO role : roles) {
                    String roleCode = role.getRoleCode();
                    if (roleCode != null) {
                        String code = roleCode.toLowerCase();
                        if (RoleCodeConstants.SUPER_ADMIN.equals(code)) {
                            isSuperAdmin = true;
                        } else if (code.contains(RoleCodeConstants.ADMIN)) {
                            isAdmin = true;
                        } else if (RoleCodeConstants.TEACHER.equals(code)) {
                            isTeacher = true;
                        }
                    }
                }
                
                // 获取可管理的用户ID列表
                if (!isSuperAdmin) {
                    manageableUserIds = userService.getManageableUserIds(currentUserId, isTeacher);
                }
            }
        }
        
        // 构建查询条件
        Page<UserOnlineLog> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<UserOnlineLog> wrapper = new LambdaQueryWrapper<>();
        
        // 根据权限过滤
        if (!isSuperAdmin) {
            if (manageableUserIds.isEmpty()) {
                // 没有可管理用户，只能看自己
                wrapper.eq(UserOnlineLog::getUserId, currentUserId);
            } else {
                // 可以看自己管理的用户 + 自己
                manageableUserIds.add(currentUserId);
                wrapper.in(UserOnlineLog::getUserId, manageableUserIds);
            }
        }
        
        // 指定用户过滤
        if (userId != null) {
            wrapper.eq(UserOnlineLog::getUserId, userId);
        }
        
        // 按登录时间倒序
        wrapper.orderByDesc(UserOnlineLog::getLoginTime);
        
        IPage<UserOnlineLog> logPage = userOnlineLogMapper.selectPage(pageParam, wrapper);
        
        // 转换为带用户信息的返回结果
        IPage<Map<String, Object>> resultPage = logPage.convert(log -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", log.getId());
            map.put("userId", log.getUserId());
            map.put("loginTime", log.getLoginTime());
            map.put("logoutTime", log.getLogoutTime());
            map.put("loginIp", log.getLoginIp());
            map.put("userAgent", log.getUserAgent());
            map.put("duration", log.getDuration());
            
            // 获取用户信息
            SysUser logUser = userService.getById(log.getUserId());
            if (logUser != null) {
                map.put("username", logUser.getUsername());
                map.put("nickname", logUser.getNickname());
            }
            return map;
        });
        
        return JsonResult.success(resultPage);
    }

    @ApiOperation("生成密码哈希（仅开发调试用）")
    @GetMapping("/encode")
    public JsonResult<String> encode(@RequestParam String password) {
        return JsonResult.success(SecurityUtils.encryptPassword(password));
    }
}
