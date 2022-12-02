package com.zybio.clouddesk.utils;

import cn.hutool.http.HttpException;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.zybio.clouddesk.enums.OptGroupEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ADManagerUtils {

    @Value("${ad-manager.host}")
    private String host;

    @Value("${ad-manager.login}")
    private String login;

    @Value("${ad-manager.addGroup}")
    private String addGroup = "/RestAPI/AddUsersToGroup";

    @Value("${ad-manager.loginName}")
    private String loginName;

    @Value("${ad-manager.password}")
    private String password;

    private final String moveGroup = "/RestAPI/RemoveUsersFromGroup";

    /**
     * 请求ADManager token
     *
     * @return token
     */
    public String getToken() {
        String url = host + login + "?loginName=" + loginName + "&password=" + password + "&&domainName=zy-ivd";
        try {
            String response = HttpUtil.post(url, "");
            JSONObject jb = JSONObject.parseObject(response);
            log.info("请求tokenUrl为：" + url);
            log.info("token返回数据体为：" + response);
            String token = "Success".equals(jb.getString("LoginStatusMessage")) ? jb.getString("AuthTicket") : "";
            if (token.isBlank()) {
                return "token请求失败:" + jb.getString("LoginStatusMessage");
            } else {
                return token;
            }
        } catch (HttpException e) {
            log.error("ADManager登录请求失败：" + e.getMessage());
            throw e;
        }
    }

    /**
     * 用户加入rdp组
     *
     * @param sAMAccountName 登录名
     * @param group          rdp组
     * @param optional       Enums 移除或加入
     * @return success
     */
    public String addGroup(String sAMAccountName, String group, OptGroupEnum optional) {
        JSONArray userDetails = new JSONArray();
        JSONObject json = new JSONObject();
        json.put("sAMAccountName", sAMAccountName);
        String ou = "OU=RDP专用,OU=手动创建部门分组,DC=zy-ivd,DC=com";
        json.put("ouname", ou);
//        json.put("name",name);
        userDetails.add(json);
        String departments = optional.getValue() + "=[" + group + "]";
        String groupUrl = "";
        switch (optional) {
            case ADD:
                groupUrl = addGroup;
                break;
            case MOVE:
                groupUrl = moveGroup;
                break;
            default:
                break;
        }
        try {
            String token = this.getToken();
            String url = host + groupUrl + "?PRODUCT_NAME=RDP&AuthToken=" + token + "&domainName=zy-ivd&inputFormat=" + userDetails + "&" + departments;
            log.info("开始请求adManager,请求地址为：" + url);
            String response = HttpUtil.post(url, "");
            JSONArray res = JSONArray.parseArray(response);
            if (res.getJSONObject(0).getString("status").equals("1")) {
                return res.getJSONObject(0).getString("statusMessage");
            } else {
                throw new RuntimeException(res.getJSONObject(0).getString("statusMessage"));
            }
        } catch (HttpException e) {
            log.error("ADManager转换部门请求失败：" + e.getMessage());
            throw e;
        }
    }
}
