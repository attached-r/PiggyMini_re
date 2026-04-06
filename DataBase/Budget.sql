CREATE TABLE IF NOT EXISTS `budgets` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '预算ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `budget_amount` DECIMAL(15, 2) NOT NULL COMMENT '预算金额',
    `used_amount` DECIMAL(15, 2) DEFAULT 0.00 COMMENT '已用金额',
    `cycle_type` TINYINT NOT NULL COMMENT '周期：1-周，2-月，3-年',
    `category` VARCHAR(50) COMMENT '消费分类（为空表示总预算）',
    `start_time` DATETIME NOT NULL COMMENT '开始时间',
    `end_time` DATETIME NOT NULL COMMENT '结束时间',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `warning_threshold` TINYINT DEFAULT 80 COMMENT '预警阈值（百分比）',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_user_id (`user_id`),
    INDEX idx_start_end_time (`start_time`, `end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='预算表';