-- 综合修复所有表中的用户ID数据问题
-- 将所有表中存储用户名的user_id字段统一转换为用户表的数字ID

-- 备份所有相关表（可选，建议执行前取消注释）
-- CREATE TABLE documents_backup AS SELECT * FROM documents;
-- CREATE TABLE document_categories_backup AS SELECT * FROM document_categories;
-- CREATE TABLE knowledge_qa_records_backup AS SELECT * FROM knowledge_qa_records;
-- CREATE TABLE popular_questions_backup AS SELECT * FROM popular_questions;
-- CREATE TABLE user_search_settings_backup AS SELECT * FROM user_search_settings;
-- CREATE TABLE orders_backup AS SELECT * FROM orders;
-- CREATE TABLE conversations_backup AS SELECT * FROM conversations;
-- CREATE TABLE chat_messages_backup AS SELECT * FROM chat_messages;
-- CREATE TABLE file_attachments_backup AS SELECT * FROM file_attachments;

-- 查看users表结构，确认ID字段类型
SELECT 'Users表信息:' as info;
SELECT id, username, nickname, email FROM users ORDER BY id LIMIT 10;

-- 1. 修复documents表的user_id字段（如果存储的是用户名）
SELECT 'documents表修复前状态:' as info;
SELECT d.id, d.name, d.user_id, u.id as correct_user_id, u.username
FROM documents d
LEFT JOIN users u ON d.user_id = u.username
WHERE d.user_id IS NOT NULL
LIMIT 10;

UPDATE documents d 
JOIN users u ON d.user_id = u.username 
SET d.user_id = u.id
WHERE d.user_id = u.username;

-- 2. 修复documents表的category字段（将分类名称转换为分类ID）
SELECT 'documents表分类字段修复:' as info;
UPDATE documents d 
JOIN document_categories dc ON d.category = dc.name 
SET d.category = dc.id
WHERE d.category IS NOT NULL 
  AND d.category != dc.id;

-- 3. 修复document_categories表的user_id字段
SELECT 'document_categories表修复前状态:' as info;
SELECT dc.id, dc.name, dc.user_id, u.id as correct_user_id, u.username
FROM document_categories dc
LEFT JOIN users u ON dc.user_id = u.username
WHERE dc.user_id IS NOT NULL
LIMIT 10;

UPDATE document_categories dc 
JOIN users u ON dc.user_id = u.username 
SET dc.user_id = u.id
WHERE dc.user_id = u.username;

-- 4. 修复knowledge_qa_records表的user_id字段
SELECT 'knowledge_qa_records表修复前状态:' as info;
SELECT kqr.id, kqr.question, kqr.user_id, u.id as correct_user_id, u.username
FROM knowledge_qa_records kqr
LEFT JOIN users u ON kqr.user_id = u.username
WHERE kqr.user_id IS NOT NULL
LIMIT 10;

UPDATE knowledge_qa_records kqr 
JOIN users u ON kqr.user_id = u.username 
SET kqr.user_id = u.id
WHERE kqr.user_id = u.username;

-- 5. 修复popular_questions表的user_id字段
SELECT 'popular_questions表修复前状态:' as info;
SELECT pq.id, pq.question_pattern, pq.user_id, u.id as correct_user_id, u.username
FROM popular_questions pq
LEFT JOIN users u ON pq.user_id = u.username
WHERE pq.user_id IS NOT NULL
LIMIT 10;

UPDATE popular_questions pq 
JOIN users u ON pq.user_id = u.username 
SET pq.user_id = u.id
WHERE pq.user_id = u.username;

-- 6. 修复user_search_settings表的user_id字段
SELECT 'user_search_settings表修复前状态:' as info;
SELECT uss.id, uss.user_id, u.id as correct_user_id, u.username
FROM user_search_settings uss
LEFT JOIN users u ON uss.user_id = u.username
WHERE uss.user_id IS NOT NULL
LIMIT 10;

UPDATE user_search_settings uss 
JOIN users u ON uss.user_id = u.username 
SET uss.user_id = u.id
WHERE uss.user_id = u.username;

