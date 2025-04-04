package com.ecnu.controller;

import com.ecnu.dto.OnlineCounselorDTO;
import com.ecnu.result.Result;
import com.ecnu.service.SupervisorService;
import com.ecnu.vo.*;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.websocket.server.PathParam;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/supervisor")
public class SupervisorController {

    @Autowired
    private SupervisorService supervisorService;

    @GetMapping("/home")
    @ApiOperation(value = "督导首页")
    public Result<SupervisorHomeVO> getHomeInfo() {
        SupervisorInfo supervisorInfo = supervisorService.getSupervisorInfo();
        List<LocalDate> schedule = supervisorService.getSchedule();
        List<RecentRequest> recentRequests = supervisorService.getRecentRequests();
        List<Request> requestList = supervisorService.getRequestList();
        SupervisorHomeVO supervisorHomeVO = new SupervisorHomeVO().builder()
                .supervisorInfo(supervisorInfo)
                .schedule(schedule)
                .recentRequests(recentRequests)
                .requestList(requestList)
                .build();
        return Result.success(supervisorHomeVO);
    }

    @GetMapping("onlinecounselor")
    @ApiOperation(value = "在线咨询师列表")
    public Result<List<OnlineCounselor>> getOnlineCounselor(OnlineCounselorDTO onlineCounselorDTO) {
        List<OnlineCounselor> onlineCounselorList = supervisorService.getOnlineCounselor(onlineCounselorDTO);
        return Result.success(onlineCounselorList);
    }
}
