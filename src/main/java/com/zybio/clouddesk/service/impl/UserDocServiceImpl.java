package com.zybio.clouddesk.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.purgeteam.cloud.dispose.starter.exception.category.BusinessException;
import com.zybio.clouddesk.config.EncryptConfig;
import com.zybio.clouddesk.enums.FileOpsType;
import com.zybio.clouddesk.enums.Regions;
import com.zybio.clouddesk.kafka.producer.SendMessage;
import com.zybio.clouddesk.mapper.BdFileRecordMapper;
import com.zybio.clouddesk.pojo.domain.BdFileRecord;
import com.zybio.clouddesk.pojo.dto.FileRecordDTO;
import com.zybio.clouddesk.pojo.dto.PageDTO;
import com.zybio.clouddesk.pojo.dto.RegionDTO;
import com.zybio.clouddesk.pojo.form.FileRecordForm;
import com.zybio.clouddesk.pojo.form.UserDocForm;
import com.zybio.clouddesk.service.UserDocService;
import com.zybio.clouddesk.utils.WebServiceUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class UserDocServiceImpl extends ServiceImpl<BdFileRecordMapper, BdFileRecord> implements UserDocService {

    @Value("${file.path}")
    private String filePath;

    @Value("${file.tempPath}")
    private String tempFile;

    @Value("${spring.kafka.template.default-topic}")
    private String encodeTopic;

    @Value("${spring.kafka.template.decode-topic}")
    private String decodeTopic;

    @Autowired
    private WebServiceUtils webUtils;

    @Resource
    private BdFileRecordMapper bdFileRecordMapper;

    @Autowired
    private SendMessage send;

    private final LoadingCache<String, String> loginCache = Caffeine.newBuilder()
            .expireAfterWrite(1800, TimeUnit.SECONDS)
            .build(loginId -> webUtils.login(loginId));

    /**
     * 传输 文件
     * todo 更改文件落盘逻辑，改为生成缓存文件
     * @param files 文件流
     * @param userName 用户名
     * @param form 操作类型
     * @return 文件集合
     */
    @Override
    public List<BdFileRecord> sendFile(MultipartFile[] files, String userName, UserDocForm form) {
        String basePath = tempFile + "/" + userName;
        File fileDir = new File(basePath);
        if (!fileDir.exists()) {
            boolean res = fileDir.mkdirs();
            if (res) {
                log.info("新建用户文件目录：" + basePath);
            }
        }

        ArrayList<BdFileRecord> fileRecords = new ArrayList<>();

        try {
            for (MultipartFile file : files) {
                String fileName = file.getOriginalFilename();
                String filePath = fileDir + "\\" + fileName;

                File res1 = new File(filePath);
                file.transferTo(res1);
                BdFileRecord record = new BdFileRecord();
                record.setFile_name(fileName);
                record.setUsername(userName);
                record.setFile_path(filePath);
                record.setStatus(0);
                record.setCreated_at(ZonedDateTime.now());
                record.setUpdated_at(ZonedDateTime.now());
                record.setFile_size(file.getSize());
                record.setFile_type(form.getType() == FileOpsType.DECODE_FILE ? 1 : 0);

                if (form.getType() == FileOpsType.ENCODE_FILE){
                    if (form.getRegion() == null){
                        throw new BusinessException("501","安全区域未填写");
                    }
                    record.setRegion(form.getRegion());
                    if (form.getSecurityLevel() == null){
                        throw new BusinessException("501","安全级别未填写");
                    }
                    record.setSecurity_level(form.getSecurityLevel());
                }

                bdFileRecordMapper.insert(record);
                if (record.getId() == null || record.getId().isBlank()) {
                    throw new Exception("保存操作队列出错");
                }
                fileRecords.add(record);
                if (form.getType() == FileOpsType.DECODE_FILE) {
                    send.sendMessage(decodeTopic, record);
                } else {
                    send.sendMessage(encodeTopic, record);
                }
            }
        } catch (Exception e) {
            log.error("发送消息队列出错：" + e.getMessage());
            throw new BusinessException("500", "发送消息队列出错：" + e.getMessage());
        }
        return fileRecords;
    }

    /**
     * 加密监听
     *
     * @param dto 加密消息队列
     */
    @Override
    public void encodeFiles(FileRecordDTO dto) {

        String filePath = dto.getFile_path();
        ArrayList<String> filePaths = new ArrayList<>();
        dto.setUpdated_at(ZonedDateTime.now());

        File file = new File(filePath);
        if (file.isFile()) {
            filePaths.add(filePath);
        } else {
            log.error("读取不到文件，请检查文件路径");
            dto.setError_message("读取不到文件，请检查文件路径");
            dto.setStatus(-1);
            updateStatus(dto);
            return;
        }

        try {
            String res = webUtils.encryptFile(loginCache.get("loginId"), filePaths, dto.getRegion().getValue(), dto.getSecurity_level());
            if (!res.equals("0")) {
                log.error("文件加密失败" + res);
                dto.setError_message("文件加密失败" + res);
                dto.setStatus(-1);
                updateStatus(dto);
                return;
            } else {
                log.info("加密文件完成");
            }
        } catch (Exception e) {
            log.error("加密调用发生异常：" + e.getMessage());
            dto.setError_message("加密调用发生异常：" + e.getMessage());
            dto.setStatus(-1);
            updateStatus(dto);
            return;
        }
        dto.setStatus(1);
        dto.setFile_size(file.length());
        updateStatus(dto);
    }

    /**
     * 解密文件
     *
     * @param dto 记录dto
     */
    @Override
    public void decodeFiles(FileRecordDTO dto) {
        String filePath = dto.getFile_path();
        File file = new File(filePath);
        dto.setUpdated_at(ZonedDateTime.now());
        if (!file.isFile()) {
            log.error("读取不到文件，请检查文件路径");
            dto.setError_message("读取不到文件，请检查文件路径");
            dto.setStatus(-1);
            updateStatus(dto);
            return;
        }
        try {
            long res = webUtils.decryptSdFile(loginCache.get("loginId"), filePath);
            if (res == 0) {
                dto.setStatus(0);
            } else {
                dto.setStatus(-1);
                log.error("文件加密失败" + res);
                dto.setError_message("文件加密失败" + res);
                updateStatus(dto);
                return;
            }
        } catch (Exception e) {
            log.error("解密文件出错：" + e.getMessage());
            dto.setError_message("解密文件出错：" + e.getMessage());
            dto.setStatus(-1);
            updateStatus(dto);
            return;
        }
        dto.setFile_size(file.length());
        dto.setStatus(1);
        updateStatus(dto);
    }

    private void updateStatus(FileRecordDTO dto) {
        BdFileRecord record = new BdFileRecord();
        BeanUtil.copyProperties(dto, record);
        this.updateById(record);
    }

    @Override
    public ResponseEntity<org.springframework.core.io.Resource> downloadFile(FileRecordForm dto, String username){
        if (!dto.getUsername().equals(username)){
            throw new BusinessException("403","您没有权限下载该文件");
        }
        File file = new File(dto.getFile_path());
        try {
            FileSystemResource resource = new FileSystemResource(file);

            String contentType = null;

            // Fallback to the default content type if type could not be determined
            contentType = "application/octet-stream";

            //设置响应头
            HttpHeaders headers = new HttpHeaders();
            headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
            headers.add("Content-Disposition", String.format("attachment; filename=\"%s\"", file.getName()));
            headers.add("Pragma", "no-cache");
            headers.add("Expires", "0");
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .headers(headers)
                    .body(resource);
        }catch (Exception e){
            e.printStackTrace();
            throw new BusinessException("500",e.getMessage());
        }
    }

    @Override
    public PageDTO<BdFileRecord> getRecords(String username, int pages, int pageSize, Integer status, Integer fileType) {
        QueryWrapper<BdFileRecord> pageWrapper = new QueryWrapper<>();
        pageWrapper.eq("username",username);
        if (status != null){
            pageWrapper.eq("status",status);
        }
        if (fileType != null){
            pageWrapper.eq("file_type",fileType);
        }
        pageWrapper.orderByDesc("updated_at");
        IPage<BdFileRecord> page = new Page<>(pages,pageSize);
        IPage<BdFileRecord> res = bdFileRecordMapper.selectPage(page, pageWrapper);
        PageDTO<BdFileRecord> dto = new PageDTO<>(res.getRecords(),pageSize,pages,res.getTotal());
        return dto;
    }

    @Override
    public BdFileRecord getRecord(String id){
        if (id == null || id.isBlank()){
            throw new BusinessException("500","输入空id");
        }
        BdFileRecord data = bdFileRecordMapper.selectById(id);
        if (data == null){
            throw new BusinessException("500","输入错误的id");
        }
        return data;
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

    public void updateStatus(BdFileRecord form){
        bdFileRecordMapper.updateById(form);
    }
}
