-- =====================================================
-- 用户数据隔离迁移脚本
-- 执行时间：2024-12-21
-- 目的：为DocumentCategory和PopularQuestion表添加userId字段实现用户数据隔离
-- =====================================================

-- 1. 为document_categories表添加userId字段
ALTER TABLE document_categories 
ADD COLUMN user_id VARCHAR(255) NOT NULL DEFAULT 'system' COMMENT '用户ID，用于数据隔离';

-- 2. 为document_categories表添加索引
ALTER TABLE document_categories 
ADD INDEX idx_user_id (user_id),
ADD INDEX idx_user_id_status (user_id, status),
ADD INDEX idx_user_id_sort_order (user_id, sort_order);

-- 3. 修改document_categories表的name字段唯一约束（改为用户内唯一）
ALTER TABLE document_categories 
DROP INDEX name;

ALTER TABLE document_categories 
ADD UNIQUE INDEX uk_user_id_name (user_id, name);

-- 4. 为popular_questions表添加userId字段
ALTER TABLE popular_questions 
ADD COLUMN user_id VARCHAR(255) NOT NULL DEFAULT 'system' COMMENT '用户ID，用于数据隔离';

-- 5. 为popular_questions表添加索引
ALTER TABLE popular_questions 
ADD INDEX idx_user_id (user_id),
ADD INDEX idx_user_id_category (user_id, category),
ADD INDEX idx_user_id_trend_score (user_id, trend_score DESC),
ADD INDEX idx_user_id_question_count (user_id, question_count DESC);

-- 6. 修改popular_questions表的questionPattern唯一约束（改为用户内唯一）
ALTER TABLE popular_questions 
ADD UNIQUE INDEX uk_user_id_question_pattern_category (user_id, question_pattern, category);

-- 7. 创建user_search_settings表（如果不存在）
CREATE TABLE IF NOT EXISTS user_search_settings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '设置ID',
    user_id VARCHAR(255) NOT NULL UNIQUE COMMENT '用户ID',
    top_k INT NOT NULL DEFAULT 5 COMMENT '检索数量Top-K',
    similarity_threshold DOUBLE NOT NULL DEFAULT 0.7 COMMENT '相似度阈值',
    max_tokens INT NOT NULL DEFAULT 2000 COMMENT '最大令牌数',
    temperature DOUBLE NOT NULL DEFAULT 0.1 COMMENT '温度参数',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户搜索设置表';

-- 8. 为user_search_settings表添加索引
ALTER TABLE user_search_settings 
ADD INDEX idx_user_id (user_id);

-- =====================================================
-- 数据迁移（可选，如果需要保留现有数据）
-- =====================================================

-- 将现有的分类数据分配给默认用户（可根据实际情况调整）
-- UPDATE document_categories SET user_id = 'default-user' WHERE user_id = 'system';

-- 将现有的热门问题数据分配给默认用户（可根据实际情况调整）
-- UPDATE popular_questions SET user_id = 'default-user' WHERE user_id = 'system';

-- =====================================================
-- 验证脚本执行结果
-- =====================================================

-- 检查document_categories表结构
DESCRIBE document_categories;

-- 检查popular_questions表结构
DESCRIBE popular_questions;

-- 检查user_search_settings表结构
DESCRIBE user_search_settings;

-- 检查索引创建情况
SHOW INDEX FROM document_categories;
SHOW INDEX FROM popular_questions;
SHOW INDEX FROM user_search_settings;

-- =====================================================
-- 重要提醒
-- =====================================================
-- 1. 执行此脚本前请备份数据库
-- 2. 执行后需要重启agents服务
-- 3. 建议在测试环境先验证无误后再在生产环境执行
-- 4. 现有数据会被分配给默认用户'system'，可根据需要调整
-- ===================================================== 