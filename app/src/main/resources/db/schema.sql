CREATE DATABASE FlowVo;

-- 创建users表
create table users
(
    id       bigint auto_increment
        primary key,
    username varchar(50)                     not null COMMENT '用户名',
    password varchar(255)                    not null COMMENT '密码',
    nickname varchar(50)                     not null COMMENT '昵称',
    email    varchar(100)                    not null COMMENT '邮箱',
    role     varchar(20) default 'ROLE_USER' not null COMMENT '角色',
    avatar_url varchar(255)                  null COMMENT '头像URL',
    constraint email
        unique (email),
    constraint username
        unique (username)
);


-- 创建订单表
CREATE TABLE IF NOT EXISTS orders (
    id VARCHAR(36) PRIMARY KEY COMMENT '主键ID',
    order_number VARCHAR(50) NOT NULL UNIQUE COMMENT '订单号',
    customer_name VARCHAR(100) NOT NULL COMMENT '客户姓名',
    amount DECIMAL(10, 2) NOT NULL COMMENT '订单金额',
    status VARCHAR(20) NOT NULL COMMENT '订单状态',
    created_at DATETIME NOT NULL COMMENT '创建时间',
    updated_at DATETIME NOT NULL COMMENT '更新时间',
    INDEX idx_order_number (order_number),
    INDEX idx_customer_name (customer_name),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建对话表
CREATE TABLE IF NOT EXISTS conversations (
    id VARCHAR(36) PRIMARY KEY COMMENT '主键ID',
    title VARCHAR(255) NOT NULL COMMENT '对话名称',
    service VARCHAR(50) NOT NULL COMMENT 'mcp服务名',
    model VARCHAR(50) COMMENT '模型名',
    user_id VARCHAR(50) COMMENT '用户ID',
    source VARCHAR(20) COMMENT '对话来源',
    created_at DATETIME NOT NULL COMMENT '创建时间',
    updated_at DATETIME NOT NULL COMMENT '更新时间',
    INDEX idx_title (title),
    INDEX idx_service (service),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;



-- 创建聊天消息表
CREATE TABLE IF NOT EXISTS chat_messages (
    id VARCHAR(36) PRIMARY KEY not null COMMENT '主键ID',
    conversation_id varchar(255) not null COMMENT '对话ID',
    role VARCHAR(20) NOT NULL COMMENT '角色',
    content TEXT NOT NULL COMMENT '信息',
    tool_call_id VARCHAR(100) ,
    tool_name VARCHAR(100) COMMENT '工具调用function名',
    tool_calls JSON COMMENT '工具调用的function',
    sequence INT COMMENT '序号',
    attachments JSON COMMENT '对话附件，图片为base64编码，文件为文档内容',
    created_at DATETIME NOT NULL COMMENT '创建时间',
    updated_at DATETIME NOT NULL COMMENT '更新时间',
    INDEX idx_conversation_id (conversation_id),
    INDEX idx_role (role),
    INDEX idx_sequence (sequence),
    CONSTRAINT fk_conversation FOREIGN KEY (conversation_id) REFERENCES conversations(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

