package com.company;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by MingJe on 2016/1/5.
 */
public class SHSystemPanel {


    private JPanel mainPanel;
    private JPanel bottomPanel;
    private JButton trainButton;
    private JButton recognizeButton;
    private JTable labelMapTable;

    public JPanel getMainPanel() {
        return mainPanel;
    }

    public JButton getTrainButton() {
        return trainButton;
    }

    public JButton getRecognizeButton() {
        return recognizeButton;
    }

    public JTable getLabelMapTable() {
        return labelMapTable;
    }


    public void setLabelMapTable(JTable labelMapTable) {
        this.labelMapTable = labelMapTable;

    }

    public static void main(String[] args) {


    }




}

