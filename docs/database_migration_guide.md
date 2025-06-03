# 数据库迁移操作指南

## 问题说明
用户添加分类后知识库分类统计不显示，原因是数据库表缺少`user_id`字段进行用户数据隔离。

## 解决方案

### 方案1: 使用MySQL客户端工具（推荐）

#### A. 命令行方式
```bash
# 如果有MySQL客户端
mysql -u root -p flowvo < agents/database_migration_user_isolation_fixed.sql

# 或者分步执行
mysql -u root -p flowvo
# 然后复制粘贴脚本内容
```

#### B. MySQL Workbench方式
1. 打开MySQL Workbench
2. 连接到flowvo数据库
3. 打开`agents/database_migration_user_isolation_fixed.sql`文件
4. 执行整个脚本

#### C. phpMyAdmin方式
1. 访问phpMyAdmin界面
2. 选择flowvo数据库
3. 点击"SQL"选项卡
4. 复制粘贴迁移脚本内容并执行

### 方案2: 手动SQL执行（如果自动脚本有问题）

#### 核心SQL语句
```sql
-- 1. 为document_categories表添加user_id字段
ALTER TABLE document_categories 
ADD COLUMN user_id VARCHAR(255) DEFAULT 'system' COMMENT '用户ID，用于数据隔离';

-- 2. 为popular_questions表添加user_id字段  
ALTER TABLE popular_questions 
ADD COLUMN user_id VARCHAR(255) DEFAULT 'system' COMMENT '用户ID，用于数据隔离';

-- 3. 创建用户搜索设置表
CREATE TABLE IF NOT EXISTS user_search_settings (
    id VARCHAR(50) PRIMARY KEY COMMENT '设置ID',
    user_id VARCHAR(255) NOT NULL COMMENT '用户ID',
    top_k INT DEFAULT 5 COMMENT '检索数量',
    similarity_threshold DECIMAL(3,2) DEFAULT 0.70 COMMENT '相似度阈值',
    max_tokens INT DEFAULT 2000 COMMENT '最大令牌数',
    temperature DECIMAL(3,2) DEFAULT 0.70 COMMENT '温度参数',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户搜索设置表';

-- 4. 添加索引
ALTER TABLE document_categories ADD INDEX idx_user_id_status (user_id, status);
ALTER TABLE popular_questions ADD INDEX idx_user_id_category (user_id, category);

-- 5. 验证修改
DESCRIBE document_categories;
DESCRIBE popular_questions;
DESCRIBE user_search_settings;
```

### 方案3: 应用级解决（临时方案）

如果暂时无法执行数据库迁移，代码已经包含降级逻辑：

1. **重启agents服务** - 代码修复已应用
2. **用户数据隔离** - 基于entity的userId字段工作
3. **分类统计** - 使用降级查询机制

## 验证步骤

### 1. 检查数据库结构
```sql
-- 检查字段是否添加成功
SHOW COLUMNS FROM document_categories LIKE 'user_id';
SHOW COLUMNS FROM popular_questions LIKE 'user_id';

-- 检查表是否创建成功
SHOW TABLES LIKE 'user_search_settings';
```

### 2. 验证功能
1. 不同用户登录系统
2. 创建新的文档分类
3. 检查知识库页面分类统计是否正确显示
4. 确认用户只能看到自己的分类

### 3. 检查日志
```bash
# 查看agents服务日志
tail -f agents/logs/application.log | grep -i "用户分类"
```

## 常见问题

### Q: 执行脚本报错"table already exists"
A: 这是正常的，脚本使用了`IF NOT EXISTS`，可以安全重复执行

### Q: 新建分类仍然不显示
A: 检查agents服务是否重启，以及日志中是否有相关错误信息

### Q: 用户看到其他用户的分类  
A: 数据库迁移可能未成功，检查user_id字段是否正确添加

## 技术支持

如果遇到问题：
1. 检查agents服务日志文件
2. 确认数据库连接正常
3. 验证用户认证状态
4. 检查迁移脚本执行结果

## 安全提醒

⚠️ **执行前请备份数据库**
```bash
mysqldump -u root -p flowvo > flowvo_backup_$(date +%Y%m%d_%H%M%S).sql
``` 