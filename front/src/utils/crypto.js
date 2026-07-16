/**
 * AES-256-CBC 加密工具
 * 用于登录、修改密码等场景的密码加密传输
 *
 * 工作原理：
 *   1. 随机生成 16 字节 IV（初始化向量）
 *   2. 使用 AES-256-CBC 加密明文
 *   3. 将 IV 拼接在密文前面，Base64 编码后返回
 *
 * 后端用相同的密钥和分离 IV 的方式解密。
 */

const AES_KEY = 'PMF-WMS-AES-Key-2026!!SecretKey#' // 32 字节密钥（AES-256）
const encoder = new TextEncoder()

/**
 * 将字符串转换为 CryptoKey
 */
async function getKey() {
  const keyBytes = encoder.encode(AES_KEY)
  return crypto.subtle.importKey(
    'raw',
    keyBytes,
    { name: 'AES-CBC' },
    false,
    ['encrypt']
  )
}

/**
 * AES-256-CBC 加密
 * @param {string} plaintext - 明文
 * @returns {Promise<string>} Base64 编码的密文（IV + 密文）
 */
export async function encrypt(plaintext) {
  if (!plaintext) return ''
  const key = await getKey()
  const iv = crypto.getRandomValues(new Uint8Array(16))
  const data = encoder.encode(plaintext)
  const ciphertext = await crypto.subtle.encrypt(
    { name: 'AES-CBC', iv },
    key,
    data
  )
  const combined = new Uint8Array(iv.length + ciphertext.byteLength)
  combined.set(iv)
  combined.set(new Uint8Array(ciphertext), iv.length)
  return btoa(String.fromCharCode(...combined))
}

export default { encrypt }
