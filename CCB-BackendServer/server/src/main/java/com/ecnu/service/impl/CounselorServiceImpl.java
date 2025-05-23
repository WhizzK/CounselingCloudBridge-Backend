package com.ecnu.service.impl;

import com.ecnu.constant.RequestStatusConstant;
import com.ecnu.constant.SessionStatusConstant;
import com.ecnu.context.BaseContext;
import com.ecnu.dto.CounselorHistoryDTO;
import com.ecnu.dto.CounselorTodaySessionDTO;
import com.ecnu.dto.SessionAddAdviceDTO;
import com.ecnu.entity.Counselor;
import com.ecnu.entity.Session;
import com.ecnu.entity.SupervisionRequest;
import com.ecnu.entity.User;
import com.ecnu.mapper.*;
import com.ecnu.service.CounselorService;
import com.ecnu.vo.*;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class CounselorServiceImpl implements CounselorService {

    @Autowired
    private CounselorMapper counselorMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private SessionsMapper sessionsMapper;

    @Autowired
    private ScheduleMapper scheduleMapper;

    @Autowired
    private RequestMapper requestMapper;

    @Autowired
    private RelationMapper relationMapper;



    /**
     * 获取咨询师信息
     * @return
     */
    public CounselorInfo getCounselorInfo() {
        User user = userMapper.getById(BaseContext.getCurrentId());
        Counselor counselor = counselorMapper.getById(BaseContext.getCurrentId());
        CounselorTodaySessionDTO counselorTodaySessionDTO = sessionsMapper.getCounselorTodaySession(BaseContext.getCurrentId(), SessionStatusConstant.CLOSED);
        long todaySessions = 0;
        String todayHours = "00:00:00";
        if(counselorTodaySessionDTO != null) {
            Long seconds = counselorTodaySessionDTO.getTodayHours();
            if (seconds != null) {
                Long hours = seconds / 3600;
                Long remainder = seconds % 3600;
                Long minutes = remainder / 60;
                seconds = remainder % 60;
                todayHours = String.format("%02d:%02d:%02d", hours, minutes, seconds);
            }
        }
        CounselorInfo counselorInfo = new CounselorInfo().builder()
                .realName(user.getRealName())
                .avatarUrl(user.getAvatarUrl())
                .totalSessions(sessionsMapper.getTotalSessions(BaseContext.getCurrentId(), SessionStatusConstant.CLOSED))
                .todaySessions(todaySessions)
                .todayHours(todayHours)
                .currentSessions(counselor.getCurrentSessions())
                .build();
        return counselorInfo;
    }

    /**
     * 获取咨询师的排班信息
     * @return
     */
    public List<String> getSchedule() {
        List<String> schedule = scheduleMapper.getSchedule(BaseContext.getCurrentId());
        return schedule;
    }

    /**
     * 获取和咨询师关联的督导ID
     * @return
     */
    @Override
    public List<Long> getSupervisorIds(Long counselorId) {
        return relationMapper.getSupervisorIds(counselorId);
    }

    /**
     * 获取咨询师最近的会话
     * @return
     */
    public List<RecentSession> getRecentSessions() {
        List<RecentSession> recentSessions = sessionsMapper.getRecentSessions(BaseContext.getCurrentId(), SessionStatusConstant.CLOSED);
        for (RecentSession recentSession : recentSessions) {
            Long seconds = Long.parseLong(recentSession.getDuration());
            Long hours = seconds / 3600;
            Long remainder = seconds % 3600;
            Long minutes = remainder / 60;
            seconds = remainder % 60;
            String duration = String.format("%02d:%02d:%02d", hours, minutes, seconds);
            recentSession.setDuration(duration);
        }
        return recentSessions;
    }

    /**
     * 获取咨询师的咨询列表
     *
     * @return
     */
    public List<com.ecnu.vo.Session> getSessionList() {
        List<Session> sessions = sessionsMapper.getSessionList(BaseContext.getCurrentId(), SessionStatusConstant.ACTIVE);
        List<com.ecnu.vo.Session> sessionList = new ArrayList<>();
        for (Session session : sessions) {
            com.ecnu.vo.Session sessionVO = new com.ecnu.vo.Session();
            BeanUtils.copyProperties(session, sessionVO);
            User user = userMapper.getById(session.getClientId());
            sessionVO.setRealName(user.getRealName());
            sessionList.add(sessionVO);
        }
        return sessionList;
    }

    @Override
    public void incrementCurrentSessions(Long counselorId) {
        counselorMapper.updateCurrentSessions(counselorId, 1);
    }

    @Override
    public void decrementCurrentSessions(Long counselorId) {
        counselorMapper.updateCurrentSessions(counselorId, -1);
    }

    @Override
    public Counselor getById(Long counselorId) {
        return counselorMapper.getById(counselorId);
    }

    /**
     * 获取咨询师的历史会话
     * @return
     */
    public CounselorHistoryVO getHistory(CounselorHistoryDTO counselorHistoryDTO) {
        PageHelper.startPage(counselorHistoryDTO.getPage(), counselorHistoryDTO.getPagesize());
        Page<RecentSession> page = sessionsMapper.getHistory(counselorHistoryDTO, BaseContext.getCurrentId());
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

    /**
     * 获取咨询师的咨询页面信息
     * @param clientid
     * @return
     */
    public CounselorSessionVO getSession(Long sessionid, Long clientid) {
        User user = userMapper.getById(clientid);
        Session session = sessionsMapper.getById(sessionid);
        CounselorSessionVO counselorSessionVO = CounselorSessionVO.builder()
                .realName(user.getRealName())
                .phoneNumber(user.getPhoneNumber())
                .avatarUrl(user.getAvatarUrl())
                .startTime(session.getStartTime())
                .endTime(session.getEndTime())
                .status(session.getStatus())
                .type(session.getType())
                .advice(session.getAdvice())
                .rating(session.getRating())
                .build();
        return counselorSessionVO;
    }

    /**
     * 咨询师添加咨询评价
     * @param sessionAddAdviceDTO
     */
    public void addSessionAdvice(SessionAddAdviceDTO sessionAddAdviceDTO) {
        sessionsMapper.addSessionAdvice(sessionAddAdviceDTO);
    }

    @Override
    public CounselorDetailVO getCounselorDetailById(Long counselorId) {
        Counselor counselor = counselorMapper.getById(counselorId);
        User user = userMapper.getById(counselorId);
        return CounselorDetailVO.builder()
                .name(user.getRealName())
                .expertise(counselor.getExpertise())
                .certification(counselor.getCertification())
                .rating(counselor.getRating())
                .totalSessions(counselor.getTotalSessions())
                .yearsExperience(counselor.getYearsExperience())
                .bio(counselor.getBio())
                .build();
    }


    public String getBio() {
        return counselorMapper.getBio(BaseContext.getCurrentId());
    }

    public void updateBio(String bio) {
        counselorMapper.updateBio(BaseContext.getCurrentId(), bio);
    }
    @Override
    public Long addRequest(Long supervisorId, Long sessionId) {

        Long relationId = relationMapper.getByParticipationId(supervisorId, BaseContext.getCurrentId()).getRelationId();

        SupervisionRequest request = SupervisionRequest.builder()
                .relationId(relationId)
                .counselorId(BaseContext.getCurrentId())
                .supervisorId(supervisorId)
                .sessionId(sessionId)
                .startTime(LocalDateTime.now())
                .status("pending")
                .build();

        requestMapper.insert(request);
        return request.getRequestId();
    }

    @Override
    public LocalDateTime endRequest(Long requestId) {

        SupervisionRequest request = requestMapper.getById(requestId);

        request.setStatus(RequestStatusConstant.COMPLETED);

        request.setEndTime(LocalDateTime.now());

        requestMapper.update(request);

        return request.getEndTime();
    }
}
