-- 文件上传功能数据库初始化脚本
USE flowvo;

-- 1. 更新chat_messages表，添加attachments字段
ALTER TABLE chat_messages 
ADD COLUMN IF NOT EXISTS attachments JSON;

-- 2. 创建文件附件表
CREATE TABLE IF NOT EXISTS file_attachments (
    id VARCHAR(36) PRIMARY KEY,
    file_name VARCHAR(255) NOT NULL,
    file_size BIGINT NOT NULL,
    file_type VARCHAR(100) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_url VARCHAR(500),
    conversation_id VARCHAR(36),
    user_id VARCHAR(36) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_conversation_id (conversation_id),
    INDEX idx_user_id (user_id),
    INDEX idx_created_at (created_at)
);

-- 3. 创建上传目录（需要手动创建）
-- mkdir -p uploads && chmod 755 uploads

SELECT 'File upload database schema initialized successfully!' as result; 