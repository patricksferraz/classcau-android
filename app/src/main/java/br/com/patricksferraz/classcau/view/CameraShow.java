package br.com.patricksferraz.classcau.view;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import br.com.patricksferraz.classcau.R;

/**
 * TODO: NOTE: Informações úteis sobre API camera
 * - TextureView p/ camera preview (camera_show.xml)
 * - android:windowTranslucentStatus p/ utilizar todo espaço da tela (styles.xml)
 * - requestPermissions p/ utilizar a camera
 * - cameraFacing p/ setar a camera // NÃO UTILIZADO
 * - surfaceTextureListener p/ configurar a camera preview
 */

public class CameraShow extends AppCompatActivity {

    // Constantes
    static final double PROPORTION_TABLET_CAMERA_HEIGHT = 0.62;

    // Componentes da interface
    private Button btnCapture;
    private TextureView textureView;
    private RelativeLayout layoutCamera;

    // Camera, estado e ações
    private CameraDevice cameraDevice;
    private CameraDevice.StateCallback stateCallBack;
    private TextureView.SurfaceTextureListener textureListener;

    // Permissão de uso e informações da camera
    private static final int REQUEST_CAMERA_PERMISSION = 0;
    private String cameraId;
    private Size imageDimension;
    private CameraCaptureSession cameraCaptureSessions;
    private CaptureRequest.Builder captureRequestBuilder;
    private boolean mFlashSupported;

    // Para executar a câmera fora da thread principal
    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundThread;

    // Salvar arquivo
    private File file;

