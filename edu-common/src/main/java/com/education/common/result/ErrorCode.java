package com.education.common.result;

/**
 * 错误码枚举类
 * 
 * 错误码规范:
 * - 1xxx: 用户相关
 * - 2xxx: 课程相关
 * - 3xxx: 考试相关
 * - 4xxx: 系统相关
 * - 5xxx: 文件相关
 * - 9xxx: 通用错误
 * 
 * @author system
 * @since 2026-04-09
 */
public enum ErrorCode {

    // ========== 通用错误码 9xxx ==========
    SUCCESS(200, "操作成功"),
    FAIL(500, "操作失败"),
    UNAUTHORIZED(401, "未认证"),
    FORBIDDEN(403, "权限不足"),
    NOT_FOUND(404, "资源不存在"),
    PARAM_ERROR(400, "参数错误"),
    REPEAT_SUBMIT(429, "重复提交"),
    RATE_LIMIT(429, "请求过于频繁"),
    SYSTEM_ERROR(9999, "系统异常"),

    // ========== 用户相关 1xxx ==========
    USER_NOT_EXIST(1001, "用户不存在"),
    USER_DISABLED(1002, "用户已被禁用"),
    USER_PASSWORD_ERROR(1003, "密码错误"),
    USER_LOGIN_FAIL_LIMIT(1004, "登录失败次数过多,请稍后再试"),
    USER_TOKEN_EXPIRED(1005, "登录已过期,请重新登录"),
    USER_TOKEN_INVALID(1006, "无效token"),
    USER_PHONE_EXIST(1007, "手机号已注册"),
    USER_EMAIL_EXIST(1008, "邮箱已注册"),
    USER_OLD_PASSWORD_ERROR(1009, "原密码错误"),
    USER_LOCKED(1010, "账户已被锁定"),

    // ========== 课程相关 2xxx ==========
    COURSE_NOT_EXIST(2001, "课程不存在"),
    COURSE_DISABLED(2002, "课程已下架"),
    CATEGORY_NOT_EXIST(2003, "分类不存在"),
    CATEGORY_HAS_CHILDREN(2004, "分类下存在子分类,无法删除"),
    CATEGORY_HAS_COURSES(2005, "分类下存在课程,无法删除"),
    CHAPTER_NOT_EXIST(2006, "章节不存在"),
    COURSE_ALREADY_PUBLISHED(2007, "课程已发布"),
    COURSE_NOT_PUBLISHED(2008, "课程未发布"),

    // ========== 考试相关 3xxx ==========
    EXAM_NOT_EXIST(3001, "考试不存在"),
    EXAM_NOT_STARTED(3002, "考试未开始"),
    EXAM_ENDED(3003, "考试已结束"),
    EXAM_ALREADY_SUBMIT(3004, "试卷已提交"),
    EXAM_TIME_EXPIRED(3005, "考试时间已到"),
    PAPER_NOT_EXIST(3006, "试卷不存在"),
    QUESTION_NOT_EXIST(3007, "题目不存在"),
    SUBJECT_NOT_EXIST(3008, "学科不存在"),
    EXAM_ATTEMPT_LIMIT(3009, "考试次数已用完"),
    EXAM_CHEAT_DETECTED(3010, "检测到切屏行为"),

    // ========== 系统相关 4xxx ==========
    ROLE_NOT_EXIST(4001, "角色不存在"),
    ROLE_HAS_USERS(4002, "角色下存在用户,无法删除"),
    MENU_NOT_EXIST(4003, "菜单不存在"),
    MENU_HAS_CHILDREN(4004, "菜单下存在子菜单,无法删除"),
    CONFIG_NOT_EXIST(4005, "配置不存在"),
    CONFIG_SYSTEM_PROTECTED(4006, "系统配置不可删除"),
    DEPT_NOT_EXIST(4007, "部门不存在"),
    DEPT_HAS_CHILDREN(4008, "部门下存在子部门,无法删除"),
    DEPT_HAS_USERS(4009, "部门下存在用户,无法删除"),

    // ========== 文件相关 5xxx ==========
    FILE_UPLOAD_FAIL(5001, "文件上传失败"),
    FILE_TYPE_NOT_ALLOWED(5002, "不支持的文件类型"),
    FILE_SIZE_EXCEED(5003, "文件大小超过限制"),
    FILE_NOT_EXIST(5004, "文件不存在"),
    FILE_DOWNLOAD_FAIL(5005, "文件下载失败"),
    IMPORT_DATA_ERROR(5006, "导入数据格式错误"),
    IMPORT_FAIL(5007, "导入失败"),
    EXPORT_FAIL(5008, "导出失败");

    private final Integer code;
    private final String message;

    ErrorCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
