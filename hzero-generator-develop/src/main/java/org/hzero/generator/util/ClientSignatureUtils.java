package org.hzero.generator.util;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.io.Charsets;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpMethod;

/**
 * 客户端签名工具
 */
public final class ClientSignatureUtils {

    public static final String PARAM_TIMESTAMP = "timestamp";
    public static final String PARAM_SECRET_ID = "secretId";
    public static final String PARAM_NONCE = "nonce";
    public static final String PARAM_SIGNATURE = "signature";
    /**
     * 签名原文的模板 <i>hzero-api/{method}/{timestamp}/{nonce}/{secretId}?{params}</i>
     */
    private static final String SIGN_TEMPLATE = "hzero-api/%s/%s/%s/%s?%s";
    private static final String MAC_NAME = "HmacSHA1";
    private static final Set<HttpMethod> SUPPORT_METHOD = Stream.of(HttpMethod.POST, HttpMethod.GET, HttpMethod.DELETE, HttpMethod.PUT).collect(Collectors.toSet());

    public static void main(String[] args) {
        // 封装请求参数
        Map<String, Object> params = new HashMap<>(8);
//        params.put("username", "test");
//        params.put("email", "test@hand-china.com");

        // 签名ID和密钥
        String secretId = "hzero";
        String secretKey = "537509248a3da7804d12905c102d14cd1bec000797a6178a7353a4c3ac23a0b3";

        // 获取签名
        String signature = ClientSignatureUtils.buildSignature(params, HttpMethod.GET, secretId, secretKey);

        // 将签名加入参数中再请求API
        params.put(PARAM_SIGNATURE, signature);

        System.out.println(SUPPORT_METHOD);
        System.out.println("params is " + params);
        System.out.println("signature is " + signature);
    }

    /**
     * 根据参数等信息获取签名
     *
     * @param params    请求参数
     * @param method    请求方法，支持 {@link HttpMethod#POST}、{@link HttpMethod#GET}、{@link HttpMethod#DELETE}、{@link HttpMethod#PUT}
     * @param secretId  密钥Id
     * @param secretKey 签名密钥
     * @return 签名
     */
    public static String buildSignature(Map<String, Object> params, HttpMethod method, String secretId, String secretKey) {
        if (!SUPPORT_METHOD.contains(method)) {
            throw new IllegalArgumentException("HttpMethod not supported.");
        }
        params = Optional.ofNullable(params).orElse(new HashMap<>(8));
        // 时间戳
        long timestamp = System.currentTimeMillis();
        // 随机正整数
        long nonce = RandomUtils.nextLong();

        // 1.加入签名所需参数
        params.remove(PARAM_SIGNATURE);
        params.put(PARAM_TIMESTAMP, timestamp);
        params.put(PARAM_SECRET_ID, secretId);
        params.put(PARAM_NONCE, nonce);

        // 2.对参数字典排序
        String[] keys = params.keySet().toArray(new String[]{});
        Arrays.sort(keys);

        // 3.拼接请求字符串
        StringBuilder paramBuilder = new StringBuilder();
        for (String key : keys) {
            paramBuilder.append("&").append(key).append("=");
            String value = String.valueOf(params.getOrDefault(key, ""));
            paramBuilder.append(value);
        }
        String paramStr = StringUtils.replaceOnce(paramBuilder.toString(), "&", "");

        // 4.拼接签名原文字符串
        String originSignature = String.format(SIGN_TEMPLATE, method.name(), params.get(PARAM_TIMESTAMP), params.get(PARAM_SECRET_ID), secretId, paramStr);

        // 5.生成签名串
        // 根据给定的字节数组构造一个密钥,第二参数指定一个密钥算法的名称
        SecretKey keySpec = new SecretKeySpec(secretKey.getBytes(Charsets.UTF_8), MAC_NAME);
        Mac mac = null;
        try {
            //生成一个指定 Mac 算法 的 Mac 对象
            mac = Mac.getInstance(MAC_NAME);
            //用给定密钥初始化 Mac 对象
            mac.init(keySpec);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Sign error.");
        }

        // 完成 Mac 操作
        byte[] signBytes = mac.doFinal(originSignature.getBytes(Charsets.UTF_8));

        // 6. 将生成的签名串使用 Base64 进行编码
        return Base64.getEncoder().encodeToString(signBytes);
    }

}
