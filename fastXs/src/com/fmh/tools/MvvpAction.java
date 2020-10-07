package com.fmh.tools;

import com.fmh.tools.config.KeyConfig;
import com.fmh.tools.utils.ClassUtils;
import com.fmh.tools.utils.FileUtils;
import com.intellij.codeInsight.CodeInsightActionHandler;
import com.intellij.codeInsight.generation.actions.BaseGenerateAction;
import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiUtilBase;
import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;

public class MvvpAction extends BaseGenerateAction {
    /**
     * 点击的文件
     */
    private PsiFile mOpenFile;
    /**
     * 当前打开class
     */
    private PsiClass mOpenClass;
    private PsiElementFactory mFactory;
    private PsiDirectory mMvvmDir;
    private String mOpenClassName;
    private String mViewName;
    private String mModelName;
    private String mModelViewName;

    public String mvvmdir = "com.fast.fastxs.mvvm";
    private String kongge = "   ";
    private String targetSuffix = JavaFileType.DOT_DEFAULT_EXTENSION;
    private String viewSuffix = "View";
    private String modelSuffix = "Model";
    private String modelViewSuffix = "ModelView";

    private String parentClassName;
    private String parentClassImport;

    public MvvpAction() {
        super(null);
    }

    public MvvpAction(CodeInsightActionHandler handler) {
        super(handler);
    }

    @Override
    public void actionPerformed(AnActionEvent event) {
        Project project = event.getData(PlatformDataKeys.PROJECT);
        Editor editor = event.getData(PlatformDataKeys.EDITOR);

        actionPerformedImpl(project, editor);
    }

    @Override
    public void actionPerformedImpl(@NotNull Project project, Editor editor) {

        mOpenFile = PsiUtilBase.getPsiFileInEditor(editor, project);
        mOpenClass = getTargetClass(editor, mOpenFile);
        if (mOpenClass.getName() == null) {
            return;
        }
        mFactory = JavaPsiFacade.getElementFactory(project);

        mOpenClassName = mOpenClass.getName();
        //前缀名称
        String prefixName;
        if (mOpenClassName.endsWith(KeyConfig.KEY_ACTIVITY)) {
            prefixName = mOpenClassName.substring(0, mOpenClassName.indexOf(KeyConfig.KEY_ACTIVITY));
            parentClassImport = "com.fast.fastxs.XsBaseActivity";
        } else if (mOpenClassName.endsWith(KeyConfig.KEY_FRAGMENT)) {
            prefixName = mOpenClassName.substring(0, mOpenClassName.indexOf(KeyConfig.KEY_FRAGMENT));
            parentClassImport = "com.fast.fastxs.XsBaseFragment";
        } else {
            ClassUtils.showErr("Only supports suffixes Acitivity and Fragment");
            return;
        }

        initName(prefixName);

        runWriteCommandAction(project);
    }

    /**
     * 初始化名称
     *
     * @param prefix
     */
    private void initName(String prefix) {
        mViewName = prefix + viewSuffix;
        mModelName = prefix + modelSuffix;
        mModelViewName = prefix + modelViewSuffix;

        parentClassName = parentClassImport.substring(parentClassImport.lastIndexOf(".") + 1, parentClassImport.length());
    }

    /**
     * 写文件操作,请在此方法
     *
     * @param project
     */
    private void runWriteCommandAction(@NotNull Project project) {

        WriteCommandAction.runWriteCommandAction(project, new Runnable() {
            @Override
            public void run() {

                mMvvmDir = createMvvpDir();
                creatMvvmFile();

                writeCurrentClass(project);

            }
        });
    }

    private void writeCurrentClass(@NotNull Project project) {
        //添加单个
        ClassUtils.addImport(project, mOpenClass, mFactory, parentClassImport, true);
        ClassUtils.addImport(project, mOpenClass, mFactory, FileUtils.getFilePackageName(mMvvmDir.getVirtualFile()), false);

        //继承
        PsiClass parentClass = mFactory.createClass(parentClassName);
        mOpenClass.getExtendsList().add(mFactory.createClassReferenceElement(parentClass));

        //添加泛型
        PsiClass view = mFactory.createClass(mModelViewName);
        mOpenClass.getExtendsList().getReferenceElements()[0].getParameterList().add(mFactory.createClassReferenceElement(view));

        //添加方法
        mOpenClass.add(mFactory.createMethodFromText("@Override  public " + mModelViewName + " initModelView() {return  new " + mModelViewName + "(new " + mModelName + "(),new " + mViewName + "(this))  ;}", mOpenClass));
    }

