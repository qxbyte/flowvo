
CREATE TABLE business (
  id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '业务ID',
  name VARCHAR(100) NOT NULL COMMENT '业务名称',
  type VARCHAR(100) NOT NULL COMMENT '业务类型',
  status VARCHAR(20) NOT NULL COMMENT '状态（运行中/已停止）',
  create_time DATETIME NOT NULL COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
);


CREATE TABLE customer (
  id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '客户ID',
  name VARCHAR(100) NOT NULL COMMENT '客户名称',
  contact_person VARCHAR(100) COMMENT '联系人',
  contact_phone VARCHAR(20) COMMENT '联系电话',
  level VARCHAR(50) COMMENT '客户等级（VIP/重要客户）',
  latest_order_time DATETIME COMMENT '最近下单时间',
  total_order INT DEFAULT 0 COMMENT '总订单数',
  create_time DATETIME NOT NULL COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
);


CREATE TABLE orders (
  id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
  order_no VARCHAR(50) NOT NULL COMMENT '订单号',
  customer_id BIGINT NOT NULL COMMENT '客户ID',
  amount DECIMAL(10, 2) NOT NULL COMMENT '订单金额',
  status VARCHAR(20) NOT NULL COMMENT '状态（待付款/已付款）',
  create_time DATETIME NOT NULL COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  CONSTRAINT fk_customer FOREIGN KEY (customer_id) REFERENCES customer(id)
);

-- chat_record 表：创建时间、更新时间
ALTER TABLE chat_record
  MODIFY COLUMN create_time DATETIME(3),
  MODIFY COLUMN update_time DATETIME(3);

-- messages 表：创建时间
ALTER TABLE messages
  MODIFY COLUMN create_time DATETIME(3);

-- call_message（如有此函数调用对话表）
ALTER TABLE call_message
  MODIFY COLUMN created_at DATETIME(3);