package com.ecnu.service.impl;

import com.ecnu.constant.*;
import com.ecnu.context.BaseContext;
import com.ecnu.dto.*;
import com.ecnu.entity.Counselor;
import com.ecnu.entity.CounselorSupervisorRelation;
import com.ecnu.entity.User;
import com.ecnu.exception.AccountHasExistedException;
import com.ecnu.mapper.*;
import com.ecnu.service.AdminService;
import com.ecnu.vo.*;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private SessionsMapper sessionsMapper;
    @Autowired
    private RequestMapper requestMapper;
    @Autowired
    private RelationMapper relationMapper;
    @Autowired
    private CounselorMapper counselorMapper;
    @Autowired
    private ScheduleMapper scheduleMapper;

    public AdminHomeVO getHomeInfo() {
        User user = userMapper.getById(BaseContext.getCurrentId());
        AdminTodaySessionDTO adminTodaySessionDTO = sessionsMapper.getTodaySession(SessionStatusConstant.CLOSED);
        Long seconds = adminTodaySessionDTO.getTodayHours();
        String todayHours = "00:00:00";
        if(seconds != null) {
            Long hours = seconds / 3600;
            Long remainder = seconds % 3600;
            Long minutes = remainder / 60;
            seconds = remainder % 60;
            todayHours = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        }
        AdminHomeVO adminHomeVO = new AdminHomeVO().builder()
                .realName(user.getRealName())
                .avatarUrl(user.getAvatarUrl())
                .todaySessions(adminTodaySessionDTO.getTodaySessions())
                .todayHours(todayHours)
                .currentSessions(sessionsMapper.getCurrentSessions(SessionStatusConstant.ACTIVE))
                .currentRequests(requestMapper.getCurrentRequests(RequestStatusConstant.ACCEPTED))
                .build();
        return adminHomeVO;
    }

    public OnlineCounselorVO getOnlineCounselor(OnlineCounselorDTO onlineCounselorDTO) {
        PageHelper.startPage(onlineCounselorDTO.getPage(), onlineCounselorDTO.getPagesize());
        Page<OnlineCounselor> page = counselorMapper.getOnlineCounselor();

        OnlineCounselorVO onlineCounselorVO = OnlineCounselorVO.builder()
                .onlineCounselors(page.getResult())
                .total(page.getTotal())
                .build();

        return onlineCounselorVO;
    }

    public List<ScheduleVO> getSchedule() {
        List<ScheduleDTO> schedules = scheduleMapper.getScheduleDTO();
        Map<String, Integer> dayMap = new HashMap<>();
        String[] days = new String[]{"周一", "周二", "周三", "周四", "周五", "周六", "周日"};
        for (int i = 0; i < 7; i++) {
            dayMap.put(days[i], i);
        }
        long[][] cnt = new long[7][2];
        for (ScheduleDTO scheduleDTO : schedules) {
            String day = scheduleDTO.getDayOfWeek();
            String userType = scheduleDTO.getUserType();
            if (userType.equals("counselor")) {
                cnt[dayMap.get(day)][0]++;
            } else {
                cnt[dayMap.get(day)][1]++;
            }
        }
        List<ScheduleVO> schedule = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            schedule.add(new ScheduleVO().builder()
                    .dayOfWeek(days[i])
                    .counselorNum(cnt[i][0])
                    .supervisorNum(cnt[i][1])
                    .build());
        }
        return schedule;
    }

    public ScheduleOfDayVO getScheduleOfDay(String day) {
        List<ScheduleDTO> schedules = scheduleMapper.getScheduleDTO();
        List<ScheduleCounselorVO> counselorList = new ArrayList<>();
        List<ScheduleSupervisor> supervisorList = new ArrayList<>();
        for (ScheduleDTO scheduleDTO : schedules) {
            if(scheduleDTO.getDayOfWeek().equals(day)) {
                if(scheduleDTO.getUserType().equals("counselor")) {
                    counselorList.add(new ScheduleCounselorVO().builder()
                            .counselorId(scheduleDTO.getCounselorId())
                            .realName(scheduleDTO.getRealName())
                            .avatarUrl(scheduleDTO.getAvatarUrl())
                            .build());
                } else {
                    supervisorList.add(new ScheduleSupervisor().builder()
                            .supervisorId(scheduleDTO.getCounselorId())
                            .realName(scheduleDTO.getRealName())
                            .avatarUrl(scheduleDTO.getAvatarUrl())
                            .build());
                }
            }
        }
        ScheduleOfDayVO scheduleOfDayVO = new ScheduleOfDayVO().builder()
                .counselorList(counselorList)
                .supervisorList(supervisorList)
                .build();
        return scheduleOfDayVO;
    }

    public AdminCounselorPageVO getCounselorList(AdminCounselorDTO adminCounselorDTO) {
        PageHelper.startPage(adminCounselorDTO.getPage(), adminCounselorDTO.getPagesize());
        Page<AdminCounselorVO> page = counselorMapper.getCounselorList(adminCounselorDTO);
        List<AdminCounselorVO> counselorList = page.getResult();
        for (AdminCounselorVO adminCounselorVO : counselorList) {
            User user = userMapper.getById(adminCounselorVO.getSupervisorId());
            adminCounselorVO.setSupervisorName(user == null ? null : user.getRealName());
            adminCounselorVO.setTotalSessions(sessionsMapper.getTotalSessions(adminCounselorVO.getCounselorId(), SessionStatusConstant.CLOSED));
            Long seconds = sessionsMapper.getTotalHours(adminCounselorVO.getCounselorId(), SessionStatusConstant.CLOSED);
            String todayHours = "00:00:00";
            if(seconds != null) {
                Long hours = seconds / 3600;
                Long remainder = seconds % 3600;
                Long minutes = remainder / 60;
                seconds = remainder % 60;
                todayHours = String.format("%02d:%02d:%02d", hours, minutes, seconds);
            }
            adminCounselorVO.setTotalHours(todayHours);
            adminCounselorVO.setSchedule(scheduleMapper.getSchedule(adminCounselorVO.getCounselorId()));
        }
        return new AdminCounselorPageVO(counselorList, page.getTotal());
    }

    @Transactional
    public void updateCounselor(AdminUpdateCounselorDTO adminUpdateCounselorDTO) {
        userMapper.updateCounselor(adminUpdateCounselorDTO.getCounselorId(), adminUpdateCounselorDTO.getRealName());
        relationMapper.updateByCounselorId(adminUpdateCounselorDTO.getCounselorId(),adminUpdateCounselorDTO.getSupervisorId());
        scheduleMapper.deleteCounselorSchedule(adminUpdateCounselorDTO.getCounselorId());
        List<String> schedule = adminUpdateCounselorDTO.getSchedule();
        for (String day : schedule) {
            scheduleMapper.insertCounselorSchedule(adminUpdateCounselorDTO.getCounselorId(), day);
        }
    }

    @Transactional
    public void addCounselor(AdminAddCounselorDTO adminAddCounselorDTO) {
        if(userMapper.getByPhoneNumber(adminAddCounselorDTO.getPhoneNumber()) != null) {
            throw new AccountHasExistedException(MessageConstant.ACCOUNT_HAS_EXISTED);
        }
        userMapper.register(new User().builder()
                .age(adminAddCounselorDTO.getAge())
                .avatarUrl(adminAddCounselorDTO.getAvatarUrl())
                .gender(adminAddCounselorDTO.getGender())
                .realName(adminAddCounselorDTO.getRealName())
                .phoneNumber(adminAddCounselorDTO.getPhoneNumber())
                .passwordHash("E10ADC3949BA59ABBE56E057F20F883E")
                .userType(UserTypeConstant.COUNSELOR)
                .status(CommonStatusConstant.INACTIVE)
                .build());
        Long counselorId = userMapper.getByPhoneNumber(adminAddCounselorDTO.getPhoneNumber()).getUserId();
        counselorMapper.insert(new Counselor().builder()
                .counselorId(counselorId)
                .certification(adminAddCounselorDTO.getCertification())
                .expertise(adminAddCounselorDTO.getExpertise())
                .yearsExperience(adminAddCounselorDTO.getYearsExperience())
                .build());
        relationMapper.insert(new CounselorSupervisorRelation().builder()
                .counselorId(counselorId)
                .supervisorId(adminAddCounselorDTO.getSupervisorId())
                .build());
    }

    public AdminSupervisorPageVO getSupervisorList(AdminCounselorDTO adminCounselorDTO) {
        PageHelper.startPage(adminCounselorDTO.getPage(), adminCounselorDTO.getPagesize());
        Page<AdminSupervisorVO> page = userMapper.getSupervisorList(adminCounselorDTO);
        List<AdminSupervisorVO> supervisorList = page.getResult();
        for (AdminSupervisorVO adminSupervisorVO : supervisorList) {
            adminSupervisorVO.setTotalRequests(requestMapper.getTotalRequests(adminSupervisorVO.getSupervisorId(), RequestStatusConstant.COMPLETED));
            Long seconds = requestMapper.getTotalHours(adminSupervisorVO.getSupervisorId(), RequestStatusConstant.COMPLETED);
            String todayHours = "00:00:00";
            if(seconds != null) {
                Long hours = seconds / 3600;
                Long remainder = seconds % 3600;
                Long minutes = remainder / 60;
                seconds = remainder % 60;
                todayHours = String.format("%02d:%02d:%02d", hours, minutes, seconds);
            }
            adminSupervisorVO.setTotalHours(todayHours);
            adminSupervisorVO.setSchedule(scheduleMapper.getSchedule(adminSupervisorVO.getSupervisorId()));
        }
        return new AdminSupervisorPageVO(supervisorList, page.getTotal());
    }

    @Transactional
    public void updateSupervisor(AdminUpdateSupervisorDTO adminUpdateSupervisorDTO) {
        userMapper.updateSupervisor(adminUpdateSupervisorDTO.getSupervisorId(), adminUpdateSupervisorDTO.getRealName());
        scheduleMapper.deleteSupervisorSchedule(adminUpdateSupervisorDTO.getSupervisorId());
        List<String> schedule = adminUpdateSupervisorDTO.getSchedule();
        for (String day : schedule) {
            scheduleMapper.insertSupervisorSchedule(adminUpdateSupervisorDTO.getSupervisorId(), day);
        }
    }

    public void addSupervisor(AdminAddSupervisorDTO adminAddSupervisorDTO) {
        if(userMapper.getByPhoneNumber(adminAddSupervisorDTO.getPhoneNumber()) != null) {
            throw new AccountHasExistedException(MessageConstant.ACCOUNT_HAS_EXISTED);
        }
        userMapper.register(new User().builder()
                .age(adminAddSupervisorDTO.getAge())
                .avatarUrl(adminAddSupervisorDTO.getAvatarUrl())
                .gender(adminAddSupervisorDTO.getGender())
                .realName(adminAddSupervisorDTO.getRealName())
                .phoneNumber(adminAddSupervisorDTO.getPhoneNumber())
                .passwordHash("E10ADC3949BA59ABBE56E057F20F883E")
                .userType(UserTypeConstant.SUPERVISOR)
                .status(CommonStatusConstant.INACTIVE)
                .build());
    }

    public List<SupervisorListVO> supervisorList() {
        List<SupervisorListVO> supervisorList = userMapper.supervisorList();
        return supervisorList;
    }

    public OnlineSupervisorVO getOnlineSupervisor(OnlineCounselorDTO onlineCounselorDTO) {
        PageHelper.startPage(onlineCounselorDTO.getPage(), onlineCounselorDTO.getPagesize());
        Page<OnlineSupervisor> page = userMapper.getOnlineSupervisor(onlineCounselorDTO);

        OnlineSupervisorVO onlineSupervisorVO = OnlineSupervisorVO.builder()
                .onlineSupervisors(page.getResult())
                .total(page.getTotal())
                .build();

        return onlineSupervisorVO;
    }

    public CounselorHistoryVO getHistory(CounselorHistoryDTO counselorHistoryDTO) {
        PageHelper.startPage(counselorHistoryDTO.getPage(), counselorHistoryDTO.getPagesize());
        Page<RecentSession> page = sessionsMapper.getHistory(counselorHistoryDTO, null);
        List<RecentSession> sessions = page.getResult();
        for (RecentSession recentSession : sessions) {
            Long seconds = Long.parseLong(recentSession.getDuration());
            Long hours = seconds / 3600;
            Long remainder = seconds % 3600;
            Long minutes = remainder / 60;
            seconds = remainder % 60;
            String duration = String.format("%02d:%02d:%02d", hours, minutes, seconds);
            recentSession.setDuration(duration);
        }
        return new CounselorHistoryVO(sessions, page.getTotal());
    }

    public TodaySessionVariationVO getTodaySessionVariation() {
        List<LocalDateTime> timeList = new ArrayList<>(25);
        LocalDateTime begin = LocalDateTime.now().with(LocalTime.MIN);
        for (int i = 0; i < 25; i++) {
            timeList.add(begin.plusHours(i));
        }
        List<Long> numList = new ArrayList<>(24);
        for(int i = 0; i < 24; i++) {
            numList.add(sessionsMapper.getTodaySessionVariation(timeList.get(i), timeList.get(i + 1)));
        }
        return new TodaySessionVariationVO(timeList, numList);
    }

    public WeekSessionVariationVO getWeekSessionVariation() {
        List<LocalDate> timeList = new ArrayList<>(7);
        LocalDate begin = LocalDate.now();
        begin = begin.minusDays(6);
        for (int i = 0; i < 7; i++) {
            timeList.add(begin.plusDays(i));
        }
        List<Long> numList = new ArrayList<>(7);
        for(int i = 0; i < 7; i++) {
            numList.add(sessionsMapper.getWeekSSessionVariation(timeList.get(i)));
        }
        return new WeekSessionVariationVO(timeList, numList);
    }

    public List<CounselorNumRankVO> getCounselorNumRank() {
        return sessionsMapper.getCounselorNumRank();
    }

    public List<CounselorRatingRankVO> getCounselorRatingRank() {
        return sessionsMapper.getCounselorRatingRank();
    }

    public void banUser(Long userId) {
        userMapper.banUser(userId);
    }

    public void unbanUser(Long userId) {
        userMapper.unbanUser(userId);
    }

}
