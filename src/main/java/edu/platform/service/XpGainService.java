package edu.platform.service;

import com.fasterxml.jackson.databind.JsonNode;
import edu.platform.models.User;
import edu.platform.models.XpGain;
import edu.platform.repository.XpGainRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static edu.platform.constants.GraphQLConstants.*;

@RequiredArgsConstructor
@Service
public class XpGainService {
    private final XpGainRepository xpGainRepository;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final Logger LOG = LoggerFactory.getLogger(XpGainService.class);

    public void createIfNotExist(User user, JsonNode xpGainsJson) {
        if (xpGainsJson.isEmpty()) {
            LOG.error("Empty xpGain");
            return;
        }

        List<XpGain> userXpGains = xpGainRepository.findXpGainsByUser(user);
        JsonNode xpGainsArr = xpGainsJson.at(PATH_XP_HISTORY);
        for (JsonNode xpGainJson : xpGainsArr) {
            String dateStr = xpGainJson.get(AWARD_DATE).asText();
            int xpValue = xpGainJson.get(XP_VALUE).asInt();

            XpGain xpGain = create(user, dateStr, xpValue);
            if (!userXpGains.contains(xpGain)) {
                xpGainRepository.save(xpGain);
            }
        }
    }

    public XpGain create(User user, String dateStr, int xpValue) {
        XpGain xpGain = new XpGain();
        xpGain.setUser(user);
        xpGain.setDate(LocalDate.parse(dateStr, formatter));
        xpGain.setPoints(xpValue);
        return xpGain;
    }
}
