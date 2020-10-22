package com.fmh.tools.weight;

import com.fmh.tools.callback.OnCheckBoxStateChangedListener;
import com.fmh.tools.utils.Element;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;

public class InjectView extends JPanel {

    protected InjectDialog mParent;
    protected Element mElement;
    protected ArrayList<String> mGeneratedIDs;
    protected OnCheckBoxStateChangedListener mListener;
    // ui
    protected JCheckBox mCheck;
    protected JLabel mType;
    protected JLabel mID;
    //    protected JCheckBox mEvent;
    protected JTextField mName;
    protected Color mNameDefaultColor;
    protected Color mNameErrorColor = new Color(0xFFDB5860);
    private int itemHeight = 26;

    public JCheckBox getCheck() {
        return mCheck;
    }

    public void setListener(final OnCheckBoxStateChangedListener onStateChangedListener) {
        this.mListener = onStateChangedListener;
    }

    public InjectView(InjectDialog parent, Element element, ArrayList<String> ids) {
        mElement = element;
        mParent = parent;
        mGeneratedIDs = ids;

        mCheck = new JCheckBox();
        mCheck.setPreferredSize(new Dimension(40, itemHeight));
        if (!mGeneratedIDs.contains(element.getFullID())) {
            mCheck.setSelected(mElement.used);
        } else {
            mCheck.setSelected(false);
        }
        mCheck.addChangeListener(new CheckListener());

      /*  mEvent = new JCheckBox();
        mEvent.setPreferredSize(new Dimension(100, 26));*/

        mType = new JLabel(mElement.name);
        mType.setPreferredSize(new Dimension(100, itemHeight));

        mID = new JLabel(mElement.id);
        mID.setPreferredSize(new Dimension(100, itemHeight));

        mName = new JTextField(mElement.fieldName, 10);
        mNameDefaultColor = mName.getBackground();
        mName.setPreferredSize(new Dimension(100, itemHeight));
        mName.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                // empty
            }

            @Override
            public void focusLost(FocusEvent e) {
                syncElement();
            }
        });

        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
        setMaximumSize(new Dimension(Short.MAX_VALUE, 54));
        //添加空隙
        add(Box.createRigidArea(new Dimension(10, 0)));
        add(mCheck);
        add(Box.createRigidArea(new Dimension(10, 0)));
        add(mType);
        add(Box.createRigidArea(new Dimension(10, 0)));
        add(mID);
       /* add(Box.createRigidArea(new Dimension(10, 0)));
        add(mEvent);*/
        add(Box.createRigidArea(new Dimension(10, 0)));
        add(mName);
        add(Box.createHorizontalGlue());

        checkState();
    }

    public Element syncElement() {
        mElement.used = mCheck.isSelected();
        //mElement.isClick = mEvent.isSelected();
        mElement.fieldName = mName.getText();

        if (mElement.checkValidity()) {
            mName.setBackground(mNameDefaultColor);
        } else {
            mName.setBackground(mNameErrorColor);
        }

        return mElement;
    }

    private void checkState() {
        if (mCheck.isSelected()) {
            mType.setEnabled(true);
            mID.setEnabled(true);
            mName.setEnabled(true);
        } else {
            mType.setEnabled(false);
            mID.setEnabled(false);
            mName.setEnabled(false);
        }

        if (mListener != null) {
            mListener.changeState(mCheck.isSelected());
        }
    }

    // classes

    public class CheckListener implements ChangeListener {

        @Override
        public void stateChanged(ChangeEvent event) {
            checkState();
        }
    }

}
