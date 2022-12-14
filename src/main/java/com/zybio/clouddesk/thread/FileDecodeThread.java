package com.zybio.clouddesk.thread;

import com.zybio.clouddesk.utils.FtpUtils;
import com.zybio.clouddesk.utils.WebServiceUtils;
import lombok.extern.slf4j.Slf4j;

import javax.xml.rpc.ServiceException;
import java.rmi.RemoteException;
import java.util.Date;

@Slf4j
public class FileDecodeThread implements Runnable {

    private final WebServiceUtils webServiceUtils;

    private final String filePath;

    private final String loginId;

    private final FtpUtils ftpUtils;

    private final String username;

    public FileDecodeThread(WebServiceUtils webServiceUtils, String filePath, String loginId, FtpUtils ftpUtils,String username) {
        this.webServiceUtils = webServiceUtils;
        this.filePath = filePath;
        this.loginId = loginId;
        this.ftpUtils = ftpUtils;
        this.username = username;
    }

    @Override
    public void run() {
        log.info(Thread.currentThread().getName() + "--- 开始解密文件" + filePath + new Date() + " ---");
        long res = 0;
        try {
            res = webServiceUtils.decryptSdFile(loginId, filePath);
        } catch (ServiceException | RemoteException e) {
            log.error(Thread.currentThread().getName() + "--- 解密系统访问失败" + e + " ---");
            throw new RuntimeException(e);
        }
        if (res == 0) {
            log.info(Thread.currentThread().getName() + "--- 解密文件成功" + filePath + new Date() + " ---");
        } else {
            log.warn(Thread.currentThread().getName() + "--- 解密文件失败" + filePath + new Date() + " ---");
            throw new RuntimeException("解密文件失败");
        }
        ftpUtils.sftp(filePath, username);

    }
}
