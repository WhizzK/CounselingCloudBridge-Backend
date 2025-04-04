package com.ecnu.mapper;

import com.ecnu.entity.Counselor;
import com.ecnu.vo.ClientHomeVO;
import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface CounselorMapper {

    /**
     * 根据id获取咨询师信息
     * @param currentId
     * @return
     */
    @Select("select * from counselors where counselor_id = #{currentId}")
    Counselor getById(Long currentId);


    /**
     * 给id对应的咨询师的当前会话数加delta
     * @return
     */
    @Update("UPDATE counselor SET current_sessions = current_sessions + #{delta} WHERE counselor_id = #{counselorId}")
    int updateCurrentSessions(Long counselorId, int delta);

    /**
     * 获取评分最高的两个咨询师
     * @return
     */
    @Select("SELECT * FROM counselors ORDER BY rating DESC LIMIT 2")
    List<Counselor> getTop();

    /**
     * 获取咨询师排班信息
     * @return
     */
    Page<ClientHomeVO> getCounselorScheduled();
}
