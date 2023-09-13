package web.modelView;

import lombok.Data;
import org.springframework.stereotype.Component;

@Data
@Component
public class UserStatView {
    private String login;
    private String email;
    private String campus;
    private String coalition;
    private String wave;
    private String waveName;
    private String bootcampName;
    private int level;
    private int xp;
    private int peerPoints;
    private int codeReviewPoints;
    private int coins;
    private int progress3month;
    private String currentProject;
}
