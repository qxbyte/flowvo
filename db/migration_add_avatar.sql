-- 为现有users表添加avatar_url字段
ALTER TABLE users ADD COLUMN avatar_url VARCHAR(255) NULL COMMENT '头像URL';

-- 更新已存在的用户记录，设置默认头像为空
UPDATE users SET avatar_url = NULL WHERE avatar_url IS NULL; 