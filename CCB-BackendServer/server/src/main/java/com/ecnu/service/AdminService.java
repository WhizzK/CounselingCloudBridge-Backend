package com.ecnu.service;

import com.ecnu.dto.*;
import com.ecnu.vo.*;

import java.util.List;

public interface AdminService {

    AdminHomeVO getHomeInfo();

    OnlineCounselorVO getOnlineCounselor(OnlineCounselorDTO onlineCounselorDTO);

    List<ScheduleVO> getSchedule();

    ScheduleOfDayVO getScheduleOfDay(String day);

    AdminCounselorPageVO getCounselorList(AdminCounselorDTO adminCounselorDTO);

    void updateCounselor(AdminUpdateCounselorDTO adminUpdateCounselorDTO);

    void addCounselor(AdminAddCounselorDTO adminAddCounselorDTO);

    AdminSupervisorPageVO getSupervisorList(AdminCounselorDTO adminCounselorDTO);

    void updateSupervisor(AdminUpdateSupervisorDTO adminUpdateSupervisorDTO);

    void addSupervisor(AdminAddSupervisorDTO adminAddSupervisorDTO);

    List<SupervisorListVO> supervisorList();

    OnlineSupervisorVO getOnlineSupervisor(OnlineCounselorDTO onlineCounselorDTO);

    CounselorHistoryVO getHistory(CounselorHistoryDTO counselorHistoryDTO);

    TodaySessionVariationVO getTodaySessionVariation();

    WeekSessionVariationVO getWeekSessionVariation();

    List<CounselorNumRankVO> getCounselorNumRank();

    List<CounselorRatingRankVO> getCounselorRatingRank();

    void banUser(Long userId);

    void unbanUser(Long userId);
}
