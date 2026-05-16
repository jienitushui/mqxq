/*
 Navicat Premium Data Transfer

 Source Server         : jieni1
 Source Server Type    : MySQL
 Source Server Version : 80028
 Source Host           : localhost:3306
 Source Schema         : mqxq

 Target Server Type    : MySQL
 Target Server Version : 80028
 File Encoding         : 65001

 Date: 13/05/2026 14:36:17
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for announcement_categories
-- ----------------------------
DROP TABLE IF EXISTS `announcement_categories`;
CREATE TABLE `announcement_categories`  (
  `id` int(0) NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '分类名称，如：政策更新、赛事通知、教学资源等',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '分类的描述或备注信息',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `create_user` int(0) NOT NULL DEFAULT 0 COMMENT '创建者id',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '修改时间',
  `update_user` int(0) NULL DEFAULT 0 COMMENT '修改者id',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `name`(`name`) USING BTREE,
  INDEX `idx_create_time`(`create_time`) USING BTREE,
  INDEX `idx_update_time`(`update_time`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '公告分类表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for announcements
-- ----------------------------
DROP TABLE IF EXISTS `announcements`;
CREATE TABLE `announcements`  (
  `id` int(0) NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `category_id` int(0) NOT NULL COMMENT '关联的公告分类ID',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '公告标题',
  `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '公告正文内容，支持富文本或大文本',
  `publish_date` datetime(0) NOT NULL COMMENT '公告对外发布的日期',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '记录创建时间',
  `create_user` int(0) NOT NULL DEFAULT 0 COMMENT '创建者用户ID',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '修改时间',
  `update_user` int(0) NULL DEFAULT 0 COMMENT '修改者ID',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_category_id`(`category_id`) USING BTREE,
  INDEX `idx_publish_date`(`publish_date`) USING BTREE,
  INDEX `idx_create_time`(`create_time`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 22 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '公告详情表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for carousel
-- ----------------------------
DROP TABLE IF EXISTS `carousel`;
CREATE TABLE `carousel`  (
  `id` int(0) NOT NULL AUTO_INCREMENT COMMENT '首页轮播图主键id',
  `carousel_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '图片地址',
  `link_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '链接地址',
  `sort` int(0) NOT NULL DEFAULT 1 COMMENT '排序值(字段越大越靠前)',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除 1（true）已删除， 0（false）未删除',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `create_user` int(0) NOT NULL COMMENT '创建者id',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '修改时间',
  `update_user` int(0) NOT NULL COMMENT '修改者id',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '首页banner表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for chapter
-- ----------------------------
DROP TABLE IF EXISTS `chapter`;
CREATE TABLE `chapter`  (
  `id` int(0) NOT NULL AUTO_INCREMENT COMMENT '主键章节ID',
  `course_id` int(0) NOT NULL COMMENT '课程ID',
  `title` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '章节名称',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `create_user` int(0) NOT NULL DEFAULT 0 COMMENT '创建者id',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '修改时间',
  `update_user` int(0) NULL DEFAULT 0 COMMENT '修改者id',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_course_id`(`course_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 37 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '课程' ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for chat_message
-- ----------------------------
DROP TABLE IF EXISTS `chat_message`;
CREATE TABLE `chat_message`  (
  `id` int(0) NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `session_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '会话id',
  `user_id` int(0) NOT NULL DEFAULT 0 COMMENT '用户id',
  `message_type` tinyint(0) NOT NULL COMMENT '消息类型：1-用户提问，2-AI回答',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '消息内容',
  `params` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '附加参数（JSON格式）',
  `message_order` int(0) NOT NULL DEFAULT 0 COMMENT '消息顺序（在同一会话中的顺序）',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_session_id`(`session_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_create_time`(`create_time`) USING BTREE,
  INDEX `idx_session_order`(`session_id`, `message_order`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 20 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '聊天消息详情表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for chat_session
-- ----------------------------
DROP TABLE IF EXISTS `chat_session`;
CREATE TABLE `chat_session`  (
  `id` int(0) NOT NULL AUTO_INCREMENT COMMENT '数据id',
  `session_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '会话id',
  `user_id` int(0) NOT NULL DEFAULT 0 COMMENT '用户id',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '会话标题',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `create_user` int(0) NOT NULL COMMENT '创建人',
  `updater_user` int(0) NOT NULL COMMENT '更新人',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `session_id_index`(`session_id`) USING BTREE,
  INDEX `user_id_index`(`user_id`) USING BTREE,
  INDEX `update_time_index`(`update_time`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 46 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '对话session' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for course
-- ----------------------------
DROP TABLE IF EXISTS `course`;
CREATE TABLE `course`  (
  `id` int(0) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `teacher_id` int(0) NOT NULL DEFAULT 0 COMMENT '课程教师ID',
  `subject_id` int(0) NOT NULL DEFAULT 0 COMMENT '课程分类ID',
  `title` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '课程标题',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '课程简介',
  `price` decimal(10, 2) NOT NULL COMMENT '课程销售价格，设置为0则可免费观看',
  `lesson_num` int(0) NOT NULL DEFAULT 0 COMMENT '总课时',
  `duration_sum` int(0) NOT NULL DEFAULT 0 COMMENT '视频总时长（秒）',
  `cover` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '课程封面图片路径',
  `buy_count` int(0) NOT NULL DEFAULT 0 COMMENT '销售数量',
  `view_count` int(0) NOT NULL DEFAULT 0 COMMENT '浏览数量',
  `status` tinyint(0) NOT NULL DEFAULT 0 COMMENT '课程状态 0未发布 1已发布',
  `publish_time` datetime(0) NULL DEFAULT NULL COMMENT '课程发布时间',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `create_user` int(0) NOT NULL DEFAULT 0 COMMENT '创建者id',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '修改时间',
  `update_user` int(0) NULL DEFAULT 0 COMMENT '修改者id',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_title`(`title`) USING BTREE,
  INDEX `idx_subject_id`(`subject_id`) USING BTREE,
  INDEX `idx_teacher_id`(`teacher_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 38 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '课程' ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for course_comment
-- ----------------------------
DROP TABLE IF EXISTS `course_comment`;
CREATE TABLE `course_comment`  (
  `id` int(0) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `course_id` int(0) NOT NULL COMMENT '课程ID',
  `teacher_id` int(0) NOT NULL COMMENT '讲师ID',
  `user_id` int(0) NOT NULL COMMENT '用户ID',
  `score` tinyint(0) NOT NULL DEFAULT 5 COMMENT '评分1-5星',
  `content` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '评论内容',
  `status` tinyint(0) NOT NULL DEFAULT 1 COMMENT '状态：0-隐藏，1-显示',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_course_id`(`course_id`) USING BTREE,
  INDEX `idx_teacher_id`(`teacher_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '课程评论表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for course_homework
-- ----------------------------
DROP TABLE IF EXISTS `course_homework`;
CREATE TABLE `course_homework`  (
  `id` int(0) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `course_id` int(0) NOT NULL COMMENT '课程ID',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '作业标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '作业内容',
  `answer` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '参考答案',
  `score` decimal(5, 2) NOT NULL DEFAULT 100.00 COMMENT '总分值',
  `start_time` datetime(0) NOT NULL COMMENT '开始时间',
  `end_time` datetime(0) NOT NULL COMMENT '截止时间',
  `status` tinyint(0) NOT NULL DEFAULT 0 COMMENT '状态：0-未发布，1-已发布',
  `create_user` int(0) NOT NULL COMMENT '创建人ID',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_user` int(0) NOT NULL COMMENT '修改人ID',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_course_id`(`course_id`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE,
  INDEX `idx_create_user`(`create_user`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '课程作业表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for course_subject
-- ----------------------------
DROP TABLE IF EXISTS `course_subject`;
CREATE TABLE `course_subject`  (
  `id` int(0) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '类别名称',
  `parent_id` int(0) NOT NULL DEFAULT 0 COMMENT '父ID',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `create_user` int(0) NOT NULL COMMENT '创建者id',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '修改时间',
  `update_user` int(0) NULL DEFAULT 0 COMMENT '修改者id',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_parent_id`(`parent_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 19 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '课程分类' ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for course_view
-- ----------------------------
DROP TABLE IF EXISTS `course_view`;
CREATE TABLE `course_view`  (
  `id` int(0) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `course_id` int(0) NOT NULL DEFAULT 0 COMMENT '课程ID',
  `user_id` int(0) NOT NULL DEFAULT 0 COMMENT '用户ID',
  `view_time` datetime(0) NOT NULL COMMENT '浏览时间',
  `ip_address` varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'IP地址',
  `user_agent` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户代理',
  `create_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '记录创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_course`(`user_id`, `course_id`) USING BTREE,
  INDEX `idx_course_id`(`course_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_view_time`(`view_time`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 264 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '课程浏览记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for homework_submission
-- ----------------------------
DROP TABLE IF EXISTS `homework_submission`;
CREATE TABLE `homework_submission`  (
  `id` int(0) NOT NULL AUTO_INCREMENT COMMENT '提交ID',
  `homework_id` int(0) NOT NULL COMMENT '作业ID，关联course_homework表',
  `student_id` int(0) NOT NULL COMMENT '学生ID，关联user表',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '提交内容',
  `attachment_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '附件URL',
  `submit_time` datetime(0) NULL DEFAULT NULL COMMENT '提交时间',
  `score` decimal(5, 2) NULL DEFAULT NULL COMMENT '得分',
  `max_score` decimal(5, 2) NULL DEFAULT NULL COMMENT '满分',
  `teacher_comment` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '教师评语',
  `grade_time` datetime(0) NULL DEFAULT NULL COMMENT '批改时间',
  `grade_user` int(0) NULL DEFAULT NULL COMMENT '批改教师ID',
  `status` tinyint(0) NOT NULL DEFAULT 0 COMMENT '状态: 0-已提交待批改, 1-已批改',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `create_user` int(0) NOT NULL DEFAULT 0 COMMENT '创建者id',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `update_user` int(0) NULL DEFAULT 0 COMMENT '修改者id',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uniq_homework_student`(`homework_id`, `student_id`) USING BTREE COMMENT '同一作业每个学生只能提交一次',
  INDEX `idx_homework_id`(`homework_id`) USING BTREE,
  INDEX `idx_student_id`(`student_id`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE,
  INDEX `idx_submit_time`(`submit_time`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 19 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '作业提交表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for my_course
-- ----------------------------
DROP TABLE IF EXISTS `my_course`;
CREATE TABLE `my_course`  (
  `id` int(0) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` int(0) NOT NULL COMMENT '用户ID',
  `course_id` int(0) NOT NULL COMMENT '课程ID',
  `order_id` int(0) NULL DEFAULT NULL COMMENT '订单ID',
  `status` tinyint(0) NOT NULL DEFAULT 0 COMMENT '学习状态：0-已加入，1-学习中，2-已完成',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uniq_user_course`(`user_id`, `course_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_course_id`(`course_id`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE,
  INDEX `idx_my_course_order_id`(`order_id`) USING BTREE,
  INDEX `idx_my_course_user_course`(`user_id`, `course_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '我的课程表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for orders
-- ----------------------------
DROP TABLE IF EXISTS `orders`;
CREATE TABLE `orders`  (
  `id` int(0) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `goods_id` int(0) NULL DEFAULT NULL COMMENT '商品ID',
  `goods_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '商品名称',
  `goods_img` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '商品图片',
  `goods_price` decimal(10, 2) NULL DEFAULT NULL COMMENT '商品价格',
  `order_no` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '订单编号',
  `user_id` int(0) NULL DEFAULT NULL COMMENT '创建人ID',
  `create_time` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '创建时间',
  `pay_no` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '支付单号',
  `pay_time` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '支付时间',
  `status` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '订单状态:CANCEL(\"已取消\"),\r\nNOT_PAY(\"待支付\"),DONE(\"已完成\"),REFUND_DONE(\"已退款\"),COMMENT_DONE(\"已评价\")',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 56 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '订单信息' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for role
-- ----------------------------
DROP TABLE IF EXISTS `role`;
CREATE TABLE `role`  (
  `id` int(0) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `role_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '常用名（中文名）',
  `role_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '角色名（用于鉴权）',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for section
-- ----------------------------
DROP TABLE IF EXISTS `section`;
CREATE TABLE `section`  (
  `id` int(0) NOT NULL AUTO_INCREMENT COMMENT '小节ID',
  `chapter_id` int(0) NOT NULL COMMENT '章节ID',
  `course_id` int(0) NOT NULL COMMENT '课程ID',
  `title` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '小节名称',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '小节内容',
  `video_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '视频url',
  `duration` int(0) NOT NULL DEFAULT 0 COMMENT '小节时长(秒)',
  `status` tinyint(0) NOT NULL DEFAULT 0 COMMENT '状态 0未发布 1已发布',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `create_user` int(0) NOT NULL DEFAULT 0 COMMENT '创建者id',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '修改时间',
  `update_user` int(0) NOT NULL DEFAULT 0 COMMENT '修改者id',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_chapter_id`(`chapter_id`) USING BTREE,
  INDEX `idx_course_id`(`course_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 30 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '课程章节小节' ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` int(0) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '账号',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '密码',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户名',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '头像路径',
  `phone` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '手机号',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `status` int(0) NULL DEFAULT 1 COMMENT '状态 1:启用 0:禁用',
  `create_user` int(0) NULL DEFAULT NULL COMMENT '创建人ID',
  `update_user` int(0) NULL DEFAULT NULL COMMENT '更新人ID',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_status`(`status`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for user_role
-- ----------------------------
DROP TABLE IF EXISTS `user_role`;
CREATE TABLE `user_role`  (
  `id` int(0) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `user_id` int(0) NULL DEFAULT NULL COMMENT '用户id',
  `role_id` int(0) NULL DEFAULT NULL COMMENT '角色id',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 64 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
