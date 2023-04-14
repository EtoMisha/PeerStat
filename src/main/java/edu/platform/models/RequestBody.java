package edu.platform.models;

import java.time.LocalDate;

public class RequestBody {

    public static String getGraphBasisGoals(User user) {
        return String.format("""
            {
              "operationName": "getGraphBasisGoals",
              "variables": {
                "studentId": "%s"
              },
              "query": "query getGraphBasisGoals($studentId: UUID!) {\\n  student {\\n    getBasisGraph(studentId: $studentId) {\\n      isIntensiveGraphAvailable\\n      graphNodes {\\n        ...BasisGraphNode\\n        __typename\\n      }\\n      __typename\\n    }\\n    __typename\\n  }\\n}\\n\\nfragment BasisGraphNode on GraphNode {\\n  graphNodeId\\n  nodeCode\\n  studyDirections {\\n    id\\n    name\\n    color\\n    textColor\\n    __typename\\n  }\\n  entityType\\n  entityId\\n  goal {\\n    goalExecutionType\\n    projectState\\n    projectName\\n    projectDescription\\n    projectPoints\\n    projectDate\\n    duration\\n    isMandatory\\n    __typename\\n  }\\n  course {\\n    courseType\\n    projectState\\n    projectName\\n    projectDescription\\n    projectPoints\\n    projectDate\\n    duration\\n    localCourseId\\n    __typename\\n  }\\n  parentNodeCodes\\n  __typename\\n}\\n"
            }
            """, user.getStudentId());
    }

    public static String getCredentialInfo(User user) {
        return String.format("""
                {
                  "operationName": "getCredentialsByLogin",
                  "variables": {
                    "login": "%s@student.21-school.ru"
                  },
                  "query": "query getCredentialsByLogin($login: String!) {\\n  school21 {\\n    getStudentByLogin(login: $login) {\\n      studentId\\n      userId\\n      schoolId\\n      isActive\\n      isGraduate\\n      __typename\\n    }\\n    __typename\\n  }\\n}\\n"
                }
                """, user.getLogin());
    }

    public static String getPersonalInfo(User user) {
        return String.format("""
                {
                  "operationName": "publicProfileGetPersonalInfo",
                  "variables": {
                    "userId": "%s",
                    "studentId": "%s",
                    "schoolId": "%s",
                    "login": "%s@student.21-school.ru"
                  },
                  "query": "query publicProfileGetPersonalInfo($userId: UUID!, $studentId: UUID!, $login: String!, $schoolId: UUID!) {\\n  student {\\n    getAvatarByUserId(userId: $userId)\\n    getStageGroupS21PublicProfile(studentId: $studentId) {\\n      waveId\\n      waveName\\n      eduForm\\n      __typename\\n    }\\n    getExperiencePublicProfile(userId: $userId) {\\n      value\\n      level {\\n        levelCode\\n        range {\\n          leftBorder\\n          rightBorder\\n          __typename\\n        }\\n        __typename\\n      }\\n      cookiesCount\\n      coinsCount\\n      codeReviewPoints\\n      __typename\\n    }\\n    getEmailbyUserId(userId: $userId)\\n    getWorkstationByLogin(login: $login) {\\n      workstationId\\n      hostName\\n      row\\n      number\\n      __typename\\n    }\\n    getClassRoomByLogin(login: $login) {\\n      id\\n      number\\n      floor\\n      __typename\\n    }\\n    getFeedbackStatisticsAverageScore(studentId: $studentId) {\\n      countFeedback\\n      feedbackAverageScore {\\n        categoryCode\\n        categoryName\\n        value\\n        __typename\\n      }\\n      __typename\\n    }\\n    __typename\\n  }\\n  user {\\n    getSchool(schoolId: $schoolId) {\\n      id\\n      fullName\\n      shortName\\n      address\\n      __typename\\n    }\\n    __typename\\n  }\\n}\\n"
                }
                """, user.getUserId(), user.getStudentId(), user.getSchoolId(), user.getLogin());
    }

