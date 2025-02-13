package com.example.siftsocket;
import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.*;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.ByteArrayOutputStream;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;
    private static final String SERVER_IP = "192.168.18.13";
    private static final int SERVER_PORT = 5005;
    private int frameCount = 0;
    private long startTime;
    private PreviewView previewView; // PreviewView para mostrar la cámara


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        previewView = findViewById(R.id.previewView); // Obtén el PreviewView


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST_CODE);
        } else {
            startCamera();
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                // Crear y configurar el Preview
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider()); // Vincula el preview al PreviewView

                // Crear y configurar el ImageAnalysis
                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                imageAnalysis.setAnalyzer(Executors.newSingleThreadExecutor(), image -> {
                    if (startTime == 0) startTime = System.currentTimeMillis();
                    frameCount++;
                    if (frameCount % 30 == 0) {
                        long elapsed = (System.currentTimeMillis() - startTime) / 1000;
                        Log.d("UDP_STREAM", "Frames enviados: " + frameCount + ", Tiempo: " + elapsed + "s, FPS: " + (frameCount / (float)elapsed));
                    }
                    sendFrame(imageToByteArray(image));
                    image.close();
                });

                // Seleccionar la cámara trasera
                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build();

                // Vincula los componentes de la cámara a la actividad
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);
            } catch (Exception e) {
                Toast.makeText(this, "Error al iniciar cámara", Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private byte[] imageToByteArray(ImageProxy image) {
        // Convertir a Bitmap (con color y rotación)
        Bitmap bitmap = imageToBitmap(image);

        // Redimensionar el Bitmap
        Bitmap resizedBitmap = resizeBitmap(bitmap, 640, 480); // Ajusta el tamaño según sea necesario

        // Corregir la rotación
        Bitmap rotatedBitmap = rotateBitmap(resizedBitmap, image.getImageInfo().getRotationDegrees());

        // Comprimir para enviar
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 85, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    private Bitmap resizeBitmap(Bitmap source, int maxWidth, int maxHeight) {
        int width = source.getWidth();
        int height = source.getHeight();

        float ratioBitmap = (float) width / (float) height;
        float ratioMax = (float) maxWidth / (float) maxHeight;

        int finalWidth = maxWidth;
        int finalHeight = maxHeight;
        if (ratioMax > ratioBitmap) {
            finalWidth = (int) ((float) maxHeight * ratioBitmap);
        } else {
            finalHeight = (int) ((float) maxWidth / ratioBitmap);
        }

        return Bitmap.createScaledBitmap(source, finalWidth, finalHeight, true);
    }

    private Bitmap yuvToRgb(ImageProxy image) {
        ImageProxy.PlaneProxy[] planes = image.getPlanes();
        ByteBuffer yBuffer = planes[0].getBuffer();
        ByteBuffer uBuffer = planes[1].getBuffer();
        ByteBuffer vBuffer = planes[2].getBuffer();

        int ySize = yBuffer.remaining();
        int uSize = uBuffer.remaining();
        int vSize = vBuffer.remaining();

        byte[] nv21 = new byte[ySize + uSize + vSize];
        yBuffer.get(nv21, 0, ySize);
        vBuffer.get(nv21, ySize, vSize);
        uBuffer.get(nv21, ySize + vSize, uSize);

        YuvImage yuvImage = new YuvImage(nv21, ImageFormat.NV21, image.getWidth(), image.getHeight(), null);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        yuvImage.compressToJpeg(new Rect(0, 0, image.getWidth(), image.getHeight()), 85, out);
        byte[] jpegBytes = out.toByteArray();

        return BitmapFactory.decodeByteArray(jpegBytes, 0, jpegBytes.length);
    }

    private Bitmap imageToBitmap(ImageProxy image) {
        return yuvToRgb(image);
    }

    // Rotar el bitmap según la orientación de la cámara
    private Bitmap rotateBitmap(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }


    private void sendFrame(byte[] frameData) {
        try (DatagramSocket socket = new DatagramSocket()) {
            InetAddress serverAddress = InetAddress.getByName(SERVER_IP);
            DatagramPacket packet = new DatagramPacket(frameData, frameData.length, serverAddress, SERVER_PORT);
            socket.send(packet);
            Log.d("UDP_STREAM", "Paquete enviado de " + frameData.length + " bytes");
        } catch (Exception e) {
            Log.e("UDP_STREAM", "Error al enviar frame: " + e.getMessage());
        }
    }

}
