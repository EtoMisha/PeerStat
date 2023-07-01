package edu.platform.mapper;

import edu.platform.constants.ProjectState;
import edu.platform.modelView.ProjectUserView;
import edu.platform.models.Campus;
import edu.platform.models.User;
import edu.platform.models.UserProject;
import edu.platform.service.CampusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class UserProjectMapper {

    private static final Map<ProjectState, String> STATE_LOCALE = Map.of(
            ProjectState.IN_PROGRESS, "In progres",
            ProjectState.FAILED, "Failed",
            ProjectState.UNAVAILABLE, "Unavailable",
            ProjectState.COMPLETED, "Completed",
            ProjectState.LOCKED, "Locked",
            ProjectState.UNLOCKED, "Unlocked",
            ProjectState.P2P_EVALUATIONS, "Evaluations",
            ProjectState.WAITING_FOR_START, "Waiting for start",
            ProjectState.READY_TO_START, "Ready to start",
            ProjectState.REGISTRATION_IS_OPEN, "Registration is open"
    );

    private static final Map<ProjectState, String> STATE_LOCALE_RU = Map.of(
            ProjectState.IN_PROGRESS, "В процессе",
            ProjectState.FAILED, "Фейл",
            ProjectState.UNAVAILABLE, "Недоступен",
            ProjectState.COMPLETED, "Сдан",
            ProjectState.LOCKED, "Недоступен",
            ProjectState.UNLOCKED, "Доспупен",
            ProjectState.P2P_EVALUATIONS, "Проверяется",
            ProjectState.WAITING_FOR_START, "Ждёт старта",
            ProjectState.READY_TO_START, "Можно начать",
            ProjectState.REGISTRATION_IS_OPEN, "Открыта регистрация"
    );

    private CampusService campusService;

    @Autowired
    public void setCampusService(CampusService campusService) {
        this.campusService = campusService;
    }

    public ProjectUserView getProjectUserView(UserProject userProject) {
        ProjectUserView view = new ProjectUserView();
        User user = userProject.getUser();
        Campus userCampus = campusService.getCampusById((user.getCampus().getSchoolId()));

        view.setLogin(UserMapper.getLogin(user));
        view.setEmail(user.getEmail());
        view.setCampus(userCampus.getCampusName());
        view.setCoalition(user.getCoalitionName());
        view.setWave(UserMapper.getRealWave(user, userCampus));
        view.setPlatformClass(user.getWaveName());
        view.setLevel(user.getLevel());
        view.setXp(user.getXp());
        view.setState(STATE_LOCALE.get(userProject.getProjectState()));
        view.setScore(userProject.getScore());

        return view;
    }
}
