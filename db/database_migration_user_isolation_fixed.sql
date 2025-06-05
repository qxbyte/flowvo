-- =====================================================
-- 用户数据隔离迁移脚本（修正版）
-- 执行时间：2024-12-21
-- 目的：为DocumentCategory和PopularQuestion表添加userId字段实现用户数据隔离
-- =====================================================

-- 检查当前表结构和索引
SHOW CREATE TABLE document_categories;
SHOW INDEX FROM document_categories;

-- 1. 为document_categories表添加userId字段（如果不存在）
SET @sql = CASE 
    WHEN (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
          WHERE TABLE_SCHEMA = DATABASE() 
          AND TABLE_NAME = 'document_categories' 
          AND COLUMN_NAME = 'user_id') = 0 
    THEN 'ALTER TABLE document_categories ADD COLUMN user_id VARCHAR(255) NOT NULL DEFAULT ''system'' COMMENT ''用户ID，用于数据隔离'';'
    ELSE 'SELECT ''user_id字段已存在'' AS message;'
END;
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2. 为document_categories表添加索引（如果不存在）
-- 检查并添加idx_user_id索引
SET @sql = CASE 
    WHEN (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS 
          WHERE TABLE_SCHEMA = DATABASE() 
          AND TABLE_NAME = 'document_categories' 
          AND INDEX_NAME = 'idx_user_id') = 0 
    THEN 'ALTER TABLE document_categories ADD INDEX idx_user_id (user_id);'
    ELSE 'SELECT ''idx_user_id索引已存在'' AS message;'
END;
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 检查并添加idx_user_id_status索引
SET @sql = CASE 
    WHEN (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS 
          WHERE TABLE_SCHEMA = DATABASE() 
          AND TABLE_NAME = 'document_categories' 
          AND INDEX_NAME = 'idx_user_id_status') = 0 
    THEN 'ALTER TABLE document_categories ADD INDEX idx_user_id_status (user_id, status);'
    ELSE 'SELECT ''idx_user_id_status索引已存在'' AS message;'
END;
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 检查并添加idx_user_id_sort_order索引
SET @sql = CASE 
    WHEN (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS 
          WHERE TABLE_SCHEMA = DATABASE() 
          AND TABLE_NAME = 'document_categories' 
          AND INDEX_NAME = 'idx_user_id_sort_order') = 0 
    THEN 'ALTER TABLE document_categories ADD INDEX idx_user_id_sort_order (user_id, sort_order);'
    ELSE 'SELECT ''idx_user_id_sort_order索引已存在'' AS message;'
END;
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 3. 安全地删除name字段的唯一约束（如果存在）
-- 首先查找所有可能的唯一约束名称
SELECT CONSTRAINT_NAME 
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE 
WHERE TABLE_SCHEMA = DATABASE() 
AND TABLE_NAME = 'document_categories' 
AND COLUMN_NAME = 'name'
AND CONSTRAINT_NAME != 'PRIMARY';

-- 删除name字段的唯一约束（可能的名称：name, uk_name, document_categories_name_unique等）
SET @sql = CASE 
    WHEN (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS 
          WHERE TABLE_SCHEMA = DATABASE() 
          AND TABLE_NAME = 'document_categories' 
          AND INDEX_NAME = 'name') > 0 
    THEN 'ALTER TABLE document_categories DROP INDEX name;'
    ELSE 'SELECT ''name索引不存在，跳过删除'' AS message;'
END;
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 尝试删除其他可能的name唯一约束
SET @sql = CASE 
    WHEN (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS 
          WHERE TABLE_SCHEMA = DATABASE() 
          AND TABLE_NAME = 'document_categories' 
          AND INDEX_NAME = 'uk_name') > 0 
    THEN 'ALTER TABLE document_categories DROP INDEX uk_name;'
    ELSE 'SELECT ''uk_name索引不存在，跳过删除'' AS message;'
END;
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 尝试删除可能的唯一约束
SET @sql = CASE 
    WHEN (SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS 
          WHERE TABLE_SCHEMA = DATABASE() 
          AND TABLE_NAME = 'document_categories' 
          AND CONSTRAINT_TYPE = 'UNIQUE'
          AND CONSTRAINT_NAME LIKE '%name%') > 0 
    THEN CONCAT('ALTER TABLE document_categories DROP CONSTRAINT ', 
                (SELECT CONSTRAINT_NAME FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS 
                 WHERE TABLE_SCHEMA = DATABASE() 
                 AND TABLE_NAME = 'document_categories' 
                 AND CONSTRAINT_TYPE = 'UNIQUE'
                 AND CONSTRAINT_NAME LIKE '%name%' LIMIT 1), ';')
    ELSE 'SELECT ''没有找到name相关的唯一约束'' AS message;'
