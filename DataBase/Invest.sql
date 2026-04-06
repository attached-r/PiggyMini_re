CREATE TABLE IF NOT EXISTS `invests` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '投资ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `invest_name` VARCHAR(100) NOT NULL COMMENT '投资名称',
    `invest_type` VARCHAR(50) NOT NULL COMMENT '投资类型（基金/股票/理财等）',
    `invest_amount` DECIMAL(15, 2) NOT NULL COMMENT '投资金额',
    `current_value` DECIMAL(15, 2) DEFAULT 0.00 COMMENT '当前价值',
    `yield_rate` DECIMAL(10, 4) DEFAULT 0.0000 COMMENT '收益率（百分比）',
    `invest_time` DATETIME NOT NULL COMMENT '投资时间',
    `maturity_time` DATETIME COMMENT '到期时间',
    `remark` VARCHAR(500) COMMENT '备注',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-已赎回，1-持有中',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_user_id (`user_id`),
    INDEX idx_invest_time (`invest_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='投资记录表';