package com.zybio.clouddesk;

import com.zybio.clouddesk.utils.FtpUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class CloudDeskApplicationTests {

    @Autowired
    FtpUtils ftpUtils;

    @Test
    void contextLoads() {
        ftpUtils.sftp("D:\\test\\zhangzihao\\新建文件夹\\付款单.docx","zhangzihao");
        ftpUtils.closeSFtp();
    }

}
