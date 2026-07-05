-- ============================================================
-- 表名：etcplus_recon_bill_invoice_retry_log
-- 说明：发票执行重试日志表
-- ============================================================
CREATE TABLE `etcplus_recon_bill_invoice_retry_log` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `invoice_id` bigint NOT NULL COMMENT '关联主表 etcplus_recon_bill_invoice.id',
    `retry_no` tinyint NOT NULL COMMENT '第几次请求（0=首次，1-3=重试）',
    `action_type` tinyint NOT NULL COMMENT '操作类型：1=首次开票，2=重试',
    `request_payload_snapshot` mediumtext COMMENT '该次请求报文快照',
    `response_code` varchar(16) DEFAULT NULL COMMENT '乐企返回码',
    `response_message` varchar(1000) DEFAULT NULL COMMENT '乐企返回消息',
    `trigger_by` varchar(64) DEFAULT NULL COMMENT '触发人（系统/商户/运营）',
    `trigger_at` datetime(3) NOT NULL COMMENT '触发时间',

    PRIMARY KEY (`id`),
    KEY `idx_invoice_id` (`invoice_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='发票执行重试日志表';