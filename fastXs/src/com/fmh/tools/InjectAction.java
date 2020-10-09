package com.fmh.tools;

import com.fmh.tools.config.Config;
import com.fmh.tools.config.KeyConfig;
import com.fmh.tools.form.EntryList;
import com.fmh.tools.iface.ICancelListener;
import com.fmh.tools.iface.IConfirmListener;
import com.fmh.tools.utils.*;
import com.intellij.codeInsight.CodeInsightActionHandler;
import com.intellij.codeInsight.generation.actions.BaseGenerateAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiUtilBase;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.ArrayList;

public class InjectAction extends BaseGenerateAction implements IConfirmListener, ICancelListener {

    protected JFrame mDialog;
    private String pkg;
    /**
     * 点击的文件
     */
    private PsiFile mOpenFile;
    /**
     * 当前class
     */
    private PsiClass mOpenClass;
    private PsiElementFactory mFactory;
    private String prefix = "";

    protected static final Logger log = Logger.getInstance(InjectAction.class);

    @SuppressWarnings("unused")
    public InjectAction() {
        super(null);
    }

    @SuppressWarnings("unused")
    public InjectAction(CodeInsightActionHandler handler) {
        super(handler);
    }


    @Override
    public void actionPerformed(AnActionEvent event) {
        Project project = event.getData(PlatformDataKeys.PROJECT);
        Editor editor = event.getData(PlatformDataKeys.EDITOR);
        actionPerformedImpl(project, editor);
    }

    @Override
    public void actionPerformedImpl(Project project, Editor editor) {
        mFactory = JavaPsiFacade.getElementFactory(project);

        mOpenFile = PsiUtilBase.getPsiFileInEditor(editor, project);
        mOpenClass = getTargetClass(editor, mOpenFile);

        String selectStr = ClassUtils.getSelectText(editor);

        PsiFile[] files = ClassUtils.findFilesByFileName(project, selectStr);

        if (files == null || files.length == 0) {
            files = ClassUtils.findFilesByFileName(project, selectStr + ".xml");
        }

        PsiFile[] androidManifests = ClassUtils.findFilesByFileName(project, AndroidManifestAnalyze.FILENAME);
        if (androidManifests != null && androidManifests.length > 0) {
            AndroidManifestAnalyze a = new AndroidManifestAnalyze();

            String realPath = null;
            for (int i = 0; i < androidManifests.length; i++) {
                if (androidManifests[i].getVirtualFile().getPath().contains("src/main")) {
                    realPath = androidManifests[i].getVirtualFile().getPath();
                    break;
                }
            }
            //to test
            if (realPath == null) {
                realPath = androidManifests[0].getVirtualFile().getPath();
            }
            a.xmlHandle(realPath);
            pkg = a.appPackage;
        }

        PsiFile layout = files[0];

        if (layout == null) {
            ClassUtils.showBalloonPopup(project, "No layout found", MessageType.ERROR);
            return; // no layout found
        }

        log.info("Layout file: " + layout.getVirtualFile());

        ArrayList<Element> elements = Utils.getIDsFromLayout(layout, prefix);
        if (!elements.isEmpty()) {
            showDialog(project, editor, elements);
        } else {
            ClassUtils.showBalloonPopup(project, "No IDs found in layout", MessageType.WARNING);
        }
    }


    public void onConfirm(Project project, Editor editor, ArrayList<Element> elements, String fieldNamePrefix,
                          boolean createHolder, boolean splitOnclickMethods) {
        PsiFile file = PsiUtilBase.getPsiFileInEditor(editor, project);
        if (file == null) {
            return;
        }
        closeDialog();

        if (Utils.getInjectCount(elements) > 0 || Utils.getClickCount(elements) > 0) { // generate injections
            //new InjectWriter(file, getTargetClass(editor, file), "Generate Injections", elements, layout.getName(), fieldNamePrefix, createHolder, splitOnclickMethods).execute();
            runWriteCommandAction(project, elements);
        } else { // just notify user about no element selected
            ClassUtils.showBalloonPopup(project, "No injection was selected", MessageType.WARNING);
        }

    }

