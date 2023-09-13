package web.modelView;

import lombok.Data;
import org.springframework.stereotype.Component;

@Data
@Component
public class UserProjectView {
    private String login;
    private String email;
    private String campus;
    private String coalition;
    private String wave;
    private String waveName;
    private int level;
    private int xp;
    private String state;
    private int score;
    private String location;
}