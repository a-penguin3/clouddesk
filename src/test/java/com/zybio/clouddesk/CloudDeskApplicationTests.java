package com.zybio.clouddesk;

import com.zybio.clouddesk.mapper.BdFileRecordMapper;
import com.zybio.clouddesk.pojo.domain.BdFileRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.List;

@SpringBootTest
class CloudDeskApplicationTests {
    @Resource
    private BdFileRecordMapper mapper;

    @Test
    void contextLoads() {
       List<BdFileRecord> data= mapper.findFileByUsernameAndStatus("zhangzihao",0);
       System.out.println(data.size());
       for (BdFileRecord it : data){
           System.out.println(it.getId());
       }
    }

}
