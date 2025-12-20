-- 物流管理平台数据库表结构
-- 创建时间: 2025-01-25

-- 使用项目数据库
USE fruit_recognition;

-- 1. 物流公司表
CREATE TABLE logistics_company (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    company_name VARCHAR(100) NOT NULL COMMENT '公司名称',
    company_code VARCHAR(20) UNIQUE NOT NULL COMMENT '公司代码',
    contact_phone VARCHAR(20) COMMENT '联系电话',
    address VARCHAR(255) COMMENT '公司地址',
    status TINYINT DEFAULT 1 COMMENT '状态：1-正常，0-停用',
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 2. 物流订单表
CREATE TABLE logistics_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_no VARCHAR(50) UNIQUE NOT NULL COMMENT '订单号',
    tracking_no VARCHAR(50) UNIQUE NOT NULL COMMENT '运单号',
    company_id BIGINT NOT NULL COMMENT '物流公司ID',
    sender_name VARCHAR(100) NOT NULL COMMENT '寄件人姓名',
    sender_phone VARCHAR(20) NOT NULL COMMENT '寄件人电话',
    sender_address VARCHAR(255) NOT NULL COMMENT '寄件地址',
    sender_city VARCHAR(50) NOT NULL COMMENT '寄件城市',
    sender_province VARCHAR(50) NOT NULL COMMENT '寄件省份',
    receiver_name VARCHAR(100) NOT NULL COMMENT '收件人姓名',
    receiver_phone VARCHAR(20) NOT NULL COMMENT '收件人电话',
    receiver_address VARCHAR(255) NOT NULL COMMENT '收件地址',
    receiver_city VARCHAR(50) NOT NULL COMMENT '收件城市',
    receiver_province VARCHAR(50) NOT NULL COMMENT '收件省份',
    package_type TINYINT NOT NULL COMMENT '包裹类型：0-文件，1-物品',
    package_weight DECIMAL(8,2) COMMENT '包裹重量(kg)',
    package_value DECIMAL(10,2) COMMENT '包裹价值(元)',
    freight_cost DECIMAL(8,2) COMMENT '运费(元)',
    order_status TINYINT DEFAULT 1 COMMENT '订单状态：1-已下单，2-已揽收，3-运输中，4-派送中，5-已签收，6-异常',
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (company_id) REFERENCES logistics_company(id)
);

-- 3. 物流追踪表（与代码中的 LogisticsTracking 对齐）
CREATE TABLE IF NOT EXISTS logistics_tracking (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tracking_number VARCHAR(50) UNIQUE NOT NULL COMMENT '追踪号',
    fruit_type VARCHAR(50) COMMENT '水果类型',
    origin_location VARCHAR(100) COMMENT '起始地',
    destination VARCHAR(100) COMMENT '目的地',
    current_location VARCHAR(100) COMMENT '当前位置',
    status VARCHAR(50) COMMENT '状态',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
);

-- 4. 仓库信息表
CREATE TABLE warehouse (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    warehouse_name VARCHAR(100) NOT NULL COMMENT '仓库名称',
    warehouse_code VARCHAR(20) UNIQUE NOT NULL COMMENT '仓库代码',
    city VARCHAR(50) NOT NULL COMMENT '城市',
    province VARCHAR(50) NOT NULL COMMENT '省份',
    address VARCHAR(255) NOT NULL COMMENT '详细地址',
    capacity INT NOT NULL COMMENT '仓库容量',
    current_stock INT DEFAULT 0 COMMENT '当前库存',
    manager_name VARCHAR(50) COMMENT '仓库管理员',
    manager_phone VARCHAR(20) COMMENT '管理员电话',
    status TINYINT DEFAULT 1 COMMENT '状态：1-正常，0-停用',
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 5. 仓库库存记录表
CREATE TABLE warehouse_stock (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    warehouse_id BIGINT NOT NULL COMMENT '仓库ID',
    tracking_no VARCHAR(50) NOT NULL COMMENT '运单号',
    operation_type TINYINT NOT NULL COMMENT '操作类型：1-入库，2-出库',
    package_count INT NOT NULL COMMENT '包裹数量',
    operator VARCHAR(50) NOT NULL COMMENT '操作员',
    operation_time TIMESTAMP NOT NULL COMMENT '操作时间',
    remark VARCHAR(200) COMMENT '备注',
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (warehouse_id) REFERENCES warehouse(id)
);

-- 6. 实时统计数据表（用于大屏展示）
CREATE TABLE logistics_statistics (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    stat_date DATE NOT NULL COMMENT '统计日期',
    province VARCHAR(50) NOT NULL COMMENT '省份',
    city VARCHAR(50) NOT NULL COMMENT '城市',
    company_id BIGINT COMMENT '公司ID',
    package_type TINYINT COMMENT '包裹类型：0-文件，1-物品',
    total_orders INT DEFAULT 0 COMMENT '总订单数',
    delivered_orders INT DEFAULT 0 COMMENT '已派送订单数',
    in_transit_orders INT DEFAULT 0 COMMENT '运输中订单数',
    warehouse_in_count INT DEFAULT 0 COMMENT '入库数量',
    warehouse_out_count INT DEFAULT 0 COMMENT '出库数量',
    total_weight DECIMAL(10,2) DEFAULT 0 COMMENT '总重量',
    total_value DECIMAL(12,2) DEFAULT 0 COMMENT '总价值',
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_stat (stat_date, province, city, company_id, package_type),
    FOREIGN KEY (company_id) REFERENCES logistics_company(id)
);

-- 插入初始数据
INSERT INTO logistics_company (company_name, company_code, contact_phone, address) VALUES
('顺丰速运', 'SF', '400-111-1111', '深圳市福田区'),
('京东物流', 'JD', '400-606-5500', '北京市大兴区'),
('中国邮政', 'EMS', '11183', '北京市西城区'),
('圆通速递', 'YTO', '95554', '上海市青浦区'),
('中通快递', 'ZTO', '95311', '上海市青浦区');

INSERT INTO warehouse (warehouse_name, warehouse_code, city, province, address, capacity, manager_name, manager_phone) VALUES
('深圳南山仓', 'SZ001', '深圳', '广东', '南山区科技园', 10000, '张三', '13800138001'),
('广州天河仓', 'GZ001', '广州', '广东', '天河区珠江新城', 8000, '李四', '13800138002'),
('珠海香洲仓', 'ZH001', '珠海', '广东', '香洲区拱北', 5000, '王五', '13800138003'),
('东莞松山湖仓', 'DG001', '东莞', '广东', '松山湖高新区', 6000, '赵六', '13800138004');