package com.company;


import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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
            frame.setLocation(500, 50);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    classifierAgent.saveLabelMapping();
                }
            });
        });
        SwingUtilities.invokeLater(() -> {
            init();
        });
    }


    public void init() {
        // Check the state of user env. setting
        int trained = classifierAgent.init();
        if (trained == 1) {
            JOptionPane.showMessageDialog(frame, "Don't have attribute config, you should set it first");
        } else if (trained == 2) {
            JOptionPane.showMessageDialog(frame, "Don't have model, you should train it first");
        } else if (trained == 3) {

        }
        // Organize the label map table
        String[] columnDef = {"<html><h2>Pseudo label", "<html><h2>Semantic label (double click to edit)"};
        Map<String, String> labelMapping = classifierAgent.getLabelMapping();
        String[] classes = classifierAgent.getClasses();
        String[][] rowData = new String[classes.length][2];
        for (int i = 0; i < classes.length; i++) {
            String label = labelMapping.get(String.valueOf(i));
            if (label != null) {
                rowData[i][0] = classes[i];
                rowData[i][1] = label;
            } else {
                rowData[i][0] = classes[i];
                rowData[i][1] = "";
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
        //Set Table content align center
        DefaultTableCellRenderer dtcr = new DefaultTableCellRenderer();
        dtcr.setHorizontalTextPosition(SwingConstants.CENTER);
        TableColumnModel tableColumn = labelMapTable.getColumnModel();
        tableColumn.getColumn(0).setCellRenderer(dtcr);
        tableColumn.getColumn(1).setCellRenderer(dtcr);
        //Set Table detail
        labelMapTable.setFont(new Font("Arial", Font.BOLD, 20));
        labelMapTable.setRowHeight(60);
        labelMapTable.setPreferredScrollableViewportSize(new Dimension(400, 400));
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
                    labelMapping.put(String.valueOf(row), (String) labelMapTable.getValueAt(row, col));
                    System.out.println(labelMapping.get(row));
                }
            }
        });
        JScrollPane scrollPane = new JScrollPane(labelMapTable);
        frame.add(scrollPane, BorderLayout.NORTH);

        //Init messageLabel
        JLabel messageLabel = shSystemPanel.getMessageLabel();
        messageLabel.setFont(new Font("Arial", Font.BOLD, 20));
        messageLabel.setText("<html>Please press the action button that you prefer " + "<br />" +
                "1) Train, if you want have a new model" + "<br />" +
                "2) Recognize, if you already have a model, and you want recognize your activity</html>");
        messageLabel.setPreferredSize(new Dimension(800, 200));
        messageLabel.setBorder(new EmptyBorder(0, 80, 0, 0));
        messageLabel.setBackground(Color.white);

        //Init train button
        shSystemPanel.getTrainButton().addActionListener(e -> {
            shSystemPanel.getMessageLabel().setText("Training");
            //Worker for progressbar
            SwingWorker progressWorker = new SwingWorker() {
                @Override
                protected Object doInBackground() throws Exception {
                    Random random = new Random();
                    int progress = 0;
                    setProgress(0);
                    try {
                        int dotCount = 0;
                        StringBuilder trainState = new StringBuilder("Training");
                        while (progress < 100 && !isCancelled()) {
                            trainState.append(".");
                            ++dotCount;
                            if (dotCount == 5) {
                                trainState = new StringBuilder("Training");
                                dotCount = 0;
                            }
                            shSystemPanel.getMessageLabel().setText(trainState.toString());

                            int newProgress = classifierAgent.getTrainState() + random.nextInt(70);
                            if (newProgress > progress)
                                progress = newProgress;
                            setProgress(Math.min(100, progress));
                            Thread.sleep(random.nextInt(1000));
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
            //Worker for training
            SwingWorker trainWorker = new SwingWorker() {
                @Override
                protected Object doInBackground() throws Exception {
                    Thread.sleep(1000);
                    shSystemPanel.getTrainButton().setEnabled(false);
                    String confusionMatrix = classifierAgent.train().replace("\n", "<br />");
                    Thread.sleep(500);
                    shSystemPanel.getMessageLabel().setText("<html>Train finished<br />" + confusionMatrix + "</html>");
                    return null;
                }
            };
            //Init ProgressMonitor
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
                                shSystemPanel.getMessageLabel().setText("<html>Train canceled</html>");
                            }
                            shSystemPanel.getTrainButton().setEnabled(true);
                        }
                    }
                }
            });
            //Start two workers
            progressWorker.execute();
            trainWorker.execute();


        });

        //Init recognize button
        shSystemPanel.getRecognizeButton().addActionListener(e ->
                {
                    if (isRecognize) {
                        shSystemPanel.getRecognizeButton().setText("Recognize Off");
                        isRecognize = !isRecognize;
                    } else {
                        shSystemPanel.getRecognizeButton().setText("Recognize On");
                        isRecognize = !isRecognize;
                    }
                }
        );

        //Init exit button
        shSystemPanel.getExitButton().addActionListener(e -> frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING)));
    }

    public boolean isRecognize() {
        return isRecognize;
    }

    public void recognize(String data) {
        String act = classifierAgent.recognize(data);
        //int act = 0;
        Map<String, String> labelMap = classifierAgent.getLabelMapping();
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
            shSystemPanel.getLabelMapTable().setValueAt(s, Integer.parseInt(act), 1);
        }
        shSystemPanel.getMessageLabel().setText("<html><h2>Recognized Activity is " + s);


    }

    public static void main(String[] args) {
        new SHSystem();
    }
}
