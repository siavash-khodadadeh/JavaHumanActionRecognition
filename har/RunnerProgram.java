package har;

import io.ReadData;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.TreeMap;

import javax.swing.JOptionPane;

import models.BagOfWords;
import models.Model;
import utils.KMeans;
import utils.PCA;
import Jama.Matrix;
import evaluations.LeaveOneOutEvaluation;
import evaluations.SimpleEvaluation;

public class RunnerProgram {
    static boolean extract_all_videos_gradient_data = false;
    static String allGradeintDataFileAddress = Constants.allGradientDataOfVideosAddress;

    static boolean visualizeFeaturesAndGradient = false;
    static boolean visualizeAllWords = false;
    static Classes videoToVisualizeClass = Classes.BOXING;
    static int videoNumberToVisualizeInClass = 1;
    static int wordNumberInVideoToVisualize = 6;
    static int frameToShow = 1;

    static boolean trainTheModel = true;
    static boolean simpleEvaluation = false;
    static double trainPercent = 0.9;

    static boolean leaveOneOutEvaluation = true;

    static boolean checkPCAWords = false;

    static boolean checkKMeans = false;
    static int kmeansCenterWordNumber = 478;

    public static void main(String[] args) {
        if (extract_all_videos_gradient_data) {
            ReadData.saveAllVideosGradientData();
        }
        if (simpleEvaluation) {
            // TreeMap<Integer, Classes>[] datasets = ReadData
            // .randomSelect(trainPercent);
            TreeMap<Integer, Classes>[] datasets = ReadData
                    .randomSelect(trainPercent);
            TreeMap<Integer, Classes> trainSet = datasets[0];
            TreeMap<Integer, Classes> testSet = datasets[1];
            Model m = null;
            if (trainTheModel) {
                m = new BagOfWords();
                m.train(trainSet);
                m.save("bagOfWords.model");
            } else {
                m = (BagOfWords) (Model.load("bagOfWords.model"));
            }
            SimpleEvaluation evaluation = new SimpleEvaluation(m);
            double accuracy = evaluation.evaluate(testSet);
            ((BagOfWords) m).closeLogger();
            System.out.println(accuracy);
        }
        if (leaveOneOutEvaluation) {
            long curTime = System.currentTimeMillis();
            Model m = new BagOfWords();
            LeaveOneOutEvaluation evaluation = new LeaveOneOutEvaluation(m);
            for (double d : evaluation.getAccuracy()) {
                System.out.println(d);
            }
            System.out.println("The mean accuracy is: "
                    + evaluation.meanAccuracy());
            long finishTime = System.currentTimeMillis() - curTime;
            System.out.println("Time to finish: " + finishTime);
        }
        if (visualizeFeaturesAndGradient) {
            Matrix[] word;
            Matrix[] wordGradient;
            try {
                word = ReadData.readAWordOfVideo(videoToVisualizeClass,
                        videoNumberToVisualizeInClass,
                        wordNumberInVideoToVisualize);
                wordGradient = ReadData.readGradientDataOfVideoWord(
                        videoToVisualizeClass, videoNumberToVisualizeInClass,
                        wordNumberInVideoToVisualize);
                if (visualizeAllWords) {
                    DrawVideosDataPoint.drawAllWords(word);
                    DrawVideosDataPoint.drawAllWords(wordGradient);
                } else {
                    DrawVideosDataPoint.draw(word, frameToShow);
                    DrawVideosDataPoint.draw(wordGradient, frameToShow);
                }

                if (checkPCAWords) {
                    BagOfWords bagOfWords = (BagOfWords) (Model
                            .load("bagOfWords.model"));
                    PCA pca = bagOfWords.getPCA();
                    Matrix videoGradient;
                    try {
                        videoGradient = ReadData.readGradientDataOfAVideo(
                                videoToVisualizeClass,
                                videoNumberToVisualizeInClass);
                        Matrix videoGradientWord = videoGradient.getMatrix(
                                wordNumberInVideoToVisualize,
                                wordNumberInVideoToVisualize, 0, 1689);
                        Matrix transformedVideoGradientWord = pca
                                .transform(videoGradientWord);
                        Matrix[] videoGradientWordPicture = new Matrix[] { transformedVideoGradientWord };
                        DrawVideosDataPoint.draw(videoGradientWordPicture, 0);
                    } catch (NumberFormatException | IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (checkKMeans) {
            Matrix[] word;
            Matrix[] wordGradient;
            Matrix videoGradient;
            BagOfWords bagOfWords = (BagOfWords) (Model
                    .load("bagOfWords.model"));
            PCA pca = bagOfWords.getPCA();
            KMeans kMeans = bagOfWords.getKMeans();
            for (Classes c : Classes.values()) {
                for (int i = 1; i <= c.getNumberOfVideos(); i++) {
                    try {
                        videoGradient = ReadData.readGradientDataOfAVideo(c, i);
                        for (int j = 0; j < 200; j++) {
                            Matrix videoGradientWord = videoGradient.getMatrix(
                                    j, j, 0, 1689);
                            Matrix transformedVideoGradientWord = pca
                                    .transform(videoGradientWord);
                            int nearest = kMeans
                                    .findNearest(transformedVideoGradientWord);
                            System.out.println("Class: " + c.getName()
                                    + " Video: " + i + " nearest: " + nearest);
                            if (nearest == kmeansCenterWordNumber) {
                                word = ReadData.readAWordOfVideo(c, i, j + 1);
                                wordGradient = ReadData
                                        .readGradientDataOfVideoWord(c, i,
                                                j + 1);
                                JOptionPane.showMessageDialog(null, "class: "
                                        + c.getName() + " video: " + i
                                        + " word: " + (j + 1));
                                DrawVideosDataPoint.drawAllWords(word);
                                DrawVideosDataPoint.drawAllWords(wordGradient);
                            }
                        }
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
