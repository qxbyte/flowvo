-- 知识库问答功能相关表结构
-- 创建时间：2025年6月
-- 描述：实现知识库问答、最近提问、热门问题等功能

-- 1. 知识库问答记录表
CREATE TABLE knowledge_qa_records (
    id VARCHAR(50) PRIMARY KEY COMMENT '问答记录ID',
    user_id VARCHAR(50) NOT NULL COMMENT '用户ID',
    question TEXT NOT NULL COMMENT '用户问题',
    answer LONGTEXT COMMENT 'AI回答内容',
    context_sources JSON COMMENT '信息来源（文档块信息）',
    question_category VARCHAR(100) COMMENT '问题分类（基于文档类型）',
    question_keywords VARCHAR(500) COMMENT '问题关键词（用于热门问题统计）',
    response_time_ms INTEGER COMMENT '响应时间（毫秒）',
    similarity_score DECIMAL(5,4) COMMENT '向量相似度得分',
    feedback_rating TINYINT COMMENT '用户反馈评分(1-5)',
    feedback_comment VARCHAR(500) COMMENT '用户反馈意见',
    status ENUM('PROCESSING', 'COMPLETED', 'FAILED') DEFAULT 'PROCESSING' COMMENT '处理状态',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_id (user_id),
    INDEX idx_question_category (question_category),
    INDEX idx_created_at (created_at),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='知识库问答记录表';

-- 2. 文档分类表（扩展documents表的分类功能）
CREATE TABLE document_categories (
    id VARCHAR(50) PRIMARY KEY COMMENT '分类ID',
    name VARCHAR(100) NOT NULL COMMENT '分类名称',
    description TEXT COMMENT '分类描述',
    icon VARCHAR(100) COMMENT '分类图标',
    sort_order INTEGER DEFAULT 0 COMMENT '排序序号',
    status ENUM('ACTIVE', 'INACTIVE') DEFAULT 'ACTIVE' COMMENT '状态',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_name (name),
    INDEX idx_sort_order (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文档分类表';

-- 3. 热门问题统计表
CREATE TABLE popular_questions (
    id VARCHAR(50) PRIMARY KEY COMMENT '统计ID',
    question_pattern VARCHAR(500) NOT NULL COMMENT '问题模式（经过标准化处理）',
    category VARCHAR(100) COMMENT '问题分类',
    question_count INTEGER DEFAULT 1 COMMENT '问题出现次数',
    last_asked_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '最后提问时间',
    trend_score DECIMAL(10,4) DEFAULT 0 COMMENT '趋势得分（基于时间衰减和频次）',
    representative_question TEXT COMMENT '代表性问题（最完整的问题示例）',
    standard_answer LONGTEXT COMMENT '标准答案（可选，用于快速回复）',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_question_pattern_category (question_pattern, category),
    INDEX idx_question_count (question_count),
    INDEX idx_trend_score (trend_score),
    INDEX idx_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='热门问题统计表';

-- 4. 为现有documents表添加分类字段（如果不存在）
ALTER TABLE documents ADD COLUMN category VARCHAR(100) COMMENT '文档分类';
ALTER TABLE documents ADD INDEX idx_category (category);

-- 5. 插入默认的文档分类数据
INSERT INTO document_categories (id, name, description, icon, sort_order) VALUES 
('cat_user_manual', '用户手册', '产品使用指南和用户操作手册', 'book', 1),
('cat_technical_doc', '技术文档', '开发文档、API文档、技术规范', 'code', 2),
('cat_training_material', '培训材料', '培训课件、学习资料', 'graduation-cap', 3),
('cat_faq', '常见问题', '常见问题解答、疑难解答', 'question-circle', 4),
('cat_policy', '政策制度', '公司政策、规章制度', 'file-text', 5),
('cat_other', '其他', '其他类型文档', 'folder', 99)
ON DUPLICATE KEY UPDATE 
name = VALUES(name),
description = VALUES(description),
icon = VALUES(icon),
sort_order = VALUES(sort_order);

-- 6. 创建用于统计的视图
CREATE OR REPLACE VIEW knowledge_base_statistics AS
SELECT 
    d.category,
    dc.name as category_name,
    dc.icon as category_icon,
    COUNT(d.id) as document_count,
    MAX(d.updated_at) as last_updated_time,
    AVG(CASE WHEN d.status = 'COMPLETED' THEN 1 ELSE 0 END) as completion_rate
FROM documents d
LEFT JOIN document_categories dc ON d.category = dc.id
WHERE d.status != 'FAILED'
GROUP BY d.category, dc.name, dc.icon;

-- 7. 创建用于最近提问的视图
CREATE OR REPLACE VIEW recent_questions AS
SELECT 
    qa.id,
    qa.question,
    qa.answer,
    qa.question_category,
    dc.name as category_name,
    qa.created_at,
    qa.feedback_rating
FROM knowledge_qa_records qa
LEFT JOIN document_categories dc ON qa.question_category = dc.id
WHERE qa.status = 'COMPLETED'
ORDER BY qa.created_at DESC;

-- 8. 创建用于热门问题的视图
CREATE OR REPLACE VIEW hot_questions AS
SELECT 
    pq.id,
    pq.representative_question as question,
    pq.standard_answer as answer,
    pq.category,
    dc.name as category_name,
    pq.question_count,
    pq.trend_score,
    pq.last_asked_time
FROM popular_questions pq
LEFT JOIN document_categories dc ON pq.category = dc.id
WHERE pq.question_count >= 2  -- 至少被问过2次
ORDER BY pq.trend_score DESC, pq.question_count DESC; 