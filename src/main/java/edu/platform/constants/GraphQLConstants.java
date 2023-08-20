package edu.platform.constants;

public interface GraphQLConstants {
    String DATA = "data";
    String PATH_SEARCH_RESULT = "/school21/searchByText/profiles/profiles";

    // Credentials
    String PATH_STUDENT = "/school21/getStudentByLogin";
    String STUDENT_ID = "studentId";
    String USER_ID = "userId";
    String IS_ACTIVE = "isActive";
    String IS_GRADUATE = "isGraduate";

    // Personal info
    String STUDENT = "student";
    String STAGE_INFO = "getStageGroupS21PublicProfile";
    String WAVE_ID = "waveId";
    String WAVE_NAME = "waveName";
    String EDU_FORM = "eduForm";
    String XP_INFO = "getExperiencePublicProfile";
    String VALUE = "value";
    String LEVEL = "level";
    String LEVEL_CODE = "levelCode";
    String RANGE = "range";
    String LEFT_BORDER = "leftBorder";
    String RIGHT_BORDER = "rightBorder";
    String PEER_POINTS = "cookiesCount";
    String COINS_COUNT = "coinsCount";
    String CODE_REVIEW_POINTS = "codeReviewPoints";
    String EMAIL = "getEmailbyUserId";

    // Coalition
    String PATH_COALITION_NAME = "/getUserTournamentWidget/coalitionMember/coalition/name";

    // Achievement
    String PATH_ACHIEVEMENTS = "/student/getBadgesPublicProfile";
    String BADGE = "badge";
    String ID = "id";
    String NAME = "name";
    String AVATAR_URL = "avatarUrl";
    String POINTS = "points";

    // Skill
    String PATH_SKILLS = "/school21/getSoftSkillsByStudentId";
    String CODE = "code";
    String TYPE = "type";
    String TOTAL_POWER = "totalPower";

    // Feedback
    String PATH_FEEDBACK = "/getFeedbackStatisticsAverageScore/feedbackAverageScore";
    String FEEDBACK_VALUE = "value";

    // Xp gains
    String PATH_XP_HISTORY = "/student/getExperienceHistoryDate/history";
    String AWARD_DATE = "awardDate";
    String XP_VALUE = "expValue";

    // Intensive
    String PATH_STAGE_GROUPS = "/school21/loadStudentStageGroupsS21PublicProfile";
    String STAGE_GROUPS = "stageGroupS21";
    String SURVIVAL_CAMP = "Survival camp";

    // Graph
    String PATH_GRAPH = "/student/getBasisGraph/graphNodes";
    String GRAPH_NODE_ID = "graphNodeId";
    String NODE_CODE = "nodeCode";
    String ENTITY_ID = "entityId";
    String ENTITY_TYPE = "entityType";
    String GOAL_TYPE = "goalExecutionType";
    String IS_MANDATORY = "isMandatory";
    String COURSE_TYPE = "courseType";
    String COURSE_ID = "localCourseId";
    String PROJECT_NAME = "projectName";
    String PROJECT_DESCRIPTION = "projectDescription";
    String PROJECT_POINTS = "projectPoints";
    String DURATION = "duration";
    String PROJECT_STATE = "projectState";

    // User projects
    String PATH_USER_PROJECTS = "/school21/getStudentProjectsForPublicProfileByStageGroup";
    String GOAL_ID = "goalId";
    String GOAL_STATUS = "goalStatus";
    String FINAL_PERCENTAGE = "finalPercentage";
    String CORE_PROGRAM = "Core program";
    String LOGIN = "login";

    // Project info
    String PATH_PROJECT_INFO = "/student/getModuleById";
    String STUDY_MODULE = "studyModule";
    String IDEA = "idea";
    String GOAL_POINTS = "goalPoint";
}
