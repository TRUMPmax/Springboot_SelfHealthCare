package com.example.selfhealthcare.service;

import com.example.selfhealthcare.dto.AiConsultationRequest;
import com.example.selfhealthcare.dto.AiConsultationResponse;
import com.example.selfhealthcare.exception.AiIntegrationException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
public class DeepSeekConsultationService {

    private final ProfileInsightService profileInsightService;
    private final String apiKey;
    private final String baseUrl;
    private final String model;
    private final boolean enabled;

    public DeepSeekConsultationService(
            ProfileInsightService profileInsightService,
            @Value("${deepseek.api-key:}") String apiKey,
            @Value("${deepseek.base-url:https://api.deepseek.com}") String baseUrl,
            @Value("${deepseek.model:deepseek-chat}") String model,
            @Value("${deepseek.enabled:false}") boolean enabled) {
        this.profileInsightService = profileInsightService;
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        this.model = model;
        this.enabled = enabled;
    }

    public AiConsultationResponse consult(Long profileId, AiConsultationRequest request) {
        if (!enabled) {
            throw new AiIntegrationException("AI 咨询功能当前未启用，请先在服务端配置 DeepSeek。");
        }
        if (apiKey == null || apiKey.isBlank()) {
            throw new AiIntegrationException("DeepSeek API Key 未配置，暂时无法发起咨询。");
        }

        ProfileInsightService.ConsultationContext context =
                profileInsightService.buildConsultationContext(profileId, request.focusRecordId(), request.question());

        String systemPrompt = """
                你是一名谨慎、专业的家庭健康管理助手。
                你的任务是基于用户提供的健康档案、近期记录和趋势变化，输出中文分析。
                请遵守以下要求：
                1. 不要做确诊结论，不要替代医生诊疗。
                2. 明确区分“当前风险”“近期变化”“生活方式建议”“何时需要线下就医”。
                3. 回答尽量具体，结合已有指标、趋势和家庭背景。
                4. 如果信息不足，要明确指出仍需继续记录哪些数据。
                5. 语气务实、清晰，适合普通家庭成员阅读。
                """;

        String userPrompt = context.promptContext();

        Map<String, Object> requestBody = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userPrompt)),
                "temperature", 0.4,
                "max_tokens", 900);

        try {
            RestClient restClient = RestClient.builder()
                    .baseUrl(baseUrl)
                    .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                    .build();

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restClient.post()
                    .uri("/chat/completions")
                    .body(requestBody)
                    .retrieve()
                    .body(Map.class);

            if (response == null) {
                throw new AiIntegrationException("DeepSeek 未返回有效内容。");
            }

            Object modelValue = response.get("model");
            String responseModel = modelValue == null ? model : modelValue.toString();

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            if (choices == null || choices.isEmpty()) {
                throw new AiIntegrationException("DeepSeek 返回结果中没有可用的回答。");
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> message = (Map<String, Object>) choices.getFirst().get("message");
            String answer = message == null ? null : String.valueOf(message.get("content"));
            if (answer == null || answer.isBlank()) {
                throw new AiIntegrationException("DeepSeek 返回了空回答，请稍后重试。");
            }

            return new AiConsultationResponse(
                    responseModel,
                    answer.trim(),
                    context.contextDigest(),
                    LocalDateTime.now());
        } catch (RestClientException exception) {
            throw new AiIntegrationException("调用 DeepSeek 失败，请检查 API Key、网络连接或额度状态。", exception);
        }
    }
}
