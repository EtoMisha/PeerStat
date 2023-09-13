package web.modelView;

import lombok.Data;

@Data
public class ProjectView {
    private Long id;
    private String nodeCode;
    private String projectName;
    private String description;
    private int points;
    private int duration;
    private String mandatory;
    private String type;
}
