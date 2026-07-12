现在想在手机微信小程序上登录，但一直登陆不上， ip地址连接到了我手机的热点，手机上点击登录后后端也没有返回错误信息，数据库用的我电脑的本地数据库，应该怎么解决（以下是我在cmd输入ipconfig显示的部分内容）





无线局域网适配器 WLAN:

   连接特定的 DNS 后缀 . . . . . . . :
   IPv6 地址 . . . . . . . . . . . . : 2409:893d:cc6:33ee:3e2e:2423:f92:25d3
   临时 IPv6 地址. . . . . . . . . . : 2409:893d:cc6:33ee:848:7f20:c281:7dad
   本地链接 IPv6 地址. . . . . . . . : fe80::19c1:55d3:8d33:6dfb%24
   IPv4 地址 . . . . . . . . . . . . : 10.242.194.76
   子网掩码  . . . . . . . . . . . . : 255.255.255.0
   默认网关. . . . . . . . . . . . . : fe80::5058:d7ff:fe84:6042%24
                                       10.242.194.195

---

## 问题分析与解决方案

### 一、核心问题：IP 地址不对！

你当前的配置使用的是 **WLAN 适配器** 的 IP `10.242.194.76`，这是电脑连接到外部 WiFi（如校园网/公司网）时获取的 IP。

**但是**，当你用电脑开热点给手机连接时：
- Windows 热点会创建一个**虚拟网络适配器**（如 "Microsoft Wi-Fi Direct Virtual Adapter" 或 "本地连接*"），分配**不同的 IP**
- 热点子网通常是 **192.168.137.0/24**，电脑在热点网络中通常是 **192.168.137.1**
- 你的手机连接到热点后获取的是 `192.168.137.x` 的 IP
- 手机在 `192.168.137.x` 网段，**无法访问** `10.242.194.76`（WLAN 网段），这是两个完全隔离的网络！

### 二、解决步骤

#### 步骤1：找到热点的正确 IP 地址

在 cmd 中运行 `ipconfig`，找到热点相关的适配器，通常名为：
- `本地连接* 1`、`本地连接* 2`
- `Microsoft Wi-Fi Direct Virtual Adapter`
- `Mobile Hotspot`

查找对应 `IPv4 地址`，通常是 `192.168.137.1` 或 `192.168.0.1`。

> **提示**：也可以在 设置 → 网络和 Internet → 移动热点 中查看热点 IP。

---

#### 步骤2：修改微信小程序 baseUrl

编辑 `pmf_mini_program\app.js` 第 45 行，将 `baseUrl` 改为**热点 IP**：

```js
// 假设热点 IP 是 192.168.137.1
baseUrl: 'http://192.168.137.1:8088'
```

---

#### 步骤3：修改数据库连接 URL（重要！）

编辑 `backend\PMF\src\main\resources\application.yaml`，将数据库连接改用 `localhost`（因为数据库在你本机）：

```yaml
spring:
  datasource:
    url: jdbc:mysql://127.0.0.1:3306/pmf?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8
    # 或者用 localhost
    # url: jdbc:mysql://localhost:3306/pmf?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8
    driver-class-name: com.mysql.cj.jdbc.Driver
    username: root
    password: "123456"
```

**为什么改这个？** `10.242.194.76` 是 WLAN 的 IP，MySQL 默认只监听 `127.0.0.1`，用这个 IP 去连可能会失败。`127.0.0.1` 永远指向本机，最可靠。

---

#### 步骤4：确保 Spring Boot 监听所有网络接口

在 `application.yaml` 的 `server` 部分添加 address 配置（第 83 行）：

```yaml
server:
  port: 8088
  address: 0.0.0.0    # 新增这行，监听所有网卡，确保热点网卡也能接收请求
```

---

#### 步骤5：关闭 Windows 防火墙（或开放端口）

防火墙可能阻止手机访问电脑的 8088 端口。

**方法A（快速测试）**：临时关闭防火墙
- 控制面板 → Windows Defender 防火墙 → 启用或关闭 Windows Defender 防火墙 → 关闭

**方法B（推荐）**：添加入站规则
1. 控制面板 → Windows Defender 防火墙 → 高级设置
2. 入站规则 → 新建规则
3. 端口 → TCP → `8088`
4. 允许连接 → 完成

---

#### 步骤6：微信小程序关闭域名校验

微信小程序要求 HTTPS，本地开发 HTTP 会被拦截。

在手机微信上：
1. 打开小程序后，点击右上角 **...** → 开发调试 → **开启调试**
2. 或者在微信开发者工具中：详情 → 本地设置 → 勾选 **不校验合法域名**

---

#### 步骤7：确保 MySQL 允许远程连接（如果需要）

在 MySQL 中检查 root 用户是否允许从任意主机连接：

```sql
-- 登录 MySQL
mysql -u root -p

-- 查看当前用户 host 配置
SELECT host, user FROM mysql.user WHERE user='root';

-- 如果只有 localhost 和 127.0.0.1，添加热点网段的访问权限
CREATE USER 'root'@'192.168.137.%' IDENTIFIED BY '123456';
GRANT ALL PRIVILEGES ON pmf.* TO 'root'@'192.168.137.%';
FLUSH PRIVILEGES;
```

> 注意：如果步骤3 已改为 `127.0.0.1`，数据库连接走的是本机回环，**不需要**这步。这步只在你需要从其他设备直连 MySQL 时才需要。

---

#### 步骤8：重启后端服务

修改配置后，重启 Spring Boot 后端服务，确保新配置生效。

---

### 三、网络拓扑说明

```
你在 ipconfig 中看到的：          你应该找的（热点）：        手机端：
┌─────────────────┐          ┌──────────────────┐     ┌──────────┐
│  WLAN 适配器     │          │  热点虚拟适配器    │     │  手机     │
│  10.242.194.76  │          │  192.168.137.1   │     │          │
│  (连外部WiFi)   │    ✗     │  (热点网络)       │ ←─→ │  .137.x  │
└─────────────────┘          └──────────────────┘     └──────────┘
       ↑                            ↑
  不能用来通信              用这个 IP 来通信！
```

### 四、快速检查清单

- [ ] 在 `ipconfig` 中找到热点适配器的 IP（通常是 `192.168.137.1`）
- [ ] `app.js` 中 `baseUrl` 改为热点 IP
- [ ] `application.yaml` 中数据库 URL 改为 `127.0.0.1`
- [ ] `application.yaml` 中 `server.address` 设为 `0.0.0.0`
- [ ] Windows 防火墙开放 8088 端口
- [ ] 微信开启调试模式（关闭域名校验）
- [ ] 重启后端服务