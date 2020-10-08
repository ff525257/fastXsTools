package com.fmh.tools.utils;

import com.intellij.codeInsight.actions.ReformatCodeProcessor;
import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.psi.search.EverythingGlobalScope;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.ui.awt.RelativePoint;
import com.thoughtworks.xstream.core.util.Fields;
import org.jetbrains.annotations.NonNls;

public class ClassUtils {

    public static void showErr(String msg) {
        Messages.showMessageDialog(msg, "Warn", Messages.getInformationIcon());
    }

    public static void showBalloonPopup(Project project, String htmlText, MessageType messageType) {
        StatusBar statusBar = WindowManager.getInstance().getStatusBar(project);

        JBPopupFactory.getInstance()
                .createHtmlTextBalloonBuilder(htmlText, messageType, null)
                .setFadeoutTime(7500)
                .createBalloon()
                .show(RelativePoint.getCenterOf(statusBar.getComponent()), Balloon.Position.atRight);
    }

    public static String getSelectText(Editor editor) {
        final SelectionModel selectionModel = editor.getSelectionModel();

        final int start = selectionModel.getSelectionStart();
        final int end = selectionModel.getSelectionEnd();

        return editor.getDocument().getText().substring(start, end);
    }

    /**
     * 格式化类
     *
     * @param project
     * @param classZ
     */
    public static void formatClass(Project project, PsiClass classZ) {
        // reformat class
        JavaCodeStyleManager styleManager = JavaCodeStyleManager.getInstance(project);
        styleManager.shortenClassReferences(classZ);
        new ReformatCodeProcessor(project, classZ.getContainingFile(), null, false).runWithoutProgress();
    }

    /**
     * 根据名称获取文件
     *
     * @param project
     * @param fileName
     * @return
     */
    public static PsiFile[] findFilesByFileName(Project project, String fileName) {
        return FilenameIndex.getFilesByName(project, fileName, new EverythingGlobalScope(project));
    }

    /**
     * 添加导入类
     *
     * @param psiClass
     * @param elementFactory
     * @param fullyQualifiedName
     * @param single             true为单个  false为.*;
     */
    public static void addImport(Project project, PsiClass psiClass, PsiElementFactory elementFactory, String fullyQualifiedName, boolean single) {
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

    public static void addField(PsiClass classZ, PsiElementFactory elementFactory, String fieldText, String name) {
        PsiField[] fs = classZ.getFields();
        if (fs != null) {
            for (int i = 0; i < fs.length; i++) {
                if (fs[i].getName().equals(name)) {
                    return;
                }
            }
        }
        classZ.add(elementFactory.createFieldFromText(fieldText, classZ));
    }


    public static PsiImportStatement craterPsiImportStatement(String name, Project project) {
        PsiJavaFile aFile = createDummyJavaFile("import " + name + ";", project);
        PsiImportStatementBase statement = extractImport(aFile, false);
        PsiImportStatement var10000 = (PsiImportStatement) CodeStyleManager.getInstance(project).reformat(statement);
        return var10000;
    }

    public static PsiJavaFile createDummyJavaFile(@NonNls String text, Project project) {
        return (PsiJavaFile) PsiFileFactory.getInstance(project).createFileFromText("", JavaFileType.INSTANCE, text);
    }

    private static PsiImportStatementBase extractImport(PsiJavaFile aFile, boolean isStatic) {
        PsiImportList importList = aFile.getImportList();
        assert importList != null : aFile;
        PsiImportStatementBase[] statements = isStatic ? importList.getImportStaticStatements() : importList.getImportStatements();
        assert ((Object[]) statements).length == 1 : aFile.getText();
        return (PsiImportStatementBase) ((Object[]) statements)[0];
    }
}
