package com.fmh.tools;

import com.fmh.tools.config.KeyConfig;
import com.fmh.tools.utils.ClassUtils;
import com.intellij.codeInsight.CodeInsightActionHandler;
import com.intellij.codeInsight.generation.actions.BaseGenerateAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiUtilBase;
import org.jetbrains.annotations.NotNull;

/**
 * Adapter自动适配器
 * 只支持LayoutModelAdapter
 * com.fast.fastxs
 */
public class AutoAdapterAction extends BaseGenerateAction {
    /**
     * 点击的文件
     */
    private PsiFile mOpenFile;
    /**
     * 当前class
     */
    private PsiClass mOpenClass;
    private PsiElementFactory mFactory;
    private String mOpenClassName;
    /**
     * 当前类名称,截取
     * 比如LoginActivity
     * prefixName = Login
     */
    private String prefixName;

    public AutoAdapterAction() {
        super(null);
    }

    public AutoAdapterAction(CodeInsightActionHandler handler) {
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

        mOpenFile = PsiUtilBase.getPsiFileInEditor(editor, project); //获取点击的文件
        mOpenClass = getTargetClass(editor, mOpenFile); //获取点击的类
        if (mOpenClass.getName() == null) {
            return;
        }
        mFactory = JavaPsiFacade.getElementFactory(project);
        mOpenClassName = mOpenClass.getName();
        if (mOpenClassName.contains(KeyConfig.KEY_ADAPTER)) {
            prefixName = mOpenClassName.substring(0, mOpenClassName.indexOf(KeyConfig.KEY_ADAPTER));
        } else {
            prefixName = mOpenClassName;
        }

        runWriteCommandAction(project);
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
                String itemName = prefixName + KeyConfig.KEY_ITEM;

                ClassUtils.addImport(project, mOpenClass, mFactory, "android.support.annotation.NonNull", true);

                ClassUtils.addImport(project, mOpenClass, mFactory, "com.fast.fastxs.adapter.BaseItem", true);
                ClassUtils.addImport(project, mOpenClass, mFactory, "com.fast.fastxs.adapter.LayoutModelAdapter", true);
                ClassUtils.addImport(project, mOpenClass, mFactory, "com.fast.fastxs.adapter.dataobj.GeneralListObj", true);
                ClassUtils.addImport(project, mOpenClass, mFactory, "java.util.ArrayList", true);

                //继承
                PsiClass layoutModelAdapterClass = mFactory.createClass(KeyConfig.KEY_LAYOUTMODELADAPTER);
                mOpenClass.getExtendsList().add(mFactory.createClassReferenceElement(layoutModelAdapterClass));


                mOpenClass.add(mFactory.createMethodFromText(buildApdaterConstructorMethodText(mOpenClassName, itemName), mOpenClass));
                mOpenClass.add(mFactory.createMethodFromText(buildApdaterSetMethodText(itemName), mOpenClass));

                //item start
                //自定义Item
                PsiClass itemClass = mFactory.createClass(itemName);
                //自定义Item的继承class
                PsiClass basePresenter = mFactory.createClass(KeyConfig.KEY_BASEITEM);
                itemClass.getExtendsList().add(mFactory.createClassReferenceElement(basePresenter));

                PsiClass GeneralListObjClass = mFactory.createClass(KeyConfig.KEY_GENERALLISTOBJ);
                //为itemClass添加泛型
                itemClass.getExtendsList().getReferenceElements()[0].getParameterList().add(mFactory.createClassReferenceElement(GeneralListObjClass));

                itemClass.add(mFactory.createMethodFromText(buildItemConstructorMethodText(itemName), itemClass));
                //item end

                //为layoutModelAdapterClass添加泛型
                mOpenClass.getExtendsList().getReferenceElements()[0].getParameterList().add(mFactory.createClassReferenceElement(itemClass));

                mOpenClass.add(itemClass);

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


}
