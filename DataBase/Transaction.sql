CREATE TABLE IF NOT EXISTS `transactions` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '交易ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `account_id` BIGINT NOT NULL COMMENT '账户ID',
    `transaction_type` VARCHAR(20) NOT NULL COMMENT '交易类型（INCOME/EXPENSE/TRANSFER）',
    `amount` DECIMAL(15, 2) NOT NULL COMMENT '交易金额',
    `category` VARCHAR(50) COMMENT '消费分类（枚举）',
    `transaction_time` DATETIME NOT NULL COMMENT '交易时间',
    `remark` VARCHAR(500) COMMENT '备注',
    `tags` VARCHAR(500) COMMENT '标签（逗号分隔）',
    `target_account_id` BIGINT COMMENT '转账目标账户ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_user_id (`user_id`),
    INDEX idx_account_id (`account_id`),
    INDEX idx_transaction_time (`transaction_time`),
    INDEX idx_transaction_type (`transaction_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='交易记录表';