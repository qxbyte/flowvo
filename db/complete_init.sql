-- ======================================
-- FlowVO 完整数据库初始化脚本
-- 创建时间: 2025年6月2日
-- 版本: v1.0
-- 包含所有表结构和索引
-- ======================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS FlowVo CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE FlowVo;

-- 创建数据库用户（可选，根据需要使用）
-- CREATE USER IF NOT EXISTS 'flowvo'@'localhost' IDENTIFIED BY 'Aa111111';
-- GRANT ALL PRIVILEGES ON FlowVo.* TO 'flowvo'@'localhost';
-- FLUSH PRIVILEGES;

-- ======================================
-- 1. 用户管理相关表
-- ======================================

-- 创建用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码',
    nickname VARCHAR(50) NULL COMMENT '昵称',
    email VARCHAR(100) NOT NULL COMMENT '邮箱',
    role VARCHAR(20) DEFAULT 'ROLE_USER' NOT NULL COMMENT '角色',
    avatar_url VARCHAR(255) NULL COMMENT '头像URL',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_username (username),
    UNIQUE KEY uk_email (email),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ======================================
-- 2. 对话聊天相关表
-- ======================================

-- 创建对话表
CREATE TABLE IF NOT EXISTS conversations (
    id VARCHAR(36) PRIMARY KEY COMMENT '主键ID',
    title VARCHAR(255) NOT NULL COMMENT '对话标题',
    service VARCHAR(50) NOT NULL COMMENT 'MCP服务名',
    model VARCHAR(50) NULL COMMENT '模型名称',
    user_id VARCHAR(50) NULL COMMENT '用户ID',
    source VARCHAR(20) NULL COMMENT '对话来源',
    created_at DATETIME NOT NULL COMMENT '创建时间',
    updated_at DATETIME NOT NULL COMMENT '更新时间',
    INDEX idx_title (title),
    INDEX idx_service (service),
    INDEX idx_user_id (user_id),
    INDEX idx_source (source),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='对话表';

-- 创建聊天消息表
CREATE TABLE IF NOT EXISTS chat_messages (
    id VARCHAR(36) PRIMARY KEY COMMENT '主键ID',
    conversation_id VARCHAR(255) NOT NULL COMMENT '对话ID',
    role VARCHAR(20) NOT NULL COMMENT '角色(user/assistant/tool)',
    content TEXT NOT NULL COMMENT '消息内容',
    tool_call_id VARCHAR(100) NULL COMMENT '工具调用ID',
    tool_name VARCHAR(100) NULL COMMENT '工具调用function名',
    tool_calls JSON NULL COMMENT '工具调用的function JSON',
    sequence INT NULL COMMENT '消息序号',
    attachments JSON NULL COMMENT '对话附件，图片为base64编码，文件为文档内容',
    created_at DATETIME NOT NULL COMMENT '创建时间',
    updated_at DATETIME NOT NULL COMMENT '更新时间',
    INDEX idx_conversation_id (conversation_id),
    INDEX idx_role (role),
    INDEX idx_sequence (sequence),
    INDEX idx_created_at (created_at),
    CONSTRAINT fk_chat_conversation FOREIGN KEY (conversation_id) REFERENCES conversations(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天消息表';

-- 创建Function Call多轮对话记录表
CREATE TABLE IF NOT EXISTS call_message (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    chat_id VARCHAR(100) NOT NULL COMMENT '所属对话ID，可用于区分不同对话',
    role VARCHAR(20) NOT NULL COMMENT '角色：user / assistant / tool',
    content TEXT NULL COMMENT '普通对话内容，如果是tool_calls则为null',
    name VARCHAR(100) NULL COMMENT '函数名，仅tool用到',
    tool_call_id VARCHAR(100) NULL COMMENT '函数调用ID，仅tool用到',
    tool_calls JSON NULL COMMENT '如果是assistant且使用了tool_calls，完整JSON存这里',
    created_at DATETIME(3) DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_chat_id (chat_id),
    INDEX idx_role (role),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Function Call聊天记录表';

-- ======================================
-- 3. 文档管理相关表
-- ======================================

-- 创建文档表
CREATE TABLE IF NOT EXISTS documents (
    id VARCHAR(255) PRIMARY KEY COMMENT '主键ID',
    name VARCHAR(255) NULL COMMENT '文件名',
    description MEDIUMTEXT NULL COMMENT '文档描述',
    content MEDIUMTEXT NULL COMMENT '全量文本内容',
    file_path VARCHAR(255) NULL COMMENT '文件路径',
    type VARCHAR(50) NULL COMMENT '文件扩展名',
    size BIGINT NULL COMMENT '文件大小',
    chunk_count INT NULL COMMENT '文档切分数量',
    status VARCHAR(36) NULL COMMENT '处理状态',
    user_id VARCHAR(36) NOT NULL COMMENT '用户ID',
    created_at DATETIME(3) NOT NULL COMMENT '创建时间',
    updated_at DATETIME(3) NOT NULL COMMENT '更新时间',
    INDEX idx_name (name),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_type (type),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文档表';

-- 创建文档标签表
CREATE TABLE IF NOT EXISTS document_tags (
    document_id VARCHAR(255) NOT NULL COMMENT '文档关联ID',
    tag INT NOT NULL COMMENT '标签',
    INDEX idx_document_id (document_id),
    INDEX idx_tag (tag),
    CONSTRAINT fk_document_tags FOREIGN KEY (document_id) REFERENCES documents(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文档标签表';

-- ======================================
-- 4. 文件附件相关表
-- ======================================

-- 创建文件附件表
CREATE TABLE IF NOT EXISTS file_attachments (
    id VARCHAR(36) PRIMARY KEY COMMENT '主键ID',
    file_name VARCHAR(255) NOT NULL COMMENT '文件名',
    file_size BIGINT NOT NULL COMMENT '文件大小',
    file_type VARCHAR(100) NOT NULL COMMENT '文件类型',
    file_path VARCHAR(500) NOT NULL COMMENT '文件路径',
    file_url VARCHAR(500) NULL COMMENT '文件访问URL',
    conversation_id VARCHAR(36) NULL COMMENT '关联对话ID',
    user_id VARCHAR(36) NOT NULL COMMENT '用户ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_conversation_id (conversation_id),
    INDEX idx_user_id (user_id),
    INDEX idx_file_name (file_name),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文件附件表';

-- 创建文件信息表
CREATE TABLE IF NOT EXISTS file_info (
    id VARCHAR(255) PRIMARY KEY COMMENT '主键ID',
    file_name VARCHAR(255) NULL COMMENT '文件名',
    file_extension VARCHAR(50) NULL COMMENT '文件扩展名',
    upload_time DATETIME NOT NULL COMMENT '上传时间',
    INDEX idx_file_name (file_name),
    INDEX idx_upload_time (upload_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文件信息表';

-- ======================================
-- 5. 订单管理相关表
-- ======================================

-- 创建订单表
CREATE TABLE IF NOT EXISTS orders (
    id VARCHAR(36) PRIMARY KEY COMMENT '主键ID',
    order_number VARCHAR(50) NOT NULL COMMENT '订单号',
    customer_name VARCHAR(100) NOT NULL COMMENT '客户姓名',
    amount DECIMAL(10, 2) NOT NULL COMMENT '订单金额',
    status VARCHAR(20) NOT NULL COMMENT '订单状态',
    created_at DATETIME NOT NULL COMMENT '创建时间',
    updated_at DATETIME NOT NULL COMMENT '更新时间',
    UNIQUE KEY uk_order_number (order_number),
    INDEX idx_customer_name (customer_name),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';

-- ======================================
-- 6. 业务管理相关表
-- ======================================

-- 创建业务表
CREATE TABLE IF NOT EXISTS business (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '业务ID',
    name VARCHAR(100) NOT NULL COMMENT '业务名称',
    type VARCHAR(100) NOT NULL COMMENT '业务类型',
    status VARCHAR(20) NOT NULL COMMENT '状态（运行中/已停止）',
    create_time DATETIME NOT NULL COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_name (name),
    INDEX idx_type (type),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='业务表';

-- 创建客户表
CREATE TABLE IF NOT EXISTS customer (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '客户ID',
    name VARCHAR(100) NOT NULL COMMENT '客户名称',
    contact_person VARCHAR(100) NULL COMMENT '联系人',
    contact_phone VARCHAR(20) NULL COMMENT '联系电话',
    level VARCHAR(50) NULL COMMENT '客户等级（VIP/重要客户）',
    latest_order_time DATETIME NULL COMMENT '最近下单时间',
    total_order INT DEFAULT 0 COMMENT '总订单数',
    create_time DATETIME NOT NULL COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_name (name),
    INDEX idx_level (level),
    INDEX idx_latest_order_time (latest_order_time),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='客户表';

-- ======================================
-- 7. 数据库优化和设置
-- ======================================

-- 设置字符集和排序规则
ALTER DATABASE FlowVo CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- ======================================
-- 8. 初始化数据
-- ======================================

-- 插入默认用户（可选）
-- INSERT IGNORE INTO users (username, password, nickname, email, role) 
-- VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', '管理员', 'admin@flowvo.com', 'ROLE_ADMIN');

-- ======================================
-- 9. 完成信息
-- ======================================

SELECT 'FlowVO 数据库初始化完成！' as result,
       'Database: FlowVo' as database_name,
       'Character Set: utf8mb4' as charset,
       'Collation: utf8mb4_unicode_ci' as collation;

-- 显示所有创建的表
SHOW TABLES; 