END;
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 4. 添加新的用户内唯一约束
SET @sql = CASE 
    WHEN (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS 
          WHERE TABLE_SCHEMA = DATABASE() 
          AND TABLE_NAME = 'document_categories' 
          AND INDEX_NAME = 'uk_user_id_name') = 0 
    THEN 'ALTER TABLE document_categories ADD UNIQUE INDEX uk_user_id_name (user_id, name);'
    ELSE 'SELECT ''uk_user_id_name约束已存在'' AS message;'
END;
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 5. 为popular_questions表添加userId字段（如果不存在）
SET @sql = CASE 
    WHEN (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
          WHERE TABLE_SCHEMA = DATABASE() 
          AND TABLE_NAME = 'popular_questions' 
          AND COLUMN_NAME = 'user_id') = 0 
    THEN 'ALTER TABLE popular_questions ADD COLUMN user_id VARCHAR(255) NOT NULL DEFAULT ''system'' COMMENT ''用户ID，用于数据隔离'';'
    ELSE 'SELECT ''popular_questions.user_id字段已存在'' AS message;'
END;
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 6. 为popular_questions表添加索引（如果不存在）
SET @sql = CASE 
    WHEN (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS 
          WHERE TABLE_SCHEMA = DATABASE() 
          AND TABLE_NAME = 'popular_questions' 
          AND INDEX_NAME = 'idx_user_id') = 0 
    THEN 'ALTER TABLE popular_questions ADD INDEX idx_user_id (user_id);'
    ELSE 'SELECT ''popular_questions.idx_user_id索引已存在'' AS message;'
END;
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加其他索引
SET @sql = CASE 
    WHEN (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS 
          WHERE TABLE_SCHEMA = DATABASE() 
          AND TABLE_NAME = 'popular_questions' 
          AND INDEX_NAME = 'idx_user_id_category') = 0 
    THEN 'ALTER TABLE popular_questions ADD INDEX idx_user_id_category (user_id, category);'
    ELSE 'SELECT ''idx_user_id_category索引已存在'' AS message;'
END;
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = CASE 
    WHEN (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS 
          WHERE TABLE_SCHEMA = DATABASE() 
          AND TABLE_NAME = 'popular_questions' 
          AND INDEX_NAME = 'idx_user_id_trend_score') = 0 
    THEN 'ALTER TABLE popular_questions ADD INDEX idx_user_id_trend_score (user_id, trend_score DESC);'
    ELSE 'SELECT ''idx_user_id_trend_score索引已存在'' AS message;'
END;
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = CASE 
    WHEN (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS 
          WHERE TABLE_SCHEMA = DATABASE() 
          AND TABLE_NAME = 'popular_questions' 
          AND INDEX_NAME = 'idx_user_id_question_count') = 0 
    THEN 'ALTER TABLE popular_questions ADD INDEX idx_user_id_question_count (user_id, question_count DESC);'
    ELSE 'SELECT ''idx_user_id_question_count索引已存在'' AS message;'
END;
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 7. 为popular_questions添加用户内唯一约束
SET @sql = CASE 
    WHEN (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS 
          WHERE TABLE_SCHEMA = DATABASE() 
          AND TABLE_NAME = 'popular_questions' 
          AND INDEX_NAME = 'uk_user_id_question_pattern_category') = 0 
    THEN 'ALTER TABLE popular_questions ADD UNIQUE INDEX uk_user_id_question_pattern_category (user_id, question_pattern, category);'
    ELSE 'SELECT ''uk_user_id_question_pattern_category约束已存在'' AS message;'
END;
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 8. 创建user_search_settings表（如果不存在）
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

-- 9. 为user_search_settings表添加索引（如果不存在）
SET @sql = CASE 
    WHEN (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS 
          WHERE TABLE_SCHEMA = DATABASE() 
          AND TABLE_NAME = 'user_search_settings' 
          AND INDEX_NAME = 'idx_user_id') = 0 
    THEN 'ALTER TABLE user_search_settings ADD INDEX idx_user_id (user_id);'
    ELSE 'SELECT ''user_search_settings.idx_user_id索引已存在'' AS message;'
END;
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- =====================================================
-- 验证脚本执行结果
-- =====================================================

-- 检查表结构
DESCRIBE document_categories;
DESCRIBE popular_questions;
DESCRIBE user_search_settings;

-- 检查索引创建情况
SHOW INDEX FROM document_categories;
SHOW INDEX FROM popular_questions;
SHOW INDEX FROM user_search_settings;

-- 检查约束情况
SELECT CONSTRAINT_NAME, CONSTRAINT_TYPE 
FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS 
WHERE TABLE_SCHEMA = DATABASE() 
AND TABLE_NAME IN ('document_categories', 'popular_questions', 'user_search_settings');

-- =====================================================
-- 重要提醒
-- =====================================================
-- 1. 此脚本使用动态SQL安全检查，只会添加不存在的字段和索引
-- 2. 执行后需要重启agents服务
-- 3. 如果有报错，请检查具体的错误信息
-- 4. 现有数据会被分配给默认用户'system'
-- ===================================================== 