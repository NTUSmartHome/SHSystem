package com.company;


import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Map;
import java.util.Random;
import java.util.Vector;

/**
 * Created by MingJe on 2016/1/6.
 */
public class SHSystem {
    private ClassifierAgent classifierAgent;
    private JFrame frame;
    private SHSystemPanel shSystemPanel;
    private boolean isRecognize;

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


    public void init() {
        int trained = classifierAgent.init();
        if (trained == 1) {
            JOptionPane.showMessageDialog(frame, "Don't have attribute config, you should set it first");
        } else if (trained == 2) {
            JOptionPane.showMessageDialog(frame, "Don't have model, you should train it first");
        } else if (trained == 3) {

        }
        String[] columnDef = {"Pseudo label", "Semantic label"};
        Map<Integer, String> labelMapping = classifierAgent.getLabelMapping();
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
        JTable labelMapTable = new JTable(rowData, columnDef) {
            @Override
            public boolean isCellEditable(int row, int column) {
                if (column == 0)
                    return false;
                return true;
            }

        };
        shSystemPanel.setLabelMapTable(labelMapTable);
        DefaultTableCellRenderer dtcr = new DefaultTableCellRenderer();
        dtcr.setHorizontalTextPosition(SwingConstants.CENTER);
        TableColumnModel tableColumn = labelMapTable.getColumnModel();
        tableColumn.getColumn(0).setCellRenderer(dtcr);
        tableColumn.getColumn(1).setCellRenderer(dtcr);
        labelMapTable.setRowHeight(60);
        labelMapTable.setPreferredScrollableViewportSize(new Dimension(400, 300));
        labelMapTable.getModel().addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                int row = labelMapTable.getSelectedRow();
                int col = labelMapTable.getSelectedColumn();
                Object[] options = {"Yes, please",
                        "No, thanks",};
                int n = JOptionPane.showOptionDialog(frame,
                        "Would you like change the semantic mean of " + row,
                        "Warning",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        options,
                        options[1]);
                if (n == 0 && row != -1) {
                    labelMapping.put(row, (String) labelMapTable.getValueAt(row, col));
                    System.out.println(labelMapping.get(row));
                }
            }
        });
        JScrollPane scrollPane = new JScrollPane(labelMapTable);
        frame.add(scrollPane, BorderLayout.CENTER);
        JLabel messageLabel = shSystemPanel.getMessageLabel();
        messageLabel.setText("<html><h2>Please press the action button that you prefer " + "<br />" +
                "1) Train, if you want have a new model" + "<br />" +
                "2) Recognize, if you already have a model, and you want recognize your activity</h2></html>");
        messageLabel.setPreferredSize(new Dimension(400, 200));
        frame.add(messageLabel, BorderLayout.NORTH);
        frame.revalidate();
        shSystemPanel.getTrainButton().addActionListener(e -> {
            shSystemPanel.getMessageLabel().setText("<html><h2>Training</h2></html>");
            SwingWorker progressWorker = new SwingWorker() {
                @Override
                protected Object doInBackground() throws Exception {
                    Random random = new Random();
                    int progress = 0;
                    setProgress(0);
                    try {
                        while (progress < 100 && !isCancelled()) {
                            Thread.sleep(random.nextInt(100));
                            int newProgress = classifierAgent.getTrainState() + random.nextInt(50);
                            if (newProgress > progress)
                                progress = newProgress;
                            setProgress(Math.min(100, progress));
                        }
                    } catch (InterruptedException ignore) {
                    }
                    return null;
                }

                @Override
                protected void done() {
                    super.done();
                }
            };
            SwingWorker trainWorker = new SwingWorker() {
                @Override
                protected Object doInBackground() throws Exception {
                    shSystemPanel.getTrainButton().setEnabled(false);
                    classifierAgent.train();
                    return null;
                }
            };
            ProgressMonitor progressMonitor = new ProgressMonitor(frame, "Training",
                    "", 0, 100);
            progressMonitor.setProgress(progressWorker.getProgress());
            progressWorker.addPropertyChangeListener(new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if ("progress" == evt.getPropertyName()) {
                        int progress = (Integer) evt.getNewValue();
                        progressMonitor.setProgress(progress);
                        String message =
                                String.format("Completed %d%%.\n", progress);
                        progressMonitor.setNote(message);
                        if (progressMonitor.isCanceled() || classifierAgent.getTrainState() == 100) {
                            progressMonitor.close();
                            Toolkit.getDefaultToolkit().beep();
                            if (progressMonitor.isCanceled()) {
                                progressWorker.cancel(true);
                                trainWorker.cancel(true);
                                shSystemPanel.getMessageLabel().setText("<html><h2>Train canceled</h2></html>");
                            } else {
                                shSystemPanel.getMessageLabel().setText("<html><h2>Train finished</h2></html>");
                            }
                            shSystemPanel.getTrainButton().setEnabled(true);
                        }
                    }
                }
            });
            progressWorker.execute();
            trainWorker.execute();


        });
        shSystemPanel.getRecognizeButton().addActionListener(e ->
                {
                    if (isRecognize) {
                        shSystemPanel.getRecognizeButton().setText("Recognize Off");
                        recongnize("");
                        isRecognize = !isRecognize;
                    } else {
                        shSystemPanel.getRecognizeButton().setText("Recognize On");
                        isRecognize = !isRecognize;
                    }
                }
        );

    }

    public boolean isRecognize() {
        return isRecognize;
    }

    public void recongnize(String data) {
        int act = classifierAgent.recognize(data);
        //int act = 0;
        Map<Integer, String> labelMap = classifierAgent.getLabelMapping();
        String s = labelMap.get(act);
        if (s == null) {
            s = (String) JOptionPane.showInputDialog(
                    frame,
                    "<html>Now recognize activity is" + act +
                            "<br />Enter your semantic meaning:\n",
                    "Enter semantic meaning</html>",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    null,
                    null);
            labelMap.put(act, s);
            System.out.println(s);
            shSystemPanel.getLabelMapTable().setValueAt(s, act, 1);
        }
        shSystemPanel.getMessageLabel().setText("<html><h2>Recognized Activity is " + s);


    }

    public static void main(String[] args) {
        new SHSystem();
    }
}