    /**
     * 生成对应的class文件
     */
    private void creatMvvmFile() {

        boolean hasView = false;
        boolean hasModel = false;
        boolean hasModelView = false;

        for (PsiFile f : mMvvmDir.getFiles()) {
            if (f.getName().equals(mViewName + targetSuffix)) {
                hasView = true;
            } else if (f.getName().equals(mModelName + targetSuffix)) {
                hasModel = true;
            } else if (f.getName().equals(mModelViewName + targetSuffix)) {
                hasModelView = true;
            }
        }

        if (!hasModelView) {
            createModelView(mModelViewName);
        }
        if (!hasView) {
            createView(mViewName);
        }
        if (!hasModel) {
            createModel(mModelName);
        }
    }

    private void createModel(String className) {
        PsiFile ModelFile = mMvvmDir.createFile(className + targetSuffix);

        StringBuffer sb = new StringBuffer();
        sb.append("package " + FileUtils.getFilePackageName(mMvvmDir.getVirtualFile()) + ";\n\n");

        sb.append("import " + mvvmdir + ".XsBaseModel;\n\n");

        sb.append(getHeaderAnnotation() + "\n");
        sb.append("public class " + className + " extends XsBaseModel {\n\n");
        sb.append(kongge + "public " + className + "(){\n");
        sb.append(kongge + "}\n\n");

        sb.append("}");

        FileUtils.string2Stream(sb.toString(), ModelFile.getVirtualFile().getPath());
    }

    private void createView(String className) {
        PsiFile viewIFile = mMvvmDir.createFile(className + targetSuffix);

        StringBuffer sb = new StringBuffer();
        sb.append("package " + FileUtils.getFilePackageName(mMvvmDir.getVirtualFile()) + ";\n\n");

        sb.append("import " + mvvmdir + ".XsBaseViewRender" + ";\n");
        sb.append("import android.content.Context;\n\n");

        sb.append(getHeaderAnnotation() + "\n");
        sb.append("public class " + className + " extends XsBaseViewRender {\n\n");


        sb.append(kongge + "public " + className + "(Context context) {\n");
        sb.append(kongge + kongge + "super(context);\n");
        sb.append(kongge + "}\n\n");

        sb.append(kongge + "public int getLayoutId(){\n");
        sb.append(kongge + kongge + "return android.R.layout.list_content;\n");
        sb.append(kongge + "}\n\n");

        sb.append("}");

        FileUtils.string2Stream(sb.toString(), viewIFile.getVirtualFile().getPath());
    }

    private void createModelView(String className) {
        PsiFile presenterFile = mMvvmDir.createFile(className + targetSuffix);

        StringBuffer sb = new StringBuffer();
        sb.append("package " + FileUtils.getFilePackageName(mMvvmDir.getVirtualFile()) + ";\n\n");
        sb.append("import " + mvvmdir + ".XsBaseModelView" + ";\n\n");

        sb.append(getHeaderAnnotation() + "\n");
        sb.append("public class " + className + " extends XsBaseModelView<" + mModelName + "," + mViewName + "> {\n\n");


        sb.append(kongge + "public " + className + "(" + mModelName + " baseModel" + "," + mViewName + " baseView" + ") {\n");
        sb.append(kongge + kongge + "super(baseModel, baseView);\n");
        sb.append(kongge + "}\n\n");

        sb.append(kongge + "@Override\n");
        sb.append(kongge + "public void init(){\n");
        sb.append("\n");
        sb.append(kongge + "}\n\n");

        sb.append("}");

        FileUtils.string2Stream(sb.toString(), presenterFile.getVirtualFile().getPath());
    }

    /**
     * 生成该代码生成的时间
     *
     * @return
     */
    private String getHeaderAnnotation() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = sdf.format(System.currentTimeMillis());
        String annotation = "/**\n" +
                " * Created  on " + time + ".\n" +
                " */";
        return annotation;
    }

    /**
     * 创建mvvm文件夹
     *
     * @return
     */
    private PsiDirectory createMvvpDir() {
        PsiDirectory mvvmDir = mOpenFile.getParent().findSubdirectory(KeyConfig.KEY_MVVM);
        if (mvvmDir == null) {
            mvvmDir = mOpenFile.getParent().createSubdirectory(KeyConfig.KEY_MVVM);
        }
        return mvvmDir;
    }


}