    public static String getCoalitionInfo(User user) {
        return String.format("""
                {
                  "operationName": "publicProfileGetCoalition",
                  "variables": {
                    "userId": "%s"
                  },
                  "query": "query publicProfileGetCoalition($userId: UUID!) {\\n  student {\\n    getUserTournamentWidget(userId: $userId) {\\n      coalitionMember {\\n        coalition {\\n          avatarUrl\\n          color\\n          name\\n          memberCount\\n          __typename\\n        }\\n        currentTournamentPowerRank {\\n          rank\\n          power {\\n            id\\n            points\\n            __typename\\n          }\\n          __typename\\n        }\\n        __typename\\n      }\\n      lastTournamentResult {\\n        userRank\\n        power\\n        __typename\\n      }\\n      __typename\\n    }\\n    __typename\\n  }\\n}\\n"
                }
                """, user.getUserId());
    }

    public static String getAchievements(User user){
        return String.format("""
                {
                  "operationName": "publicProfileLoadAverageLogtime",
                  "variables": {
                    "login": "%s@student.21-school.ru",
                    "schoolID": "%s",
                    "date": "%s"
                  },
                  "query": "query publicProfileLoadAverageLogtime($login: String!, $schoolID: UUID!, $date: Date!) {\\n  school21 {\\n    loadAverageLogtime(login: $login, schoolID: $schoolID, date: $date) {\\n      week\\n      month\\n      weekPerMonth\\n      __typename\\n    }\\n    __typename\\n  }\\n}\\n"
                }
                """, user.getLogin(), user.getSchoolId(), LocalDate.now());
    }

    public static String getLogTime(User user) {
        return String.format("""
                {
                  "operationName": "publicProfileLoadAverageLogtime",
                  "variables": {
                    "login": "%s@student.21-school.ru",
                    "schoolID": "%s",
                    "date": "%s"
                  },
                  "query": "query publicProfileLoadAverageLogtime($login: String!, $schoolID: UUID!, $date: Date!) {\\n  school21 {\\n    loadAverageLogtime(login: $login, schoolID: $schoolID, date: $date) {\\n      week\\n      month\\n      weekPerMonth\\n      __typename\\n    }\\n    __typename\\n  }\\n}\\n"
                }
                """, user.getLogin(), user.getSchoolId(), LocalDate.now());
    }

    public static String getStageInfo(User user) {
        return String.format("""
                {
                  "operationName": "publicProfileLoadStageGroups",
                  "variables": {
                    "userId": "%s",
                    "schoolId": "%s"
                  },
                  "query": "query publicProfileLoadStageGroups($userId: UUID!, $schoolId: UUID!) {\\n  school21 {\\n    loadStudentStageGroupsS21PublicProfile(userId: $userId, schoolId: $schoolId) {\\n      stageGroupStudentId\\n      studentId\\n      stageGroupS21 {\\n        waveId\\n        waveName\\n        eduForm\\n        active\\n        __typename\\n      }\\n      safeSchool {\\n        fullName\\n        __typename\\n      }\\n      __typename\\n    }\\n    __typename\\n  }\\n}\\n"
                }
                """, user.getUserId(), user.getSchoolId());
    }

    public static String getXpHistory(User user) {
        return String.format("""
                {
                  "operationName": "publicProfileGetXpGraph",
                  "variables": {
                    "userId": "%s"
                  },
                  "query": "query publicProfileGetXpGraph($userId: UUID!) {\\n  student {\\n    getExperienceHistoryDate(userId: $userId) {\\n      history {\\n        awardDate\\n        expValue\\n        __typename\\n      }\\n      __typename\\n    }\\n    __typename\\n  }\\n}\\n"
                }
                """, user.getUserId());
    }

    public static String getProjects(User user) {
        return String.format("""
                {
                  "operationName": "publicProfileGetProjects",
                  "variables": {
                    "studentId": "%s",
                    "stageGroupId": "%s"
                  },
                  "query": "query publicProfileGetProjects($studentId: UUID!, $stageGroupId: ID!) {\\n  school21 {\\n    getStudentProjectsForPublicProfileByStageGroup(\\n      studentId: $studentId\\n      stageGroupId: $stageGroupId\\n    ) {\\n      groupName\\n      name\\n      experience\\n      finalPercentage\\n      goalId\\n      goalStatus\\n      amountAnswers\\n      amountReviewedAnswers\\n      __typename\\n    }\\n    __typename\\n  }\\n}\\n"
                }
                """, user.getStudentId(), user.getWaveId());
    }

}
