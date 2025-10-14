package com.mask47631.maskserver.util;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class TokenUtil {
    private static final String ALGORITHM = "AES";
    private static final String SEPARATOR = ":";

    /**
     * 生成固定长度的AES密钥
     * @param secret 原始密钥字符串
     * @return 16字节的AES密钥
     */
    private static SecretKeySpec generateKey(String secret) {
        try {
            // 使用SHA-256哈希算法生成固定长度的密钥
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            byte[] key = sha.digest(secret.getBytes(StandardCharsets.UTF_8));
            // 使用前16字节作为AES-128密钥
            key = java.util.Arrays.copyOf(key, 16);
            return new SecretKeySpec(key, ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("生成密钥失败", e);
        }
    }

    /**
     * 加密生成token，内容为userId:expireTimestamp
     * @param userId 用户ID
     * @param secret 密钥
     * @param expireMinutes 有效期（分钟）
     * @return token
     */
    public static String encrypt(String userId, String secret, long expireMinutes) {
        try {
            long expireAt = System.currentTimeMillis() + expireMinutes * 60 * 1000;
            String content = userId + SEPARATOR + expireAt;
            SecretKeySpec key = generateKey(secret);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] encrypted = cipher.doFinal(content.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("Token加密失败", e);
        }
    }

    /**
     * 解密token，返回userId，若token已过期抛异常
     */
    public static String decrypt(String token, String secret) {
        try {
            SecretKeySpec key = generateKey(secret);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] decoded = Base64.getDecoder().decode(token);
            byte[] decrypted = cipher.doFinal(decoded);
            String content = new String(decrypted, StandardCharsets.UTF_8);
            String[] arr = content.split(SEPARATOR);
            if (arr.length != 2) throw new RuntimeException("Token格式错误");
            String userId = arr[0];
            long expireAt = Long.parseLong(arr[1]);
            if (System.currentTimeMillis() > expireAt) {
                throw new RuntimeException("Token已过期");
            }
            return userId;
        } catch (Exception e) {
            throw new RuntimeException("Token解密失败", e);
        }
    }
}