    // Checando orientação para imagem de saída
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    // Moldura da câmera
    private CanvasShow cameraFrame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_show);

        stateCallBack = initStateCallBack();
        textureListener = initSurfaceTextureListener();

        textureView = (TextureView)findViewById(R.id.textureView);
        assert textureView != null;
        textureView.setSurfaceTextureListener(textureListener);

        // Desenhando a moldura na câmera
        cameraFrame = new CanvasShow(this);
        layoutCamera = (RelativeLayout) findViewById(R.id.layoutCamera);
        layoutCamera.addView(cameraFrame);

        btnCapture = (Button)findViewById(R.id.btnCapture);
        btnCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takePicture();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Você não pode usar a câmera sem permissão", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    /**
     * Método executado com o restart da activity
     */
    @Override
    protected void onResume() {
        super.onResume();
        // Inicia uma nova thread
        startBackgroundThread();
        // Verifica a disponibilidade da textureView
        if (textureView.isAvailable()) openCamera();
        else textureView.setSurfaceTextureListener(textureListener);
    }

    /**
     * Método executado para evitar vazamento de memória
     */
    @Override
    protected void onPause() {
        super.onPause();
        stopCamera(); // TODO: NOTE: Adicionado após a camera está pronta
        stopBackgroundThread();
    }

    /**
     * Método para indicar o estado da camera (quando é aberta e quando não conseguiu abrir)
     * @return
     */
    private CameraDevice.StateCallback initStateCallBack() {
        return new CameraDevice.StateCallback() {
            // Camera aberta
            @Override
            public void onOpened(@NonNull CameraDevice camera) {
                cameraDevice = camera;
                createCameraPreview();
            }
            // Camera desconectada
            @Override
            public void onDisconnected(@NonNull CameraDevice cameraDevice) {
                cameraDevice.close();
            }
            // Camera fechada devido algum erro
            @Override
            public void onError(@NonNull CameraDevice cameraDevice, int i) {
                cameraDevice.close();
                cameraDevice = null;
            }
        };
    }

    /**
     * Método para a previsualização da camera
     * @return
     */
    private TextureView.SurfaceTextureListener initSurfaceTextureListener() {
        return new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
                openCamera();
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

            }
        };
    }

    /**
     * Método para abetura da camera
     */
    private void openCamera() {

        // Capturando as cameras
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        try {
            // Identificando a camera de escolha (Traseira)
            cameraId = manager.getCameraIdList()[0];
            // Recuperar as caracteristicas da camera
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            // Recuperar informações sobre os tamanhos de visualização
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            assert map != null;
            // Retorna uma matriz de objetos do tipo Size preparados para classe SurfaceTexture
            imageDimension = map.getOutputSizes(SurfaceTexture.class)[0];

            // Checkando permissão em tempo de execução (API 23 +)
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[] {
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                }, REQUEST_CAMERA_PERMISSION);
                return;
            }

            // Executando a camera com as configurações de escolha
            manager.openCamera(cameraId, stateCallBack, null);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Método para finalizar a camera
     */
    private void stopCamera() {
        if (cameraCaptureSessions != null) {
            cameraCaptureSessions.close();
            cameraCaptureSessions = null;
        }
        if (cameraDevice != null) {
            cameraDevice.close();
            cameraDevice = null;
        }
    }

    /**
     * Método para capturar uma imagem
     */
    private void takePicture() {
        if (cameraDevice == null)
            return;

        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        try {
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraDevice.getId());
            Size[] jpegSizes = null;

            if (characteristics != null)
                jpegSizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                        .getOutputSizes(ImageFormat.JPEG);

            //Capture image with custom size
            int width = 640;
            int height = 480;

            if (jpegSizes != null && jpegSizes.length > 0) {
                width = jpegSizes[0].getWidth();
                height = jpegSizes[0].getHeight();
            }

            final ImageReader reader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1);
            List<Surface> outputSurface = new ArrayList<>(2);
            outputSurface.add(reader.getSurface());
            outputSurface.add(new Surface(textureView.getSurfaceTexture()));

            final CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(reader.getSurface());
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);

            //Check orientation base on device
            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));

            //Environment.getExternalStorageDirectory()
            file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/" + UUID.randomUUID().toString() + ".jpg");
            ImageReader.OnImageAvailableListener readerListener = new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader imageReader) {
                    Image image = null;
                    try {
                        image = reader.acquireLatestImage();
                        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                        byte[] bytes = new byte[buffer.capacity()];
                        buffer.get(bytes);
                        save(bytes);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if (image != null)
                            image.close();
                    }
                }
                private void save(byte[] bytes) throws IOException {
                    OutputStream outputStream = null;
                    try {
                        outputStream = new FileOutputStream(file);
                        outputStream.write(bytes);
                    } finally {
                        if (outputStream != null)
                            outputStream.close();
                    }
                }
            };

            reader.setOnImageAvailableListener(readerListener, mBackgroundHandler);
            final CameraCaptureSession.CaptureCallback captureListener = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);
                    Toast.makeText(CameraShow.this, "Salvo " + file, Toast.LENGTH_SHORT).show();
                    createCameraPreview();
                }
            };

            cameraDevice.createCaptureSession(outputSurface, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    try {
                        cameraCaptureSession.capture(captureBuilder.build(), captureListener, mBackgroundHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {

                }
            }, mBackgroundHandler);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Método para criação de uma camera preview
     */
    private void createCameraPreview() {
        try {
            // Principal classe por traś do textureView
            SurfaceTexture texture = textureView.getSurfaceTexture();
            assert texture != null;
            // Definindo os limites de exibição
            texture.setDefaultBufferSize(imageDimension.getWidth(), imageDimension.getHeight());
            Surface surface = new Surface(texture);
            // Especificação do modelo e destino (objeto Surface)
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);

            // Criando uma sessão de captura
            cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    if (cameraDevice == null)
                        return;
                    cameraCaptureSessions = cameraCaptureSession;
                    updatePreview();
                }
                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Toast.makeText(CameraShow.this, "Falhou", Toast.LENGTH_SHORT).show();
                }
            }, null);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Método para atualização da //camera preview
     */
    private void updatePreview() {
        if (cameraDevice == null)
            Toast.makeText(this, "Erro", Toast.LENGTH_SHORT).show();
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO);

        try {
            cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(), null, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Método para finalizar a thread de forma segura
     */
    private void stopBackgroundThread() {
        // TODO: REMOVE: REMOVE BEFORE
//        if (backgroundHandler != null) {
//            backgroundThread.quitSafely();
//            backgroundThread = null;
//            backgroundHandler = null;
//        }
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Método para iniciar uma thread em segundo plano
     */
    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("Camera Background");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }
}
