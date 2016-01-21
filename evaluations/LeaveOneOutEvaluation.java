package evaluations;

import java.util.TreeMap;

import har.Classes;
import io.ReadData;
import models.Model;

public class LeaveOneOutEvaluation extends SimpleEvaluation {
    private double[] accuracy;

    public LeaveOneOutEvaluation(Model model) {
        super(model);
        accuracy = new double[25];
        for (int personNumber = 1; personNumber <= 1; personNumber++) {
            TreeMap<Integer, Classes>[] datasets = ReadData
                    .leaveOneOutSelect(personNumber);
            TreeMap<Integer, Classes> trainSet = datasets[0];
            TreeMap<Integer, Classes> testSet = datasets[1];
            model.train(trainSet);
            accuracy[personNumber - 1] = this.evaluate(testSet);
        }
    }

    public double meanAccuracy() {
        double meanAccuracy = 0;
        for (double d: accuracy) {
            meanAccuracy += d;
        }
        meanAccuracy = meanAccuracy / accuracy.length;
        return meanAccuracy;
    }

    public double[] getAccuracy() {
        return this.accuracy;
    }
}
