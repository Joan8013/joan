package com.etcplus.exchange.leq;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 外部系统（如乐企发票）对接测试：用 WireMock 打桩，不真连外部，快且可复现。
 * LeqInvoiceClient/InvoiceRequest/InvoiceResult 请替换成你项目里真实的类。
 */
class LeqInvoiceClientTest {

    private WireMockServer wireMock;
    private LeqInvoiceClient client;

    @BeforeEach
    void setUp() {
        wireMock = new WireMockServer(options().dynamicPort());
        wireMock.start();
        // 把 client 指向 WireMock 地址（按你项目实际配置方式注入 baseUrl）
        client = new LeqInvoiceClient("http://localhost:" + wireMock.port());
    }

    @AfterEach
    void tearDown() {
        wireMock.stop();
    }

    @Test
    @DisplayName("开票成功时应正确解析发票号")
    void 开票成功() {
        wireMock.stubFor(post(urlEqualTo("/leq/invoice/create"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"code\":\"0000\",\"invoiceNo\":\"INV20260704\"}")));

        InvoiceResult r = client.createInvoice(new InvoiceRequest("888.88"));

        assertThat(r.getInvoiceNo()).isEqualTo("INV20260704");
    }

    @Test
    @DisplayName("外部返回失败码时应抛业务异常")
    void 开票失败() {
        wireMock.stubFor(post(urlEqualTo("/leq/invoice/create"))
                .willReturn(aResponse().withStatus(200)
                        .withBody("{\"code\":\"9999\",\"msg\":\"余额不足\"}")));

        assertThatThrownBy(() -> client.createInvoice(new InvoiceRequest("1.00")))
                .hasMessageContaining("余额不足");
    }
}
