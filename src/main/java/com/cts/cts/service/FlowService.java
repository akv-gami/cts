package com.cts.cts.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.TreeMap;

@Service
public class FlowService {

    private static final Logger log = LoggerFactory.getLogger(FlowService.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${flow.api-key}")
    private String apiKey;

    @Value("${flow.secret-key}")
    private String secretKey;

    @Value("${flow.api-url:https://sandbox.flow.cl/api}")
    private String apiUrl;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    private final RestTemplate restTemplate;

    public FlowService() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10_000);
        factory.setReadTimeout(30_000);
        this.restTemplate = new RestTemplate(factory);
    }

    public record FlowPaymentResult(String url, String token, Long flowOrder) {}
    public record FlowStatusResult(int status, Long flowOrder, String commerceOrder, Integer amount) {}

    public FlowPaymentResult createPayment(String commerceOrder, String subject, int amount, String email) {
        try {
            TreeMap<String, String> params = new TreeMap<>();
            params.put("apiKey", apiKey);
            params.put("amount", String.valueOf(amount));
            params.put("commerceOrder", commerceOrder);
            params.put("currency", "CLP");
            params.put("email", email);
            params.put("subject", subject);
            params.put("urlConfirmation", baseUrl + "/api/payments/confirm");
            params.put("urlReturn", baseUrl + "/index.html?payment=done");
            params.put("s", sign(params));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            params.forEach(body::add);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
            String raw = restTemplate.postForObject(apiUrl + "/payment/create", request, String.class);
            Map<String, Object> response = objectMapper.readValue(raw, new TypeReference<>() {});

            String payUrl = (String) response.get("url");
            String token = (String) response.get("token");
            Number flowOrderNum = (Number) response.get("flowOrder");

            return new FlowPaymentResult(
                payUrl + "?token=" + token,
                token,
                flowOrderNum != null ? flowOrderNum.longValue() : null
            );

        } catch (Exception e) {
            log.error("Error al crear pago en Flow: {}", e.getMessage(), e);
            throw new RuntimeException("Error al conectar con la pasarela de pago");
        }
    }

    public FlowStatusResult getPaymentStatus(String token) {
        try {
            TreeMap<String, String> params = new TreeMap<>();
            params.put("apiKey", apiKey);
            params.put("token", token);
            String signature = sign(params);

            String url = apiUrl + "/payment/getStatus?apiKey=" + URLEncoder.encode(apiKey, StandardCharsets.UTF_8)
                + "&token=" + URLEncoder.encode(token, StandardCharsets.UTF_8)
                + "&s=" + URLEncoder.encode(signature, StandardCharsets.UTF_8);

            String raw = restTemplate.getForObject(url, String.class);
            Map<String, Object> body = objectMapper.readValue(raw, new TypeReference<>() {});

            Number statusNum = (Number) body.get("status");
            Number flowOrderNum = (Number) body.get("flowOrder");
            Number amountNum = (Number) body.get("amount");

            return new FlowStatusResult(
                statusNum != null ? statusNum.intValue() : 0,
                flowOrderNum != null ? flowOrderNum.longValue() : null,
                (String) body.get("commerceOrder"),
                amountNum != null ? amountNum.intValue() : null
            );

        } catch (Exception e) {
            log.error("Error al consultar estado de pago en Flow: {}", e.getMessage(), e);
            throw new RuntimeException("Error al consultar estado del pago");
        }
    }

    private String sign(TreeMap<String, String> params) throws Exception {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            sb.append(entry.getKey()).append(entry.getValue());
        }
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(keySpec);
        byte[] hash = mac.doFinal(sb.toString().getBytes(StandardCharsets.UTF_8));
        StringBuilder hex = new StringBuilder();
        for (byte b : hash) {
            hex.append(String.format("%02x", b));
        }
        return hex.toString();
    }
}