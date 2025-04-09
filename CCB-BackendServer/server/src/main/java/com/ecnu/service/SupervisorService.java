package com.ecnu.service;

import com.ecnu.dto.CounselorHistoryDTO;
import com.ecnu.dto.OnlineCounselorDTO;
import com.ecnu.vo.*;

import java.time.LocalDate;
import java.util.List;

public interface SupervisorService {
    
    SupervisorInfo getSupervisorInfo();

    List<String> getSchedule();

    List<RecentRequest> getRecentRequests();

    List<Request> getRequestList();

    List<OnlineCounselor> getOnlineCounselor(OnlineCounselorDTO onlineCounselorDTO);

    SupervisorHistoryVO getHistory(CounselorHistoryDTO counselorHistoryDTO);
}
