CREATE TABLE IF NOT EXISTS `accounts` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '账户ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `account_name` VARCHAR(100) NOT NULL COMMENT '账户名称',
    `account_type` VARCHAR(30) NOT NULL COMMENT '账户类型（枚举值字符串）',
    `balance` DECIMAL(15, 2) DEFAULT 0.00 COMMENT '账户余额',
    `currency` VARCHAR(10) DEFAULT 'CNY' COMMENT '币种',
    `icon` VARCHAR(200) COMMENT '账户图标',
    `remark` VARCHAR(500) COMMENT '备注',
    `sort_order` INT DEFAULT 0 COMMENT '排序',
    `is_default` TINYINT DEFAULT 0 COMMENT '是否默认账户：0-否，1-是',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_user_id (`user_id`),
    INDEX idx_account_type (`account_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='账户表';