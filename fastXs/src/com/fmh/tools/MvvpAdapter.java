package com.fmh.tools;

import com.intellij.codeInsight.CodeInsightActionHandler;
import com.intellij.codeInsight.generation.actions.BaseGenerateAction;
import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.util.PsiUtilBase;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;

public class MvvpAdapter extends BaseGenerateAction {
    /**
     * 点击的文件
     */
    private PsiFile mFile;
    /**
     * 当前class
     */
    private PsiClass mClass;
    private PsiElementFactory mFactory;
    private PsiDirectory mMvvmDir;
    private String viewName;
    private String viewIName;
    private String modelName;
    private String modelViewName;
    /**
     * 当前类名称,截取
     * 比如LoginActivity
     * name = Login
     */
    private String name;

    public String pkg = "com.fast.fastxs.mvvm";
    private String kongge = "   ";
    private String targetSuffix = ".java";
    private String viewSuffix = "View";
    private String modelSuffix = "Model";
    private String modelViewSuffix = "ModelView";

    public MvvpAdapter() {
        super(null);
    }

    public MvvpAdapter(CodeInsightActionHandler handler) {
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

        mFile = PsiUtilBase.getPsiFileInEditor(editor, project); //获取点击的文件
        mClass = getTargetClass(editor, mFile); //获取点击的类
        if (mClass.getName() == null) {
            return;
        }
        mFactory = JavaPsiFacade.getElementFactory(project);
        mMvvmDir = createMvvpDir(); //创建mvvm文件夹
        viewName = mClass.getName();
        if (viewName.contains("Activity")) {
            name = viewName.substring(0, viewName.indexOf("Activity"));
        } else if (viewName.contains("Fragment")) {
            name = viewName.substring(0, viewName.indexOf("Fragment"));
        } else {
            name = viewName;
        }

        creatMvvmFile();
        writeActivity(project);
    }

    /**
     * 修改activity
     *
     * @param project
     */
    private void writeActivity(@NotNull Project project) {

        WriteCommandAction.runWriteCommandAction(project, new Runnable() {
            @Override
            public void run() {
                //添加单个
                addImport(project, mClass, mFactory, "com.fast.fastxs.XsBaseActivity", true);
                addImport(project, mClass, mFactory, getFilePackageName(mMvvmDir.getVirtualFile()), false);

                //继承
                PsiClass basePresenterClass = mFactory.createClass("XsBaseActivity");
                mClass.getExtendsList().add(mFactory.createClassReferenceElement(basePresenterClass));

                //添加泛型
                PsiClass view = mFactory.createClass(modelViewName);
                mClass.getExtendsList().getReferenceElements()[0].getParameterList().add(mFactory.createClassReferenceElement(view));

                //添加方法
                mClass.add(mFactory.createMethodFromText("@Override  public " + modelViewName + " initModelView() {return  new " + modelViewName + "(new " + modelName + "(),new " + viewIName + "(this))  ;}", mClass));

            }
        });
    }

    /**
     * 添加导入类
     *
     * @param psiClass
     * @param elementFactory
     * @param fullyQualifiedName
     */
    private void addImport(Project project, PsiClass psiClass, PsiElementFactory elementFactory, String fullyQualifiedName, boolean single) {
        final PsiFile file = psiClass.getContainingFile();
        if (!(file instanceof PsiJavaFile)) {
            return;
        }
        final PsiJavaFile javaFile = (PsiJavaFile) file;

        final PsiImportList importList = javaFile.getImportList();
        if (importList == null) {
            return;
        }

        // Check if already imported
        for (PsiImportStatementBase is : importList.getAllImportStatements()) {
            String impQualifiedName = is.getImportReference().getQualifiedName();
            if (fullyQualifiedName.equals(impQualifiedName)) {
                return; // Already imported so nothing neede
            }

        }

        // Not imported yet so add it
        if (single) {
            importList.add(craterPsiImportStatement(fullyQualifiedName, project));
        } else {
            importList.add(elementFactory.createImportStatementOnDemand(fullyQualifiedName));
        }


    }


    public PsiImportStatement craterPsiImportStatement(String name, Project project) {
        PsiJavaFile aFile = this.createDummyJavaFile("import " + name + ";", project);
        PsiImportStatementBase statement = extractImport(aFile, false);
        PsiImportStatement var10000 = (PsiImportStatement) CodeStyleManager.getInstance(project).reformat(statement);
        return var10000;
    }

    public PsiJavaFile createDummyJavaFile(@NonNls String text, Project project) {
        return (PsiJavaFile) PsiFileFactory.getInstance(project).createFileFromText("", JavaFileType.INSTANCE, text);
    }

    private static PsiImportStatementBase extractImport(PsiJavaFile aFile, boolean isStatic) {
        PsiImportList importList = aFile.getImportList();
        assert importList != null : aFile;
        PsiImportStatementBase[] statements = isStatic ? importList.getImportStaticStatements() : importList.getImportStatements();
        assert ((Object[]) statements).length == 1 : aFile.getText();
        return (PsiImportStatementBase) ((Object[]) statements)[0];
    }


