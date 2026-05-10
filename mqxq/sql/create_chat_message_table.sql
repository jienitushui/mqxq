-- ----------------------------
-- Table structure for chat_message
-- ----------------------------
DROP TABLE IF EXISTS `chat_message`;
CREATE TABLE `chat_message`
(
    `id`          int(0) NOT NULL AUTO_INCREMENT COMMENT '主键id',
    `session_id`  varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '会话id',
    `user_id`     int(0) NOT NULL DEFAULT 0 COMMENT '用户id',
    `message_type` tinyint(0) NOT NULL COMMENT '消息类型：1-用户提问，2-AI回答',
    `content`     text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '消息内容',
    `params`      text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '附加参数（JSON格式）',
    `message_order` int(0) NOT NULL DEFAULT 0 COMMENT '消息顺序（在同一会话中的顺序）',
    `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX         `idx_session_id`(`session_id`) USING BTREE,
    INDEX         `idx_user_id`(`user_id`) USING BTREE,
    INDEX         `idx_create_time`(`create_time`) USING BTREE,
    INDEX         `idx_session_order`(`session_id`, `message_order`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '聊天消息详情表' ROW_FORMAT = Dynamic;

