-- ============================================================
-- 表名：etcplus_recon_bill_invoice
-- 说明：服务费发票执行记录表（v6 最终版，fpqqlsh 唯一）
-- 规则：列表查询严禁 SELECT *，严禁返回 request_payload / item_detail_payload
-- ============================================================
CREATE TABLE `etcplus_recon_bill_invoice` (
    -- ========================================
    -- 1. 主键
    -- ========================================
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',

    -- ========================================
    -- 2. 来源追踪字段（关联申请单与汇总记录）
    -- ========================================
    `apply_id` bigint NOT NULL COMMENT '开票申请主键ID',
    `apply_no` varchar(64) NOT NULL COMMENT '开票申请单号',
    `detail_id` bigint NOT NULL COMMENT '来源申请明细ID（etcplus_recon_bill_invoice_detail.id）',
    `split_seq` int NOT NULL COMMENT '拆票序号（1=第一张，2=第二张）',
    `recon_bill_id` bigint NOT NULL COMMENT '服务费汇总记录主键ID',
    `recon_bill_no` varchar(64) NOT NULL COMMENT '服务费汇总记录编号',
    `merchant_id` varchar(64) NOT NULL COMMENT '商户ID',
    `merchant_name` varchar(128) NOT NULL COMMENT '商户名称',
    `biz_nature` tinyint NOT NULL COMMENT '业务性质：1=仅正常业务，2=含助缴业务',
    -- ========================================
    -- 3. 购买方信息（来自上游入参）
    -- ========================================
    `buyer_name` varchar(300) NOT NULL COMMENT '购买方名称（对应乐企 GMF_MC）',
    `buyer_tax_no` varchar(20) NOT NULL COMMENT '购买方纳税人识别号（对应乐企 GMF_NSRSBH）',

    -- ========================================
    -- 4. 销售方信息（固定配置）
    -- ========================================
    `seller_name` varchar(300) NOT NULL COMMENT '销售方名称（对应乐企 XSF_MC）',
    `seller_tax_no` varchar(20) NOT NULL COMMENT '销售方纳税人识别号（对应乐企 XSF_NSRSBH）',
    `seller_bank_name` varchar(100) NOT NULL COMMENT '销售方开户行（对应乐企 XSF_KHH）',
    `seller_bank_account` varchar(100) NOT NULL COMMENT '销售方开户行账号（对应乐企 XSF_ZH）',

    -- ========================================
    -- 5. 发票要素
    -- ========================================
    `invoice_type` varchar(8) NOT NULL COMMENT '发票类型：030=数电普票，032=数电专票（对应乐企 FPLXDM）',
    `drawer_name` varchar(64) NOT NULL COMMENT '开票人（对应乐企 KPR）',

    -- ========================================
    -- 6. 金额字段（单位：元，保留2位小数）
    -- ========================================
    `jshj_amount` decimal(18,2) NOT NULL COMMENT '价税合计，单位：元，保留2位小数（对应乐企 JSHJ）',
    `hjje_amount` decimal(18,2) NOT NULL COMMENT '合计金额（不含税），单位：元，保留2位小数（对应乐企 HJJE）',
    `hjse_amount` decimal(18,2) NOT NULL COMMENT '合计税额，单位：元，保留2位小数（对应乐企 HJSE）',
    `tax_rate` decimal(8,6) NOT NULL COMMENT '税率，固定 0.06（对应乐企 SL）',

    -- ========================================
    -- 7. 尾差标记字段
    -- ========================================
    `has_tail_diff` tinyint NOT NULL COMMENT '是否有尾差：0=无，1=有（该张发票承担了尾差调平）',
    `tail_diff_amount` decimal(18,2) NOT NULL COMMENT '尾差金额，单位：元，保留2位小数（正数表示调增，负数表示调减）',

    -- ========================================
    -- 8. 项目明细固定字段
    -- ========================================
    `project_name` varchar(128) NOT NULL COMMENT '项目名称，固定：服务费（对应乐企 XMMC）',
    `specification_model` varchar(128) NOT NULL COMMENT '规格型号，固定：*（对应乐企 GGXH）',
    `xmje_amount` decimal(18,2) NOT NULL COMMENT '项目金额（不含税），单位：元，保留2位小数（对应乐企 XMJE）',
    `unit_name` varchar(32) NOT NULL COMMENT '单位，固定：项（对应乐企 DW）',
    `quantity` varchar(2) NOT NULL COMMENT '数量，固定："1"（对应乐企 XMSL）',
    `unit_price` varchar(15) NOT NULL COMMENT '单价，单位：元，字符串格式，总长度≤15位，最多13位小数。取值为hjje_amount的字符串形式（对应乐企 XMDJ）',
    `tax_class_code` varchar(32) NOT NULL COMMENT '税收分类编码，（对应乐企 SPBM）',

    -- ========================================
    -- 9. 备注与明细快照
    -- ========================================
    `remark` varchar(240) DEFAULT NULL COMMENT '备注，上游生成并截断（专票184/普票138），本模块透传（对应乐企 BZ）',
    `item_detail_payload` mediumtext NOT NULL COMMENT '完整项目明细快照（JSON），列表查询禁止返回',

    -- ========================================
    -- 10. 乐企执行字段
    -- ========================================
    `fpqqlsh` varchar(20) NOT NULL COMMENT '乐企开票请求流水号，20位字母数字组合（对应乐企 FPQQLSH）',
    `lsh` varchar(36) DEFAULT NULL COMMENT '乐企异步流水号（对应乐企 LSH）',
    `request_payload` mediumtext NOT NULL COMMENT '完整请求报文（JSON），列表查询禁止返回',
    `request_payload_hash` varchar(64) NOT NULL COMMENT '请求报文 SHA256 摘要，用于幂等校验',
    `invoice_status` tinyint NOT NULL COMMENT '开票状态：0=待开票，1=开票中，2=开票成功，3=开票失败',
    `retry_count` int NOT NULL COMMENT '已重试次数',
    `max_retry_count` int NOT NULL COMMENT '最大重试次数，固定 3',

    -- ========================================
    -- 11. 乐企结果字段
    -- ========================================
    `return_code` varchar(16) DEFAULT NULL COMMENT '乐企返回码',
    `return_message` varchar(1000) DEFAULT NULL COMMENT '乐企返回消息',
    `fail_reason` varchar(1000) DEFAULT NULL COMMENT '最新一次失败原因',
    `invoice_code` varchar(32) DEFAULT NULL COMMENT '发票代码（乐企返回）',
    `invoice_no` varchar(32) DEFAULT NULL COMMENT '发票号码（乐企返回）',
    `invoice_date` datetime(3) DEFAULT NULL COMMENT '开票时间（乐企返回）',
    `pdf_url` varchar(1000) DEFAULT NULL COMMENT 'PDF 下载地址',
    `ofd_url` varchar(1000) DEFAULT NULL COMMENT 'OFD 下载地址',
    `xml_url` varchar(1000) DEFAULT NULL COMMENT 'XML 下载地址',
    `last_request_at` datetime(3) DEFAULT NULL COMMENT '最近请求时间',
    `last_response_at` datetime(3) DEFAULT NULL COMMENT '最近响应时间',

    -- ========================================
    -- 12. 审计字段
    -- ========================================
    `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
    `create_at` datetime(3) NOT NULL COMMENT '创建时间',
    `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
    `update_at` datetime(3) NOT NULL COMMENT '更新时间',

    -- ========================================
    -- 13. 主键与约束
    -- ========================================
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_fpqqlsh` (`fpqqlsh`),
    UNIQUE KEY `uk_apply_split_seq` (`apply_id`, `split_seq`),
    UNIQUE KEY `uk_detail_id` (`detail_id`),

    -- ========================================
    -- 14. 索引
    -- ========================================
    KEY `idx_merchant_status` (`merchant_id`, `invoice_status`),
    KEY `idx_recon_bill_id` (`recon_bill_id`),
    KEY `idx_invoice_no` (`invoice_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='服务费发票执行记录表（v6最终版，fpqqlsh唯一）';
