package com.fmh.tools.callback;

import com.fmh.tools.utils.Element;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;

import java.util.ArrayList;

public interface ConfirmListener {

    public void onConfirm(Project project, Editor editor, ArrayList<Element> elements, String fieldNamePrefix, boolean createHolder, boolean splitOnclickMethods);
}
