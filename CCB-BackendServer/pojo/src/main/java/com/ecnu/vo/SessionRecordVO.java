package com.ecnu.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionRecordVO {

    private String content;

    private Long senderId;

    private LocalDateTime time;
}
