-- 修复文档表中的分类字段
-- 将分类名称转换为分类ID

-- 备份当前数据（可选）
-- CREATE TABLE documents_backup AS SELECT * FROM documents;

-- 更新文档表的分类字段，将分类名称转换为分类ID
UPDATE documents d 
JOIN document_categories dc ON d.category = dc.name 
SET d.category = dc.id
WHERE d.category IS NOT NULL 
  AND d.category != dc.id;

-- 验证更新结果
SELECT 
    d.id as document_id,
    d.name as document_name,
    d.category as category_id,
    dc.name as category_name,
    d.user_id
FROM documents d
LEFT JOIN document_categories dc ON d.category = dc.id
WHERE d.category IS NOT NULL
ORDER BY d.updated_at DESC
LIMIT 10;

-- 检查是否还有未匹配的分类
SELECT DISTINCT d.category as unmapped_category
FROM documents d
LEFT JOIN document_categories dc ON d.category = dc.id
WHERE d.category IS NOT NULL 
  AND dc.id IS NULL; 