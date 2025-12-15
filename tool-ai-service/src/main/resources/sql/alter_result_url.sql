-- 修改result_url字段长度，支持火山引擎返回的长URL
-- 执行此SQL前请先备份数据库

-- 修改生成记录表的result_url字段
ALTER TABLE `t_generate_record` 
MODIFY COLUMN `result_url` VARCHAR(1000) NOT NULL COMMENT '结果图片URL';

-- 修改星座运势表的result_url字段
ALTER TABLE `t_constellation_fortune` 
MODIFY COLUMN `result_url` VARCHAR(1000) NOT NULL COMMENT '结果图片URL';




