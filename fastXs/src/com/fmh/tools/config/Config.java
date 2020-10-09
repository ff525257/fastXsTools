package com.fmh.tools.config;

import com.intellij.ide.util.PropertiesComponent;

public class Config {
    public static final String INJECT_CLASS_PATH = "com.fast.fastxs.inject.ViewId";
    public static final String ADAPTER_CLASSNAME = "LayoutModelAdapter";
    public static final String BASEITEM = "BaseItem";
    public static final String GENERALLISTOBJ = "GeneralListObj";

    /**
     * 数据保存
     * @param key
     * @param value
     */
    public static void saveData(String key, String value) {
        PropertiesComponent.getInstance().setValue(key, value);
    }

    /**
     * 获取数据
     * @param key
     * @return
     */
    public static String getData(String key,String def) {
        return PropertiesComponent.getInstance().getValue(key,def);
    }
}
