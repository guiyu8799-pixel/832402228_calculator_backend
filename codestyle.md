# 后端代码规范 / Backend Code Standard

> **规范来源 / Source of this standard**
>
> 本文件以后端技术栈（Java 8 + Spring Boot 2.7）对应的主流官方规范为基础：
>
> 1. [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)（主要依据）
> 2. [Oracle Code Conventions for the Java Programming Language](https://www.oracle.com/java/technologies/javase/codeconventions-contents.html)（沿用其命名与注释约定）
> 3. [Spring Boot Reference Documentation — Code Structure](https://docs.spring.io/spring-boot/docs/2.7.x/reference/html/using.html#using.structuring-your-code)（分层与包结构约定）
> 4. [Alibaba Java Coding Guidelines](https://github.com/alibaba/p3c)（异常处理与日志约定的补充）
>
> 若上述规范与本文件冲突，以本文件为准；本文件未覆盖的内容遵循 Google Java Style Guide。

---

## 目录

- [1. 适用范围](#1-适用范围)
- [2. 文件与包结构](#2-文件与包结构)
- [3. 命名规范](#3-命名规范)
- [4. 格式规范](#4-格式规范)
- [5. 注释与文档](#5-注释与文档)
- [6. 导入语句](#6-导入语句)
- [7. 异常处理](#7-异常处理)
- [8. 日志规范](#8-日志规范)
- [9. 分层与职责](#9-分层与职责)
- [10. 安全约束](#10-安全约束)
- [11. 测试规范](#11-测试规范)
- [12. 提交规范](#12-提交规范)
- [13. 自查清单](#13-自查清单)

---

## 1. 适用范围

适用于 `832402228_calculator_backend` 仓库中的全部 Java 源码、测试代码与配置文件。

## 2. 文件与包结构

1. 一个文件只包含一个顶层 public 类，文件名与类名完全一致。
2. 包名全部小写，不使用下划线，按职责分层：`config` / `controller` / `service` / `repository` / `entity` / `dto` / `exception` / `expression`。
3. 源文件结构顺序固定：
   1. 许可证/版权声明（本项目无）
   2. `package` 语句
   3. `import` 语句
   4. 唯一一个顶层类（含类 JavaDoc）
4. 配置项统一写在 `src/main/resources/application.yml`，不硬编码在 Java 代码中；需要读取时使用 `@Value("${key:default}")` 注入。

## 3. 命名规范

| 类型 | 规则 | 示例 |
| --- | --- | --- |
| 包 | 全小写 | `com.fzu.calculator.expression` |
| 类 / 接口 | 大驼峰 | `ExpressionCalculator`、`HistoryService` |
| 方法 | 小驼峰，动词开头 | `calculate`、`findHistory`、`delete` |
| 变量 / 参数 | 小驼峰，语义完整 | `canonicalExpression`、`maxPageSize` |
| 常量 | 全大写 + 下划线 | `DEFAULT_DATABASE_PATH`、`MATH_CONTEXT` |
| 实体字段 | 小驼峰，数据库列名用下划线 | `createdAt` ↔ `created_at` |
| 测试类 | 被测类名 + `Test` / 场景 + `IntegrationTest` | `ExpressionCalculatorTest` |
| 布尔方法 | `is` / `has` / `can` 开头 | `isSuccess()` |

禁止：拼音命名、单字母变量（循环变量 `i` 除外）、`data1`/`temp` 之类的无意义名称。

## 4. 格式规范

1. **缩进 4 个空格**，禁止使用 Tab。
2. **行宽上限 120 字符**（Google 建议 100，本项目因中文注释与较长方法签名放宽到 120）。
3. 左花括号不换行；右花括号独占一行（`else` / `catch` 与右花括号同行）。
4. 每个控制结构（`if` / `for` / `while`）必须使用花括号，即使只有一条语句。
5. 运算符两侧、逗号之后各留一个空格；方法名与左括号之间不留空格。
6. 方法之间空一行；逻辑无关的代码块之间空一行。
7. 单个方法不超过 80 行；超过时拆分为私有方法。

```java
// 正确
public CalculateResponse calculate(String rawExpression) {
    if (rawExpression == null || rawExpression.trim().isEmpty()) {
        throw new CalculationException(ErrorCode.EMPTY_EXPRESSION, "The expression is empty");
    }
    String canonicalExpression = ExpressionNormalizer.normalize(rawExpression);
    BigDecimal result = ExpressionCalculator.evaluate(canonicalExpression);
    return new CalculateResponse(canonicalExpression, result, null, null);
}
```

## 5. 注释与文档

1. 所有 `public` 类与 `public` 方法必须有 JavaDoc，说明**做什么**与**为什么这样设计**，而不是复述代码。
2. 复杂算法（如表达式解析、SQLite 目录预创建）必须说明设计原因。
3. 行内注释使用 `//`，写在被说明代码的上一行。
4. 禁止保留被注释掉的死代码；如需保留请说明原因。
5. 注释语言：JavaDoc 使用英文，保证与其他工具链兼容；面向课程的中文说明写在 README。

```java
/**
 * SQLite creates the database file itself but never creates missing directories,
 * therefore the parent directory of the configured database path is prepared first.
 */
private static void createDatabaseDirectoryIfNeeded(String[] args) { ... }
```

## 6. 导入语句

1. 禁止通配符导入（`import java.util.*;`）。
2. 不使用静态导入之外的冗余导入；未使用的导入必须删除。
3. 内部类不得与顶层类重名。
4. 导入顺序：`java.*` → `javax.*` → 第三方（`org.springframework.*` 等）→ 本项目，组间空一行。

## 7. 异常处理

1. **业务异常**统一继承 `CalculationException` 或 `ResourceNotFoundException`，并携带 `ErrorCode`。
2. **禁止吞掉异常**：`catch` 块必须要么重新抛出、要么记录日志、要么给出明确注释说明为何可以忽略。
3. **禁止使用异常控制正常流程**；`catch (Exception e)` 只允许在全局异常处理器中出现。
4. 全局异常处理器 `GlobalExceptionHandler` 负责把异常映射为 HTTP 状态码与统一响应体，Controller 不写 try-catch。
5. 面向用户的错误信息不得泄露堆栈、SQL 语句或文件绝对路径。

```java
// 正确：忽略是安全的，并说明原因
} catch (IOException ex) {
    log.warn("Could not read app.database.path from application.yml: {}", ex.getMessage());
}
```

## 8. 日志规范

1. 使用 SLF4J：`private static final Logger log = LoggerFactory.getLogger(Xxx.class);`
2. 禁止 `System.out.println`。
3. 使用占位符 `{}`，不使用字符串拼接：`log.info("Calculated '{}' = {}", expression, result);`
4. 日志级别：`error` 仅用于需要人工介入的故障；`warn` 用于可恢复的异常；`info` 用于关键业务动作；`debug` 用于排查细节。
5. 不记录密码、密钥等敏感信息。

## 9. 分层与职责

| 层 | 包 | 职责 | 禁止 |
| --- | --- | --- | --- |
| 控制器 | `controller` | 解析 HTTP 请求、参数绑定、返回 DTO | 写业务逻辑、直接访问 Repository |
| 服务 | `service` | 业务规则、事务边界（`@Transactional`） | 处理 HTTP 相关对象 |
| 仓储 | `repository` | 数据库访问，只暴露 Spring Data 接口 | 写业务判断 |
| 实体 | `entity` | 数据库表映射 | 作为 API 返回体 |
| 传输对象 | `dto` | API 请求/响应结构 | 包含业务逻辑 |
| 计算内核 | `expression` | 词法分析、语法分析、求值 | 依赖 Spring、访问数据库 |

1. Controller 只做“接收 — 委托 — 返回”，不出现 `new BigDecimal(...)`、循环等计算代码。
2. Service 方法必须声明事务语义（`@Transactional` 或 `@Transactional(readOnly = true)`）。
3. 实体对象不得直接序列化返回给前端，必须转换为 DTO。

## 10. 安全约束

1. **禁止** `eval`、`exec`、`ScriptEngine`、`Nashorn`、`Groovy`、反射调用等以用户输入为参数执行任意代码的方式。
2. 用户输入必须先经 `ExpressionNormalizer` 归一化，再由白名单词法分析器 `Lexer` 处理；出现白名单外字符直接报错。
3. 表达式长度由 `Lexer` 的输入长度隐式约束，不接受白名单以外的任何字符。
4. CORS 白名单通过配置控制，生产环境不得使用 `*`。
5. 数据库访问一律通过 Spring Data JPA，禁止拼接 SQL 字符串。

## 11. 测试规范

1. 测试类放在与被测类相同的包路径下；测试方法名描述“行为 + 期望”，如 `rejectsDivisionByZero`。
2. 解析与计算逻辑使用参数化测试（`@ParameterizedTest` + `@CsvSource`）覆盖边界值。
3. API 层使用 `MockMvc` 做端到端测试，覆盖成功计算、非法表达式、除零、空表达式、404、历史查询与删除。
4. 测试必须可重复执行，且使用独立的测试数据库（`target/test-data/`），不得污染开发数据。
5. 新增功能必须同时新增测试；`mvn test` 必须全部通过。

## 12. 提交规范

采用 [Conventional Commits](https://www.conventionalcommits.org/)：

```text
<type>(<scope>): <subject>

feat(calculate): add modulo and power operators
fix(database): create sqlite directory from application.yml path
test(expression): cover unary plus and division by zero
docs(readme): document deployment steps
```

`type` 取值：`feat` / `fix` / `refactor` / `test` / `docs` / `style` / `chore`。
一次提交只做一件事；提交信息使用英文，祈使句。

## 13. 自查清单

提交前逐项确认：

- [ ] 4 空格缩进，无 Tab
- [ ] 无未使用的 import，无通配符 import
- [ ] 所有 public 类与方法有 JavaDoc
- [ ] 无 `System.out.println`，日志使用占位符
- [ ] 无空的或吞掉异常的 `catch`
- [ ] Controller 中无业务逻辑
- [ ] 用户输入未进入任何代码执行接口
- [ ] 新增/修改的功能有对应测试，`mvn test` 全绿
- [ ] README 中的启动步骤与实际一致
