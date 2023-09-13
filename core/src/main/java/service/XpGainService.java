package service;

import com.fasterxml.jackson.databind.JsonNode;
import models.User;
import models.XpGain;
import repository.XpGainRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RequiredArgsConstructor
@Service
public class XpGainService {

    private static final Logger LOG = LoggerFactory.getLogger(XpGainService.class);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;

    private static final String PATH_XP_HISTORY = "/student/getExperienceHistoryDate/history";
    private static final String AWARD_DATE = "awardDate";
    private static final String XP_VALUE = "expValue";

    private final XpGainRepository xpGainRepository;

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
