package com.etcplus.system.manage.settle;

import com.etcplus.test.GoldenFile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 清分结算单元测试（纯逻辑，Mock 掉 Mapper，毫秒级）。
 * 被测方法假设：SettleService.settle(order) 计算手续费与商户实收并落库，重复提交需幂等；
 *              SettleService.calculate(amount, rate) 返回含各金额字段的 SettleResult。
 *
 * 注意：本类是模板，SettleService/SettleMapper/SettleResult/SettleOrder/SettleOrderBuilder
 *      请替换成你项目里真实的类名与构造方式。
 */
class SettleServiceTest {

    private SettleMapper settleMapper;
    private SettleService settleService;

    @BeforeEach
    void setUp() {
        settleMapper = mock(SettleMapper.class);
        settleService = new SettleService(settleMapper); // 按你项目实际构造/注入调整
    }

    // ---------- 规则类：用参数化数据表，人只审这张表 ----------
    @DisplayName("按费率计算手续费与商户实收")
    @ParameterizedTest(name = "金额{0} 费率{1} => 手续费{2} 实收{3}")
    @CsvSource({
            //  交易额,      费率,     预期手续费,   预期商户实收
            "   100.00,   0.006,       0.60,        99.40",
            "     0.01,   0.006,       0.00,         0.01",   // 边界:最小额，手续费四舍五入到0
            "     0.00,   0.006,       0.00,         0.00",   // 边界:零元
            "9999999.99,  0.006,   60000.00,   9939999.99"    // 边界:大额
    })
    void 手续费计算(BigDecimal amount, BigDecimal rate,
                BigDecimal expectedFee, BigDecimal expectedMerchant) {
        SettleResult r = settleService.calculate(amount, rate);

        assertThat(r.getFee()).isEqualByComparingTo(expectedFee);
        assertThat(r.getMerchantAmount()).isEqualByComparingTo(expectedMerchant);
    }

    // ---------- 金额守恒：分账后各方之和 == 原始金额 ----------
    @Test
    @DisplayName("清分后 商户实收 + 手续费 应等于原始交易额（一分不差）")
    void 金额守恒() {
        BigDecimal amount = new BigDecimal("12345.67");
        SettleResult r = settleService.calculate(amount, new BigDecimal("0.006"));

        assertThat(r.getMerchantAmount().add(r.getFee()))
                .isEqualByComparingTo(amount);
    }

    // ---------- 幂等：同一笔重复清分只生效一次 ----------
    @Test
    @DisplayName("同一笔交易重复清分应幂等，不重复落库")
    void 重复清分应幂等() {
        SettleOrder order = SettleOrderBuilder.anOrder()
                .withOrderNo("T20260704001")
                .withAmount("100.00")
                .build();
        // 模拟：该笔已清分过
        when(settleMapper.existsByOrderNo("T20260704001")).thenReturn(true);

        settleService.settle(order);

        // 断言：没有再次插入结算记录
        verify(settleMapper, never()).insert(any());
    }

    // ---------- 异常路径：负数金额不允许清分 ----------
    @Test
    @DisplayName("负数金额清分应抛出校验异常且不落库")
    void 负数金额应拒绝() {
        SettleOrder order = SettleOrderBuilder.anOrder()
                .withOrderNo("T20260704002")
                .withAmount("-50.00")
                .build();

        assertThatThrownBy(() -> settleService.settle(order))
                .isInstanceOf(IllegalArgumentException.class); // 换成你项目的 ServiceException

        verify(settleMapper, never()).insert(any());
    }

    // ---------- 多字段结果：用黄金文件，人审查快照 ----------
    @Test
    @DisplayName("完整清分结果应与黄金文件一致")
    void 完整清分结果快照() {
        SettleOrder order = SettleOrderBuilder.anOrder()
                .withOrderNo("T20260704003")
                .withAmount("888.88")
                .withRate("0.006")
                .build();

        SettleResult result = settleService.calculate(order.getAmount(), order.getRate());

        // 人审查 src/test/resources/golden/settle/完整清分结果.json 这一个文件即可
        GoldenFile.assertMatch("settle/完整清分结果.json", result);
    }
}
