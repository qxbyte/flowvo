CREATE DATABASE FlowVo;

       -- 创建users表
create table users
(
    id       bigint auto_increment
        primary key,
    username varchar(50)                     not null,
    password varchar(255)                    not null,
    email    varchar(100)                    not null,
    role     varchar(20) default 'ROLE_USER' not null,
    constraint email
        unique (email),
    constraint username
        unique (username)
);


-- 创建订单表
CREATE TABLE IF NOT EXISTS orders (
    id VARCHAR(36) PRIMARY KEY,
    order_number VARCHAR(50) NOT NULL UNIQUE,
    customer_name VARCHAR(100) NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    INDEX idx_order_number (order_number),
    INDEX idx_customer_name (customer_name),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建对话表
CREATE TABLE IF NOT EXISTS conversations (
    id VARCHAR(36) PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    service VARCHAR(50) NOT NULL,
    model VARCHAR(50),
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    INDEX idx_title (title),
    INDEX idx_service (service),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建聊天消息表
CREATE TABLE IF NOT EXISTS chat_messages (
    id VARCHAR(36) PRIMARY KEY not null,
    conversation_id varchar(255) not null,
    role VARCHAR(20) NOT NULL,
    content TEXT NOT NULL,
    tool_call_id VARCHAR(100),
    tool_name VARCHAR(100),
    sequence INT,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    INDEX idx_conversation_id (conversation_id),
    INDEX idx_role (role),
    INDEX idx_sequence (sequence),
    CONSTRAINT fk_conversation FOREIGN KEY (conversation_id) REFERENCES conversations(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;