# AES 加密传输详解

> 登录、修改密码、重置密码时，密码在网络上明文传输是非常危险的。本项目使用 AES-256-CBC 加密，确保密码从浏览器到服务器的整条链路上都是密文。

---

## 一、为什么需要加密传输？

### 没有加密时会发生什么？

```
浏览器 ── 明文密码 "123456" ──▶ 互联网 ──▶ 服务器
                ▲
        任何人都能看到！
```

如果不加密，攻击者可以在网络中间（路由器、WiFi 热点、ISP）截获你的密码。即便使用了 HTTPS，在浏览器开发者工具的 Network 面板中也能看到明文密码。

### 加密后的效果

```
浏览器 ── Base64("j8Kp...乱码...==") ──▶ 互联网 ──▶ 服务器
                      ▲
              看不懂是什么！
```

---

## 二、AES 加密是什么？

**AES**（Advanced Encryption Standard，高级加密标准）是目前全球最常用的对称加密算法。关键概念：

| 概念 | 解释 | 本项目取值 |
|------|------|-----------|
| **密钥（Key）** | 一把"钥匙"，加密和解密都用它 | `PMF-WMS-AES-Key-2026!!SecretKey#`（32字节 = AES-256） |
| **IV（初始化向量）** | 每次加密随机生成的"盐"，让同一密码每次加密结果不同 | 16字节随机数 |
| **CBC 模式** | 一种加密模式，每个明文块都与前一个密文块进行 XOR 运算 | AES/CBC/PKCS5Padding |

### 为什么同一密码每次加密结果不同？

```
第一次加密 "123456" → "j8KpXm2N...=="
第二次加密 "123456" → "Wq3Fb7Rt...=="   ← 完全不同！
```

因为每次加密都生成一个**随机的 IV**。即使密码相同，加密结果也不同。这防止了攻击者通过"密文对比"来猜测密码。

---

## 三、前端加密代码（crypto.js）

### 完整代码

```javascript
// front/src/utils/crypto.js

const AES_KEY = 'PMF-WMS-AES-Key-2026!!SecretKey#' // 32 字节密钥
const encoder = new TextEncoder()

// 步骤1：把密钥字符串转换成浏览器能用的 CryptoKey 对象
async function getKey() {
  const keyBytes = encoder.encode(AES_KEY)
  return crypto.subtle.importKey(
    'raw',           // 密钥格式：原始字节
    keyBytes,        // 密钥数据
    { name: 'AES-CBC' }, // 算法：AES-CBC
    false,           // 是否可导出
    ['encrypt']      // 用途：加密
  )
}

// 步骤2：加密函数
export async function encrypt(plaintext) {
  if (!plaintext) return ''

  const key = await getKey()                          // 获取密钥对象
  const iv = crypto.getRandomValues(new Uint8Array(16)) // 生成16字节随机IV
  const data = encoder.encode(plaintext)               // 把明文转成字节数组

  // 调用浏览器内置的 AES-CBC 加密
  const ciphertext = await crypto.subtle.encrypt(
    { name: 'AES-CBC', iv },
    key,
    data
  )

  // 把 IV 拼接在密文前面
  const combined = new Uint8Array(iv.length + ciphertext.byteLength)
  combined.set(iv)                           // 前16字节是IV
  combined.set(new Uint8Array(ciphertext), iv.length) // 后面是密文

  // 转成 Base64 字符串返回
  return btoa(String.fromCharCode(...combined))
}

export default { encrypt }
```

### 逐行讲解

**1. `crypto.subtle` 是什么？**

`crypto.subtle` 是浏览器内置的加密 API（Web Crypto API）。它不需要引入任何第三方库，所有现代浏览器都支持。"subtle" 这个名字意味着它的方法都很"微妙"——返回的是 Promise，需要 `await`。

**2. 为什么要 `TextEncoder`？**

AES 加密操作的是**字节数组**（`Uint8Array`），不是字符串。`TextEncoder` 把 JavaScript 字符串转换成 UTF-8 编码的字节数组。

```javascript
encoder.encode('Hello') // → Uint8Array [72, 101, 108, 108, 111]
```

