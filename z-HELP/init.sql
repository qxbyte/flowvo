

--创建数据库用户
GRANT ALL PRIVILEGES ON springaibot.* TO 'springai'@'localhost';
FLUSH PRIVILEGES;


--创建users表
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

CREATE USER 'springuser'@'localhost' IDENTIFIED BY 'Aa111111';


--创建数据库表
CREATE TABLE chat_record (
    id VARCHAR(255) PRIMARY KEY,
    user_id VARCHAR(255),
    title VARCHAR(255),
    create_time DATETIME,
    update_time DATETIME
);

CREATE TABLE messages (
    id VARCHAR(255) PRIMARY KEY,
    chat_id VARCHAR(255),
    role VARCHAR(20),
    content TEXT,
    create_time DATETIME,
    FOREIGN KEY (chat_id) REFERENCES chat_record(id)
);

--添加用户
INSERT INTO springaibot.users (id, username, password, email, role) VALUES (4, 'test', '$2a$10$sBV69kvLDnxqGJVrpmYTw.mpih48xlmMYFZ4zroO5R/ztEo9lBdvG', 'qiang_xue0@outlook.com', 'ROLE_USER');

CREATE TABLE `file_info` (
  `id` VARCHAR(255) NOT NULL PRIMARY KEY,
  `file_name` VARCHAR(255),
  `file_extension` VARCHAR(50),
  `upload_time` DATETIME NOT NULL
);


# function call多轮对话记录
CREATE TABLE call_message (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键 ID',

    chat_id BIGINT NOT NULL COMMENT '所属对话 ID，可用于区分不同对话',
    role VARCHAR(20) NOT NULL COMMENT '角色：user / assistant / tool',

    content TEXT COMMENT '普通对话内容，如果是 tool_calls 则为 null',
    name VARCHAR(100) COMMENT '函数名，仅 tool 用到',
    tool_call_id VARCHAR(100) COMMENT '函数调用 ID，仅 tool 用到',

    tool_calls JSON COMMENT '如果是 assistant 且使用了 tool_calls，完整 JSON 存这里',

    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) COMMENT='聊天记录表（支持函数调用）';
