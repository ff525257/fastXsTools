package com.fmh.tools.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * 解析AndroidManifest.xml
 */
public class AndroidManifestAnalyze {

    public String appPackage;
    private List<String> permissions = new ArrayList();
    private List<String> activities = new ArrayList();

    public static final String FILENAME = "AndroidManifest.xml";

    public static final String PACKAGE = "package";
    public static final String CATEGORY = "category";
    public static final String ANDROIDNAME = "android:name";
    public static final String USESPERMISSION = "uses-permission";
    public static final String CATEGORYLAUNCHER = "android.intent.category.LAUNCHER";
    private static final String ACTIVITY = "activity";

    /**
     * 解析包名
     *
     * @param doc
     * @return
     */
    public String findPackage(Document doc) {
        Node node = doc.getFirstChild();
        NamedNodeMap attrs = node.getAttributes();
        for (int i = 0; i < attrs.getLength(); i++) {
            if (PACKAGE.equals(attrs.item(i).getNodeName())) {
                return attrs.item(i).getNodeValue();
            }
        }
        return null;
    }

    /**
     * 解析入口activity
     *
     * @param doc
     * @return
     */
    public String findLaucherActivity(Document doc) {
        Node activity = null;
        String sTem = "";
        NodeList categoryList = doc.getElementsByTagName(CATEGORY);
        for (int i = 0; i < categoryList.getLength(); i++) {
            Node category = categoryList.item(i);
            NamedNodeMap attrs = category.getAttributes();
            for (int j = 0; j < attrs.getLength(); j++) {
                if (ANDROIDNAME.equals(attrs.item(j).getNodeName())) {
                    if (attrs.item(j).getNodeValue().equals(CATEGORYLAUNCHER)) {
                        activity = category.getParentNode().getParentNode();
                        break;
                    }
                }
            }
        }
        if (activity != null) {
            NamedNodeMap attrs = activity.getAttributes();
            for (int j = 0; j < attrs.getLength(); j++) {
                if (ANDROIDNAME.equals(attrs.item(j).getNodeName())) {
                    sTem = attrs.item(j).getNodeValue();
                }
            }
        }
        return sTem;
    }

    /**
     * 解析入口
     *
     * @param filePath
     */
    public void xmlHandle(String filePath) {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            // 创建DocumentBuilder对象
            DocumentBuilder db = dbf.newDocumentBuilder();

            //加载xml文件
            Document document = db.parse(filePath);
            NodeList permissionList = document.getElementsByTagName(USESPERMISSION);
            NodeList activityAll = document.getElementsByTagName(ACTIVITY);

            //获取权限列表
            for (int i = 0; i < permissionList.getLength(); i++) {
                Node permission = permissionList.item(i);
                permissions.add((permission.getAttributes()).item(0).getNodeValue());
            }

            //获取activity列表
            appPackage = (findPackage(document));
            for (int i = 0; i < activityAll.getLength(); i++) {
                Node activity = activityAll.item(i);
                NamedNodeMap attrs = activity.getAttributes();
                for (int j = 0; j < attrs.getLength(); j++) {
                    if (ANDROIDNAME.equals(attrs.item(j).getNodeName())) {
                        String sTem = attrs.item(j).getNodeValue();
                        if (sTem.startsWith(".")) {
                            sTem = appPackage + sTem;
                        }
                        activities.add(sTem);
                    }
                }
            }
            String s = findLaucherActivity(document);
            if (s.startsWith(".")) {
                s = appPackage + s;
            }
            //移动入口类至首位
            activities.remove(s);
            activities.add(0, s);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
