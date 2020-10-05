package com.fmh.tools;

import javax.swing.*;//导包
import java.awt.*;

public class AdapterWindow extends JFrame {

    //定义组件
    JPanel jp1,jp2,jp3;//定义面板
    JTextField jtf1;//定义文本框
    JPasswordField jpf1;//定义密码框
    JLabel jlb1,jlb2;//定义标签
    JButton jb1,jb2;//定义按钮


    public AdapterWindow()//构造函数
    {
        //创建组件
        jp1=new JPanel();//创建三个面板
        jp2=new JPanel();
        jp3=new JPanel();

        jlb1=new JLabel("用户名");//创建两个标签
        jlb2=new JLabel("密	  码");

        jb1=new JButton("登录");//创建两个按钮
        jb2=new JButton("取消");

        jtf1=new JTextField(10);//创建文本框
        jpf1=new JPasswordField(10);//创建密码框

        //设置布局管理器
        this.setLayout(new GridLayout(3,1));//网格布局，3行一列

        //添加组件
        this.add(jp1);//添加面板
        this.add(jp2);
        this.add(jp3);

        jp1.add(jlb1);//添加面板1的标签和文本框
        jp1.add(jtf1);

        jp2.add(jlb2);//添加面板2的标签和密码框
        jp2.add(jpf1);

        jp3.add(jb1);//添加面板3的按钮
        jp3.add(jb2);

        //设置窗口属性
        this.setTitle("登录界面");//创建界面标题
        this.setSize(300, 200);//设置界面像素
        this.setLocation(500, 100);//设置界面初始位置
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);//设置界面和虚拟机一起关闭
        this.setVisible(true);//设置界面可显示
    }
}