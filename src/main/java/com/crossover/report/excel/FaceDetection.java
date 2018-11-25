package com.crossover.report.excel;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;

public class FaceDetection {

    public static boolean hasFace(String folderName, String fileName,  String url) {
        MatOfRect faceDetections = null;
        MatOfRect eyesDetections = null;
        try {

            // For proper execution of native libraries
            // Core.NATIVE_LIBRARY_NAME must be loaded before
            // calling any of the opencv methods
            nu.pattern.OpenCV.loadShared();

            // Face detector creation by loading source cascade xml file
            // using CascadeClassifier.
            // the file can be downloade from
            // https://github.com/opencv/opencv/blob/master/data/haarcascades/
            // haarcascade_frontalface_alt.xml
            // and must be placed in same directory of the source java file
            CascadeClassifier faceDetector = new CascadeClassifier();
            CascadeClassifier eyeDetector = new CascadeClassifier();
            faceDetector.load("haarcascade_frontalface_alt.xml");
            eyeDetector.load("haarcascade_eye.xml");
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            byte[] buffer = new byte[10 * 1024];
            HttpURLConnection con = (HttpURLConnection)new URL(url).openConnection();
            if(con.getResponseCode() != 200 ) {
                return true;
            }
            InputStream in = con.getInputStream();

            int read = -1;

            while ((read = in.read(buffer)) != -1) {
                bytes.write(buffer, 0, read);
            }
            DataOutputStream writer = new DataOutputStream(new FileOutputStream(new File("img.jpg")));
            writer.write(bytes.toByteArray());
            writer.close();



            BufferedImage baseImage = ImageIO.read(new File("img.jpg"));

            byte[] data = ((DataBufferByte) baseImage.getRaster().getDataBuffer()).getData();
            Mat mat = new Mat(baseImage.getHeight(), baseImage.getWidth(), CvType.CV_8UC3);
            mat.put(0, 0, data);

            Mat mat1 = new Mat(baseImage.getHeight(),baseImage.getWidth(),CvType.CV_8UC1);
            Imgproc.cvtColor(mat, mat1, Imgproc.COLOR_RGB2GRAY);

            byte[] data1 = new byte[mat1.rows() * mat1.cols() * (int)(mat1.elemSize())];
            mat1.get(0, 0, data1);

            // Detecting faces
            faceDetections = new MatOfRect();
            eyesDetections = new MatOfRect();
            faceDetector.detectMultiScale(mat1, faceDetections);
            eyeDetector.detectMultiScale(mat1, eyesDetections);
            if(faceDetections.toArray().length == 0 && eyesDetections.toArray().length == 0) {
                if(!new File("suspected-files/"+folderName+"/").exists()) {
                    new File("suspected-files/"+folderName+"/").mkdirs();
                }
                Files.copy(new File("img.jpg").toPath(), new File("suspected-files/"+folderName+"/"+fileName+".jpg").toPath());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
        return faceDetections.toArray().length > 0 || eyesDetections.toArray().length > 0;
    }

    public static void main(String[] args) throws Exception {
        System.out.println(hasFace("","", "https://devfactory-client-prod-bucket.s3.amazonaws .com/webcamshot/21296/2018-11-19/08-38-48.jpg?AWSAccessKeyId=AKIAIHDFPWMHKDA2ZCIA&Expires=1542894632&Signature=umW%2Fbrhki%2B8ElE8gXn2Qc28POKY%3D"));
    }
}
