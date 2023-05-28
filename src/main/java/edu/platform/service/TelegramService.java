package edu.platform.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestTemplate;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Component
public class TelegramService {
    private static final String TG_API_URL = "https://api.telegram.org/bot";
    private static final String TG_METHOD_SEND_MESSAGE = "/sendMessage";
    private static final String TG_CHAT_ID = "chat_id";
    private static final String TG_TEXT = "text";

    @Value("${bot.adminId}")
    private long botAdminId;

    @Value("${bot.token}")
    private String botToken;

    public void sendToAdmin(String input) {
        String message = URLDecoder
                .decode(input, StandardCharsets.UTF_8)
                .replaceAll("&", "\n");

        sendMessageToBot(botAdminId, message);
    }

    private void sendMessageToBot(long chatId, String message) {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode requestJson = objectMapper.createObjectNode();
        requestJson.put(TG_CHAT_ID, chatId);
        requestJson.put(TG_TEXT, message);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity<>(requestJson.toString(), headers);
        String tgUrl = TG_API_URL + botToken + TG_METHOD_SEND_MESSAGE;
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.postForObject(tgUrl, request, String.class);
    }
}


