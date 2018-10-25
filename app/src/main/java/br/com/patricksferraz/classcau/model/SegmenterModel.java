package br.com.patricksferraz.classcau.model;

import android.util.Log;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

/**
 * Esta classe contém funções de manipulação de dados relacionados a segmentação de imagens,
 * especificamente baseado em regiões de interesse (ROI)
 * @author  Patrick Silva Ferraz
 * @version 1.0
 */
public class SegmenterModel {

    // TODO: HACK: Inicialização e LOG do OPENCV
    private static final String TAG = "SegmenterModel";
    static {
        if (!OpenCVLoader.initDebug()) Log.d(TAG, "OpenCV NOT LOADED");
        else Log.d(TAG, "OpenCV LOADED");
    }

    /**
     * Segmenta uma imagem com base nas regiões de interesse
     * @param src_image Imagem de entrada
     * @param roi       Lista com as regiões de interesse
     * @param dst_image Lista com as imagens segmentadas pelas regiões de interesse
     */
    public static void segment(Mat src_image, List<Rect> roi, List<Mat> dst_image) {
        Mat roi_image;  // imagem obtida da roi
        Mat aux         = new Mat(); // imagem auxiliar
        Mat bw_image    = new Mat(); // imagem preto & branco
        Mat gs_image    = new Mat(); // imagem escala de cinza
        Mat canny_image = new Mat(); // imagem após detecção de bordas
        Mat result      = new Mat(); // imagem segmentada resultante

        Mat res2 = new Mat(), res3 = new Mat(); // TODO: REMOVE: Verificar processos duplicados
        String caminho_res = "";

        List<MatOfPoint> contours = new ArrayList<>(); // Contornos da imagem
        dst_image = new ArrayList<Mat>(roi.size()); // Definindo quantidade de imgs segmentadas

        // Iteração para segmentar src_image com base nas ROI
        for (Rect r : roi) {
            // Destaca a roi
            roi_image = src_image.submat(r);
            // Converte roi para escala de cinza
            Imgproc.cvtColor(roi_image, aux, Imgproc.COLOR_BGR2GRAY);
            Imgproc.GaussianBlur(aux, aux, new Size(5,5), 0);
            // Converte imagem para preto & branco
            Imgproc.threshold(aux, bw_image, 0, 255, Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU);
            // Erosão da imagem
            Imgproc.erode(bw_image, bw_image, new Mat(), new Point(-1,-1), 3);
            // Dilatação da imagem
            Imgproc.dilate(bw_image, bw_image, new Mat(), new Point(-1,-1), 3);
            // Converte imagem p/ escala de cinza
            Imgproc.threshold(bw_image, gs_image, 1, 128, 1);
            // Detecta bordas com canny
            Imgproc.Canny(gs_image, canny_image, 255, 255);

            Imgproc.findContours(canny_image, contours, new Mat(), Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
            //Core.convertScaleAbs(bg, m);
            Imgproc.threshold(gs_image, aux, 0, 255, Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU);
            Core.bitwise_not(aux, bw_image);
            Core.bitwise_and(roi_image, roi_image, result, aux);
            Core.bitwise_and(roi_image, roi_image, res2, bw_image);
            Core.addWeighted(result, 1, res2, 1, 0, res3);
            Imgproc.drawContours(res3, contours, -1, new Scalar(0,255,0), 1);

            Core.convertScaleAbs(bw_image, aux);
            Imgproc.threshold(aux, aux, 0, 255, Imgproc.THRESH_BINARY_INV + Imgproc.THRESH_OTSU);
            Core.bitwise_and(roi_image, roi_image, result, aux);
            Core.inRange(result, new Scalar(0,0,0), new Scalar(0,0,0), aux);
            // Convertendo para padrão de cor c/transparencia
            Imgproc.cvtColor(result, result, Imgproc.COLOR_BGR2BGRA);
            // Aplicando transparencia aos locais brancos
            result.setTo(new Scalar(255,255,255,0), aux);

            // Salvado res
            Imgcodecs.imwrite(caminho_res+"x"+"["+r.x+"_"+r.y+"]"+".png", result);

            dst_image.add(result);

            result.release();
            res2.release();
            res3.release();
            contours.clear();
        }
    }
}
