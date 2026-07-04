package com.etcplus.system.manage.settle;

import com.etcplus.test.AbstractMySqlIT;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 清分结算集成测试（真实 MySQL，验证 MyBatis SQL + 事务 + 幂等约束）。
 * 类名/字段请对照你项目实际情况替换。
 */
@DisplayName("清分结算集成测试（真实 MySQL）")
class SettleServiceIT extends AbstractMySqlIT {

    @Autowired
    private SettleService settleService;

    @Autowired
    private SettleMapper settleMapper;

    @Test
    @DisplayName("清分应正确落库，且重复执行不产生重复记录")
    void 清分落库并幂等() {
        SettleOrder order = SettleOrderBuilder.anOrder()
                .withOrderNo("IT20260704001")
                .withAmount("200.00")
                .withRate("0.006")
                .build();

        settleService.settle(order);
        settleService.settle(order); // 重复一次

        // 只应有一条记录（验证真实 SQL 的幂等约束/逻辑）
        assertThat(settleMapper.countByOrderNo("IT20260704001")).isEqualTo(1);

        SettleResult saved = settleMapper.selectByOrderNo("IT20260704001");
        assertThat(saved.getFee()).isEqualByComparingTo("1.20");
        assertThat(saved.getMerchantAmount()).isEqualByComparingTo("198.80");
    }
}
