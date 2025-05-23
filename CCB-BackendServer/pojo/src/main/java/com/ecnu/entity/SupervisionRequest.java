package com.ecnu.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/*
 * 督导请求表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupervisionRequest implements Serializable {

    //请求唯一标识
    private Long requestId;

    //咨询师用户ID
    private Long counselorId;

    //督导用户ID
    private Long supervisorId;

    //和督导的关系ID
    private Long relationId;

    //关联的会话ID
    private Long sessionId;

    //开始时间
    private LocalDateTime startTime;

    //结束时间
    private LocalDateTime endTime;

    //请求详情
    private String requestDetails;

    //请求状态：待处理、已接受、已拒绝、已完成
    private String status;

    //创建时间
    private LocalDateTime createdAt;

    //更新时间
    private LocalDateTime updatedAt;

}
