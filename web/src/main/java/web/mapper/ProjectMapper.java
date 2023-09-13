package web.mapper;

import constants.ProjectType;
import models.Project;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;
import org.springframework.stereotype.Component;
import web.modelView.ProjectLinkView;
import web.modelView.ProjectView;

import java.util.Map;

@Component
public class ProjectMapper {

    private static final Map<ProjectType, String> TYPE_LOCALE = Map.of(
            ProjectType.INDIVIDUAL, "Индивидуальный",
            ProjectType.GROUP, "Групповой",
            ProjectType.INTENSIVE, "Интенсив",
            ProjectType.MODULE_PROJECT, "Модульный проект",
            ProjectType.EXAM, "Экзамен",
            ProjectType.INTERNSHIP, "Стажировка"
    );

    private static final Map<Boolean, String> MANDATORY_LOCALE = Map.of(
            Boolean.TRUE, "Обязательный",
            Boolean.FALSE, "Необязательный"
    );

    private final ModelMapper projectMapper = new ModelMapper();
    private final ModelMapper projectLinkMapper = new ModelMapper();

    public ProjectMapper() {
        setupProjectMapper();
        setupProjectLinkMapper();
    }

    public ProjectView getProjectView(Project project) {
        return projectMapper.map(project, ProjectView.class);
    }

    public ProjectLinkView getProjectLinkView(Project project) {
        return projectLinkMapper.map(project, ProjectLinkView.class);
    }

    void setupProjectMapper() {
        TypeMap<Project, ProjectView> userStatTypeMap = this.projectMapper.createTypeMap(Project.class, ProjectView.class);
        userStatTypeMap.addMappings(mapper -> mapper.map(project -> MANDATORY_LOCALE.get(project.getIsMandatory()), ProjectView::setMandatory));
        userStatTypeMap.addMappings(mapper -> mapper.map(project -> TYPE_LOCALE.get(project.getProjectType()), ProjectView::setType));
    }

    void setupProjectLinkMapper() {
        TypeMap<Project, ProjectLinkView> userStatTypeMap = this.projectMapper.createTypeMap(Project.class, ProjectLinkView.class);
        userStatTypeMap.addMappings(mapper -> mapper.map(this::getProjectLinkText, ProjectLinkView::setText));
    }

    private String getProjectLinkText(Project project) {
        return project.getNodeCode() + " " + project.getProjectName();
    }
}
