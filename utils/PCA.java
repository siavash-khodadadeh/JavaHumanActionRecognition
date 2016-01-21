package utils;

import har.Constants;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;

import Jama.Matrix;
import Jama.SingularValueDecomposition;

public class PCA implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 972235087491858718L;
    public static String LOGGER_ADDRESS = "logs/pca/";
    private Matrix pcaVectors;
    private Matrix meanVector;
    private transient PrintWriter logger;

    // private Matrix minVector;
    // private Matrix maxVector;

    public PCA(Matrix data, int dimension) {
        File loggers = new File(LOGGER_ADDRESS);
        int counter = loggers.list().length;
        String loggerAddress = LOGGER_ADDRESS + counter
                + Constants.LOGGER_FILES_POSTFIX;
        try {
            logger = new PrintWriter(new FileOutputStream(new File(
                    loggerAddress)), true);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        logger.println("PCA Started.");
        logger.flush();

        long now = System.currentTimeMillis();

        preprocess(data);
        long timeForPreProcess = System.currentTimeMillis() - now;
        logger.println("Time for Pre Process: " + timeForPreProcess);

        now = System.currentTimeMillis();
        Matrix cov = covariance(data);
        long timeForCovariance = System.currentTimeMillis() - now;
        logger.println("Time for Covariance: " + timeForCovariance);

        now = System.currentTimeMillis();
        SingularValueDecomposition svd = cov.svd();
        long timeForSVD = System.currentTimeMillis() - now;
        logger.println("Time for SVD: " + timeForSVD);

        now = System.currentTimeMillis();
        this.pcaVectors = svd.getU().getMatrix(0, cov.getRowDimension() - 1, 0,
                dimension - 1);
        long timeForPCAVectors = System.currentTimeMillis() - now;
        logger.println("Time for PCA Vectors: " + timeForPCAVectors);

        logger.println("PCA Vectors: ");
        this.pcaVectors.print(logger, 4, 4);
        logger.println(Constants.LOGGER_SEPERATOR);
    }

    private void preprocess(Matrix data) {
        // Mean Normalization
        // Make sure every feature has exactly zero mean
        this.meanVector = new Matrix(1, data.getColumnDimension());
        // this.minVector = new Matrix(1, data.getColumnDimension());
        // this.maxVector = new Matrix(1, data.getColumnDimension());
        for (int i = 0; i < data.getRowDimension(); i++) {
            for (int j = 0; j < data.getColumnDimension(); j++) {
                meanVector.set(0, j, meanVector.get(0, j) + data.get(i, j));
            }
        }
        for (int j = 0; j < meanVector.getColumnDimension(); j++) {
            meanVector.set(0, j, meanVector.get(0, j) / data.getRowDimension());
        }
        // Feature Scaling
        // Scale features if they are on different scales to have comparable
        // range values
        // for (int i = 0; i < data.getRowDimension(); i++) {
        // for (int j = 0; j < data.getColumnDimension(); j++) {
        // if (data.get(i, j) < minVector.get(0, j)) {
        // minVector.set(0, j, data.get(i, j));
        // }
        // if (data.get(i, j) > maxVector.get(0, j)) {
        // maxVector.set(0, j, data.get(i, j));
        // }
        // }
        // }
    }

    public void normalize(Matrix data) {
        for (int i = 0; i < data.getRowDimension(); i++) {
            for (int j = 0; j < data.getColumnDimension(); j++) {
                data.set(i, j, data.get(i, j) - this.meanVector.get(0, j));
            }
        }
    }

    // public void scale(Matrix data) {
    // for (int i = 0; i < data.getRowDimension(); i++) {
    // for (int j = 0; j < data.getColumnDimension(); j++) {
    // double scaledValue = data.get(i, j) - this.minVector.get(0, j);
    // scaledValue = scaledValue
    // / (this.maxVector.get(0, j) - this.minVector.get(0, j));
    // data.set(i, j, scaledValue);
    // }
    // }
    // }

    public Matrix covariance(Matrix data) {
        Matrix sigma = new Matrix(data.getColumnDimension(),
                data.getColumnDimension());
        for (int i = 0; i < sigma.getRowDimension(); i++) {
            for (int j = i; j < sigma.getColumnDimension(); j++) {
                double value = 0;
                for (int k = 0; k < data.getColumnDimension(); k++) {
                    value += data.get(i, k) * data.get(j, k);
                }
                value = value / data.getRowDimension();
                sigma.set(i, j, value);
                sigma.set(j, i, value);
            }
        }
        return sigma;
    }

    public Matrix transform(Matrix data) {
        this.normalize(data);
        // this.scale(data);
        return new Matrix(data.times(this.pcaVectors).getArray());

    }

    public void closeLogger() {
        logger.println(Constants.LOGGER_SEPERATOR);
        logger.println("Closing PCA Logger");
        logger.println(Constants.LOGGER_SEPERATOR);
        logger.close();
    }

    public static void main(String[] args) {
        int N = 9;
        Matrix data = new Matrix(N, 2);
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < 2; j++) {
                data.set(i, j, i);
            }
        }
        PCA pca = new PCA(data, 1);
        for (int i = 0; i < N; i++) {
            Matrix transformedData = pca.transform(data.getMatrix(i, i, 0, 1));
            System.out.println(transformedData.get(0, 0));
        }
    }
}
