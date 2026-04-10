/*
 Navicat Premium Data Transfer

 Source Server         : localhost
 Source Server Type    : MySQL
 Source Server Version : 50513
 Source Host           : localhost:3306
 Source Schema         : edu_training

 Target Server Type    : MySQL
 Target Server Version : 50513
 File Encoding         : 65001

 Date: 10/04/2026 15:21:07
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for ai_crawl_config
-- ----------------------------
DROP TABLE IF EXISTS `ai_crawl_config`;
CREATE TABLE `ai_crawl_config`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  `config_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '配置键',
  `config_value` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '配置值',
  `config_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'string' COMMENT '配置类型: string/number/json',
  `description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '配置说明',
  `status` tinyint(4) NULL DEFAULT 1 COMMENT '状态:0-禁用 1-启用',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_config_key`(`config_key`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 15 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '抓取配置表' ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for ai_crawl_error_log
-- ----------------------------
DROP TABLE IF EXISTS `ai_crawl_error_log`;
CREATE TABLE `ai_crawl_error_log`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `task_id` bigint(20) NULL DEFAULT NULL COMMENT '关联任务ID',
  `error_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '错误类型: timeout-超时, network-网络异常, parse-解析失败, rate_limit-频率限制, other-其他',
  `error_level` tinyint(4) NULL DEFAULT 1 COMMENT '错误级别:1-警告 2-错误 3-严重',
  `error_msg` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '错误信息',
  `stack_trace` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '堆栈信息',
  `request_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '请求URL',
  `response_code` int(11) NULL DEFAULT NULL COMMENT 'HTTP响应码',
  `retry_count` int(11) NULL DEFAULT 0 COMMENT '已重试次数',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_task_id`(`task_id`) USING BTREE,
  INDEX `idx_error_type`(`error_type`) USING BTREE,
  INDEX `idx_create_time`(`create_time`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '抓取异常日志表' ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for ai_parse_rule
-- ----------------------------
DROP TABLE IF EXISTS `ai_parse_rule`;
CREATE TABLE `ai_parse_rule`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '规则ID',
  `template_id` bigint(20) NOT NULL COMMENT '关联模板ID',
  `rule_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '规则类型: title-标题 content-内容 answer-答案 question-题目 option-选项',
  `rule_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '规则名称',
  `selector_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '选择器类型: css-选择器 regex-正则 xpath-XPath',
  `selector_value` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '选择器值/正则表达式',
  `extract_attr` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '提取属性(href/text/html/src)',
  `clean_rule` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '清洗规则(JSON格式)',
  `required` tinyint(4) NULL DEFAULT 1 COMMENT '是否必填: 0-否 1-是',
  `sort_order` int(11) NULL DEFAULT 0 COMMENT '排序',
  `enabled` tinyint(4) NULL DEFAULT 1 COMMENT '是否启用',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_template_id`(`template_id`) USING BTREE,
  INDEX `idx_rule_type`(`rule_type`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '解析规则表' ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for ai_parse_template
-- ----------------------------
DROP TABLE IF EXISTS `ai_parse_template`;
CREATE TABLE `ai_parse_template`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '模板ID',
  `template_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '模板名称',
  `site_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '站点名称',
  `site_domain` varchar(150) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '站点域名',
  `list_selector` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '列表页选择器(CSS)',
  `detail_selector` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '详情页选择器(CSS)',
  `title_selector` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '标题选择器',
  `content_selector` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '内容选择器',
  `answer_selector` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '答案选择器',
  `meta_selector` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '元信息选择器(时间/作者)',
  `pagination_rule` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '分页规则',
  `encoding` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'UTF-8' COMMENT '页面编码',
  `enabled` tinyint(4) NULL DEFAULT 1 COMMENT '是否启用: 0-禁用 1-启用',
  `priority` int(11) NULL DEFAULT 0 COMMENT '优先级(数值越大越优先)',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注说明',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_site_domain`(`site_domain`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '站点解析模板表' ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for ai_resource
-- ----------------------------
DROP TABLE IF EXISTS `ai_resource`;
CREATE TABLE `ai_resource`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '资源ID',
  `task_id` bigint(20) NOT NULL COMMENT '关联任务ID',
  `resource_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '资源类型: exam_paper-试卷, article-文章, journal-外刊',
  `subject` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '学科',
  `grade` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '年级',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '资源标题',
  `summary` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '资源摘要',
  `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '资源内容(JSON格式存储结构化数据)',
  `raw_content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '原始内容(HTML/文本)',
  `file_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '文件URL(如PDF)',
  `image_urls` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '图片URL列表(JSON数组)',
  `difficulty` tinyint(4) NULL DEFAULT NULL COMMENT '难度:1-简单 2-中等 3-困难',
  `exam_year` int(11) NULL DEFAULT NULL COMMENT '考试年份(如为试卷)',
  `exam_region` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '考试地区(如为试卷)',
  `word_count` int(11) NULL DEFAULT 0 COMMENT '字数统计',
  `quality_score` decimal(3, 2) NULL DEFAULT 0.00 COMMENT '质量评分(0-5)',
  `tags` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '标签(逗号分隔)',
  `source_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '来源URL',
  `status` tinyint(4) NULL DEFAULT 0 COMMENT '状态:0-待审核 1-已通过 2-已拒绝 3-已入库',
  `review_comment` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '审核意见',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NULL DEFAULT NULL COMMENT '更新时间',
  `dr` tinyint(4) NULL DEFAULT 0 COMMENT '逻辑删除:0-正常 1-删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_task_id`(`task_id`) USING BTREE,
  INDEX `idx_resource_type`(`resource_type`, `subject`, `grade`) USING BTREE,
  INDEX `idx_status`(`status`, `dr`) USING BTREE,
  INDEX `idx_exam_year`(`exam_year`, `exam_region`) USING BTREE,
  FULLTEXT INDEX `ft_title_content`(`title`, `content`) COMMENT '全文检索索引'
) ENGINE = MyISAM AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'AI资源表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for ai_resource_task
-- ----------------------------
DROP TABLE IF EXISTS `ai_resource_task`;
CREATE TABLE `ai_resource_task`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '任务ID',
  `task_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '任务名称',
  `task_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '任务类型: exam_paper-试卷, article-文章, journal-外刊, news-新闻',
  `subject` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '学科(英语/数学/语文等)',
  `grade` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '年级',
  `source_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '资源来源URL',
  `source_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '来源网站名称',
  `crawl_status` tinyint(4) NULL DEFAULT 0 COMMENT '抓取状态:0-待执行 1-执行中 2-成功 3-失败 4-超时',
  `parse_status` tinyint(4) NULL DEFAULT 0 COMMENT '解析状态:0-未解析 1-解析中 2-成功 3-失败',
  `import_status` tinyint(4) NULL DEFAULT 0 COMMENT '导入状态:0-未导入 1-导入中 2-成功 3-失败',
  `retry_count` int(11) NULL DEFAULT 0 COMMENT '重试次数',
  `max_retry` int(11) NULL DEFAULT 3 COMMENT '最大重试次数',
  `timeout_seconds` int(11) NULL DEFAULT 300 COMMENT '超时时间(秒),默认5分钟',
  `error_msg` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '错误信息',
  `start_time` datetime NULL DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime NULL DEFAULT NULL COMMENT '结束时间',
  `next_retry_time` datetime NULL DEFAULT NULL COMMENT '下次重试时间',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NULL DEFAULT NULL COMMENT '更新时间',
  `dr` tinyint(4) NULL DEFAULT 0 COMMENT '逻辑删除:0-正常 1-删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_task_type_status`(`task_type`, `crawl_status`, `dr`) USING BTREE,
  INDEX `idx_subject_grade`(`subject`, `grade`) USING BTREE,
  INDEX `idx_next_retry`(`next_retry_time`, `crawl_status`) USING BTREE,
  INDEX `idx_create_time`(`create_time`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'AI资源抓取任务表' ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for db_optimization_log
-- ----------------------------
DROP TABLE IF EXISTS `db_optimization_log`;
CREATE TABLE `db_optimization_log`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `version` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '优化版本号',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '优化说明',
  `executed_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '执行人',
  `executed_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '执行时间',
  `status` tinyint(4) NULL DEFAULT 1 COMMENT '状态:0-回滚 1-已执行',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_version`(`version`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '数据库优化记录表' ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for edu_category
-- ----------------------------
DROP TABLE IF EXISTS `edu_category`;
CREATE TABLE `edu_category`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `parent_id` bigint(20) NULL DEFAULT 0 COMMENT '父分类ID（0为顶级）',
  `category_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '分类名称',
  `category_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '分类编码',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '分类描述',
  `sort_order` int(11) NULL DEFAULT 0 COMMENT '排序',
  `status` tinyint(4) NULL DEFAULT 1 COMMENT '状态：0禁用 1启用',
  `dr` tinyint(4) NULL DEFAULT 0 COMMENT '逻辑删除：0-正常 1-删除',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_category_code`(`category_code`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '课程分类表' ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for edu_chapter
-- ----------------------------
DROP TABLE IF EXISTS `edu_chapter`;
CREATE TABLE `edu_chapter`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '章节ID',
  `course_id` bigint(20) NOT NULL COMMENT '所属课程ID',
  `outline_id` bigint(20) NULL DEFAULT NULL COMMENT '大纲ID（可为空）',
  `chapter_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '章节名称',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '章节描述',
  `video_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '视频地址',
  `video_duration` int(11) NULL DEFAULT 0 COMMENT '视频时长（秒）',
  `sort_order` int(11) NULL DEFAULT 0 COMMENT '排序',
  `is_free` tinyint(4) NULL DEFAULT 0 COMMENT '是否免费：0否 1是',
  `status` tinyint(4) NULL DEFAULT 1 COMMENT '状态：0禁用 1启用',
  `dr` tinyint(4) NULL DEFAULT 0 COMMENT '逻辑删除：0-正常 1-删除',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_chapter_course`(`course_id`) USING BTREE,
  INDEX `idx_course_dr`(`course_id`, `dr`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 21 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '课程章节表' ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for edu_chapter_progress
-- ----------------------------
DROP TABLE IF EXISTS `edu_chapter_progress`;
CREATE TABLE `edu_chapter_progress`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `course_id` bigint(20) NOT NULL COMMENT '课程ID',
  `chapter_id` bigint(20) NOT NULL COMMENT '章节ID',
  `student_id` bigint(20) NOT NULL COMMENT '学员ID',
  `watch_duration` int(11) NULL DEFAULT 0 COMMENT '已观看时长(秒)',
  `total_duration` int(11) NULL DEFAULT 0 COMMENT '总时长(秒)',
  `is_completed` tinyint(4) NULL DEFAULT 0 COMMENT '是否完成:0-否 1-是',
  `last_watch_time` datetime NULL DEFAULT NULL COMMENT '最后观看时间',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_chapter_student`(`chapter_id`, `student_id`) USING BTREE,
  INDEX `idx_student_course`(`student_id`, `course_id`) USING BTREE,
  INDEX `idx_course_progress`(`course_id`, `student_id`, `is_completed`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '章节学习进度表' ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for edu_course
-- ----------------------------
DROP TABLE IF EXISTS `edu_course`;
CREATE TABLE `edu_course`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '课程ID',
  `course_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '课程名称',
  `course_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '课程编码',
  `category_id` bigint(20) NULL DEFAULT NULL COMMENT '分类ID',
  `outline_id` bigint(20) NULL DEFAULT NULL COMMENT '大纲ID',
  `teacher_id` bigint(20) NULL DEFAULT NULL COMMENT '讲师ID（关联sys_user）',
  `cover_image` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '课程封面图（URL或Base64）',
  `description` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '课程简介',
  `learning_objectives` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '学习目标',
  `detail` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '课程详情（富文本）',
  `difficulty` tinyint(4) NULL DEFAULT 1 COMMENT '难度：1初级 2中级 3高级',
  `course_hours` int(11) NULL DEFAULT 0 COMMENT '课时数',
  `price` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '课程价格',
  `view_count` bigint(20) NULL DEFAULT 0 COMMENT '浏览次数',
  `learn_count` bigint(20) NULL DEFAULT 0 COMMENT '学习人数',
  `rating` decimal(3, 1) NULL DEFAULT 0.0 COMMENT '平均评分(1-5星)',
  `tags` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '课程标签(逗号分隔)',
  `prerequisites` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '前置课程要求',
  `target_audience` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '目标受众',
  `status` tinyint(4) NULL DEFAULT 0 COMMENT '状态：0草稿 1已发布 2已下架',
  `dr` tinyint(4) NULL DEFAULT 0 COMMENT '逻辑删除：0-正常 1-删除',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_course_code`(`course_code`) USING BTREE,
  INDEX `idx_course_category`(`category_id`) USING BTREE,
  INDEX `idx_course_teacher`(`teacher_id`) USING BTREE,
  INDEX `idx_course_status`(`status`) USING BTREE,
  INDEX `idx_category_status`(`category_id`, `status`, `dr`) USING BTREE,
  INDEX `idx_teacher_status`(`teacher_id`, `status`, `dr`) USING BTREE,
  INDEX `idx_course_code`(`course_code`) USING BTREE,
  INDEX `idx_status_dr`(`status`, `dr`) USING BTREE,
  INDEX `idx_category_dr`(`category_id`, `dr`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 21 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '课程信息表' ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for edu_course_student
-- ----------------------------
DROP TABLE IF EXISTS `edu_course_student`;
CREATE TABLE `edu_course_student`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `course_id` bigint(20) NOT NULL COMMENT '课程ID',
  `student_id` bigint(20) NOT NULL COMMENT '学员ID',
  `enroll_time` datetime NOT NULL COMMENT '报名时间',
  `start_time` datetime NULL DEFAULT NULL COMMENT '开始学习时间',
  `finish_time` datetime NULL DEFAULT NULL COMMENT '完成时间',
  `progress` decimal(5, 2) NULL DEFAULT 0.00 COMMENT '学习进度(0-100)',
  `status` tinyint(4) NULL DEFAULT 1 COMMENT '状态:1-学习中 2-已完成 3-已过期',
  `expire_time` datetime NULL DEFAULT NULL COMMENT '课程过期时间',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_course_student`(`course_id`, `student_id`) USING BTREE,
  INDEX `idx_student_status`(`student_id`, `status`) USING BTREE,
  INDEX `idx_enroll_time`(`enroll_time`) USING BTREE,
  INDEX `idx_status_expire`(`status`, `expire_time`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '课程学员关联表' ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for edu_lesson
-- ----------------------------
DROP TABLE IF EXISTS `edu_lesson`;
CREATE TABLE `edu_lesson`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `course_id` bigint(20) NOT NULL COMMENT '课程ID',
  `course_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '课程名称（冗余）',
  `teacher_id` bigint(20) NOT NULL COMMENT '授课教师用户ID',
  `teacher_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '教师姓名（冗余）',
  `location` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '上课地点（地址，用于通勤估算）',
  `lesson_date` date NOT NULL COMMENT '上课日期',
  `start_time` time NOT NULL COMMENT '上课开始时间',
  `end_time` time NOT NULL COMMENT '上课结束时间',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态：0取消 1正常',
  `notified` tinyint(4) NOT NULL DEFAULT 0 COMMENT '是否已发送提醒邮件：0否 1是',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_lesson_date`(`lesson_date`) USING BTREE,
  INDEX `idx_teacher_id`(`teacher_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '课程排课表' ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for edu_lesson_student
-- ----------------------------
DROP TABLE IF EXISTS `edu_lesson_student`;
CREATE TABLE `edu_lesson_student`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `lesson_id` bigint(20) NOT NULL COMMENT '排课ID',
  `student_id` bigint(20) NOT NULL COMMENT '学生用户ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_lesson_student`(`lesson_id`, `student_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '排课-学生关联表' ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for exam_answer_template
-- ----------------------------
DROP TABLE IF EXISTS `exam_answer_template`;
CREATE TABLE `exam_answer_template`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `template_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '模板名称',
  `paper_type` tinyint(4) NULL DEFAULT 1 COMMENT '试卷类型:1-单元测试 2-期中 3-期末 4-模拟考 5-真题',
  `template_config` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '模板配置(JSON格式)',
  `status` tinyint(4) NULL DEFAULT 1 COMMENT '状态:0-禁用 1-启用',
  `dr` tinyint(4) NULL DEFAULT 0 COMMENT '逻辑删除:0-正常 1-删除',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_paper_type`(`paper_type`, `status`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '考试答题卡模板表' ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for exam_paper
-- ----------------------------
DROP TABLE IF EXISTS `exam_paper`;
CREATE TABLE `exam_paper`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '试卷ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '试卷名称',
  `grade_id` bigint(20) NOT NULL COMMENT '年级ID',
  `subject_id` bigint(20) NULL DEFAULT NULL COMMENT '学科ID（综合试卷为空）',
  `paper_type` tinyint(4) NULL DEFAULT 1 COMMENT '试卷类型：1单元测试 2期中 3期末 4模拟考 5真题',
  `total_score` int(11) NULL DEFAULT 100 COMMENT '总分',
  `pass_score` int(11) NULL DEFAULT 60 COMMENT '及格分',
  `duration` int(11) NULL DEFAULT 120 COMMENT '考试时长（分钟）',
  `exam_start_time` datetime NULL DEFAULT NULL COMMENT '考试开始时间',
  `exam_end_time` datetime NULL DEFAULT NULL COMMENT '考试结束时间',
  `max_attempts` int(11) NULL DEFAULT 1 COMMENT '最大考试次数',
  `anti_cheat` tinyint(4) NULL DEFAULT 0 COMMENT '防作弊:0-关闭 1-开启',
  `question_count` int(11) NULL DEFAULT 0 COMMENT '题目数量',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '试卷描述',
  `status` tinyint(4) NULL DEFAULT 0 COMMENT '状态：0草稿 1已发布 2已归档',
  `create_by` bigint(20) NULL DEFAULT NULL COMMENT '创建人',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NULL DEFAULT NULL COMMENT '更新时间',
  `dr` tinyint(4) NULL DEFAULT 0 COMMENT '逻辑删除:0-正常 1-删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `create_by`(`create_by`) USING BTREE,
  INDEX `idx_paper_grade`(`grade_id`) USING BTREE,
  INDEX `idx_paper_subject`(`subject_id`) USING BTREE,
  INDEX `idx_subject_dr`(`subject_id`, `dr`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '试卷表' ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for exam_paper_question
-- ----------------------------
DROP TABLE IF EXISTS `exam_paper_question`;
CREATE TABLE `exam_paper_question`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '关联ID',
  `paper_id` bigint(20) NOT NULL COMMENT '试卷ID',
  `question_id` bigint(20) NOT NULL COMMENT '题目ID',
  `question_order` int(11) NULL DEFAULT 0 COMMENT '题目顺序',
  `score` int(11) NULL DEFAULT 10 COMMENT '本题分值',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_paper_question`(`paper_id`, `question_id`) USING BTREE,
  INDEX `question_id`(`question_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '试卷题目关联表' ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for exam_question
-- ----------------------------
DROP TABLE IF EXISTS `exam_question`;
CREATE TABLE `exam_question`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '题目ID',
  `subject_id` bigint(20) NULL DEFAULT NULL COMMENT '学科ID（关联exam_subject）',
  `grade_id` bigint(20) NULL DEFAULT NULL COMMENT '年级ID(关联grade表)',
  `grade` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '所属年级',
  `exam_type` tinyint(4) NULL DEFAULT 1 COMMENT '考试类型:1-单元测试 2-期中 3-期末 4-模拟考 5-真题 6-课后练习',
  `question_type` int(11) NOT NULL DEFAULT 1 COMMENT '题型：1单选 2多选 3判断 4填空 5简答',
  `question_title` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '题目内容',
  `images` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '题目图片URL列表(JSON数组格式),如:[\"url1\",\"url2\"]',
  `image_count` int(11) NULL DEFAULT 0 COMMENT '图片数量',
  `image_descriptions` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '图片描述信息(JSON数组),与images对应,如:[{\"url\":\"...\",\"description\":\"...\"}]',
  `options` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '选项JSON(支持图片):[{\"label\":\"A\",\"content\":\"...\",\"imageUrl\":\"...\"}]',
  `correct_answer` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '正确答案',
  `analysis` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '答案解析(支持图片URL)',
  `difficulty` tinyint(4) NULL DEFAULT 2 COMMENT '难度：1简单 2中等 3困难',
  `score` decimal(5, 1) NULL DEFAULT 10.0 COMMENT '默认分值',
  `knowledge_point` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '知识点',
  `tags` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '知识点标签(逗号分隔)',
  `source_type` tinyint(4) NULL DEFAULT 1 COMMENT '题目来源:1-手动录入 2-批量导入 3-OCR识别',
  `usage_count` int(11) NULL DEFAULT 0 COMMENT '使用次数',
  `status` tinyint(4) NULL DEFAULT 1 COMMENT '状态：0禁用 1启用',
  `dr` tinyint(4) NULL DEFAULT 0 COMMENT '逻辑删除：0-正常 1-删除',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_eq_subject`(`subject_id`) USING BTREE,
  INDEX `idx_eq_grade`(`grade`) USING BTREE,
  INDEX `idx_eq_type`(`question_type`) USING BTREE,
  INDEX `idx_eq_difficulty`(`difficulty`) USING BTREE,
  INDEX `idx_eq_status`(`status`) USING BTREE,
  INDEX `idx_knowledge_point`(`knowledge_point`(100)) USING BTREE,
  INDEX `idx_usage_count`(`usage_count`) USING BTREE,
  INDEX `idx_grade_id`(`grade_id`) USING BTREE,
  INDEX `idx_subject_type_dr`(`subject_id`, `question_type`, `dr`) USING BTREE,
  INDEX `idx_difficulty_dr`(`difficulty`, `dr`) USING BTREE,
  INDEX `idx_grade_dr`(`grade_id`, `dr`) USING BTREE,
  INDEX `idx_subject_grade_type`(`subject_id`, `grade_id`, `question_type`, `status`, `dr`) USING BTREE,
  INDEX `idx_tags`(`tags`(100)) USING BTREE,
  INDEX `idx_exam_type`(`exam_type`) USING BTREE,
  INDEX `idx_subject_type_difficulty`(`subject_id`, `question_type`, `exam_type`, `difficulty`, `status`, `dr`) USING BTREE,
  INDEX `idx_subject_exam_type`(`subject_id`, `exam_type`, `question_type`, `status`, `dr`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '题库表' ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for exam_record
-- ----------------------------
DROP TABLE IF EXISTS `exam_record`;
CREATE TABLE `exam_record`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `paper_id` bigint(20) NOT NULL COMMENT '试卷ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `start_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '开始时间',
  `end_time` timestamp NULL DEFAULT NULL COMMENT '结束时间',
  `duration` int(11) NULL DEFAULT 0 COMMENT '用时（分钟）',
  `total_score` int(11) NULL DEFAULT 0 COMMENT '总分',
  `score` int(11) NULL DEFAULT 0 COMMENT '得分',
  `correct_count` int(11) NULL DEFAULT 0 COMMENT '正确题数',
  `wrong_count` int(11) NULL DEFAULT 0 COMMENT '错误题数',
  `status` tinyint(4) NULL DEFAULT 0 COMMENT '状态：0进行中 1已完成 2已批改',
  `ip_address` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'IP地址',
  `browser_info` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '浏览器信息',
  `screen_switch_count` int(11) NULL DEFAULT 0 COMMENT '切屏次数',
  `warning_count` int(11) NULL DEFAULT 0 COMMENT '警告次数',
  `dr` tinyint(4) NULL DEFAULT 0 COMMENT '逻辑删除:0-正常 1-删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_record_user`(`user_id`) USING BTREE,
  INDEX `idx_record_paper`(`paper_id`) USING BTREE,
  INDEX `idx_user_start_time`(`user_id`, `start_time`) USING BTREE,
  INDEX `idx_paper_status`(`paper_id`, `status`) USING BTREE,
  INDEX `idx_start_time`(`start_time`) USING BTREE,
  INDEX `idx_user_paper_dr`(`user_id`, `paper_id`, `dr`) USING BTREE,
  INDEX `idx_status_dr`(`status`, `dr`) USING BTREE,
  INDEX `idx_exam_time`(`start_time`, `end_time`) USING BTREE,
  INDEX `idx_screen_switch`(`screen_switch_count`) USING BTREE,
  INDEX `idx_warning`(`warning_count`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '考试记录表' ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for exam_record_detail
-- ----------------------------
DROP TABLE IF EXISTS `exam_record_detail`;
CREATE TABLE `exam_record_detail`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '详情ID',
  `record_id` bigint(20) NOT NULL COMMENT '记录ID',
  `question_id` bigint(20) NOT NULL COMMENT '题目ID',
  `user_answer` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '用户答案',
  `is_correct` tinyint(4) NULL DEFAULT 0 COMMENT '是否正确：0否 1是',
  `score` int(11) NULL DEFAULT 0 COMMENT '得分',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `question_id`(`question_id`) USING BTREE,
  INDEX `idx_detail_record`(`record_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '考试答题详情表' ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for exam_subject
-- ----------------------------
DROP TABLE IF EXISTS `exam_subject`;
CREATE TABLE `exam_subject`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '学科ID',
  `subject_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '学科名称',
  `subject_code` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '学科编码',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '学科描述',
  `grade` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '所属年级（如：六年级、初一、高三）',
  `icon` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '?' COMMENT '学科图标（emoji）',
  `color` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '#1890ff' COMMENT '学科颜色（十六进制）',
  `sort_order` int(11) NULL DEFAULT 0 COMMENT '排序',
  `status` tinyint(4) NULL DEFAULT 1 COMMENT '状态：0禁用 1启用',
  `dr` tinyint(4) NULL DEFAULT 0 COMMENT '逻辑删除：0-正常 1-删除',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_subject_grade`(`grade`) USING BTREE,
  INDEX `idx_subject_status`(`status`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 53 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '学科表（项目业务用）' ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for exam_wrong_question
-- ----------------------------
DROP TABLE IF EXISTS `exam_wrong_question`;
CREATE TABLE `exam_wrong_question`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `student_id` bigint(20) NOT NULL COMMENT '学员ID',
  `question_id` bigint(20) NOT NULL COMMENT '题目ID',
  `paper_id` bigint(20) NULL DEFAULT NULL COMMENT '试卷ID',
  `wrong_count` int(11) NULL DEFAULT 1 COMMENT '错误次数',
  `last_wrong_time` datetime NULL DEFAULT NULL COMMENT '最后错误时间',
  `is_mastered` tinyint(4) NULL DEFAULT 0 COMMENT '是否已掌握:0-否 1-是',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_student_question`(`student_id`, `question_id`) USING BTREE,
  INDEX `idx_student_mastered`(`student_id`, `is_mastered`) USING BTREE,
  INDEX `idx_last_wrong_time`(`last_wrong_time`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '错题本表' ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for sys_activity
-- ----------------------------
DROP TABLE IF EXISTS `sys_activity`;
CREATE TABLE `sys_activity`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '活动ID',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '活动标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '活动内容',
  `activity_type` tinyint(4) NULL DEFAULT 1 COMMENT '活动类型：1系统公告 2课程相关 3考试相关 4用户相关',
  `icon` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '?' COMMENT '图标',
  `color` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '#667eea' COMMENT '颜色',
  `status` tinyint(4) NULL DEFAULT 0 COMMENT '状态：0草稿 1已发布 2已结束',
  `is_top` tinyint(4) NULL DEFAULT 0 COMMENT '是否置顶：0否 1是',
  `sort` int(11) NULL DEFAULT 0 COMMENT '排序',
  `start_time` timestamp NULL DEFAULT NULL COMMENT '开始时间',
  `end_time` timestamp NULL DEFAULT NULL COMMENT '结束时间',
  `publish_time` timestamp NULL DEFAULT NULL COMMENT '发布时间',
  `create_by` bigint(20) NULL DEFAULT NULL COMMENT '创建人ID',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_activity_status`(`status`) USING BTREE,
  INDEX `idx_activity_type`(`activity_type`) USING BTREE,
  INDEX `idx_activity_top`(`is_top`) USING BTREE,
  INDEX `idx_activity_sort`(`sort`) USING BTREE,
  INDEX `idx_activity_time`(`create_time`) USING BTREE,
  INDEX `idx_type_status_top`(`activity_type`, `status`, `is_top`, `sort`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '活动管理表' ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for sys_activity_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_activity_role`;
CREATE TABLE `sys_activity_role`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `activity_id` bigint(20) NOT NULL COMMENT '活动ID',
  `role_id` bigint(20) NOT NULL COMMENT '角色ID',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_activity_role`(`activity_id`, `role_id`) USING BTREE,
  INDEX `role_id`(`role_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 12 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '活动角色推送关联表' ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for sys_button
-- ----------------------------
DROP TABLE IF EXISTS `sys_button`;
CREATE TABLE `sys_button`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '按钮ID',
  `button_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '按钮编码（唯一，如：user:add）',
  `button_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '按钮名称（如：新增用户）',
  `permission` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '权限标识（如：user:add）',
  `description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '描述',
  `status` tinyint(4) NULL DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_button_code`(`button_code`) USING BTREE,
  UNIQUE INDEX `uk_permission`(`permission`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 26 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '按钮配置表' ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for sys_config
-- ----------------------------
DROP TABLE IF EXISTS `sys_config`;
CREATE TABLE `sys_config`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  `category_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '配置分类编码',
  `config_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '配置键',
  `config_value` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '配置值',
  `config_type` tinyint(4) NULL DEFAULT 1 COMMENT '配置类型:1文本 2数字 3布尔 4JSON',
  `description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '描述',
  `is_system` tinyint(4) NULL DEFAULT 0 COMMENT '是否系统配置:0否 1是(系统配置不可删除)',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_config_key`(`config_key`) USING BTREE,
  INDEX `idx_config_category`(`category_code`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '系统配置表' ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for sys_config_category
-- ----------------------------
DROP TABLE IF EXISTS `sys_config_category`;
CREATE TABLE `sys_config_category`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '分类编码（唯一）',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '分类名称',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '分类描述',
  `icon` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '图标',
  `sort` int(11) NULL DEFAULT 0 COMMENT '排序',
  `status` tinyint(4) NULL DEFAULT 1 COMMENT '状态：0禁用 1启用',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_config_category_code`(`code`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 12 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '配置分类表' ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for sys_config_item
-- ----------------------------
DROP TABLE IF EXISTS `sys_config_item`;
CREATE TABLE `sys_config_item`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '配置项ID',
  `category_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '分类编码',
  `item_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '配置键',
  `item_value` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '配置值',
  `item_label` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '显示标签',
  `item_color` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '颜色（用于前端展示）',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '描述',
  `sort` int(11) NULL DEFAULT 0 COMMENT '排序',
  `status` tinyint(4) NULL DEFAULT 1 COMMENT '状态：0禁用 1启用',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_config_item_key`(`category_code`, `item_key`) USING BTREE,
  INDEX `idx_config_item_category`(`category_code`) USING BTREE,
  INDEX `idx_config_item_status`(`status`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 60 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '配置项表（枚举值）' ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for sys_email_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_email_log`;
CREATE TABLE `sys_email_log`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `from_email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '发件人',
  `to_email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '收件人',
  `subject` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '主题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '内容',
  `send_status` tinyint(4) NULL DEFAULT 0 COMMENT '发送状态：0失败 1成功',
  `error_msg` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '错误信息',
  `send_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_email_to`(`to_email`) USING BTREE,
  INDEX `idx_email_time`(`send_time`) USING BTREE,
  INDEX `idx_send_status`(`send_status`, `send_time`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '邮件发送日志表' ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for sys_email_template
-- ----------------------------
DROP TABLE IF EXISTS `sys_email_template`;
CREATE TABLE `sys_email_template`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `template_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '模板编码（唯一标识，如 lesson_notify）',
  `template_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '模板名称',
  `subject` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '邮件主题（支持变量 ${xxx}）',
  `body` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '邮件正文（支持变量 ${xxx}，HTML格式）',
  `variables` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '支持的变量说明，JSON格式',
  `role_type` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'all' COMMENT '适用角色：teacher/parent/student/all',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态：0禁用 1启用',
  `create_by` bigint(20) NULL DEFAULT NULL COMMENT '创建人ID',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_template_code`(`template_code`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '邮件模板表' ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for sys_evaluation
-- ----------------------------
DROP TABLE IF EXISTS `sys_evaluation`;
CREATE TABLE `sys_evaluation`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '评价ID',
  `user_id` bigint(20) NULL DEFAULT NULL COMMENT '评价用户ID',
  `user_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '用户名称',
  `user_avatar` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '用户头像',
  `target_id` bigint(20) NOT NULL COMMENT '评价对象ID（课程ID或考试ID）',
  `target_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '评价对象名称',
  `target_type` tinyint(4) NOT NULL COMMENT '评价对象类型：1课程 2考试',
  `rating` tinyint(4) NULL DEFAULT 5 COMMENT '评分：1-5星',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '评价内容',
  `status` tinyint(4) NULL DEFAULT 0 COMMENT '状态：0待回复 1已回复',
  `reply` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '回复内容',
  `reply_time` timestamp NULL DEFAULT NULL COMMENT '回复时间',
  `dr` tinyint(4) NULL DEFAULT 0 COMMENT '逻辑删除：0-正常 1-删除',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_eval_user`(`user_id`) USING BTREE,
  INDEX `idx_eval_target`(`target_id`, `target_type`) USING BTREE,
  INDEX `idx_eval_status`(`status`) USING BTREE,
  INDEX `idx_target_rating`(`target_id`, `target_type`, `rating`, `status`, `dr`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '评价管理表' ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for sys_excel_template
-- ----------------------------
DROP TABLE IF EXISTS `sys_excel_template`;
CREATE TABLE `sys_excel_template`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `template_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '模板编码(唯一标识)',
  `template_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '模板名称',
  `template_type` tinyint(1) NOT NULL DEFAULT 1 COMMENT '模板类型: 1-导入模板 2-导出模板',
  `business_module` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '业务模块(如: question, course, exam)',
  `sheet_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'Sheet1' COMMENT 'Sheet名称',
  `header_row_height` int(11) NULL DEFAULT 25 COMMENT '表头行高',
  `content_row_height` int(11) NULL DEFAULT 20 COMMENT '内容行高',
  `default_column_width` int(11) NULL DEFAULT 20 COMMENT '默认列宽',
  `header_bg_color` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '#4472C4' COMMENT '表头背景色',
  `header_font_color` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '#FFFFFF' COMMENT '表头字体颜色',
  `status` tinyint(1) NULL DEFAULT 1 COMMENT '是否启用: 0-禁用 1-启用',
  `sort_order` int(11) NULL DEFAULT 0 COMMENT '排序',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `dr` tinyint(1) NULL DEFAULT 0 COMMENT '逻辑删除: 0-正常 1-删除',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_template_code`(`template_code`) USING BTREE,
  INDEX `idx_business_module`(`business_module`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'Excel模板配置表' ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for sys_excel_template_column
-- ----------------------------
DROP TABLE IF EXISTS `sys_excel_template_column`;
CREATE TABLE `sys_excel_template_column`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `template_id` bigint(20) NOT NULL COMMENT '模板ID',
  `column_index` int(11) NOT NULL COMMENT '列索引(从0开始)',
  `field_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '列名(字段名)',
  `column_title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '列标题',
  `column_width` int(11) NULL DEFAULT NULL COMMENT '列宽(覆盖默认列宽)',
  `data_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'String' COMMENT '数据类型: String, Integer, Long, Double, BigDecimal, Date',
  `required` tinyint(1) NULL DEFAULT 0 COMMENT '是否必填: 0-否 1-是',
  `visible` tinyint(1) NULL DEFAULT 1 COMMENT '是否显示: 0-隐藏 1-显示',
  `alignment` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'center' COMMENT '对齐方式: left, center, right',
  `example_value` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '示例数据',
  `dict_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '数据字典编码(用于下拉选项)',
  `sort_order` int(11) NULL DEFAULT 0 COMMENT '排序',
  `validation_rules` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '验证规则(JSON格式)',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `dr` tinyint(1) NULL DEFAULT 0 COMMENT '逻辑删除: 0-正常 1-删除',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_template_id`(`template_id`) USING BTREE,
  INDEX `idx_column_index`(`column_index`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 62 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'Excel模板列配置表' ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for sys_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
  `parent_id` bigint(20) NULL DEFAULT 0 COMMENT '父菜单ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '菜单名称',
  `icon` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '图标',
  `path` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '路由路径',
  `component` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '组件路径',
  `permission` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '权限标识，如 user:assign:role、user:assign:perm',
  `menu_type` tinyint(4) NULL DEFAULT 1 COMMENT '类型：1目录 2菜单 3按钮',
  `sort` int(11) NULL DEFAULT 0 COMMENT '排序',
  `status` tinyint(4) NULL DEFAULT 1 COMMENT '状态：0隐藏 1显示',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 112 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '菜单表' ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for sys_menu_button
-- ----------------------------
DROP TABLE IF EXISTS `sys_menu_button`;
CREATE TABLE `sys_menu_button`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `menu_id` bigint(20) NOT NULL COMMENT '菜单ID',
  `button_id` bigint(20) NOT NULL COMMENT '按钮ID',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_menu_button`(`menu_id`, `button_id`) USING BTREE,
  INDEX `idx_menu_id`(`menu_id`) USING BTREE,
  INDEX `idx_button_id`(`button_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 26 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '菜单按钮关系表' ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for sys_operation_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_operation_log`;
CREATE TABLE `sys_operation_log`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint(20) NULL DEFAULT NULL COMMENT '用户ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '用户名',
  `module` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '操作模块',
  `operation_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '操作类型:增/删/改/查/导入/导出',
  `method` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '请求方法',
  `request_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '请求URL',
  `request_method` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '请求方式:GET/POST',
  `request_params` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '请求参数',
  `response_result` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '响应结果',
  `ip` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '操作IP',
  `location` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '操作地点',
  `cost_time` bigint(20) NULL DEFAULT 0 COMMENT '执行时间(毫秒)',
  `status` tinyint(4) NULL DEFAULT 1 COMMENT '状态:0-失败 1-成功',
  `error_msg` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '错误信息',
  `operation_time` datetime NOT NULL COMMENT '操作时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_time`(`user_id`, `operation_time`) USING BTREE,
  INDEX `idx_module`(`module`) USING BTREE,
  INDEX `idx_operation_time`(`operation_time`) USING BTREE,
  INDEX `idx_status_time`(`status`, `operation_time`) USING BTREE,
  INDEX `idx_request_url`(`request_url`(100)) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '操作日志表' ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for sys_parent
-- ----------------------------
DROP TABLE IF EXISTS `sys_parent`;
CREATE TABLE `sys_parent`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '家长姓名',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '手机号',
  `wechat` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '微信号',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '邮箱',
  `relation` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '与学生关系：父亲/母亲/其他',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '家长信息表' ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for sys_parse_template
-- ----------------------------
DROP TABLE IF EXISTS `sys_parse_template`;
CREATE TABLE `sys_parse_template`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `template_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '模板名称',
  `template_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '模板编码(唯一标识)',
  `document_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '文档类型: word-单词, question-题目, course-课程, student-学生, teacher-教师',
  `business_module` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '业务模块: sys_word, exam_question, edu_course, sys_user等',
  `parse_rules` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '解析规则(JSON格式)',
  `example_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '示例文档内容',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
  `sort_order` int(11) NOT NULL DEFAULT 0 COMMENT '排序',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注说明',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NULL DEFAULT NULL COMMENT '更新时间',
  `dr` tinyint(4) NOT NULL DEFAULT 0 COMMENT '删除标记: 0-未删除, 1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_template_code`(`template_code`) USING BTREE,
  INDEX `idx_document_type`(`document_type`) USING BTREE,
  INDEX `idx_business_module`(`business_module`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '文档解析模板配置表' ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '角色名称',
  `code` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '角色代码',
  `description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '描述',
  `user_count` int(11) NULL DEFAULT 0 COMMENT '用户数量',
  `status` tinyint(4) NULL DEFAULT 1 COMMENT '状态：0禁用 1启用',
  `role_level` int(11) NULL DEFAULT 0 COMMENT '角色等级：0-学生 1-教师 2-管理员 3-超级管理员',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_role_code`(`code`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '角色表' ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for sys_role_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_menu`;
CREATE TABLE `sys_role_menu`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `role_id` bigint(20) NOT NULL COMMENT '角色ID',
  `menu_id` bigint(20) NOT NULL COMMENT '菜单ID',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_role_menu`(`role_id`, `menu_id`) USING BTREE,
  INDEX `menu_id`(`menu_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 87 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '角色菜单关联表' ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for sys_setting
-- ----------------------------
DROP TABLE IF EXISTS `sys_setting`;
CREATE TABLE `sys_setting`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `group_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '分组编码:basic/security/notify/learn/file',
  `setting_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '配置键',
  `setting_value` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '配置值',
  `value_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'string' COMMENT '值类型:string/int/boolean/json',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '描述',
  `sort` int(11) NULL DEFAULT 0 COMMENT '排序',
  `is_system` tinyint(1) NULL DEFAULT 0 COMMENT '是否系统内置:0-否 1-是',
  `status` tinyint(1) NULL DEFAULT 1 COMMENT '状态:0-禁用 1-启用',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_group_key`(`group_code`, `setting_key`) USING BTREE,
  INDEX `idx_group_code`(`group_code`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 33 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '系统设置表' ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for sys_setting_backup
-- ----------------------------
DROP TABLE IF EXISTS `sys_setting_backup`;
CREATE TABLE `sys_setting_backup`  (
  `id` bigint(20) NOT NULL DEFAULT 0,
  `group_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '分组编码（basic/security/notify/learn）',
  `setting_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '配置键',
  `setting_value` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '配置值',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '描述',
  `create_time` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00' COMMENT '创建时间',
  `update_time` timestamp NULL DEFAULT NULL COMMENT '更新时间'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for sys_task
-- ----------------------------
DROP TABLE IF EXISTS `sys_task`;
CREATE TABLE `sys_task`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint(20) NOT NULL COMMENT '所属用户ID',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '任务标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '任务详情',
  `task_date` date NOT NULL COMMENT '任务日期',
  `start_time` time NULL DEFAULT NULL COMMENT '开始时间',
  `end_time` time NULL DEFAULT NULL COMMENT '结束时间',
  `priority` tinyint(4) NOT NULL DEFAULT 1 COMMENT '优先级：1低 2中 3高',
  `status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '状态：0待完成 1已完成',
  `task_type` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'custom' COMMENT '任务类型：lesson/exam/custom',
  `ref_id` bigint(20) NULL DEFAULT NULL COMMENT '关联ID（如排课ID、考试ID）',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_date`(`user_id`, `task_date`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户任务表' ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户名',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '密码',
  `last_password_change_time` timestamp NULL DEFAULT NULL COMMENT '最后修改密码时间',
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '昵称',
  `avatar` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '头像(base64)',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '手机号',
  `id_card` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '身份证号',
  `birthday` date NULL DEFAULT NULL COMMENT '出生日期',
  `join_date` date NULL DEFAULT NULL COMMENT '入学/入职日期',
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '常用地址（用于通勤时间估算）',
  `grade_id` bigint(20) NULL DEFAULT NULL COMMENT '年级ID（学生）',
  `parent_id` bigint(20) NULL DEFAULT NULL COMMENT '上级用户ID（用于层级关系：管理员->教师->学生）',
  `status` tinyint(4) NULL DEFAULT 1 COMMENT '状态：0禁用 1启用',
  `sex` tinyint(4) NULL DEFAULT 0 COMMENT '性别：0-未知 1-男 2-女',
  `last_login_time` timestamp NULL DEFAULT NULL COMMENT '最后登录时间',
  `last_login_ip` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '最后登录IP',
  `is_online` tinyint(4) NULL DEFAULT 0 COMMENT '是否在线：0否 1是',
  `login_fail_count` int(11) NULL DEFAULT 0 COMMENT '登录失败次数',
  `lock_time` timestamp NULL DEFAULT NULL COMMENT '锁定时间',
  `online_time` int(11) NULL DEFAULT 0 COMMENT '在线时长（分钟）',
  `dr` tinyint(4) NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_username`(`username`) USING BTREE,
  UNIQUE INDEX `uk_user_email`(`email`) USING BTREE,
  UNIQUE INDEX `uk_user_phone`(`phone`) USING BTREE,
  INDEX `idx_parent_id`(`parent_id`) USING BTREE,
  INDEX `idx_status_role`(`status`, `dr`) USING BTREE,
  INDEX `idx_last_login`(`last_login_time`) USING BTREE,
  INDEX `idx_username_status`(`username`, `status`) USING BTREE,
  INDEX `idx_grade_status`(`grade_id`, `status`, `dr`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 505 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户表' ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for sys_user_login_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_login_log`;
CREATE TABLE `sys_user_login_log`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户名',
  `login_time` datetime NOT NULL COMMENT '登录时间',
  `login_ip` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '登录IP',
  `login_location` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '登录地点',
  `browser` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '浏览器',
  `os` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '操作系统',
  `status` tinyint(4) NULL DEFAULT 1 COMMENT '状态:0-失败 1-成功',
  `msg` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '提示信息',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_time`(`user_id`, `login_time`) USING BTREE,
  INDEX `idx_login_time`(`login_time`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户登录日志表' ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for sys_user_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_menu`;
CREATE TABLE `sys_user_menu`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `menu_id` bigint(20) NOT NULL COMMENT '菜单ID',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_menu`(`user_id`, `menu_id`) USING BTREE,
  INDEX `menu_id`(`menu_id`) USING BTREE,
  CONSTRAINT `sys_user_menu_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `sys_user_menu_ibfk_2` FOREIGN KEY (`menu_id`) REFERENCES `sys_menu` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 17 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户菜单关联表' ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for sys_user_parent
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_parent`;
CREATE TABLE `sys_user_parent`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID（通常是学生）',
  `parent_id` bigint(20) NOT NULL COMMENT '家长ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_parent`(`user_id`, `parent_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_parent_id`(`parent_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户-家长关联表' ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for sys_user_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `role_id` bigint(20) NOT NULL COMMENT '角色ID',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_role`(`user_id`, `role_id`) USING BTREE,
  INDEX `role_id`(`role_id`) USING BTREE,
  CONSTRAINT `sys_user_role_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `sys_user_role_ibfk_2` FOREIGN KEY (`role_id`) REFERENCES `sys_role` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 503 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户角色关联表' ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for sys_word
-- ----------------------------
DROP TABLE IF EXISTS `sys_word`;
CREATE TABLE `sys_word`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '单词ID',
  `word` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '英文单词',
  `phonetic` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '音标',
  `translation` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '中文释义',
  `example` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '例句',
  `question_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '题目类型(逗号分隔): spell-拼写,choice_select-看词选义,choice_word-看义选词,translate-翻译,sentence_fill-例句填空',
  `ai_recommended` tinyint(4) NULL DEFAULT 0 COMMENT 'AI推荐标记:0-否 1-是',
  `wrong_count` int(11) NULL DEFAULT 0 COMMENT '错误次数统计',
  `difficulty` tinyint(4) NULL DEFAULT 1 COMMENT '难度：1简单 2中等 3困难',
  `grade` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '适用年级（如：六年级、初一、高三）',
  `category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '词性分类（如：动词、名词、形容词）',
  `study_count` int(11) NULL DEFAULT 0 COMMENT '学习次数',
  `status` tinyint(4) NULL DEFAULT 1 COMMENT '状态：0禁用 1启用',
  `dr` tinyint(4) NULL DEFAULT 0 COMMENT '逻辑删除：0正常 1删除',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_word_grade`(`grade`) USING BTREE,
  INDEX `idx_word_difficulty`(`difficulty`) USING BTREE,
  INDEX `idx_word_status`(`status`) USING BTREE,
  INDEX `idx_grade_difficulty`(`grade`, `difficulty`, `dr`) USING BTREE COMMENT '年级难度查询索引',
  INDEX `idx_ai_recommended`(`ai_recommended`, `dr`) USING BTREE COMMENT 'AI推荐查询索引'
) ENGINE = InnoDB AUTO_INCREMENT = 51 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '单词训练表' ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for user_online_log
-- ----------------------------
DROP TABLE IF EXISTS `user_online_log`;
CREATE TABLE `user_online_log`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `login_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
  `logout_time` timestamp NULL DEFAULT NULL COMMENT '登出时间',
  `login_ip` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '登录IP',
  `user_agent` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '浏览器标识',
  `duration` int(11) NULL DEFAULT 0 COMMENT '在线时长（分钟）',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `user_id`(`user_id`) USING BTREE,
  CONSTRAINT `user_online_log_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户在线日志表' ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for word_training_record
-- ----------------------------
DROP TABLE IF EXISTS `word_training_record`;
CREATE TABLE `word_training_record`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `session_id` bigint(20) NOT NULL COMMENT '会话ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `word_id` bigint(20) NOT NULL COMMENT '单词ID',
  `question_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '题目类型',
  `question_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '题目内容(如:例句填空的题干)',
  `correct_answer` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '正确答案',
  `user_answer` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '用户答案',
  `is_correct` tinyint(4) NULL DEFAULT NULL COMMENT '是否正确:0-错误 1-正确 NULL-未作答',
  `is_skipped` tinyint(4) NULL DEFAULT 0 COMMENT '是否跳过:0-否 1-是',
  `answer_time` int(11) NULL DEFAULT NULL COMMENT '答题用时(秒)',
  `timeout` tinyint(4) NULL DEFAULT 0 COMMENT '是否超时:0-否 1-是',
  `sort_order` int(11) NULL DEFAULT 0 COMMENT '题目顺序',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_session_id`(`session_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_word_id`(`word_id`) USING BTREE,
  INDEX `idx_is_correct`(`is_correct`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '单词训练答题记录表' ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for word_training_session
-- ----------------------------
DROP TABLE IF EXISTS `word_training_session`;
CREATE TABLE `word_training_session`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '会话ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `grade` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '年级(如:六年级、初一、高三)',
  `difficulty` tinyint(4) NOT NULL COMMENT '难度:1-简单 2-中等 3-困难',
  `question_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '题目类型:spell-拼写,choice_select-看词选义,choice_word-看义选词,translate-翻译,sentence_fill-例句填空',
  `question_count` int(11) NOT NULL COMMENT '题目数量',
  `per_question_time` int(11) NULL DEFAULT 30 COMMENT '每题时间(秒),默认30秒',
  `total_score` int(11) NULL DEFAULT 0 COMMENT '总分',
  `correct_count` int(11) NULL DEFAULT 0 COMMENT '正确题数',
  `wrong_count` int(11) NULL DEFAULT 0 COMMENT '错误题数',
  `skip_count` int(11) NULL DEFAULT 0 COMMENT '跳答题数',
  `status` tinyint(4) NULL DEFAULT 0 COMMENT '状态:0-进行中 1-已完成 2-已放弃',
  `start_time` datetime NULL DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime NULL DEFAULT NULL COMMENT '结束时间',
  `actual_duration` int(11) NULL DEFAULT 0 COMMENT '实际用时(秒)',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NULL DEFAULT NULL COMMENT '更新时间',
  `dr` tinyint(4) NULL DEFAULT 0 COMMENT '逻辑删除:0-正常 1-删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_user_status`(`user_id`, `status`, `dr`) USING BTREE,
  INDEX `idx_create_time`(`create_time`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '单词训练会话表' ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for word_wrong_book
-- ----------------------------
DROP TABLE IF EXISTS `word_wrong_book`;
CREATE TABLE `word_wrong_book`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '错题ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `word_id` bigint(20) NOT NULL COMMENT '单词ID',
  `question_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '题目类型',
  `wrong_count` int(11) NULL DEFAULT 1 COMMENT '错误次数',
  `last_wrong_time` datetime NOT NULL COMMENT '最后错误时间',
  `review_status` tinyint(4) NULL DEFAULT 0 COMMENT '复习状态:0-待复习 1-已掌握 2-需强化',
  `next_review_date` date NULL DEFAULT NULL COMMENT '下次复习日期(艾宾浩斯遗忘曲线)',
  `mastery_level` tinyint(4) NULL DEFAULT 0 COMMENT '掌握程度:0-未掌握 1-部分掌握 2-已掌握',
  `note` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '用户备注',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NULL DEFAULT NULL COMMENT '更新时间',
  `dr` tinyint(4) NULL DEFAULT 0 COMMENT '逻辑删除:0-正常 1-删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_word_type`(`user_id`, `word_id`, `question_type`, `dr`) USING BTREE,
  INDEX `idx_user_review`(`user_id`, `review_status`, `next_review_date`, `dr`) USING BTREE,
  INDEX `idx_last_wrong_time`(`user_id`, `last_wrong_time`) USING BTREE,
  INDEX `idx_word_id`(`word_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '单词错题本表' ROW_FORMAT = Compact;

-- ----------------------------
-- View structure for v_user_hierarchy
-- ----------------------------
DROP VIEW IF EXISTS `v_user_hierarchy`;
CREATE ALGORITHM = UNDEFINED DEFINER = `root`@`localhost` SQL SECURITY DEFINER VIEW `v_user_hierarchy` AS SELECT 
    u.id,
    u.username,
    u.nickname,
    u.parent_id,
    p.username as parent_username,
    p.nickname as parent_nickname,
    r.id as role_id,
    r.name as role_name,
    r.code as role_code,
    r.role_level
FROM sys_user u
LEFT JOIN sys_user p ON u.parent_id = p.id
LEFT JOIN sys_user_role ur ON u.id = ur.user_id
LEFT JOIN sys_role r ON ur.role_id = r.id ;

-- ----------------------------
-- Procedure structure for generate_students
-- ----------------------------
DROP PROCEDURE IF EXISTS `generate_students`;
delimiter ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `generate_students`()
BEGIN
    DECLARE i INT DEFAULT 7;
    DECLARE grade_id_val INT;
    
    WHILE i <= 501 DO
        SET grade_id_val = FLOOR(1 + RAND() * 7);
        
        INSERT INTO sys_user (id, username, password, nickname, email, phone, grade_id, status, is_online) 
        VALUES (i, CONCAT('student', LPAD(i-6, 4, '0')), 
            '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO', 
            CONCAT('学生', i-6), 
            CONCAT('student', i-6, '@edu.com'), 
            CONCAT('139', LPAD(i, 8, '0')), 
            grade_id_val, 1, 0);
        
        INSERT INTO sys_user_role (user_id, role_id) VALUES (i, 4);
        
        SET i = i + 1;
        
        IF i % 100 = 0 THEN
            COMMIT;
        END IF;
    END WHILE;
END
;;
delimiter ;

-- ----------------------------
-- Triggers structure for table sys_user
-- ----------------------------
DROP TRIGGER IF EXISTS `trg_user_delete_before`;
delimiter ;;
CREATE TRIGGER `trg_user_delete_before` BEFORE DELETE ON `sys_user` FOR EACH ROW BEGIN
    -- 将被删除用户的下级转移到被删除用户的上级
    UPDATE sys_user SET parent_id = OLD.parent_id WHERE parent_id = OLD.id;
END
;;
delimiter ;

SET FOREIGN_KEY_CHECKS = 1;
