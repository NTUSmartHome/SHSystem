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
import java.security.Timestamp;
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
            //Init labelMapping table
            initLabelMapping();
        } else if (trained == 3) {
            //Init labelMapping table
            initLabelMapping();
        }


        //Init messageLabel
        JLabel messageLabel = shSystemPanel.getMessageLabel();
        //messageLabel.setFont(new Font("Arial", Font.BOLD, 20));
        messageLabel.setText("<html>Please press the action button that you prefer " + "<br />" +
                "1) Train, if you want have a new model" + "<br />" +
                "2) Recognize, if you already have a model, and you want recognize your activity</html>");
        //messageLabel.setPreferredSize(new Dimension(800, 200));
        //messageLabel.setBorder(new EmptyBorder(0, 80, 0, 0));
        messageLabel.setBackground(Color.white);

        //Init collect button
        shSystemPanel.getCollectButton().addActionListener(e1 -> {
            if (isCollect) {
                shSystemPanel.getCollectButton().setBackground(null);
                shSystemPanel.getMessageLabel().setText("Collect Off");
                setMessage("Collect Off");
                isCollect = !isCollect;
                if (shSystemPanel.getLabelMapTable() != null)
                    frame.remove(shSystemPanel.getLabelMapTable());
                initLabelMapping();
            } else {
                shSystemPanel.getCollectButton().setBackground(Color.red);
                shSystemPanel.getMessageLabel().setText("Collect On");
                setMessage("Collect On");
                isCollect = !isCollect;
            }
        });
        //Init train button
        shSystemPanel.getTrainButton().addActionListener(e -> {
            setMessage("Training");
            shSystemPanel.getTrainButton().setEnabled(false);
            initWorkers();
            classifierAgent.setTrainState(0);
            progressMonitor.setProgress(progressWorker.getProgress());
            shSystemPanel.getMessageLabel().setText("Training");
            //Start two workers
            progressWorker.execute();
            trainWorker.execute();

        });

        //Init recognize button
        shSystemPanel.getRecognizeButton().addActionListener(e ->
                {
                    if (isRecognize) {
                        shSystemPanel.getRecognizeButton().setBackground(null);
                        shSystemPanel.getMessageLabel().setText("Recognize Off");
                        setMessage("Recognize Off");
                        isRecognize = !isRecognize;
                    } else {
                        shSystemPanel.getRecognizeButton().setBackground(Color.RED);
                        shSystemPanel.getMessageLabel().setText("Recognize On");
                        setMessage("Recognize On");
                        isRecognize = !isRecognize;
                    }
                }
        );

        //Init exit button
        shSystemPanel.getExitButton().addActionListener(e -> frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING)));

        //Init ProgressMonitor
        progressMonitor = new ProgressMonitor(frame, "Training",
                "", 0, 100);
    }

    public void initLabelMapping() {

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

        //Set Table detail
        labelMapTable.setFont(new Font("Courier New", Font.BOLD, 20));
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
                    int dotCount = 0;
                    StringBuilder trainState = new StringBuilder("Training");
                    while (progress < 100 && !isCancelled()) {
                        Thread.sleep(random.nextInt(1000));
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
                            shSystemPanel.getMessageLabel().setText("<html>Train canceled</html>");
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
                shSystemPanel.getMessageLabel().setText("<html>Train finished<br />");
                setMessage("Train finished");
                setMessage(confusionMatrix);
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
                    "Enter semantic meaning</html>",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    null,
                    null);
            labelMap.put(act, s);
            System.out.println(s);
            shSystemPanel.getLabelMapTable().setValueAt(s, Integer.parseInt(act), 1);
        }
        String message = "<html>" +
                new java.sql.Timestamp(System.currentTimeMillis()) +
                "<br />Recognized Activity is <h1>" + s + "</h1>";
        shSystemPanel.getMessageLabel().setText(message);
        setMessage(message);

    }

    public boolean isCollect() {
        return isCollect;
    }

    public void setMessage(String message) {

        JLabel logLabel = shSystemPanel.getLogLabel();
        logLabel.setText(logLabel.getText() + "<br/>" + message);
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        shSystemPanel.getLogScroll().validate();
        JScrollBar verticalScrollBar = shSystemPanel.getLogScroll().getVerticalScrollBar();
        verticalScrollBar.setValue(verticalScrollBar.getMaximum());


    }

    public static void main(String[] args) {
        new SHSystem();
    }
}
