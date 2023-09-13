package web.mapper;

import constants.ProjectState;
import constants.UserStatus;
import models.*;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;
import org.springframework.stereotype.Component;
import web.modelView.UserProjectView;
import web.modelView.UserStatView;

import java.time.LocalDate;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class UserMapper {

    private static final String WAVE_INTRA = "Intra";
    private static final String WAVE_UNKNOWN = "???";
    private static final String ALUMNI = "(alumni)";
    private static final String DEACTIVATED = "(deactivated)";

    public static final Map<String, String> CAMPUS_LOCALE = Map.of(
            "msk", "Москва",
            "kzn", "Казань",
            "nsk", "Новосибирск"
    );

    private static final Map<ProjectState, String> STATE_LOCALE = Map.of(
            ProjectState.IN_PROGRESS, "In progress",
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

    private final ModelMapper userStatMapper = new ModelMapper();
    private final ModelMapper userProjectMapper = new ModelMapper();

    public UserMapper() {
        setupUserStatMapper();
        setupUserProjectMapper();
    }

    public UserStatView getUserStatView(User user) {
        return user != null ? userStatMapper.map(user, UserStatView.class) : null;
    }

    public UserProjectView getUserProjectView(UserProject userProject) {
        return userProject != null ? userStatMapper.map(userProject, UserProjectView.class) : null;
    }

    private void setupUserStatMapper() {
        TypeMap<User, UserStatView> userStatTypeMap = this.userStatMapper.createTypeMap(User.class, UserStatView.class);
        userStatTypeMap.addMappings(mapper -> mapper.map(this::getLoginWithStatus, UserStatView::setLogin));
        userStatTypeMap.addMappings(mapper -> mapper.map(this::getWaveNumber, UserStatView::setWave));
        userStatTypeMap.addMappings(mapper -> mapper.map(this::getCurrentProject, UserStatView::setCurrentProject));
        userStatTypeMap.addMappings(mapper -> mapper.map(this::getXPProgress, UserStatView::setProgress3month));
        userStatTypeMap.addMappings(mapper -> mapper.map(user -> CAMPUS_LOCALE.get(user.getCampus().getName()), UserStatView::setCampus));
        userStatTypeMap.addMappings(mapper -> mapper.map(user -> user.getCoalition().getId(), UserStatView::setCoalition));
    }

    private void setupUserProjectMapper() {
        TypeMap<UserProject, UserProjectView> userProjectTypeMap = this.userProjectMapper.createTypeMap(UserProject.class, UserProjectView.class);
        userProjectTypeMap.addMappings(mapper -> mapper.map(up -> getLoginWithStatus(up.getUser()), UserProjectView::setLogin));
        userProjectTypeMap.addMappings(mapper -> mapper.map(up -> up.getUser().getEmail(), UserProjectView::setEmail));
        userProjectTypeMap.addMappings(mapper -> mapper.map(up -> CAMPUS_LOCALE.get(up.getUser().getCampus().getName()), UserProjectView::setCampus));
        userProjectTypeMap.addMappings(mapper -> mapper.map(up -> up.getUser().getCoalition().getId(), UserProjectView::setCoalition));
        userProjectTypeMap.addMappings(mapper -> mapper.map(up -> getWaveNumber(up.getUser()), UserProjectView::setWave));
        userProjectTypeMap.addMappings(mapper -> mapper.map(up -> up.getUser().getWaveName(), UserProjectView::setWaveName));
        userProjectTypeMap.addMappings(mapper -> mapper.map(up -> up.getUser().getLevel(), UserProjectView::setLevel));
        userProjectTypeMap.addMappings(mapper -> mapper.map(up -> up.getUser().getXp(), UserProjectView::setXp));
        userProjectTypeMap.addMappings(mapper -> mapper.map(up -> STATE_LOCALE.get(up.getProjectState()), UserProjectView::setState));
        userProjectTypeMap.addMappings(mapper -> mapper.map(up -> up.getUser().getWorkplace().getFullName(), UserProjectView::setLocation));
    }

    public String getWaveNumber(User user) {
        String platformClass = user.getWaveName();
        String wavePrefix = user.getCampus().getWavePrefix();

        String wave;
        if (platformClass.startsWith(wavePrefix) && platformClass.length() == 13) {
            wave = platformClass.substring(
                    wavePrefix.length(),
                    platformClass.indexOf("_", wavePrefix.length()));
        } else if (platformClass.startsWith(wavePrefix)){
            wave = WAVE_INTRA;
        } else {
            wave = WAVE_UNKNOWN;
        }
        return wave;
    }

    public String getLoginWithStatus(User user) {
        String login = user.getLogin();
        UserStatus status = user.getStatus();
        if (status.equals(UserStatus.ALUMNI)) {
            login += " " + ALUMNI;
        } else if (status.equals(UserStatus.DEACTIVATED)) {
            login += " " + DEACTIVATED;
        }
        return login;
    }

    private String getCurrentProject(User user) {
        return user.getUserProjectList().stream()
                .filter(up -> up.getProjectState().equals(ProjectState.IN_PROGRESS)
                        || up.getProjectState().equals(ProjectState.P2P_EVALUATIONS))
                .map(UserProject::getProject)
                .map(Project::getProjectName)
                .collect(Collectors.joining(" "));
    }

    private int getXPProgress(User user) {
        return user.getXpGains().stream()
                .filter(xp -> xp.getDate().isAfter(LocalDate.now().minusMonths(3)))
                .mapToInt(XpGain::getPoints)
                .sum();
    }
}
