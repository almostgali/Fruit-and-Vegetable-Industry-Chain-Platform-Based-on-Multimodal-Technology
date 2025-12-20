-- 物流管理员表
-- 创建时间: 2025-01-25

USE fruit_recognition;

-- 使用正确的数据库
USE fruit_recognition;

-- 创建物流管理员表
CREATE TABLE IF NOT EXISTS logistics_admin (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码（MD5加密）',
    real_name VARCHAR(100) NOT NULL COMMENT '真实姓名',
    phone VARCHAR(20) COMMENT '联系电话',
    email VARCHAR(100) COMMENT '邮箱',
    status TINYINT DEFAULT 1 COMMENT '状态：1-正常，0-停用',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) COMMENT='物流管理员表';

-- 插入默认管理员账号
INSERT IGNORE INTO logistics_admin (username, password, real_name, phone, email, status) 
VALUES 
('logistics_admin', MD5('123456'), '物流管理员', '13800138000', 'logistics@example.com', 1),
('wuliu001', MD5('wuliu123'), '张三', '13800138001', 'zhangsan@example.com', 1),
('wuliu002', MD5('wuliu123'), '李四', '13800138002', 'lisi@example.com', 1);

-- 验证数据
SELECT * FROM logistics_admin;