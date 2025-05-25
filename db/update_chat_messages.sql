-- 更新chat_messages表，添加attachments字段
ALTER TABLE chat_messages 
ADD COLUMN IF NOT EXISTS attachments JSON; 