package web.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Service
public class TelegramService {
    private static final String TG_API_URL = "https://api.telegram.org/bot";
    private static final String TG_METHOD_SEND_MESSAGE = "/sendMessage";
    private static final String TG_CHAT_ID = "chat_id";
    private static final String TG_TEXT = "text";
    private static final String FORM_RESPONSE = "Ок, записал, спасибо, попозже внесу на карту";

    @Value("${bot.admin-id}")
    private long botAdminId;

    @Value("${bot.token}")
    private String botToken;

    public String sendFormDataToAdmin(String input) {
        String message = URLDecoder
                .decode(input, StandardCharsets.UTF_8)
                .replaceAll("&", "\n");

        sendMessageToBot(botAdminId, message);

        return FORM_RESPONSE;
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


