package com.company;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by MingJe on 2016/1/6.
 */
public class SHSystem {
    private ClassifierAgent classifierAgent;
    private Map<Integer, String> labelMapping;
    private JFrame frame;
    private SHSystemPanel shSystemPanel;
    public SHSystem() {
        classifierAgent = new ClassifierAgent();
        shSystemPanel = new SHSystemPanel();
        SwingUtilities.invokeLater(() -> {
            frame = new JFrame("SHSystemPanel");
            frame.setContentPane(shSystemPanel.getMainPanel());
            frame.setSize(800, 800);
            frame.setLocation(500, 0);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
        });
        SwingUtilities.invokeLater(() -> {
            init();
        });
    }
    public static Map<Integer, String> labelMappingReader() {
        try {
            FileReader fr = new FileReader(new File("LabelMapping.txt"));
            BufferedReader br = new BufferedReader(fr);
            Map<Integer, String> labelMapping = new HashMap<>();
            String line;
            while ((line = br.readLine()) != null) {
                String[] labelNMap = line.split("\\s+");
                labelMapping.put(Integer.parseInt(labelNMap[0]), labelNMap[1]);
            }
            fr.close();
            br.close();
            return labelMapping;
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void init() {
        int trained = classifierAgent.init();
        if (trained == 1) {
            JOptionPane.showMessageDialog(frame, "Don't have attribute config, you should set it first");
        } else if (trained == 2) {
            JOptionPane.showMessageDialog(frame, "Don't have model, you should train it first");
            String[] columnDef = {"Pseudo label", "Semantic label"};
            labelMapping = labelMappingReader();
            String[] classes = classifierAgent.getClasses();
            String[][] rowData = new String[classes.length][2];
            for (int i = 0; i < classes.length; i++) {
                String label = labelMapping.get(i);
                if (label != null) {
                    rowData[i][0] = String.valueOf(i);
                    rowData[i][1] = label;
                } else {
                    rowData[i][0] = String.valueOf(i);
                    rowData[i][1] = String.valueOf("no definition");
                }
            }
            JTable labelMapTable = new JTable(rowData, columnDef);
            labelMapTable.setPreferredScrollableViewportSize(new Dimension(400,300));
            labelMapTable.addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    System.out.println("dsada");
                }

                @Override
                public void focusLost(FocusEvent e) {

                }
            });
            /*ListSelectionModel cellSelectionModel = labelMapTable.getSelectionModel();
            cellSelectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            cellSelectionModel.addListSelectionListener(new ListSelectionListener() {
                @Override
                public void valueChanged(ListSelectionEvent e) {

                }
            });*/
            JScrollPane scrollPane = new JScrollPane(labelMapTable);
            frame.add(scrollPane);
            frame.revalidate();

        }
    }

    public static void main(String[] args) {
        new SHSystem();
    }
}
