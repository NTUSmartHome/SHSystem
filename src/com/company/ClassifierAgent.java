package com.company;


/**
 * Created by g2525_000 on 2015/11/8.
 */


import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.BayesNet;
import weka.core.*;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ClassifierAgent {

    private ArrayList<Attribute> attributes;
    private Classifier cModel;
    private String[] classes;

    public void train() throws Exception {

        // Read training data
        Instances trainingSet = readTrainingData(attributes);

        cModel = new BayesNet();
        // Training
        long startTime = System.currentTimeMillis();
        cModel.buildClassifier(trainingSet);

        Evaluation eTest = new Evaluation(trainingSet);
        // CrossValidate
        eTest.crossValidateModel(cModel, trainingSet, 10, new Debug.Random(1));
        System.out.println((System.currentTimeMillis() - startTime));
        // Print the statistics result :
        String strSummary = eTest.toSummaryString();
        System.out.println(strSummary);

        // Get the confusion matrix
        double[][] cmMatrix = eTest.confusionMatrix();
        for (int row_i = 0; row_i < cmMatrix.length; row_i++) {
            for (int col_i = 0; col_i < cmMatrix.length; col_i++) {
                System.out.print(cmMatrix[row_i][col_i]);
                System.out.print("|");
            }
            System.out.println();
        }
        // save the model
        new File("Model/").mkdir();
        weka.core.SerializationHelper.write("Model/ar.model", cModel);
        FileWriter fw = new FileWriter("Model/ar.class");
        fw.write(trainingSet.classAttribute().toString());
        fw.close();
    }

    public Instances readTrainingData(ArrayList<Attribute> attributes) throws Exception {
        Instances trainingSet = new Instances("train", attributes, 0);
        trainingSet.setClassIndex(trainingSet.numAttributes() - 1);
        FileReader fr = new FileReader(new File("trainingData.txt"));
        BufferedReader br = new BufferedReader(fr);
        String dataString;
        while ((dataString = br.readLine()) != null) {
            String[] data = dataString.split("\\s+");
            Instance inst = new DenseInstance(trainingSet.numAttributes() + 1);
            int i = 0;
            for (; i < data.length - 1; i++) {
                try {
                    inst.setValue(attributes.get(i), Double.valueOf(data[i]));
                } catch (Exception e) {
                    inst.setValue(attributes.get(i), data[i]);
                }
            }
            inst.setValue(attributes.get(attributes.size() - 1), data[i]);
            trainingSet.add(inst);
        }
        br.close();
        fr.close();
        return trainingSet;

    }

    public void recognize() throws IOException, ClassNotFoundException {
        // Init
        Instances predictSet = new Instances("train", attributes, 0);
        predictSet.setClassIndex(predictSet.numAttributes() - 1);
        // Read model
        ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream("Model/ar.model"));
        cModel = (Classifier) ois.readObject();
        ois.close();
        Map<Integer, String> label = new HashMap<>();
        // Predict
        // double prd = bn.classifyInstance(inst);
        //System.out.println(attributes.get(attributes.size() - 1).value((int) prd));
    }

    public int init() {
        // Read feature config
        ArrayList<Attribute> attributes = null;
        try {
            attributes = readAtt();
        } catch (IOException e) {
            return 1;
        }
        Instances predictSet = new Instances("train", attributes, 0);
        predictSet.setClassIndex(predictSet.numAttributes() - 1);
        // Read model
        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(
                    new FileInputStream("Model/ar.model"));
            cModel = (Classifier) ois.readObject();
            return 3;
        } catch (IOException e) {
            return 2;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return -1;
        }

    }

    public String[] getClasses() {
        return classes;
    }

    public ArrayList<Attribute> readAtt() throws IOException {

        FileReader fr = new FileReader("featureConfig.txt");
        BufferedReader br = new BufferedReader(fr);
        ArrayList<Attribute> attributes = new ArrayList<>();
        String att;
        int count = 0;
        while ((att = br.readLine()) != null) {
            if (att.contains("Numeric")) {
                attributes.add(new Attribute(String.valueOf(count)));
            } else if (att.contains("Nominal")) {
                String[] values = att.split(",");
                FastVector fastVector = new FastVector(values.length - 1);
                for (int i = 1; i < values.length; i++) {
                    fastVector.addElement(values[i]);
                }
                attributes.add(new Attribute(String.valueOf(count), fastVector));
            } else if (att.contains("class")) {
                String[] values = att.split(",");
                // Init classes
                classes = new String[values.length - 1];
                FastVector fastVector = new FastVector(values.length - 1);
                for (int i = 1; i < values.length; i++) {
                    fastVector.addElement(values[i]);
                    classes[i - 1] = values[i];

                }
                attributes.add(new Attribute("class", fastVector));
            }
            count++;
        }
        br.close();
        fr.close();
        return attributes;
    }

    public Classifier getcModel() {
        return cModel;
    }


    public ArrayList<Attribute> getAttributes() {
        return attributes;
    }

}
