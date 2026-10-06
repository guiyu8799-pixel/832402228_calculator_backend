# 演示截图 / Demonstration screenshots

本目录中的 15 张截图全部来自**真实运行的系统**：后端 `http://localhost:8080`，前端
`http://localhost:5510`。截图由无头 Chrome 驱动真实页面生成（表达式真实输入到输入框、
按钮真实点击），不是设计稿。

图片说明的机器可读版本见 [`manifest.json`](manifest.json)。

| # | 文件 | 内容 |
| --- | --- | --- |
| 01 | [01-main-interface.png](01-main-interface.png) | 主界面：打开页面后自动从后端数据库加载计算历史 |
| 02 | [02-addition-keypad.png](02-addition-keypad.png) | 加法 `12+8 = 20`，使用屏幕按键逐个输入 |
| 03 | [03-subtraction.png](03-subtraction.png) | 减法 `9-4 = 5` |
| 04 | [04-multiplication.png](04-multiplication.png) | 乘法 `6*7 = 42`（界面按钮显示 `×`，发送 `*`） |
| 05 | [05-division.png](05-division.png) | 除法 `20/4 = 5`（界面按钮显示 `÷`，发送 `/`） |
| 06 | [06-decimal.png](06-decimal.png) | 小数 `0.1+0.2 = 0.3`（BigDecimal，无浮点误差） |
| 07 | [07-operator-precedence.png](07-operator-precedence.png) | 运算优先级 `1+2*3 = 7` |
| 08 | [08-parentheses.png](08-parentheses.png) | 括号 `(1+2)*3 = 9` |
| 09 | [09-unary-minus.png](09-unary-minus.png) | 一元负号 `3*-2 = -6` |
| 10 | [10-invalid-expression.png](10-invalid-expression.png) | 非法表达式 `1+*2`：HTTP 400 与后端给出的原因 |
| 11 | [11-division-by-zero.png](11-division-by-zero.png) | 除零 `1/0`：`DIVISION_BY_ZERO` |
| 12 | [12-history-list.png](12-history-list.png) | 计算历史：表达式、结果、时间全部来自后端数据库 |
| 13 | [13-history-persistence.png](13-history-persistence.png) | 刷新页面后历史仍存在（数据来自后端数据库） |
| 14 | [14-delete-record.png](14-delete-record.png) | 删除指定历史记录，并重新查询后端最新历史 |
| 15 | [15-backend-offline-no-result.png](15-backend-offline-no-result.png) | 前后端分离验证：后端停止时按 `=` 无法得到结果 |

## 重新生成截图

截图脚本位于项目根目录的 `verification/capture.js`，使用 Chrome DevTools Protocol 直接驱动
浏览器，无需安装任何 npm 依赖：

```bash
# 先启动后端与前端，然后
set CHROME_PATH=C:\Program Files\Google\Chrome\Application\chrome.exe   # 按实际路径修改
node verification/capture.js

# 第 15 张需要后端处于停止状态，停掉后端后单独重拍：
set ONLY_STEP=15-backend-offline
node verification/capture.js
```
