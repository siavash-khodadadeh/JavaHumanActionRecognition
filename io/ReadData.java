package io;

import har.Classes;
import har.Constants;
import image.ImageFilters;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Stream;

import utils.ArraysFunctions;
import utils.Utils;
import Jama.Matrix;

public class ReadData {

    private final static int VIDEO_DATA_NUMBER_OF_ROWS = 200;
    private final static int VIDEO_DATA_NUMBER_OF_COLUMNS = 1690;

    public static Matrix[] readAWordOfVideo(Classes classOfVideo,
            int numberOfVideo, int wordNumber) throws FileNotFoundException,
            IOException {
        String videoAddress = getVideoDataFileAddress(classOfVideo,
                numberOfVideo);
        return readAWordOfVideo(videoAddress, wordNumber);
    }

    public static Matrix[] readAWordOfVideo(String videoAddress, int wordNumber)
            throws FileNotFoundException, IOException {
        Matrix[] word = new Matrix[10];
        Path path = Paths.get(videoAddress);
        Stream<String> lines = null;
        try {
            lines = Files.lines(path);
            String line = lines.skip(wordNumber - 1).findFirst().get();
            String[] data = line.split("  ");
            double[][] frame = new double[13][13];
            int frameCounter = 0;
            int rowCounter = 0;
            int columnCounter = 0;
            for (int i = 1; i < data.length; i++) {
                frame[rowCounter][columnCounter] = Double.parseDouble(data[i]);
                columnCounter++;
                if (columnCounter == 13) {
                    columnCounter = 0;
                    rowCounter++;
                    if (rowCounter == 13) {
                        word[frameCounter] = new Matrix(frame);
                        rowCounter = 0;
                        frameCounter++;
                        frame = new double[13][13];
                    }
                }
            }
        } finally {
            if (lines != null)
                lines.close();
        }
        return word;
    }

    public static Matrix readDataOfAVideo(Classes classOfVideo,
            int numberOfVideo) throws FileNotFoundException, IOException {
        String videoAddress = getVideoDataFileAddress(classOfVideo,
                numberOfVideo);
        return readDataOfAVideo(videoAddress);
    }

    public static Matrix readDataOfAVideo(String videoAddress)
            throws FileNotFoundException, IOException {
        double[][] values = new double[VIDEO_DATA_NUMBER_OF_ROWS][VIDEO_DATA_NUMBER_OF_COLUMNS];
        int i = 0;
        int j = 0;
        BufferedReader in = new BufferedReader(new InputStreamReader(
                new FileInputStream(videoAddress)));
        String line = null;
        while ((line = in.readLine()) != null) {
            String[] words = line.split("  ");
            for (int k = 1; k < words.length; k++) {
                String word = words[k];
                values[i][j] = Double.parseDouble(word);
                j++;
                if (j == VIDEO_DATA_NUMBER_OF_COLUMNS) {
                    j = 0;
                    i++;
                }
            }
        }
        in.close();
        return new Matrix(values);
    }

    public static String getVideoDataFileAddress(Classes classOfVideo,
            int numberOfVideo) {
        return Constants.dataOfVideosAddress + classOfVideo.getAddress()
                + classOfVideo.getName() + "_" + numberOfVideo + ".table";
    }

    public static String getVideoGradientDataFileAddress(Classes classOfVideo,
            int numberOfVideo) {
        return Constants.allGradientDataOfVideosAddress
                + classOfVideo.getAddress() + classOfVideo.getName() + "_"
                + numberOfVideo + ".gradientdata";
    }

    public static void saveVideoGradientData(Classes classOfVideo,
            int numberOfVideo) throws FileNotFoundException, IOException {
        File gradientFile = new File(getVideoGradientDataFileAddress(
                classOfVideo, numberOfVideo));
        PrintWriter out = null;
        out = new PrintWriter(gradientFile);

        for (int w = 1; w <= 200; w++) {
            Matrix[] m = ImageFilters.sobel3D(readAWordOfVideo(classOfVideo,
                    numberOfVideo, w));
            for (int frame = 0; frame < m.length; frame++) {
                for (int x = 0; x < m[frame].getRowDimension(); x++) {
                    for (int y = 0; y < m[frame].getColumnDimension(); y++) {
                        if (x != 0 || y != 0 || frame != 0) {
                            out.print(" ");
                        }
                        out.print((int) m[frame].get(x, y));
                    }
                }
            }
            out.println();
        }
        out.close();
    }

