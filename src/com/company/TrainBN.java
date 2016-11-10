package com.company;

/**
 * Created by TingYing on 2016/11/10.
 */
public class TrainBN {
    private ClassifierAgent classifierAgent;

    public TrainBN() {
        classifierAgent = new ClassifierAgent();
        classifierAgent.init();

        classifierAgent.train();
    }

    public static void main(String[] args) {
        new TrainBN();
    }
}


