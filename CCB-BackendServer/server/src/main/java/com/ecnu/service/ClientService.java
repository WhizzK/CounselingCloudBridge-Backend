package com.ecnu.service;

import com.ecnu.dto.ClientCounselorDTO;
import com.ecnu.vo.ClientHomeVO;

import java.util.List;

public interface ClientService {
    /**
     * 获取首页信息
     * @return
     */
    List<ClientHomeVO> getHomeInfo();

    /**
     * 获取咨询师排班信息
     * @return
     */
    List<ClientHomeVO> getCounselorScheduled(ClientCounselorDTO clientCounselorDTO);
}