    private void creatMvvmFile() {
        viewIName = name + viewSuffix;
        modelName = name + modelSuffix;
        modelViewName = name + modelViewSuffix;

        boolean hasModel = false;
        boolean hasModelView = false;
        boolean hasView = false;

        //查找是否已经包含有mvvm文件，如果有的话，则不再创建
        for (PsiFile f : mMvvmDir.getFiles()) {
            if (f.getName().contains(modelSuffix)) {
                String realName = f.getName().split(modelSuffix)[0];
                if (mClass.getName().contains(realName)) {
                    hasModel = true;
                    modelName = f.getName().replace(targetSuffix, "");
                }
            }

            if (f.getName().contains(modelViewSuffix)) {
                String realName = f.getName().split(modelViewSuffix)[0];
                if (mClass.getName().contains(realName)) {
                    hasModelView = true;
                    modelViewName = f.getName().replace(targetSuffix, "");
                }
            }

            if (f.getName().contains(viewSuffix)) {
                String realName = f.getName().split(viewSuffix)[0];
                if (mClass.getName().contains(realName)) {
                    hasView = true;
                    viewIName = f.getName().replace(targetSuffix, "");
                }
            }
        }

        if (!hasModelView) {
            createModelView(modelViewName);
        }
        if (!hasView) {
            createView(viewIName);
        }
        if (!hasModel) {
            createModel(modelName);
        }
    }

    private void createModel(String className) {
        PsiFile ModelFile = mMvvmDir.createFile(className + targetSuffix);
        StringBuffer modelText = new StringBuffer();
        modelText.append("package " + getFilePackageName(mMvvmDir.getVirtualFile()) + ";\n\n");
        modelText.append("import " + pkg + ".XsBaseModel;\n\n");

        modelText.append(getHeaderAnnotation() + "\n");
        modelText.append("public class " + className + " extends XsBaseModel {\n\n");
        modelText.append(kongge + "public " + className + "(){\n");
        modelText.append(kongge + "}\n\n");
        modelText.append("}");
        FileUtils.string2Stream(modelText.toString(), ModelFile.getVirtualFile().getPath());
    }

    private void createView(String className) {
        PsiFile viewIFile = mMvvmDir.createFile(className + targetSuffix);
        StringBuffer modelText = new StringBuffer();
        modelText.append("package " + getFilePackageName(mMvvmDir.getVirtualFile()) + ";\n\n");
        modelText.append("import " + pkg + ".XsBaseViewRender" + ";\n");
        modelText.append("import android.content.Context;\n\n");

        modelText.append(getHeaderAnnotation() + "\n");
        modelText.append("public class " + className + " extends XsBaseViewRender {\n\n");


        modelText.append(kongge + "public " + className + "(Context context) {\n");
        modelText.append(kongge + kongge + "super(context);\n");
        modelText.append(kongge + "}\n");

        modelText.append(kongge + "public int getLayoutId(){\n");
        modelText.append(kongge + kongge + "return android.R.layout.list_content;\n");
        modelText.append(kongge + "}\n");

        modelText.append("}");
        FileUtils.string2Stream(modelText.toString(), viewIFile.getVirtualFile().getPath());
    }

    private void createModelView(String className) {
        //创建文件
        PsiFile presenterFile = mMvvmDir.createFile(className + targetSuffix);

        //生成要写入的字符串
        StringBuffer modelText = new StringBuffer();
        modelText.append("package " + getFilePackageName(mMvvmDir.getVirtualFile()) + ";\n\n");
        modelText.append("import " + pkg + ".XsBaseModelView" + ";\n\n");

        modelText.append(getHeaderAnnotation() + "\n");
        modelText.append("public class " + className + " extends XsBaseModelView<" + modelName + "," + viewIName + "> {\n\n");


        modelText.append(kongge + "public " + className + "(" + modelName + " baseModel" + "," + viewIName + " baseView" + ") {\n");
        modelText.append(kongge + kongge + "super(baseModel, baseView);\n");
        modelText.append(kongge + "}\n\n");

        modelText.append(kongge + "@Override\n");
        modelText.append(kongge + "public void init(){\n");
        modelText.append("\n");
        modelText.append(kongge + "}\n\n");


        modelText.append("}");

        //将字符串写入文件
        FileUtils.string2Stream(modelText.toString(), presenterFile.getVirtualFile().getPath());
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

    private PsiDirectory createMvvpDir() {
        PsiDirectory mvvmDir = mFile.getParent().findSubdirectory("mvvm");
        if (mvvmDir == null) {
            mvvmDir = mFile.getParent().createSubdirectory("mvvm");
        }
        return mvvmDir;
    }

    public static String getFilePackageName(VirtualFile dir) {
        if (!dir.isDirectory()) {
            dir = dir.getParent();
        }
        String path = dir.getPath().replace("/", ".");
        String preText = "src";
        if (path.contains("src.main.java")) {
            preText = "src.main.java";
        }

        int preIndex = path.indexOf(preText) + preText.length() + 1;
        path = path.substring(preIndex);
        return path;
    }

}
