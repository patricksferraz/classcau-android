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

        // Matrizes
        Mat roi_image;  // matriz obtida da roi
        Mat aux         = new Mat(); // matriz auxiliar
        Mat bw_image    = new Mat(); // matriz preto & branco
        Mat canny_image = new Mat(); // matriz após detecção de bordas
        Mat result      = new Mat(); // matriz segmentada resultante
        Mat empty       = new Mat(); // matriz vazia

        // Others
        Scalar transparency = new Scalar(255,255,255,0);  // transparência para imagens bitmap
        Point ed_point      = new Point(-1,-1);     // ponto p/ erosão e dilatação
        Size s_blur         = new Size(5,5); // tamanho para aplicação do desfoque
        List<MatOfPoint> contours = new ArrayList<>();    // contornos da imagem
        dst_image = new ArrayList<Mat>(roi.size());       // espaço p/ imgs segmentadas

        // Iteração para segmentar src_image com base nas ROI
        for (Rect r : roi) {
            // Destaca a roi
            roi_image = src_image.submat(r);
            // Converte roi para escala de cinza
            Imgproc.cvtColor(roi_image, aux, Imgproc.COLOR_BGR2GRAY);
            // Desfoque da imagem
            Imgproc.GaussianBlur(aux, aux, s_blur, 0);
            // Converte imagem para preto & branco
            Imgproc.threshold(aux, bw_image, 0, 255, Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU);
            // Erosão da imagem
            Imgproc.erode(bw_image, bw_image, empty, ed_point, 3);
            empty.release();
            // Dilatação da imagem
            Imgproc.dilate(bw_image, aux, empty, ed_point, 3);
            empty.release();
            // Inverte Black <=> White
            Core.bitwise_not(aux, bw_image);
            // Detecta bordas com canny
            Imgproc.Canny(bw_image, canny_image, 255, 255);
            // Aplica um "recorte" na imagem original utilizando c imagem preto e branco como mask
            Core.bitwise_and(roi_image, roi_image, result, bw_image);
            // Convertendo para padrão de cor c/transparencia
            Imgproc.cvtColor(result, result, Imgproc.COLOR_BGR2BGRA);
            // Aplicando transparencia aos locais brancos
            result.setTo(transparency, aux);

            // Armazenando imagem segmentada na Lista
            dst_image.add(result);

            result.release();
            contours.clear();
        }
    }
}
