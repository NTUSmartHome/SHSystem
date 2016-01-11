package com.company;


import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.Timestamp;
import java.util.Map;
import java.util.Random;

/**
 * Created by MingJe on 2016/1/6.
 */
public class SHSystem {
    private ClassifierAgent classifierAgent;
    private JFrame frame;
    private SHSystemPanel shSystemPanel;
    private boolean isRecognize;
    private boolean isCollect;
    private SwingWorker progressWorker, trainWorker;
    private ProgressMonitor progressMonitor;

    public SHSystem() {
        classifierAgent = new ClassifierAgent();
        shSystemPanel = new SHSystemPanel();
        SwingUtilities.invokeLater(() -> {
            frame = new JFrame("MARCS");
            frame.setContentPane(shSystemPanel.getMainPanel());
            frame.setSize(1000, 850);
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
            JOptionPane.showMessageDialog(frame, "Please setup by following the instruction");
        } else if (trained == 2) {
            JOptionPane.showMessageDialog(frame, "Don't have a model, you should train it first");
            //Init labelMapping table
            initLabelMapping();
        } else if (trained == 3) {
            //Init labelMapping table
            initLabelMapping();
        }

        //Init collect button
        shSystemPanel.getCollectStartButton().addActionListener(e1 -> {
            setMessage("Collect On");
            isCollect = true;
            shSystemPanel.getCollectStartButton().setEnabled(false);
            shSystemPanel.getCollectStopButton().setEnabled(true);
        });
        shSystemPanel.getCollectStopButton().addActionListener(e1 -> {
            setMessage("Collect Off");
            isCollect = false;
            shSystemPanel.getCollectStartButton().setEnabled(true);
            shSystemPanel.getCollectStopButton().setEnabled(false);
            JOptionPane.showMessageDialog(frame, "Analysis result : 9 contexts Found");
        });
        //Init train button
        shSystemPanel.getTrainButton().addActionListener(e -> {
            setMessage("Training");
            shSystemPanel.getTrainButton().setEnabled(false);
            initWorkers();
            classifierAgent.setTrainState(0);
            progressMonitor.setProgress(progressWorker.getProgress());
            //Start two workers
            progressWorker.execute();
            trainWorker.execute();

        });

        //Init recognize button
        shSystemPanel.getRecognizeStartButton().addActionListener(e -> {
            shSystemPanel.getRecognizeStartButton().setEnabled(false);
            shSystemPanel.getRecognizeStopButton().setEnabled(true);
            isRecognize = true;
            setMessage("Recognize On");

        });
        shSystemPanel.getRecognizeStopButton().addActionListener(e1 -> {
            shSystemPanel.getRecognizeStartButton().setEnabled(true);
            shSystemPanel.getRecognizeStopButton().setEnabled(false);
            isRecognize = false;
            setMessage("Recognize On");
        });

        //Init exit button
        shSystemPanel.getExitButton().addActionListener(e -> frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING)));

        //Init ProgressMonitor
        progressMonitor = new ProgressMonitor(frame, "Training",
                "", 0, 100);
    }

    public void initLabelMapping() {
        // Organize the label map table
        String[] columnDef = {"<html><h2>Context", "<html><h2>Semantic label (double click to edit)"};
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

        //Set Table detail
        labelMapTable.setFont(new Font("Courier New", Font.BOLD, 20));
        labelMapTable.setRowHeight(40);
        labelMapTable.setPreferredScrollableViewportSize(new Dimension(400, 500));
        labelMapTable.getModel().addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                int row = labelMapTable.getSelectedRow();
                int col = labelMapTable.getSelectedColumn();
                Object[] options = {"Yes, please",
                        "No, thanks",};
                int n = JOptionPane.showOptionDialog(frame,
                        "Are you sure ?",
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
        shSystemPanel.getLabelTab().add(scrollPane, BorderLayout.NORTH);
    }

    public void initWorkers() {
        //Worker for progressbar
        progressWorker = new SwingWorker() {
            @Override
            protected Object doInBackground() throws Exception {
                Random random = new Random();
                int progress = 0;
                setProgress(0);
                try {
                    while (progress < 100 && !isCancelled()) {
                        Thread.sleep(random.nextInt(1000));

                        int newProgress = classifierAgent.getTrainState() + random.nextInt(70);
                        if (newProgress > progress)
                            progress = newProgress;
                        setProgress(Math.min(100, progress));
                    }
                } catch (InterruptedException ignore) {
                }
                return null;
            }

        };
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

                        Toolkit.getDefaultToolkit().beep();
                        if (progressMonitor.isCanceled()) {
                            progressWorker.cancel(true);
                            trainWorker.cancel(true);
                            setMessage("Train cancel");
                        }
                        shSystemPanel.getTrainButton().setEnabled(true);
                        progressMonitor.close();
                    }
                }
            }
        });

        //Worker for training
        trainWorker = new SwingWorker() {
            @Override
            protected Object doInBackground() throws Exception {
                Thread.sleep(2000);
                String confusionMatrix = classifierAgent.train().replace("\n", "<br />");
                while (!progressWorker.isDone()) ;
                setMessage("Train finished" + "<br />Confusion Matrix:<br/>" + confusionMatrix);
                return null;
            }
        };


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
                    "Enter semantic meaning",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    null,
                    null);
            labelMap.put(act, s);
            System.out.println(s);
            shSystemPanel.getLabelMapTable().setValueAt(s, Integer.parseInt(act), 1);
        }
        String message = "<html>" +
                "<br />Recognized Activity is <h1>" + s + "</h1>";
        setMessage(message);

    }

    public boolean isCollect() {
        return isCollect;
    }

    public void setMessage(String message) {

        JLabel logLabel = shSystemPanel.getLogLabel();
        logLabel.setText(logLabel.getText() + "<br/>" + new Timestamp(System.currentTimeMillis()) + " " + message);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        shSystemPanel.getLogScroll().validate();
        shSystemPanel.getLogScroll().revalidate();
        JScrollBar verticalScrollBar = shSystemPanel.getLogScroll().getVerticalScrollBar();
        verticalScrollBar.setValue(verticalScrollBar.getMaximum());


    }

    public static void main(String[] args) {
        new SHSystem();
    }
}
