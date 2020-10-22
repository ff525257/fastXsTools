package com.fmh.tools.weight;

import com.fmh.tools.i18n.Resource;
import com.fmh.tools.callback.OnCheckBoxStateChangedListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

public class EntryHeaderLayout extends JPanel {

    protected JCheckBox mAllCheck;
    protected JLabel mType;
    protected JLabel mID;
    protected JLabel mEvent;
    protected JLabel mName;
    protected OnCheckBoxStateChangedListener mAllListener;
    private int itemHeight = 26;

    public void setAllListener(final OnCheckBoxStateChangedListener onStateChangedListener) {
        this.mAllListener = onStateChangedListener;
    }

    public EntryHeaderLayout() {
        mAllCheck = new JCheckBox();
        mAllCheck.setPreferredSize(new Dimension(40, itemHeight));
        mAllCheck.setSelected(false);
        mAllCheck.addItemListener(new AllCheckListener());

        mType = new JLabel(Resource.getText("classname"));
        mType.setPreferredSize(new Dimension(100, itemHeight));
        mType.setFont(new Font(mType.getFont().getFontName(), Font.BOLD, mType.getFont().getSize()));

        mID = new JLabel("ID");
        mID.setPreferredSize(new Dimension(100, itemHeight));
        mID.setFont(new Font(mID.getFont().getFontName(), Font.BOLD, mID.getFont().getSize()));

       /* mEvent = new JLabel("OnClick");
        mEvent.setPreferredSize(new Dimension(100, 26));
        mEvent.setFont(new Font(mEvent.getFont().getFontName(), Font.BOLD, mEvent.getFont().getSize()));*/

        mName = new JLabel(Resource.getText("variablename"));
        mName.setPreferredSize(new Dimension(100, 26));
        mName.setFont(new Font(mName.getFont().getFontName(), Font.BOLD, mName.getFont().getSize()));

        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
        add(Box.createRigidArea(new Dimension(10, 0)));
        add(mAllCheck);
        add(Box.createRigidArea(new Dimension(10, 0)));
        add(mType);
        add(Box.createRigidArea(new Dimension(10, 0)));
        add(mID);
       /* add(Box.createRigidArea(new Dimension(12, 0)));
        add(mEvent);*/
        add(Box.createRigidArea(new Dimension(10, 0)));
        add(mName);
        add(Box.createHorizontalGlue());
    }

    public JCheckBox getAllCheck() {
        return mAllCheck;
    }

    // classes

    private class AllCheckListener implements ItemListener {
        @Override
        public void itemStateChanged(ItemEvent itemEvent) {
            if (mAllListener != null) {
                mAllListener.changeState(itemEvent.getStateChange() == ItemEvent.SELECTED);
            }
        }
    }
}
