package com.fmh.tools;

import com.fmh.tools.config.KeyConfig;
import com.fmh.tools.utils.ClassUtils;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.editor.actionSystem.EditorAction;
import com.intellij.openapi.editor.actionSystem.EditorActionHandler;
import com.intellij.openapi.editor.actionSystem.EditorWriteActionHandler;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.ui.MessageType;

import javax.swing.*;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

/**
 * Created by Aleksandr on 24.09.2015.
 */
public class PasteAction extends EditorAction {


    public PasteAction(EditorActionHandler defaultHandler) {
        super(defaultHandler);
    }

    public PasteAction() {
        this(new StylePasteHandler());
    }

    private static class StylePasteHandler extends EditorWriteActionHandler {
        private StylePasteHandler() {
        }

        @Override
        public void executeWriteAction(Editor editor, DataContext dataContext) {
            Document document = editor.getDocument();

            if (editor == null || document == null || !document.isWritable()) {
                return;
            }

            // get text from clipboard
            String source = getCopiedText();
            if (source == null) {
                return;
            }


            //String styleName = getStyleName();
        }

       /* private static String getStyleName() {
            return (String) JOptionPane.showInputDialog(
                    new JFrame(), KeyConfig.DIALOG_NAME_CONTENT,
                    KeyConfig.DIALOG_NAME_TITLE,
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    null, "");
        }*/

        private String getCopiedText() {
            try {
                return (String) CopyPasteManager.getInstance().getContents().getTransferData(DataFlavor.stringFlavor);
            } catch (NullPointerException | IOException | UnsupportedFlavorException e) {
                e.printStackTrace();
            }
            return null;
        }

        private void deleteSelectedText(Editor editor, Document document) {
            SelectionModel selectionModel = editor.getSelectionModel();
            document.deleteString(selectionModel.getSelectionStart(), selectionModel.getSelectionEnd());
        }
    }
}
