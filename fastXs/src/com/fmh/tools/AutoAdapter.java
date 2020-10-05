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

public class AutoAdapter extends BaseGenerateAction {
    /**
     * 点击的文件
     */
    private PsiFile mFile;
    /**
     * 当前class
     */
    private PsiClass mClass;
    private PsiElementFactory mFactory;
    private String className;
    /**
     * 当前类名称,截取
     * 比如LoginActivity
     * name = Login
     */
    private String name;

    public String pkg = "com.fastxs.simple";
    private String kongge = "   ";

    public AutoAdapter() {
        super(null);
    }

    public AutoAdapter(CodeInsightActionHandler handler) {
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
        className = mClass.getName();
        if (className.contains("Adapter")) {
            name = className.substring(0, className.indexOf("Adapter"));
        } else {
            name = className;
        }

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
                String itemName = name + "Item";
                addImport(project, mClass, mFactory, "android.support.annotation.NonNull", true);

                addImport(project, mClass, mFactory, "com.fast.fastxs.adapter.BaseItem", true);
                addImport(project, mClass, mFactory, "com.fast.fastxs.adapter.LayoutModelAdapter", true);
                addImport(project, mClass, mFactory, "com.fast.fastxs.adapter.dataobj.GeneralListObj", true);
                addImport(project, mClass, mFactory, "java.util.ArrayList", true);

                //继承
                PsiClass basePresenterClass = mFactory.createClass("LayoutModelAdapter");
                mClass.getExtendsList().add(mFactory.createClassReferenceElement(basePresenterClass));


                mClass.add(mFactory.createMethodFromText(buildApdaterConstructorMethodText(className, itemName), mClass));
                mClass.add(mFactory.createMethodFromText(buildApdaterSetMethodText(itemName), mClass));

               /* //添加泛型
                PsiClass view = mFactory.createClass("DialogItem");
                mClass.getExtendsList().getReferenceElements()[0].getParameterList().add(mFactory.createClassReferenceElement(view));*/


                PsiClass itemClass = mFactory.createClass(itemName);
                //继承
                PsiClass basePresenter = mFactory.createClass("BaseItem");
                itemClass.getExtendsList().add(mFactory.createClassReferenceElement(basePresenter));

                PsiClass GeneralListObjClass = mFactory.createClass("GeneralListObj");
                itemClass.getExtendsList().getReferenceElements()[0].getParameterList().add(mFactory.createClassReferenceElement(GeneralListObjClass));

                itemClass.add(mFactory.createMethodFromText(buildItemConstructorMethodText(itemName), itemClass));

                //添加泛型
                mClass.getExtendsList().getReferenceElements()[0].getParameterList().add(mFactory.createClassReferenceElement(itemClass));

                mClass.add(itemClass);

            }
        });
    }


    private String buildApdaterSetMethodText(String pramter) {
        return "@Override\n" +
                "public void bindViewHolder(XHolder holder, " + pramter + " baseBean, long position){\n" +
                " }";
    }


    private String buildItemConstructorMethodText(String prefix) {
        return
                "public " + prefix + "(GeneralListObj data){\n" +
                        "super(data);" +
                        "layoutId = android.R.layout.activity_list_item;" +
                        " }";
    }

    private String buildApdaterConstructorMethodText(String prefix, String pramter) {
        return
                "public " + prefix + "(@NonNull ArrayList<" + pramter + "> list){\n" +
                        "super(list);" +
                        " }";
    }

    /**
     * 添加导入类
     *
     * @param psiClass
     * @param elementFactory
     * @param fullyQualifiedName
     * @param single true为单个  false为.*;
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
