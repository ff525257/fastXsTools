package com.fmh.tools.form;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InputTextView extends JPanel {

    protected JLabel mTitle;
    protected JTextField mEdittext;
    private int itemHeight = 26;
    protected Color mDefaultColor;
    protected Color mErrorColor = new Color(0xFFDB5860);

    private static final Pattern sValidityPattern = Pattern.compile("[a-zA-Z]+[0-9a-zA-Z_]*(\\.[a-zA-Z]+[0-9a-zA-Z_]*)*\\.[a-zA-Z]+[0-9a-zA-Z_]*(\\$[a-zA-Z]+[0-9a-zA-Z_]*)*", Pattern.CASE_INSENSITIVE);

    public InputTextView(String title, String def) {

        mTitle = new JLabel(title);
        if (def == null) {
            def = "";
        }
        mEdittext = new JTextField(def);
        mDefaultColor = mEdittext.getBackground();
        mEdittext.setPreferredSize(new Dimension(100, itemHeight));
        mEdittext.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                // empty
            }

            @Override
            public void focusLost(FocusEvent e) {
                syncAndCheck();
            }
        });

        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        setMaximumSize(new Dimension(Short.MAX_VALUE, 54));
        //添加空隙
        add(Box.createRigidArea(new Dimension(10, 0)));
        add(mTitle);
       /* add(Box.createRigidArea(new Dimension(10, 0)));
        add(mEvent);*/
        add(Box.createRigidArea(new Dimension(10, 0)));
        add(mEdittext);
        add(Box.createHorizontalGlue());

    }

    public boolean syncAndCheck() {
        boolean invalidity = checkValidity();
        if (invalidity) {
            mEdittext.setBackground(mDefaultColor);
        } else {
            mEdittext.setBackground(mErrorColor);
        }
        return invalidity;
    }

    public String getText() {
        return mEdittext.getText();
    }

    private boolean checkValidity() {
        Matcher matcher = sValidityPattern.matcher(mEdittext.getText());
        return matcher.find();
    }

}
