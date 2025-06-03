# 用户数据隔离验证指南

## 概述
本文档描述如何验证向量数据库用户隔离修复是否成功，确保系统安全性。

## 🔍 验证步骤

### 1. 数据库迁移验证
```bash
# 1. 执行迁移脚本
mysql -u root -p flowvo < agents/database_migration_user_isolation_fixed.sql

# 2. 验证表结构
mysql -u root -p flowvo -e "DESCRIBE document_categories;" | grep user_id
mysql -u root -p flowvo -e "DESCRIBE popular_questions;" | grep user_id
mysql -u root -p flowvo -e "DESCRIBE user_search_settings;"

# 3. 验证索引
mysql -u root -p flowvo -e "SHOW INDEX FROM document_categories;" | grep user_id
mysql -u root -p flowvo -e "SHOW INDEX FROM popular_questions;" | grep user_id
```

### 2. 服务启动验证
```bash
# 重启agents服务
cd agents
./mvnw spring-boot:run

# 检查启动日志，确认无用户隔离相关错误
tail -f logs/application.log | grep -i "user"
```

### 3. 功能验证测试

#### A. 用户注册和登录
1. 创建测试用户A和用户B
2. 分别登录，确认JWT令牌正确

#### B. 文档上传测试
```bash
# 用户A上传文档
curl -X POST "http://localhost:8081/api/documents/upload" \
  -H "Authorization: Bearer $USER_A_TOKEN" \
  -F "file=@test_doc_a.pdf" \
  -F "userId=userA" \
  -F "category=cat_manual"

# 用户B上传文档  
curl -X POST "http://localhost:8081/api/documents/upload" \
  -H "Authorization: Bearer $USER_B_TOKEN" \
  -F "file=@test_doc_b.pdf" \
  -F "userId=userB" \
  -F "category=cat_manual"
```

#### C. 向量检索隔离验证
```bash
# 用户A进行知识库问答
curl -X POST "http://localhost:8081/api/knowledge-qa/ask" \
  -H "Authorization: Bearer $USER_A_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "question": "测试问题",
    "userId": "userA",
    "topK": 5,
    "similarityThreshold": 0.7
  }'

# 用户B进行同样的问答
curl -X POST "http://localhost:8081/api/knowledge-qa/ask" \
  -H "Authorization: Bearer $USER_B_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "question": "测试问题", 
    "userId": "userB",
    "topK": 5,
    "similarityThreshold": 0.7
  }'
```

### 4. 安全验证检查点

#### ✅ 预期正确行为
1. **向量检索日志**: 包含`user_id == 'userA'`过滤条件
2. **检索结果**: 用户A只能看到自己上传的文档相关内容
3. **分类管理**: 用户A只能看到自己创建的分类
4. **热门问题**: 用户A只能看到自己的问答历史生成的热门问题

#### ❌ 安全风险指标
1. 向量检索无用户ID过滤
2. 跨用户能看到其他用户的文档内容
3. 分类管理显示全局数据
4. 热门问题显示其他用户的问题

### 5. 日志验证
检查以下关键日志信息：

```bash
# 向量检索日志
grep "向量检索参数" logs/application.log | tail -5
# 应该包含: userId=xxx, filter=user_id == 'xxx'

# 用户隔离日志  
grep "获取用户" logs/application.log | tail -5
# 应该显示正确的用户ID获取

# 错误日志检查
grep -i "error\|exception" logs/application.log | grep -i "user"
# 不应该有用户相关的错误
```

### 6. 前端验证
1. 不同用户登录前端系统
2. 进入知识库页面
3. 确认看到的数据都是各自的数据：
   - 分类列表只显示自己的分类
   - 最近提问只显示自己的问答记录
   - 热门问题只显示自己的问题
   - 知识库统计只显示自己的文档统计

## 🛡️ 安全测试场景

### 场景1: 跨用户数据访问测试
1. 用户A上传包含敏感信息的文档
2. 用户B尝试通过各种问题检索是否能获得用户A的文档内容
3. 预期结果：用户B完全无法获取用户A的任何文档信息

### 场景2: API直接调用测试
1. 尝试在用户A的token下直接指定用户B的userId
2. 系统应该忽略请求中的userId，使用token中的用户身份
3. 预期结果：依然只能访问用户A的数据

### 场景3: 分类权限测试
1. 用户A创建分类"机密文档"
2. 用户B尝试查看、编辑、删除该分类
3. 预期结果：用户B无法看到或操作用户A的分类

## 📊 验证报告模板

```
用户隔离验证报告
==================

验证时间: [日期时间]
验证环境: [开发/测试/生产]

✅ 数据库迁移: [成功/失败]
✅ 服务启动: [成功/失败] 
✅ 向量检索隔离: [成功/失败]
✅ 分类管理隔离: [成功/失败]
✅ 热门问题隔离: [成功/失败]
✅ 跨用户安全测试: [通过/失败]

备注: [详细说明]
验证人: [姓名]
```

## 🚨 问题排查

### 常见问题
1. **向量检索无用户过滤**: 检查searchRelevantDocuments方法是否正确添加了user_id过滤
2. **数据库字段缺失**: 确认迁移脚本执行成功，user_id字段已添加
3. **认证失败**: 检查JWT token是否正确，用户ID是否正确提取

### 调试命令
```bash
# 检查向量数据库metadata
# 确认存储的向量包含user_id信息

# 检查MySQL数据
mysql -u root -p flowvo -e "SELECT id, name, user_id FROM document_categories LIMIT 5;"
mysql -u root -p flowvo -e "SELECT id, question_pattern, user_id FROM popular_questions LIMIT 5;"

# 检查认证
curl -X GET "http://localhost:8081/api/knowledge-qa/categories" \
  -H "Authorization: Bearer $TOKEN" \
  -v
```

## 结论
完成以上验证步骤后，确认系统已实现完整的用户数据隔离，消除了向量数据库跨用户访问的安全风险。 