**3. IV 为什么要随机？**

如果 IV 固定，同一密码每次加密结果就相同。攻击者虽然不知道明文，但可以发现"这两个密文相同 = 这两个密码相同"。随机 IV 让每次加密结果完全不同。

**4. 为什么要把 IV 拼在密文前面？**

因为解密时需要知道当时加密用的 IV。IV 不是秘密，可以公开传输。把 IV 和密文打包在一起，后端收到后先拆出 IV，再用它解密。

**5. 为什么用 `btoa`？**

加密结果是二进制数据，不能直接放在 JSON 中传输。`btoa` 把二进制转成 Base64 字符串（只包含 A-Z、a-z、0-9、+、/、=），可以安全地放在 HTTP 请求体中。

---

## 四、后端解密代码（AesUtil.java）

### 完整代码

```java
// backend/.../util/AesUtil.java

package com.neuedu.pmf.util;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class AesUtil {

    // 密钥必须和前端的 AES_KEY 完全一致
    private static final String AES_KEY = "PMF-WMS-AES-Key-2026!!SecretKey#";
    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";

    public static String decrypt(String encryptedBase64) {
        if (encryptedBase64 == null || encryptedBase64.isEmpty()) {
            return null;
        }
        try {
            // 步骤1：Base64 解码
            byte[] combined = Base64.getDecoder().decode(encryptedBase64);

            // 步骤2：分离 IV（前16字节）和密文（剩余部分）
            byte[] iv = new byte[16];
            byte[] ciphertext = new byte[combined.length - 16];
            System.arraycopy(combined, 0, iv, 0, 16);
            System.arraycopy(combined, 16, ciphertext, 0, ciphertext.length);

            // 步骤3：构建密钥对象
            SecretKeySpec keySpec = new SecretKeySpec(
                AES_KEY.getBytes(StandardCharsets.UTF_8), "AES"
            );
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            // 步骤4：执行解密
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
            byte[] plaintext = cipher.doFinal(ciphertext);

            // 步骤5：返回明文字符串
            return new String(plaintext, StandardCharsets.UTF_8);
        } catch (Exception e) {
            System.err.println("AES decrypt failed: " + e.getMessage());
            return null;
        }
    }
}
```

### 逐行讲解

**1. `SecretKeySpec` 是什么？**

Java 的加密 API（JCA）需要特定格式的密钥对象。`SecretKeySpec` 把原始字节数组包装成 AES 算法可识别的密钥对象。

**2. `IvParameterSpec` 是什么？**

类似地，把 IV 的字节数组包装成 Java 加密 API 能用的 IV 参数对象。

**3. `Cipher` 是什么？**

`Cipher` 是 Java 加密的核心类。它提供了加密和解密的统一接口：
- `Cipher.getInstance("AES/CBC/PKCS5Padding")` —— 指定算法、模式、填充方式
- `cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)` —— 初始化为解密模式
- `cipher.doFinal(ciphertext)` —— 执行解密

**4. `System.arraycopy` 做了什么？**

前端的密文结构是：`[IV 16字节][密文 N字节]`。后端用 `System.arraycopy` 把这个拼接的数组拆开：
```
combined: [i][i][i]...[i][c][c][c]...[c]
          ← 16字节IV →← 密文部分 →
```

---

## 五、完整数据流

