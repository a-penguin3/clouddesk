package com.zybio.clouddesk.utils;

import cn.hutool.extra.ssh.JschUtil;
import cn.hutool.extra.ssh.Sftp;
import com.jcraft.jsch.SftpProgressMonitor;
import com.zybio.clouddesk.config.ProgressMonitor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Configuration
public class FtpUtils {

    //ftp服务器地址
    @Value("${ftp.server}")
    private String hostname;

    //ftp服务器端口
    @Value("${ftp.port}")
    private int port;

    //ftp登录账号
    @Value("${ftp.userName}")
    private String username;

    //ftp登录密码
    @Value("${ftp.password}")
    private String password;

    //ftp保存目录
    @Value("${ftp.basePath}")
    private String basePath;

    private final Sftp ftp;

    private final SftpProgressMonitor progressMonitor = new ProgressMonitor();

    /**
     * 初始化连接
     */
    public FtpUtils(
            @Value("${ftp.server}") String hostname,
            @Value("${ftp.port}") int port,
            @Value("${ftp.userName}") String username,
            @Value("${ftp.password}") String password
    ) {
        this.ftp = JschUtil.createSftp(hostname, port, username, password);
    }

    /**
     * 发送文件至nas服务
     * @param filePath 文件路径
     * @param userName 用户名
     */
    public void sftp(String filePath, String userName) {
        try {
            ftp.put(filePath, "/share/CACHEDEV2_DATA/homes/DOMAIN=ZY-IVD/" + userName, progressMonitor, Sftp.Mode.OVERWRITE);
//            ftp.put(filePath,"/share/CACHEDEV2_DATA/homes/DOMAIN=ZY-IVD/" + userName);
        }catch (Exception e){
            log.error("上传文件失败：" + e.getMessage());
            throw e;
        }
    }

    /**
     * 关闭连接
     */
    public void closeSFtp() {
        this.ftp.close();
    }
}

