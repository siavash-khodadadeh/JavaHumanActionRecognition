package evaluations;

import har.Classes;

import java.util.TreeMap;

import models.Model;

public abstract class Evaluation {
    Model model;

    public Evaluation(Model model) {
        this.model = model;
    }

    public abstract double evaluate(TreeMap<Integer, Classes> testSet);
}
