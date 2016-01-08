package com.company;

import javax.swing.*;

/**
 * Created by MingJe on 2016/1/5.
 */
public class SHSystemPanel {


    private JPanel mainPanel;
    private JButton trainButton;
    private JButton recognizeButton;
    private JLabel messageLabel;
    private JButton exitButton;
    private JButton collectButton;
    private JLabel logLabel;
    private JScrollPane LogScroll;
    private JTable labelMapTable;
    private JPanel bottomPanel;
    public JLabel getMessageLabel() {
        return messageLabel;
    }

    public SHSystemPanel() {

    }

    public JScrollPane getLogScroll() {
        return LogScroll;
    }
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

    public JButton getExitButton() {
        return exitButton;
    }

    public JLabel getLogLabel() {
        return logLabel;
    }

    public JButton getCollectButton() {
        return collectButton;
    }

    public void setLabelMapTable(JTable labelMapTable) {
        this.labelMapTable = labelMapTable;
    }

    public static void main(String[] args) {


    }




}

