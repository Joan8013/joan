package com.etcplus.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 轻量黄金文件（快照）断言工具，无需第三方依赖。
 *
 * 用法：GoldenFile.assertMatch("settle/完整清分结果.json", result);
 * - 首次运行：自动生成快照文件并让测试失败，提示人工审查该文件；
 * - 之后运行：实际结果与快照不一致则失败，diff 一目了然；
 * - 确认预期确实变了：加 -Dgolden.update=true 运行，重新生成快照后再人工审查。
 *
 * 人审查的是 src/test/resources/golden 下那个结构化 JSON，而不是逐行断言。
 */
public final class GoldenFile {

    private static final boolean UPDATE = Boolean.getBoolean("golden.update");
    private static final Path ROOT = Paths.get("src/test/resources/golden");
    private static final ObjectMapper MAPPER =
            new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    private GoldenFile() {
    }

    public static void assertMatch(String goldenName, Object actualObject) {
        String actual = toPrettyJson(actualObject);
        Path path = ROOT.resolve(goldenName);
        try {
            if (UPDATE || !Files.exists(path)) {
                Files.createDirectories(path.getParent());
                Files.write(path, actual.getBytes(StandardCharsets.UTF_8));
                if (!UPDATE) {
                    throw new AssertionError("黄金文件不存在，已自动生成，请人工审查后再运行: " + path);
                }
                return;
            }
            String expected = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
            assertThat(actual)
                    .as("结果与黄金文件不一致: %s（若为预期变更，用 -Dgolden.update=true 更新后人工审查）", path)
                    .isEqualTo(expected);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static String toPrettyJson(Object obj) {
        try {
            return MAPPER.writeValueAsString(obj);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