    public static void saveAllVideosGradientData() {
        for (Classes c : Classes.values()) {
            for (int i = 1; i <= c.getNumberOfVideos(); i++) {
                System.out.println("Saving video: " + c.getName() + "_" + i);
                try {
                    saveVideoGradientData(c, i);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

    public static Matrix readAllVideosGradientData() {
        Matrix allVideosData = new Matrix(599 * 200, 1690);
        int videoCounter = 0;
        for (Classes c : Classes.values()) {
            for (int n = 1; n <= c.getNumberOfVideos(); n++) {
                System.out.println("Reading data of video: " + c.getName()
                        + "_" + n);
                try {
                    Matrix temp = readGradientDataOfAVideo(c, n);
                    for (int i = 0; i < temp.getRowDimension(); i++) {
                        for (int j = 0; j < temp.getColumnDimension(); j++) {
                            allVideosData.set(videoCounter * 200 + i, j,
                                    temp.get(i, j));
                        }
                    }
                    videoCounter++;
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return allVideosData;
    }

    public static Matrix readGradientDataOfAVideo(Classes videoClass,
            int videoNumber) throws FileNotFoundException, IOException {
        String gradientAddress = getVideoGradientDataFileAddress(videoClass,
                videoNumber);
        return readGradientDataOfAVideo(gradientAddress);
    }

    public static Matrix readGradientDataOfAVideo(String gradientAddress)
            throws NumberFormatException, IOException {
        double[][] videoGradientData = new double[200][1690];
        int row = 0, col = 0;
        File file = new File(gradientAddress);
        BufferedReader in = new BufferedReader(new InputStreamReader(
                new FileInputStream(file)));
        String line = null;
        while ((line = in.readLine()) != null) {
            String[] data = line.split(" ");
            for (int i = 0; i < data.length; i++) {
                videoGradientData[row][col] = Integer.parseInt(data[i]);
                col++;
                if (col == 1690) {
                    col = 0;
                    row++;
                }
            }
        }
        in.close();
        return new Matrix(videoGradientData);
    }

    public static Matrix[] readGradientDataOfVideoWord(Classes videoClass,
            int videoNumber, int wordNumber) throws NumberFormatException,
            IOException {
        String gradientAddress = getVideoGradientDataFileAddress(videoClass,
                videoNumber);
        return (readGradientDataOfVideoWord(gradientAddress, wordNumber));
    }

    public static Matrix[] readGradientDataOfVideoWord(String address,
            int wordNumber) throws NumberFormatException, IOException {
        wordNumber = wordNumber - 1;
        Matrix[] gradientWord = new Matrix[10];
        for (int i = 0; i < 10; i++) {
            gradientWord[i] = new Matrix(13, 13);
        }
        // int[][] videoGradientData = readGradientDataOfAVideo(address);
        Matrix videoGradientData = readGradientDataOfAVideo(address);
        Matrix temp = videoGradientData.getMatrix(wordNumber, wordNumber, 0,
                1689);
        int frame = 0, row = 0, col = 0;
        for (int i = 0; i < temp.getColumnDimension(); i++) {
            gradientWord[frame].set(row, col, temp.get(0, i));
            col++;
            if (col == 13) {
                row++;
                col = 0;
                if (row == 13) {
                    frame++;
                    row = 0;
                }
            }
        }
        return gradientWord;
    }

    public static Object[] getClassAndVideoNumber(int videoNumber) {
        // Returns an object[] of 2 elements the first one is and instance of
        // Classes and the second one is the video number in that class
        Object[] data = new Object[2];
        for (Classes c : Classes.values()) {
            if (videoNumber > c.getNumberOfVideos()) {
                videoNumber = videoNumber - c.getNumberOfVideos();
            } else {
                data[0] = c;
                data[1] = videoNumber;
                break;
            }
        }
        return data;
    }

    public static Matrix readDataOfSomeVideos(Set<Integer> videoNumbers) {
        Matrix data = new Matrix(videoNumbers.size() * 200, 1690);
        int videoCounter = 0;
        for (int videoNumber : videoNumbers) {
            System.out.println("Reading data of video: " + videoNumber);
            try {
                Matrix vidData = ReadData.readGradientDataOfAVideo(
                        Utils.getVideoClass(videoNumber),
                        Utils.getVideoNumberInClass(videoNumber));
                for (int i = 0; i < vidData.getRowDimension(); i++) {
                    for (int j = 0; j < vidData.getColumnDimension(); j++) {
                        data.set(videoCounter * 200 + i, j, vidData.get(i, j));
                    }
                }
                videoCounter++;
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return data;
    }

    public static TreeMap<Integer, Classes>[] randomSelect(double trainPercent) {
        TreeMap<Integer, Classes> train = new TreeMap<Integer, Classes>();
        TreeMap<Integer, Classes> test = new TreeMap<Integer, Classes>();
        int counter = 1;
        for (Classes c : Classes.values()) {
            int[] allClassVideos = new int[c.getNumberOfVideos()];
            for (int i = 0; i < c.getNumberOfVideos(); i++) {
                allClassVideos[i] = counter;
                counter++;
            }
            ArraysFunctions.shuffle(allClassVideos);
            int numberOfTrainVideos = (int) (c.getNumberOfVideos() * trainPercent);
            int numberOfTestVideos = c.getNumberOfVideos()
                    - numberOfTrainVideos;
            for (int i = 0; i < numberOfTrainVideos; i++) {
                train.put(allClassVideos[i], c);
            }

            for (int i = 0; i < numberOfTestVideos; i++) {
                test.put(allClassVideos[numberOfTrainVideos + i], c);
            }
        }
        return (TreeMap<Integer, Classes>[]) new TreeMap[] { train, test };
    }

    public static TreeMap<Integer, Classes>[] leaveOneOutSelect(int personNumber) {
        TreeMap<Integer, Classes> train = new TreeMap<Integer, Classes>();
        TreeMap<Integer, Classes> test = new TreeMap<Integer, Classes>();
        personNumber--;
        int trainCounter = 0;
        for (Classes c : Classes.values()) {
            // Check the case where class is handclapping and
            // personNumber >= 12
            for (int i = 1; i <= c.getNumberOfVideos(); i++) {
                int p = i;
                if (c == Classes.HANDCLAPPING && personNumber > 12) {
                    p = p - 1;
                }
                if (personNumber * 4 < i && i <= (personNumber + 1) * 4) {
                    test.put(trainCounter + p, c);
                } else {
                    train.put(trainCounter + p, c);
                }
            }

            if (c == Classes.HANDCLAPPING) {
                if (personNumber == 12) {
                    test.remove(152);
                    train.put(152, c);
                } else if (personNumber == 24) {
                    test.put(199, c);
                }
            }
            trainCounter += c.getNumberOfVideos();
        }
        return (TreeMap<Integer, Classes>[]) new TreeMap[] { train, test };
    }

    public static void main(String[] args) {
        TreeMap[] t = leaveOneOutSelect(25);
        for (Object item : t[1].keySet())
            System.out.println(item);
    }
    // public static void main(String[] args) {

    // try {
    // Matrix m = readDataOfAVideo(Classes.BOXING, 3);
    // System.out.println(m);
    // } catch (FileNotFoundException e) {
    // // TODO Auto-generated catch block
    // e.printStackTrace();
    // } catch (IOException e) {
    // // TODO Auto-generated catch block
    // e.printStackTrace();
    // }

    // try {
    // Matrix[] m = readAWordOfVideo(Classes.BOXING, 3, 3);
    // for (int i = 0; i < m.length; i++) {
    // System.out.println(m[i]);
    // }
    // } catch (IOException e) {
    // // TODO Auto-generated catch block
    // e.printStackTrace();
    // }

    // try {
    // saveAllVideosGradientData();
    // saveVideoGradientData(Classes.BOXING, 1);
    // } catch (IOException e) {
    // // // TODO Auto-generated catch block
    // e.printStackTrace();
    // }
    // saveAllVideosGradientData(new File(
    // Constants.dataAddress + "g.alldatagradients"));

    // try {
    // int[][] a = readGradientDataOfAVideo(Classes.BOXING, 1);
    // System.out.println(a[0][1]);
    // } catch (IOException e) {
    // // TODO Auto-generated catch block
    // e.printStackTrace();
    // }

    // try {
    // Matrix[] m = readGradientDataOfVideoWord(Classes.BOXING, 1, 0);
    // System.out.println(m[0].getData(0, 0));
    // } catch (NumberFormatException | IOException e) {
    // // TODO Auto-generated catch block
    // e.printStackTrace();
    // }
    // }
}
