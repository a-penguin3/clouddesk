package com.zybio.clouddesk.utils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;
import javax.xml.namespace.QName;
import javax.xml.rpc.*;
import javax.xml.xpath.*;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.encoding.*;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

public class TestWeb {
    //解析xml数据
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

    //base64加密
    private String base64Encoder(String filepath) {
        try {
            File file = new File(filepath);
            FileInputStream inputfile = new FileInputStream(file);
            byte[] filebuff = new byte[(int) file.length()];
            inputfile.read(filebuff);
            inputfile.close();
            return Base64.encode(filebuff);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    //base64解密
    private boolean base64Decoder(String base64code, String targetfile) {
        try {
            byte[] filebuff = Base64.decode(base64code);
            FileOutputStream outfile = new FileOutputStream(targetfile);
            outfile.write(filebuff);
            outfile.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    //登录IP-guard，返回loginid
    public String login(String endpoint, String loginUser, String passwd) {
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

            String userdata = (String) call.invoke(new Object[]{loginUser, passwd});//远程调用
            //System.out.println(userdata);
            loginid = parseXML(userdata, "//loginid");
            if (loginid.length() == 0) {
                System.out.println("loginid:" + loginid + "\nLogin failed.\n" + userdata);
            }
        } catch (ServiceException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return loginid;
    }

    //登出，返回登录结果
    //0表示登录成功
    public String logout(String endpoint, String loginID) {
        String logoutResult = "";
        Service service = new Service();
        Call call;
        try {
            call = (Call) service.createCall();
            call.setTargetEndpointAddress(endpoint);//远程调用路径
            call.setOperationName(new QName("urn:octrans", "logout")); //调用方法

            //设置参数
            call.addParameter("szLoginID", XMLType.XSD_STRING, ParameterMode.IN);

            //设置返回类型
            call.setReturnType(XMLType.XSD_STRING);//返回值：String
            logoutResult = (String) call.invoke(new Object[]{loginID});
        } catch (ServiceException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return logoutResult;
    }

    //是否存在加密头
    //先判断文件是否有加密头
    //用于避免远程调用的时候，无论是否加密文件都需要调用webservice接口判断
    //使用本地路径即可
    public boolean hasSdHead(String filepath) {
        File file = new File(filepath);
        try {
            Reader readfile = new InputStreamReader(new FileInputStream(file));
            String sdhead = "%TSD-Header-###%";
            char[] filehead = new char[16];
            int charread = readfile.read(filehead); //返回读取到的字节数
            readfile.close();
            if (!sdhead.equals(String.valueOf(filehead)) || charread == 0) {
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    //判断是否为加密文件
    public boolean isSdFile(String endpoint, String filepath) {
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
        } catch (ServiceException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        // System.out.println(result);
        return result;
    }

    //解密文件
    public long decryptSdFile(String endpoint, String loginid, String filepath) {
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
        } catch (ServiceException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return result;
    }

    //加密文件
    public String encryptFile(String endpoint, String loginid, String encryptParam) {
        String result = "";
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
        } catch (ServiceException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return result;
    }

    //获取安全区域ID
    public String getsaferegion(String endpoint, String loginid, String region) {
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
        } catch (ServiceException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return "";
    }

    //上传文件
    //返回：上传的文件路径（用于下载）
    public String uploadFile(String endpoint, String loginid, String filepath) {
        try {
            Service service = new Service();
            Call call = (Call) service.createCall();
            call.setTargetEndpointAddress(endpoint);
            call.setOperationName(new QName("urn:octrans", "callfunction"));
            call.addParameter("szLoginID", XMLType.XSD_STRING, ParameterMode.IN);
            call.addParameter("szFunc", XMLType.XSD_STRING, ParameterMode.IN);
            call.addParameter("szParam", XMLType.XSD_STRING, ParameterMode.IN);
            call.setReturnType(XMLType.XSD_STRING);
            Object[] uploadobj = new Object[]{loginid, "uploadfile", base64Encoder(filepath)};
            String uploadResponse = (String) call.invoke(uploadobj);

            if (parseXML(uploadResponse, "//error").equals("0")) {
                return parseXML(uploadResponse, "//file");
            } else {
                System.out.println(uploadResponse);
            }
        } catch (ServiceException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return "";
    }

    //下载文件
    public boolean downloadFile(String endpoint, String loginid, String uploadpath, String targetfile) {
        try {
            Service service = new Service();
            Call call = (Call) service.createCall();
            call.setTargetEndpointAddress(endpoint);
            call.setOperationName(new QName("urn:octrans", "callfunction"));
            call.addParameter("szLoginID", XMLType.XSD_STRING, ParameterMode.IN);
            call.addParameter("szFunc", XMLType.XSD_STRING, ParameterMode.IN);
            call.addParameter("szParam", XMLType.XSD_STRING, ParameterMode.IN);
            call.setReturnType(XMLType.XSD_STRING);
            Object[] downloadobj = new Object[]{loginid, "downloadfile", uploadpath};
            String downloadResponse = (String) call.invoke(downloadobj);

            if (parseXML(downloadResponse, "//error").equals("0")) {
                String fileBase64Code = parseXML(downloadResponse, "//data");
                return base64Decoder(fileBase64Code, targetfile);
            } else {
                System.out.println("Download failed.");
            }
        } catch (ServiceException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return false;
    }

    //测试：
    public static void main(String[] args) {
        TestWeb test = new TestWeb();

        String endpoint = "http://localhost:8280/?wsdl";

        //login
//		String loginuser = "test";
//		String passwd = "";
        String loginuser = "vdi-api";
        String passwd = "123321@#AWzy";
        String loginid = test.login(endpoint, loginuser, passwd);

        //login failed,return
        if (loginid.length() == 0) {
            return;
        }

        //文件信息
        String fileName = "付款单.docx";
        String localPath = "D:\\test\\zhangzihao\\新建文件夹\\";
//		String netPath = "\\\\192.168.1.1\\temp\\duanqiang\\";
//		String filePath = netPath + fileName;
        String localFile = localPath + fileName;
        String safeRegionName = "普通";
        String safeRegionID = test.getsaferegion(endpoint, loginid, safeRegionName);
        //未获取到安全区域，设置为默认安全区域
        if (safeRegionID.length() == 0) {
            safeRegionID = "00000000-0000-0000-0000-000000000000";
        }
        //System.out.println("get saferegion:" + safeRegionID);

        if (!test.hasSdHead(localFile)) {
            System.out.println(localFile + " is not encrypted file��");
            //return;
            String uploadpath = test.uploadFile(endpoint, loginid, localFile);
            if (uploadpath.length() == 0) {
                System.out.println("upload failed.");
                return;
            }
            //加密属性：公共安全区域+普通
            String encryptParam =
                    "<data>"
                            + "<files>"
                            + "<item>" + uploadpath + "</item>"
                            + "</files>"
                            + "<setting>"
                            + "<item>"
                            + "<guid>" + safeRegionID + "</guid>"
                            + "<level>0</level>"
                            + "</item>"
                            + "</setting>"
                            + "<access>"
                            + "<item>"
                            + "<guid>" + safeRegionID + "</guid>"
                            + "<level>0</level>"
                            + "</item>"
                            + "</access>"
                            + "</data>";
            String encryptResult = test.encryptFile(endpoint, loginid, encryptParam);
            System.out.println("Encrypt Result:\n" + encryptResult);
            if (test.parseXML(encryptResult, "//error").equals("0")) {
                System.out.println("Encrypt successfully!\nDownload...");
                test.downloadFile(endpoint, loginid, uploadpath, localFile);
            }
        }
		/*
		boolean isSdF = test.isSdFile(endpoint, filePath);
		if (isSdF) {
			long decryptResult = test.decryptSdFile(endpoint, loginid, filePath);
			if(decryptResult == 0) {
				System.out.println("Decrypt file successfully.");
			}
			else {
				System.out.println("Decrypt failed. Errorcode:" + decryptResult);
			}
		}
		else {
			System.out.println("The file is not encrypted.");
		}
		
		//加密文件
		if(!isSdF) {
			String encryptResult = test.encryptFile(endpoint, loginid, encryptParam);
			System.out.println("Encrypt Result:\n"+encryptResult);
		}
		*/
        //logout
        String logoutresult = test.logout(endpoint, loginid);
        System.out.println(logoutresult);
    }

}
