package com.zybio.clouddesk.config;

import com.jcraft.jsch.SftpProgressMonitor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ProgressMonitor implements SftpProgressMonitor {

    private long count = 0; //当前接收的总字节数
    private long max = 0; //最终文件大小
    private long percent = -1; //进度

    private String src;


    /**
     * 当文件开始传输时，调用init方法
     */
    @Override
    public void init(int op, String src, String dest, long max) {
        if (op==SftpProgressMonitor.PUT) {
            log.info("上传文件: " +  src + " 开始.");
        }else {
            log.info("Download file begin.");
        }
        this.max = max;
        this.count = 0;
        this.percent = -1;
        this.src = src;
    }


    /**
     * 当每次传输了一个数据块后，调用count方法，count方法的参数为这一次传输的数据块大小
     */
    @Override
    public boolean count(long count) {
        this.count += count;
        if (percent >= this.count * 100 / max) {
            return true;
        }
        percent = this.count * 100 / max;
        log.info("Completed " + this.count + "(" + percent
                + "%) out of " + max + ".");
        return true;
    }

    /**
     * 当传输结束时，调用end方法
     */
    @Override
    public void end() {
        log.info("上传文件： " + src + " 完成.");
    }
}