-- 7. 修复orders表的user_id字段（如果存在且存储用户名）
SELECT 'orders表修复前状态:' as info;
SELECT o.id, o.order_number, o.user_id, u.id as correct_user_id, u.username
FROM orders o
LEFT JOIN users u ON o.user_id = u.username
WHERE o.user_id IS NOT NULL
LIMIT 10;

UPDATE orders o 
JOIN users u ON o.user_id = u.username 
SET o.user_id = u.id
WHERE o.user_id = u.username;

-- 8. 修复conversations表的user_id字段（如果存在）
SELECT 'conversations表修复前状态:' as info;
SELECT c.id, c.title, c.user_id, u.id as correct_user_id, u.username
FROM conversations c
LEFT JOIN users u ON c.user_id = u.username
WHERE c.user_id IS NOT NULL
LIMIT 10;

UPDATE conversations c 
JOIN users u ON c.user_id = u.username 
SET c.user_id = u.id
WHERE c.user_id = u.username;

-- 9. 修复file_attachments表的user_id字段（如果存在）
SELECT 'file_attachments表修复前状态:' as info;
SELECT fa.id, fa.file_name, fa.user_id, u.id as correct_user_id, u.username
FROM file_attachments fa
LEFT JOIN users u ON fa.user_id = u.username
WHERE fa.user_id IS NOT NULL
LIMIT 10;

UPDATE file_attachments fa 
JOIN users u ON fa.user_id = u.username 
SET fa.user_id = u.id
WHERE fa.user_id = u.username;

-- 10. 处理core模块的表（如果存在于同一数据库）
-- 修复chat_record表的user_id字段
UPDATE chat_record cr 
JOIN users u ON cr.user_id = u.username 
SET cr.user_id = u.id
WHERE cr.user_id = u.username;

-- 修复file_info表的user_id字段（如果类型不匹配需要转换）
-- 注意：file_info表的user_id是Long类型，可能需要特殊处理

-- 验证修复结果
SELECT '=== 修复结果验证 ===' as verification;

-- 验证documents表
SELECT 'documents表验证:' as info;
SELECT 
    d.id as document_id,
    d.name as document_name,
    d.user_id,
    d.category as category_id,
    dc.name as category_name,
    u.username
FROM documents d
LEFT JOIN users u ON d.user_id = u.id
LEFT JOIN document_categories dc ON d.category = dc.id
WHERE d.user_id IS NOT NULL
ORDER BY d.updated_at DESC
LIMIT 10;

-- 验证document_categories表
SELECT 'document_categories表验证:' as info;
SELECT 
    dc.id as category_id,
    dc.name as category_name,
    dc.user_id,
    u.username
FROM document_categories dc
LEFT JOIN users u ON dc.user_id = u.id
WHERE dc.user_id IS NOT NULL
ORDER BY dc.updated_at DESC
LIMIT 10;

-- 验证knowledge_qa_records表
SELECT 'knowledge_qa_records表验证:' as info;
SELECT 
    kqr.id,
    LEFT(kqr.question, 50) as question_preview,
    kqr.user_id,
    u.username
FROM knowledge_qa_records kqr
LEFT JOIN users u ON kqr.user_id = u.id
WHERE kqr.user_id IS NOT NULL
ORDER BY kqr.created_at DESC
LIMIT 10;

-- 检查是否还有未修复的数据
SELECT '=== 未修复数据检查 ===' as unmapped_check;

-- 检查documents表中是否还有无效的user_id
SELECT 'documents表未匹配用户:' as info;
SELECT DISTINCT d.user_id as unmapped_user_id
FROM documents d
LEFT JOIN users u ON d.user_id = u.id
WHERE d.user_id IS NOT NULL 
  AND u.id IS NULL;

-- 检查documents表中是否还有无效的category
SELECT 'documents表未匹配分类:' as info;
SELECT DISTINCT d.category as unmapped_category
FROM documents d
LEFT JOIN document_categories dc ON d.category = dc.id
WHERE d.category IS NOT NULL 
  AND dc.id IS NULL;

-- 检查document_categories表中是否还有无效的user_id
SELECT 'document_categories表未匹配用户:' as info;
SELECT DISTINCT dc.user_id as unmapped_user_id
FROM document_categories dc
LEFT JOIN users u ON dc.user_id = u.id
WHERE dc.user_id IS NOT NULL 
  AND u.id IS NULL; 