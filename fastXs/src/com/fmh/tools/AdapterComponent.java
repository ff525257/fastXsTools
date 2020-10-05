package com.fmh.tools;

import com.intellij.openapi.components.BaseComponent;

public class AdapterComponent implements BaseComponent {
    public AdapterComponent() {
    }

    @Override
    public void initComponent() {
        // TODO: insert component initialization logic here
    }

    @Override
    public void disposeComponent() {
        // TODO: insert component disposal logic here
    }

    @Override
    public String getComponentName() {
        return "AdapterComponent";
    }

    public void helloWorld() {
        AdapterWindow swing_jTextField = new AdapterWindow();
    }
}
