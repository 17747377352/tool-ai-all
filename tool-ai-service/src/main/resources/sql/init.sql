-- 数据库初始化SQL

-- 用户表
CREATE TABLE IF NOT EXISTS `t_user` (
    `id` VARCHAR(36) NOT NULL COMMENT '主键ID（UUID）',
    `openid` VARCHAR(100) NOT NULL COMMENT '微信openid',
    `nickname` VARCHAR(100) DEFAULT NULL COMMENT '昵称',
    `avatar` VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_openid` (`openid`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 生成记录表
CREATE TABLE IF NOT EXISTS `t_generate_record` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `openid` VARCHAR(100) NOT NULL COMMENT '微信openid',
    `type` TINYINT(4) NOT NULL COMMENT '类型：1-去水印 2-AI头像 3-姓氏签名 4-运势测试 5-星座运势',
    `input_data` TEXT COMMENT '输入数据（JSON格式）',
    `result_url` VARCHAR(1000) NOT NULL COMMENT '结果图片URL',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_openid` (`openid`),
    KEY `idx_type` (`type`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='生成记录表';

-- 每日限流表
CREATE TABLE IF NOT EXISTS `t_daily_limit` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `openid` VARCHAR(100) NOT NULL COMMENT '微信openid',
    `date` DATE NOT NULL COMMENT '日期',
    `remove_logo_count` INT(11) DEFAULT 0 COMMENT '去水印次数',
    `ai_avatar_count` INT(11) DEFAULT 0 COMMENT 'AI头像次数',
    `name_sign_count` INT(11) DEFAULT 0 COMMENT '姓氏签名次数',
    `fortune_count` INT(11) DEFAULT 0 COMMENT '运势测试次数',
    `constellation_count` INT(11) DEFAULT 0 COMMENT '星座运势次数',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_openid_date` (`openid`, `date`),
    KEY `idx_date` (`date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='每日限流表';

-- 星座运势表（每天为所有12个星座生成一次）
CREATE TABLE IF NOT EXISTS `t_constellation_fortune` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `constellation` VARCHAR(20) NOT NULL COMMENT '星座名称',
    `date` DATE NOT NULL COMMENT '日期',
    `fortune_text` TEXT NOT NULL COMMENT '运势文案',
    `result_url` VARCHAR(1000) NOT NULL COMMENT '结果图片URL',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_constellation_date` (`constellation`, `date`),
    KEY `idx_date` (`date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='星座运势表';

