package com.company;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/**
 * Created by MingJe on 2016/1/5.
 */
public class SHSystemPanel {


    private JPanel mainPanel;
    private JButton trainButton;
    private JButton recognizeStartButton;
    private JLabel instructionLabel;
    private JButton exitButton;
    private JButton collectStartButton;
    private JLabel logLabel;
    private JTable labelMapTable;
    private JPanel bottomPanel;
    private JTabbedPane tabbedPane;
    private JPanel trainTab;
    private JPanel collectTab;
    private JPanel recognizeTab;
    private JPanel instructionTab;
    private JPanel labelTab;
    private JButton collectStopButton;
    private JLabel collectLabel;
    private JButton recognizeStopButton;
    private JScrollPane logScroll;
    private DefaultTableModel labelMapTableModel;


    public DefaultTableModel getLabelMapTableModel() {
        return labelMapTableModel;
    }
    public void setLabelMapTableModel(DefaultTableModel labelMapTableModel) {
        this.labelMapTableModel = labelMapTableModel;
    }
    public JScrollPane getLogScroll() {
        return logScroll;
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    public JButton getTrainButton() {
        return trainButton;
    }

    public JButton getRecognizeStartButton() {
        return recognizeStartButton;
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

    public JButton getCollectStartButton() {
        return collectStartButton;
    }

    public JPanel getLabelTab() {
        return labelTab;
    }

    public JButton getCollectStopButton() {
        return collectStopButton;
    }

    public JButton getRecognizeStopButton() {
        return recognizeStopButton;
    }

    public void setLabelMapTable(JTable labelMapTable) {
        this.labelMapTable = labelMapTable;
    }

    public static void main(String[] args) {


    }


}

