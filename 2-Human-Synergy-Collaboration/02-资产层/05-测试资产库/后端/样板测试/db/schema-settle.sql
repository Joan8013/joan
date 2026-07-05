-- 测试建表脚本示例，供 Testcontainers 初始化用（按你真实表结构裁剪）
-- 复制到真实项目时放 src/test/resources/db/schema-settle.sql

CREATE TABLE settle_record (
    id           BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_no     VARCHAR(64)   NOT NULL,
    amount       DECIMAL(18,2) NOT NULL,
    fee          DECIMAL(18,2) NOT NULL,
    merchant_amt DECIMAL(18,2) NOT NULL,
    status       VARCHAR(16)   NOT NULL,
    UNIQUE KEY uk_order_no (order_no)
);
