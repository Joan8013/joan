# Maven 依赖 + 插件配置（适配 Spring Boot 2.3.7 + Java 8）

> **完整 POM 模板**：见 [pom/parent-testing.xml](pom/parent-testing.xml) 与 [pom/module-testing.xml](pom/module-testing.xml)，合并说明见 [pom/README.md](pom/README.md)。  
> 下文为片段速查；版本以 `pom/` 目录 XML 为准。

把以下片段分别粘进对应位置。所有版本均兼容 Java 8。

## 1. 父 `pom.xml` 的 `<properties>`

```xml
<properties>
    <java.version>1.8</java.version>
    <maven.compiler.source>1.8</maven.compiler.source>
    <maven.compiler.target>1.8</maven.compiler.target>

    <!-- 测试相关版本（均兼容 Java 8） -->
    <testcontainers.version>1.19.3</testcontainers.version>
    <wiremock.version>2.35.1</wiremock.version>
    <jacoco.version>0.8.8</jacoco.version>
    <pitest.version>1.9.11</pitest.version>
    <pitest-junit5.version>1.1.2</pitest-junit5.version>
</properties>
```

## 2. 父 `pom.xml` 的 `<dependencyManagement>`

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>testcontainers-bom</artifactId>
            <version>${testcontainers.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

## 3. 业务模块（如 `etcplus-system`）的 `<dependencies>`

```xml
<!-- Spring Boot 测试全家桶（自带 JUnit5 + Mockito + AssertJ + Jackson），排除老的 JUnit4 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
    <exclusions>
        <exclusion>
            <groupId>org.junit.vintage</groupId>
            <artifactId>junit-vintage-engine</artifactId>
        </exclusion>
    </exclusions>
</dependency>

<!-- Testcontainers：真实 MySQL8 + JUnit5 集成 -->
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>testcontainers</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>junit-jupiter</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>mysql</artifactId>
    <scope>test</scope>
</dependency>

<!-- MySQL 驱动（测试用，若生产已引可省略这份 test scope） -->
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <scope>test</scope>
</dependency>

<!-- WireMock（Java 8 专用 jre8 版）：给 exchange 的外部系统打桩 -->
<dependency>
    <groupId>com.github.tomakehurst</groupId>
    <artifactId>wiremock-jre8</artifactId>
    <version>${wiremock.version}</version>
    <scope>test</scope>
</dependency>
```

## 4. 父 `pom.xml` 的 `<build><plugins>`

```xml
<build>
    <plugins>
        <!-- 单元测试(*Test) -->
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-surefire-plugin</artifactId>
            <configuration>
                <includes>
                    <include>**/*Test.java</include>
                </includes>
            </configuration>
        </plugin>

        <!-- 集成测试(*IT)，用 failsafe，绑定到 verify 阶段 -->
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-failsafe-plugin</artifactId>
            <executions>
                <execution>
                    <goals>
                        <goal>integration-test</goal>
                        <goal>verify</goal>
                    </goals>
                </execution>
            </executions>
            <configuration>
                <includes>
                    <include>**/*IT.java</include>
                </includes>
            </configuration>
        </plugin>

        <!-- JaCoCo 覆盖率：行≥80%、分支≥70%（核心金融模块用；其他模块可调低或不绑 check） -->
        <plugin>
            <groupId>org.jacoco</groupId>
            <artifactId>jacoco-maven-plugin</artifactId>
            <version>${jacoco.version}</version>
            <executions>
                <execution>
                    <id>prepare-agent</id>
                    <goals><goal>prepare-agent</goal></goals>
                </execution>
                <execution>
                    <id>report</id>
                    <phase>verify</phase>
                    <goals><goal>report</goal></goals>
                </execution>
                <execution>
                    <id>check</id>
                    <phase>verify</phase>
                    <goals><goal>check</goal></goals>
                    <configuration>
                        <rules>
                            <rule>
                                <element>BUNDLE</element>
                                <limits>
                                    <limit>
                                        <counter>LINE</counter>
                                        <value>COVEREDRATIO</value>
                                        <minimum>0.80</minimum>
                                    </limit>
                                    <limit>
                                        <counter>BRANCH</counter>
                                        <value>COVEREDRATIO</value>
                                        <minimum>0.70</minimum>
                                    </limit>
                                </limits>
                            </rule>
                        </rules>
                    </configuration>
                </execution>
            </executions>
        </plugin>

        <!-- PIT 变异测试：验证“测试是否真的有效”（覆盖率骗不了它） -->
        <plugin>
            <groupId>org.pitest</groupId>
            <artifactId>pitest-maven</artifactId>
            <version>${pitest.version}</version>
            <dependencies>
                <dependency>
                    <groupId>org.pitest</groupId>
                    <artifactId>pitest-junit5-plugin</artifactId>
                    <version>${pitest-junit5.version}</version>
                </dependency>
            </dependencies>
            <configuration>
                <!-- 只对核心金融包跑变异，避免全量太慢；包名按你项目实际调整 -->
                <targetClasses>
                    <param>com.etcplus.system.*.settle.*</param>
                    <param>com.etcplus.system.*.check.*</param>
                    <param>com.etcplus.system.*.transfer.*</param>
                </targetClasses>
                <targetTests>
                    <param>com.etcplus.system.*</param>
                </targetTests>
                <mutationThreshold>70</mutationThreshold>
                <coverageThreshold>80</coverageThreshold>
                <timestampedReports>false</timestampedReports>
            </configuration>
        </plugin>
    </plugins>
</build>
```

## 5. 可选：黄金文件用现成库 ApprovalTests

如果不用 `samples/GoldenFile.java` 而想用成熟库（**Java 8 必须用 11.x，新版需 Java 11+**）：

```xml
<dependency>
    <groupId>com.approvaltests</groupId>
    <artifactId>approvaltests</artifactId>
    <version>11.9.0</version>
    <scope>test</scope>
</dependency>
```

用法：`Approvals.verify(prettyJson(result));`。推荐先用零依赖的 `GoldenFile.java`，行为透明、无版本风险。
