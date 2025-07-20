//package com.antonioteca.cc42.facerecognition;
//
//import android.app.Dialog;
//import android.content.Context;
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.media.Image;
//import android.os.Bundle;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ImageButton;
//import android.widget.ProgressBar;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.camera.core.Camera;
//import androidx.camera.core.CameraSelector;
//import androidx.camera.core.ImageAnalysis;
//import androidx.camera.core.Preview;
//import androidx.camera.lifecycle.ProcessCameraProvider;
//import androidx.camera.view.PreviewView;
//import androidx.core.content.ContextCompat;
//import androidx.fragment.app.DialogFragment;
//import androidx.lifecycle.ViewModelProvider;
//
//import com.antonioteca.cc42.R;
//import com.antonioteca.cc42.utility.FaceDetectionHelper;
//import com.antonioteca.cc42.utility.FaceRecognitionFirebaseUtils;
//import com.antonioteca.cc42.utility.FaceRecognitionHelper;
//import com.antonioteca.cc42.utility.Util;
//import com.antonioteca.cc42.viewmodel.SharedViewModel;
//import com.google.common.util.concurrent.ListenableFuture;
//import com.google.firebase.database.DataSnapshot;
//import com.google.firebase.database.DatabaseError;
//import com.google.firebase.database.ValueEventListener;
//import com.google.zxing.client.android.BeepManager;
//
//import java.io.IOException;
//import java.nio.ByteBuffer;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.concurrent.ExecutionException;
//
//
//public class FaceRecognitionFragment extends DialogFragment {
//
//    List<String> userIds;
//    private Camera camera;
//    private Context context;
//    private float[][] embeddings;
//    private BeepManager beepManager;
//    private ProgressBar progressBar;
//    private PreviewView previewView;
//    private TextView descriptionText;
//    private List<Object> embeddinList;
//    private ImageButton flashToggleButton;
//    private SharedViewModel sharedViewModel;
//    private FaceDetectionHelper faceDetectionHelper;
//    private FaceRecognitionFirebaseUtils firebaseUtils;
//    private FaceRecognitionHelper faceRecognitionHelper;
//    private boolean captured = false;
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setStyle(STYLE_NORMAL, android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar);
//        context = requireContext();
//        beepManager = new BeepManager(requireActivity());
//        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
//        FaceRecognitionFragmentArgs args = FaceRecognitionFragmentArgs.fromBundle(getArguments());
//        String campusId = args.getCampusId();
//        String cursusId = args.getCursusId();
//        int cameraId = args.getCameraId();
//        boolean isRegister = args.getRegister();
//
//        faceDetectionHelper = new FaceDetectionHelper();
//        firebaseUtils = new FaceRecognitionFirebaseUtils(campusId, cursusId);
//
//        try {
//            faceRecognitionHelper = new FaceRecognitionHelper(context);
//        } catch (IOException e) {
//            Toast.makeText(context, "Erro ao carregar modelo TFLite", Toast.LENGTH_SHORT).show();
//        }
//        if (!isRegister) {
//            userIds = new ArrayList<>();
//            embeddinList = new ArrayList<>();
//            firebaseUtils.getAllEmbeddings(new ValueEventListener() {
//                @Override
//                public void onDataChange(@NonNull DataSnapshot snapshot) {
//                    if (snapshot.exists()) {
//                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
//                            Object value = dataSnapshot.getValue();
//                            String key = dataSnapshot.getKey();
//                            if (key != null && dataSnapshot.hasChild("embedding")) {
//                                userIds.add(key);
//                                embeddinList.add(value);
//                            }
//                        }
//                        embeddings = firebaseUtils.convertListForEmbedding(embeddinList);
//                        initCamera(false, cameraId);
//                    } else {
//                        Util.showAlertDialogBuild(context.getString(R.string.err), "Vectores não carregados, verifique se os estudantes efectuaram os registos do Face ID", context, null);
//                    }
//                }
//
//                @Override
//                public void onCancelled(@NonNull DatabaseError error) {
//                    Util.showAlertDialogBuild(context.getString(R.string.err), error.getMessage(), context, null);
//                }
//            });
//        } else
//            initCamera(true, cameraId);
//    }
//
//    @Nullable
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        View view = inflater.inflate(R.layout.fragment_face_recognition, container, false);
//        previewView = view.findViewById(R.id.previewView);
//        descriptionText = view.findViewById(R.id.descriptionText);
//        flashToggleButton = view.findViewById(R.id.flashToggleButton);
//        progressBar = view.findViewById(R.id.progressBar);
//        return view;
//    }
//
//    @NonNull
//    @Override
//    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
//        return super.onCreateDialog(savedInstanceState);
//    }
//
//    @Override
//    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
//        super.onViewCreated(view, savedInstanceState);
//
//        final boolean[] flashOn = {false};
//        flashToggleButton.setOnClickListener(v -> {
//            if (camera.getCameraInfo().hasFlashUnit()) {
//                flashOn[0] = !flashOn[0];
//                camera.getCameraControl().enableTorch(flashOn[0]);
//                flashToggleButton.setImageResource(flashOn[0] ? R.drawable.baseline_flashlight_on_24 : R.drawable.baseline_flashlight_off_24);
//            }
//        });
//
//        sharedViewModel.getUserFaceIdContinueCaptureLiveData().observe(getViewLifecycleOwner(), aBoolean -> {
//            if (aBoolean) {
//                captured = false;
//            }
//        });
//    }
//
//    private void initCamera(boolean isRegister, int cameraId) {
//        // Initialize the camera
//        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(context);
//
//        cameraProviderFuture.addListener(() -> { // Listener para obter o ProcessCameraProvider
//            try {
//                ProcessCameraProvider cameraProvider = cameraProviderFuture.get(); // Obter o ProcessCameraProvider
//                bindPreviewAndAnalysis(cameraProvider, isRegister, cameraId); // Associar a visualização da câmera e o analisador de imagem
//            } catch (InterruptedException | ExecutionException e) {
//                Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
//            }
//        }, ContextCompat.getMainExecutor(context));
//    }
//
//    private void bindPreviewAndAnalysis(@NonNull ProcessCameraProvider cameraProvider, boolean isRegister, int cameraId) {
//        cameraProvider.unbindAll(); // Desvincular todas as câmeras antes de vincular novamente
//
//        CameraSelector cameraSelector = cameraId == 0 ? CameraSelector.DEFAULT_BACK_CAMERA : CameraSelector.DEFAULT_FRONT_CAMERA; // Selecionar a câmera
//
//        Preview preview = new Preview.Builder().build(); // Configurar a visualização da câmera
//        preview.setSurfaceProvider(previewView.getSurfaceProvider()); // Associar a visualização da câmera ao SurfaceProvider
//
//        // Configurar o analisador de imagem - Permite acesso directo aos frames da câmera, em tempo real
//        ImageAnalysis analysis = new ImageAnalysis.Builder()
//                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST) // Usar a estratégia de mantê-lo apenas com o último frame
//                .build();
//
//        // Configurar o analisador de imagem para detectar rostos
//        analysis.setAnalyzer(ContextCompat.getMainExecutor(context), imageProxy -> {
//            if (captured) { // Se já foi capturado, não fazer nada
//                imageProxy.close(); // Fechar a imagem
//                return;
//            }
//
//            faceDetectionHelper.detectAndCropFace(imageProxy, new FaceDetectionHelper.OnFaceCroppedListener() { // Detectar rosto
//                @Override
//                public void onFaceCropped(Bitmap faceBitmap) {
//                    captured = true;
//                    float[] embedding = faceRecognitionHelper.getFaceEmbedding(faceBitmap); // Obter o embedding do rosto
//
//                    if (isRegister) {
//                        beepManager.playBeepSoundAndVibrate();
//                        Util.showImageDialog(context, "Face ID Detectado", faceBitmap, () -> captured = false, userId -> {
//                            progressBar.setVisibility(View.VISIBLE);
//                            firebaseUtils.saveEmbedding(userId, embedding, sharedViewModel, progressBar, context);
//                        });
//                        imageProxy.close();
//                    } else {
//                        int matchIndex = FaceRecognitionHelper.matchSIMD(embedding, embeddings, 0.5f);
//                        if (matchIndex >= 0) {
//                            sharedViewModel.setUserFaceIdLiveData(userIds.get(matchIndex));
//                        } else {
//                            descriptionText.setText("Rosto não reconhecido");
//                            captured = false;
//                        }
//                    }
//                    imageProxy.close();
//                }
//
//                @Override
//                public void onFailure(Exception e) {
//                    descriptionText.setText(e.getMessage());
//                    imageProxy.close();
//                }
//            });
//        });
//        // Vincular as câmeras ao ProcessCameraProvider
//        camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, analysis);
//    }
//
//    // Usado para converter image do tipo .jpg, .jpeg, .png para Bitmap
//    private Bitmap toBitmap(@NonNull Image image) {
//        Image.Plane[] planes = image.getPlanes();
//        ByteBuffer buffer = planes[0].getBuffer();
//        byte[] bytes = new byte[buffer.remaining()];
//        buffer.get(bytes);
//        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
//    }
//
//    @Override
//    public void onStart() {
//        super.onStart();
//        if (getDialog() != null && getDialog().getWindow() != null) {
//            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
//        }
//    }
//}