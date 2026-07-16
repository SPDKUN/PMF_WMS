package com.neuedu.pmf.util;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * AES-256-CBC 解密工具
 * 与前端的 crypto.js 配合使用，解密前端加密后的密码。
 *
 * 密文格式：Base64( IV(16字节) + 密文 )
 * 解密时先从 Base64 解码，分离前 16 字节作为 IV，剩余为密文。
 */
public class AesUtil {

    private static final String AES_KEY = "PMF-WMS-AES-Key-2026!!SecretKey#"; // 32 字节
    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";

    /**
     * 解密前端传来的 AES 密文
     * @param encryptedBase64 Base64 编码的密文（含 IV）
     * @return 明文密码，解密失败返回 null
     */
    public static String decrypt(String encryptedBase64) {
        if (encryptedBase64 == null || encryptedBase64.isEmpty()) {
            return null;
        }
        try {
            byte[] combined = Base64.getDecoder().decode(encryptedBase64);
            byte[] iv = new byte[16];
            byte[] ciphertext = new byte[combined.length - 16];
            System.arraycopy(combined, 0, iv, 0, 16);
            System.arraycopy(combined, 16, ciphertext, 0, ciphertext.length);

            SecretKeySpec keySpec = new SecretKeySpec(AES_KEY.getBytes(StandardCharsets.UTF_8), "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
            byte[] plaintext = cipher.doFinal(ciphertext);
            return new String(plaintext, StandardCharsets.UTF_8);
        } catch (Exception e) {
            System.err.println("AES decrypt failed: " + e.getMessage());
            return null;
        }
    }
}
