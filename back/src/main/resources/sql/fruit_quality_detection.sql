-- 水果品质检测结果表
-- 创建时间: 2025-01-25

USE fruit_recognition;

CREATE TABLE IF NOT EXISTS fruit_quality_detection (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    image_url VARCHAR(255) NOT NULL COMMENT '图片地址',
    category VARCHAR(50) COMMENT '水果类别',
    maturity VARCHAR(50) COMMENT '成熟度',
    sweetness VARCHAR(50) COMMENT '甜度',
    result VARCHAR(255) COMMENT '检测结果',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) COMMENT='水果品质检测结果';

CREATE INDEX idx_fq_user ON fruit_quality_detection(user_id);
CREATE INDEX idx_fq_ctime ON fruit_quality_detection(create_time);