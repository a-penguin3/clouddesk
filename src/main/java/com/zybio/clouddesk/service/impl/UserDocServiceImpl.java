package com.zybio.clouddesk.service.impl;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.purgeteam.cloud.dispose.starter.exception.category.BusinessException;
import com.zybio.clouddesk.config.EncryptConfig;
import com.zybio.clouddesk.enums.Regions;
import com.zybio.clouddesk.pojo.dto.RegionDTO;
import com.zybio.clouddesk.service.UserDocService;
import com.zybio.clouddesk.thread.FileDecodeThread;
import com.zybio.clouddesk.utils.FtpUtils;
import com.zybio.clouddesk.utils.WebServiceUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.xml.rpc.ServiceException;
import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class UserDocServiceImpl implements UserDocService {

    @Value("${file.path}")
    private String filePath;

    @Autowired
    private FtpUtils ftpUtils;

    @Autowired
    private WebServiceUtils webUtils;

    private final LoadingCache<String, String> loginCache = Caffeine.newBuilder()
            .expireAfterWrite(1800, TimeUnit.SECONDS)
            .build(loginId -> webUtils.login(loginId));

    private final ThreadPoolExecutor executor = new ThreadPoolExecutor(
            10,
            50,
            1L,
            TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(100),
            new ThreadPoolExecutor.AbortPolicy());

    /**
     * 解密文件上传
     *
     * @param files    文件集
     * @param userName 登录名
     * @return 执行结果
     */
    @Override
    public String decodeFiles(MultipartFile[] files, String userName) {
        String basePath = filePath + "/" + userName;
        File fileDir = new File(basePath);
        if (!fileDir.exists()) {
            boolean res = fileDir.mkdirs();
            if (res) {
                log.info("新建用户文件目录：" + basePath);
            }
        }
        //建立线程池开始执行解密
        //todo: 思考建立类全局线程池比较好还是方法内线程池比较好； 局部线程池，会有重复初始化以及销毁过程，而全局线程池如果把所有加密过程都交给它，高并发场景下可能会锁死响应

        try {
            CountDownLatch countDownLatch = new CountDownLatch(files.length);
            for (MultipartFile file : files) {
                String fileName = file.getOriginalFilename();
                String filePath = fileDir + "\\" + fileName;

                synchronized (this) {
                    File res = new File(filePath);
                    file.transferTo(res);
                }
                executor.execute(() -> {
                    log.info(Thread.currentThread().getName() + "--- 开始解密文件" + filePath + new Date() + " ---");
                    long res = 0;
                    try {
                        res = webUtils.decryptSdFile(loginCache.get("loginId"), filePath);
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
                    synchronized (this) {
                        ftpUtils.sftp(filePath, userName);
                    }
                    countDownLatch.countDown();
                });
            }
            countDownLatch.await();
            return "上传成功";
        } catch (IOException | InterruptedException e) {
            log.error("上传文件失败：" + e);
            throw new BusinessException("500", "上传文件失败:" + e);
        }
    }

    /**
     * 加密文件上传
     *
     * @param files         文件集
     * @param userName      登录名
     * @param region        安全区域
     * @param securityLevel 安全级别
     * @return 执行结果
     */
    @Override
    public String encodeFiles(MultipartFile[] files, String userName, Regions region, Integer securityLevel) {
        String basePath = filePath + "/" + userName;
        File fileDir = new File(basePath);
        if (!fileDir.exists()) {
            boolean res = fileDir.mkdirs();
            if (res) {
                log.info("新建用户文件目录：" + basePath);
            }
        }
        try {
            List<String> filePaths = new ArrayList<>();
            synchronized (this) {
                for (MultipartFile file : files) {
                    String fileName = file.getOriginalFilename();
                    String filePath = fileDir + "\\" + fileName;

                    log.info("存入文件地址为：" + filePath);
                    File res = new File(filePath);
                    file.transferTo(res);
//                    if (webUtils.isSdFile(filePath)) {
//                        log.info("该文件为加密文件");
//                    } else {
//                        log.info("该文件 not a 加密文件");
//                    }
                    filePaths.add(filePath);
                }
            }

            log.info("开始加密文件");
            synchronized (this) {
                String res = webUtils.encryptFile(loginCache.get("loginId"), filePaths, region.getValue(), securityLevel);
                if (!res.equals("0")) {
                    throw new BusinessException("501", "文件加密失败" + res);
                } else {
                    log.info("加密文件完成");
                }
            }
            log.info("开始上传文件");
            for (String filePath : filePaths) {
                if (webUtils.isSdFile(filePath)) {
                    log.info("该文件为加密文件");
                    ftpUtils.sftp(filePath, userName);
                } else {
                    log.info("该文件 not a 加密文件");
                }
            }
            return "上传成功";
        } catch (IOException e) {
            log.error("上传文件失败：" + e);
            throw new BusinessException("500", "上传文件失败:" + e);
        }
    }

    @Override
    public List<RegionDTO> getRegion() {
        Map<Regions, Integer> regions = EncryptConfig.REGION;
        List<RegionDTO> res = new ArrayList<>();
        regions.forEach((region, code) -> {
            RegionDTO dto = new RegionDTO();
            dto.setIssDefault(region == Regions.REGION1);
            dto.setRegionCode(region.name());
            dto.setRegionName(region.getValue());
            dto.setSecurityLevel(code);
            res.add(dto);
        });
        return res;
    }
}
