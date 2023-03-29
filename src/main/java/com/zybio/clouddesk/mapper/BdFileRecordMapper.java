package com.zybio.clouddesk.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zybio.clouddesk.pojo.domain.BdFileRecord;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface BdFileRecordMapper extends BaseMapper<BdFileRecord> {

    @Select("select * from bd_file_record where username = #{username} and status = #{status}")
    List<BdFileRecord> findFileByUsernameAndStatus(String username, Integer status);

}