    /**
     * 写文件操作,请在此方法
     *
     * @param project
     */
    private void runWriteCommandAction(@NotNull Project project, ArrayList<Element> elements) {

        WriteCommandAction.runWriteCommandAction(project, new Runnable() {

            @Override
            public void run() {
                ClassUtils.addImport(project, mOpenClass, mFactory, Config.getData(KeyConfig.KEY_INJECTCLASS, Config.INJECT_CLASS_PATH), true);

                if (pkg != null) {
                    ClassUtils.addImport(project, mOpenClass, mFactory, pkg + ".R", true);
                }

                generateFields(elements);
                loadimport(elements, project);
            }
        });
    }

    private void loadimport(ArrayList<Element> elements, Project project) {
        for (Element element : elements) {
            if (!element.used) {
                continue;
            }
            if (element.nameFull != null && element.nameFull.length() > 0) { // custom package+class
                ClassUtils.addImport(project, mOpenClass, mFactory, element.nameFull, true);
            } else if (Definitions.paths.containsKey(element.name)) { // listed class
                ClassUtils.addImport(project, mOpenClass, mFactory, element.name, true);
            } else { // android.widget
                ClassUtils.addImport(project, mOpenClass, mFactory, "android.widget." + element.name, true);
            }
        }
    }

    /**
     * Create fields for injections inside main class
     */
    protected void generateFields(ArrayList<Element> elements) {
        // add injections into main class
        for (Element element : elements) {
            if (!element.used) {
                continue;
            }

            StringBuilder injection = new StringBuilder();
            injection.append('@');
            injection.append("ViewId");
            injection.append('(');
            injection.append(element.getFullID());
            injection.append(") ");
            if (element.nameFull != null && element.nameFull.length() > 0) { // custom package+class
                injection.append(element.nameFull.substring(element.nameFull.lastIndexOf(".") + 1, element.nameFull.length()));
            } else if (Definitions.paths.containsKey(element.name)) { // listed class
                injection.append(Definitions.paths.get(element.name));
            } else { // android.widget
//                injection.append("android.widget.");
                injection.append(element.name);
            }
            injection.append(" ");
            injection.append(element.fieldName);
            injection.append(";");

            ClassUtils.addField(mOpenClass, mFactory, injection.toString(), element.fieldName);
        }
    }

    public void onCancel() {
        closeDialog();
    }

    protected void showDialog(Project project, Editor editor, ArrayList<Element> elements) {
        PsiFile file = PsiUtilBase.getPsiFileInEditor(editor, project);
        if (file == null) {
            return;
        }
        PsiClass clazz = getTargetClass(editor, file);
        if (clazz == null) {
            return;
        }

        // get already generated injections
        ArrayList<String> ids = new ArrayList<String>();
        PsiField[] fields = clazz.getAllFields();
        String[] annotations;
        String id;

        for (PsiField field : fields) {
            annotations = field.getFirstChild().getText().split(" ");
            for (String annotation : annotations) {
                id = Utils.getInjectionID(annotation.trim());
                if (!Utils.isEmptyString(id)) {
                    ids.add(id);
                }
            }
        }

        EntryList panel = new EntryList(project, editor, elements, ids, false, this, this);

        mDialog = new JFrame();
        mDialog.setTitle("Inject");
        mDialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        mDialog.getRootPane().setDefaultButton(panel.getConfirmButton());
        mDialog.getContentPane().add(panel);
        mDialog.pack();
        mDialog.setLocationRelativeTo(null);
        mDialog.setVisible(true);
    }

    protected void closeDialog() {
        if (mDialog == null) {
            return;
        }

        mDialog.setVisible(false);
        mDialog.dispose();
    }
}
