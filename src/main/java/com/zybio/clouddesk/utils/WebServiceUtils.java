package com.zybio.clouddesk.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.encoding.XMLType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import javax.xml.namespace.QName;
import javax.xml.rpc.ParameterMode;
import javax.xml.rpc.ServiceException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;
import java.util.List;

@Component
@Slf4j
public class WebServiceUtils {

    @Value("${webService.endpoint}")
    private String endpoint;

    @Value("${webService.loginName}")
    private String loginName;

    @Value("${webService.password}")
    private String password;

    /**
     * 登录
     *
     * @param loginId 缓存名称
     * @return loginId
     */
    public String login(String loginId) {
        String loginid = "";
        Service service = new Service();
        try {
            Call call = (Call) service.createCall();
            call.setTargetEndpointAddress(endpoint);//远程调用路径
            call.setOperationName(new QName("urn:octrans", "login")); //调用方法

            //设置参数
            call.addParameter("szName",    //参数名
                    XMLType.XSD_STRING,    //参数类型:String
                    ParameterMode.IN);        //参数模式:'IN' or 'OUT'
            call.addParameter("szPassword",
                    XMLType.XSD_STRING,
                    ParameterMode.IN);

            //设置返回值类型
            call.setReturnType(XMLType.XSD_STRING);//返回值类型:String

            log.info("登录的用户名为:" + loginName + ",密码为:" + password + ",路由为:" + endpoint);

            String userdata = (String) call.invoke(new Object[]{loginName, password});//远程调用
            //log.info(userdata);
            loginid = parseXML(userdata, "//loginid");
            String error = parseXML(userdata, "//error");
            log.info("登录返回码为：" + error);
            if (loginid.length() == 0) {
                log.info("loginid:" + loginid + "\nLogin failed.\n" + userdata);
            }else{
                log.info("登录id为:"+loginid);
            }
        } catch (ServiceException | RemoteException e) {
            e.printStackTrace();
        }
        return loginid;
    }

    /**
     * 判断是否是加密文件
     *
     * @param filepath 文件路径
     * @return boolean 是否是加密文件
     */
    public boolean isSdFile(String filepath) {
        boolean result = false;
        Service service = new Service();
        try {
            Call call = (Call) service.createCall();
            call.setTargetEndpointAddress(endpoint);//远程调用路径
            call.setOperationName(new QName("urn:octrans", "isSdFile")); //调用方法

            //设置参数
            call.addParameter("szFile", XMLType.XSD_STRING, ParameterMode.IN);

            //设置返回值类型
            call.setReturnType(XMLType.XSD_BOOLEAN);
            result = (boolean) call.invoke(new Object[]{filepath});
        } catch (ServiceException | RemoteException e) {
            e.printStackTrace();
        }
        // log.info(result);
        return result;
    }

    /**
     * 获取安全区域（所有）
     * @param loginid 登录id
     * @param region 安全区域名称
     * @return 返回安全区域id
     */
    public String getsaferegion(String loginid, String region) {
        Service service = new Service();
        try {
            Call call = (Call) service.createCall();
            call.setTargetEndpointAddress(endpoint);
            call.setOperationName(new QName("urn:octrans", "getsdsaferegion"));
            call.addParameter("szLoginID", XMLType.XSD_STRING, ParameterMode.IN);
            call.setReturnType(XMLType.XSD_STRING);
            String serverSafeRegion = (String) call.invoke(new Object[]{loginid});

            //"//item[name='技术部']/id":表示查找name为‘技术部’的节点的id值
            //参考：https://my.oschina.net/cloudcoder/blog/223359
            String expr = "//item[name='" + region + "']/id";
            return parseXML(serverSafeRegion, expr);
        } catch (ServiceException | RemoteException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 解密文件
     *
     * @param loginid  登录id
     * @param filepath 文件地址
     * @return long 解密请求结果
     */
    public long decryptSdFile(String loginid, String filepath) throws ServiceException, RemoteException {
        long result = 1;
        Service service = new Service();
        Call call;
        try {
            call = (Call) service.createCall();
            call.setTargetEndpointAddress(endpoint);//远程调用路径
            call.setOperationName(new QName("urn:octrans", "decryptFile")); //调用方法

            //设置参数
            call.addParameter("szLoginID", XMLType.XSD_STRING, ParameterMode.IN);
            call.addParameter("szFile", XMLType.XSD_STRING, ParameterMode.IN);

            //设置返回值类型
            call.setReturnType(XMLType.XSD_LONG);//返回值类型：String
            result = (long) call.invoke(new Object[]{loginid, filepath});
        } catch (ServiceException | RemoteException e) {
            e.printStackTrace();
            throw e;
        }
        return result;
    }

    /**
     * 加密
     * @param loginid 登录id
     * @param files 文件路劲集合
     * @return 0加密成功，1加密失败
     */
    public String encryptFile(String loginid, List<String> files, String region, Integer securityLevel) {
        String result = "";
        StringBuilder sb = new StringBuilder();
        sb.append("<data>");
        sb.append("<files>");
        for (String file : files) {
            sb.append("<item>");
            sb.append(file);
            sb.append("</item>");
        }
        sb.append("</files>");
        String safeRegionID = this.getsaferegion(loginid,region);
        if (safeRegionID.length() == 0) {
            safeRegionID = "00000000-0000-0000-0000-000000000000";
        }
        sb.append(
                "<setting>" +
                        "<item>" +
                        "<guid>" + safeRegionID + "</guid>" +
                        "<level>" + securityLevel + "</level>" +
                        "</item>" +
                        "</setting>" +
                        "<access>" +
                        "<item>" +
                        "<guid>" + safeRegionID + "</guid>" +
                        "<level>" + securityLevel + "</level>" +
                        "</item>" +
                        "</access>" +
                        "</data>"
        );

        String encryptParam = sb.toString();
        Service service = new Service();
        Call call;
        try {
            call = (Call) service.createCall();
            call.setTargetEndpointAddress(endpoint);//远程调用路径
            call.setOperationName(new QName("urn:octrans", "encryptFile")); //调用方法

            //设置参数
            call.addParameter("szLoginID", XMLType.XSD_STRING, ParameterMode.IN);
            call.addParameter("szParam", XMLType.XSD_STRING, ParameterMode.IN);

            //设置返回值类型
            call.setReturnType(XMLType.XSD_STRING);//返回值类型：String
            result = (String) call.invoke(new Object[]{loginid, encryptParam});
        } catch (ServiceException | RemoteException e) {
            e.printStackTrace();
        }
        return parseXML(result,"//error");
    }
    private String parseXML(String xmldata, String expr) {
        try {
            InputStream instream = new ByteArrayInputStream(xmldata.getBytes(StandardCharsets.UTF_8));
            InputSource insource = new InputSource(instream);
            XPath xpath = XPathFactory.newInstance().newXPath();
            Node xnode = (Node) xpath.evaluate(expr, insource, XPathConstants.NODE);
            if (xnode != null) {
                return xnode.getTextContent();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
