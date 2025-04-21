-- 创建数据库
CREATE DATABASE springaibot CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
DROP USER IF EXISTS 'springuser'@'%';
-- 创建用户
CREATE USER 'springuser'@'%' IDENTIFIED BY 'Aa111111';
-- 授权访问 springaibot 数据库的所有权限
GRANT ALL PRIVILEGES ON springaibot.* TO 'springuser'@'%';
-- 刷新权限
FLUSH PRIVILEGES;

# 应用登录用户表
CREATE TABLE users (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       username VARCHAR(50) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL,
                       email VARCHAR(100) NOT NULL UNIQUE,
                       role VARCHAR(20) NOT NULL DEFAULT 'ROLE_USER'
);


CREATE TABLE IF NOT EXISTS chat_record (
                                           id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                           name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

CREATE TABLE IF NOT EXISTS message (
                                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                       chat_id BIGINT NOT NULL,
                                       sender VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (chat_id) REFERENCES chat_record(id) ON DELETE CASCADE
    );



ALTER TABLE message MODIFY COLUMN content LONGTEXT;