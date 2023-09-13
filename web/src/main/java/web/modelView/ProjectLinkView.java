package web.modelView;

import lombok.Data;
import org.springframework.stereotype.Component;

@Data
@Component
public class ProjectLinkView {
    private Long id;
    private String text;
}