```
┌─────────────────────────────────────────────────────────────────┐
│                          前端（浏览器）                           │
│                                                                  │
│  用户输入密码 "123456"                                            │
│       │                                                          │
│       ▼                                                          │
│  encrypt("123456")                                               │
│       │                                                          │
│       ├── 生成随机 IV: [a7, f3, 12, ..., 8b]  (16字节)           │
│       ├── AES-256-CBC 加密                                        │
│       ├── 拼接: IV + 密文                                         │
│       └── Base64 编码 → "p/M6q7..."                              │
│       │                                                          │
│       ▼                                                          │
│  POST /auth/login                                                 │
│  Body: {"username":"admin", "password":"p/M6q7..."}              │
│                                                                  │
└──────────────────────────┬──────────────────────────────────────┘
                           │
                    互联网（密文传输）
                           │
┌──────────────────────────▼──────────────────────────────────────┐
│                         后端（Spring Boot）                      │
│                                                                  │
│  AuthController.login()                                          │
│       │                                                          │
│       ├── 取出 body.get("password") → "p/M6q7..."                │
│       │                                                          │
│       ▼                                                          │
│  AesUtil.decrypt("p/M6q7...")                                    │
│       │                                                          │
│       ├── Base64 解码                                             │
│       ├── 分离前16字节 → IV                                       │
│       ├── 分离剩余部分 → 密文                                      │
│       ├── AES-256-CBC 解密                                        │
│       └── 得到明文 "123456"                                       │
│       │                                                          │
│       ▼                                                          │
│  BCrypt 验证密码（与数据库中的加密密码比对）                          │
│       │                                                          │
│       ├── 匹配 → 返回 JWT 令牌                                    │
│       └── 不匹配 → 返回 "用户名密码错误"                            │
│                                                                  │
└──────────────────────────────────────────────────────────────────┘
```

---

## 六、哪些地方用到了 AES 加密？

| 功能 | 前端文件 | 后端接口 | 加密的字段 |
|------|---------|---------|-----------|
| 登录 | `login/index.vue` | `POST /auth/login` | `password` |
| 修改密码 | `profile/index.vue` | `PUT /user/profile` | `old_password`, `new_password` |
| 重置密码（管理员） | `manage/index.vue` | `PUT /user` | `password` |
| 新增用户（管理员） | `manage/index.vue` | `POST /user` | `password` |

---

## 七、你可能被问到的问题

**Q: 为什么选择 AES-256-CBC 而不是其他加密方式？**

A: AES-256 是目前公认最安全的对称加密标准，256 位密钥长度足以抵御暴力破解。CBC 模式是最常用的分组密码模式，配合随机 IV 可以有效隐藏数据模式。Web Crypto API 和 Java JCA 都原生支持，不需要引入第三方库。

**Q: 密钥直接写在前端代码里，安全吗？**

A: 不够安全。在生产环境中，密钥应该通过安全的方式分发（如环境变量、密钥管理服务）。但在这个项目中，AES 加密的主要目的是**防止密码在网络传输中以明文形式出现**，而不是防止前端代码被逆向。即使攻击者拿到了前端密钥，他也需要在网络中间截获请求才能解密——而 HTTPS 本身已经保护了传输层。

**Q: AES 和 BCrypt 有什么区别？为什么两个都要用？**

A: 
- **AES** 是**对称加密**（可逆），用于传输过程中保护密码。前端加密，后端解密。
- **BCrypt** 是**哈希算法**（不可逆），用于数据库中存储密码。只能从明文算出哈希，不能从哈希反推明文。

两者分工不同：AES 保护传输过程，BCrypt 保护存储安全。

**Q: 解密失败会怎样？**

A: `AesUtil.decrypt()` 返回 `null`，AuthController 会返回"用户名密码错误"。这防止了攻击者通过伪造密文来探测系统。

**Q: 为什么加密结果的 Base64 字符串每次都不一样？**

A: 因为每次加密时生成一个随机的 16 字节 IV（初始化向量）。即使加密相同的明文，不同的 IV 也会产生完全不同的密文。这是 AES-CBC 模式的安全特性，防止密文对比攻击。

---

## 八、关键代码速查

| 你想找... | 在哪个文件 |
|-----------|-----------|
| 前端加密函数 | `front/src/utils/crypto.js` → `encrypt()` |
| 后端解密函数 | `backend/.../util/AesUtil.java` → `decrypt()` |
| 登录时如何调用 | `front/src/pages/login/index.vue` → `handleLogin()` |
| 后端登录如何接收 | `backend/.../controller/AuthController.java` → `login()` |
| 修改密码时如何加密 | `front/src/pages/profile/index.vue` → `submitProfile()` |
| 重置密码时如何加密 | `front/src/pages/manage/index.vue` → `doResetPassword()` |
| 后端如何解密更新请求 | `backend/.../controller/UserController.java` → `update()` 和 `updateProfile()` |
