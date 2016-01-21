package evaluations;

import har.Classes;

import java.util.TreeMap;

import models.Model;

public class SimpleEvaluation extends Evaluation {

    public SimpleEvaluation(Model model) {
        super(model);
    }

    @Override
    public double evaluate(TreeMap<Integer, Classes> testSet) {
        int trueGuesses = 0;
        for (int i : testSet.keySet()) {
            if (model.test(i) == testSet.get(i)) {
                trueGuesses++;
            }
        }
        double accuracy = (double) (trueGuesses) / (double) (testSet.size());
        return accuracy;
    }
}
