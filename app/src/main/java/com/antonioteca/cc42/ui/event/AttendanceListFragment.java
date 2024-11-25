package com.antonioteca.cc42.ui.event;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.antonioteca.cc42.R;
import com.antonioteca.cc42.dao.daofarebase.DaoEventFirebase;
import com.antonioteca.cc42.databinding.FragmentAttendanceListBinding;
import com.antonioteca.cc42.network.FirebaseDataBaseInstance;
import com.antonioteca.cc42.utility.Util;
import com.antonioteca.cc42.viewmodel.SharedViewModel;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.FirebaseDatabase;
import com.google.zxing.ResultPoint;
import com.google.zxing.client.android.BeepManager;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.List;

public class AttendanceListFragment extends Fragment {

    private Long eventId;
    private Context context;
    private Activity activity;
    private Integer cameraId;
    private String resultQrCode;
    private View inflatedViewStub;
    private BeepManager beepManager;
    private ScanOptions scanOptions;
    private SharedViewModel sharedViewModel;
    private FirebaseDatabase firebaseDatabase;
    private ProgressBar progressBarMarkAttendance;
    private FragmentAttendanceListBinding binding;
    private DecoratedBarcodeView decoratedBarcodeView;

    private final ActivityResultLauncher<String> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            result -> {
                if (result)
                    openCamera();
                else
                    Util.showAlertDialogBuild(getString(R.string.err), getString(R.string.msg_permis_camera_denied), context, null);
            });

    private final BarcodeCallback callback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            if (result.getText().equals(resultQrCode)) {
                String message = context.getString(R.string.msg_you_already_mark_attendance_event) + ".";
                Util.showAlertDialogMessage(context, getLayoutInflater(), context.getString(R.string.warning), message, "#FDD835");
            } else {
                if (result.getText().startsWith("cc42user")) {
                    Util.setVisibleProgressBar(progressBarMarkAttendance, binding.fabOpenCameraScannerQrCodeBack, sharedViewModel);
                    resultQrCode = result.getText();
                    resultQrCode = resultQrCode.replace("cc42user", "");
                    String[] parts = resultQrCode.split("#", 5);
                    if (parts.length == 5) {
                        DaoEventFirebase.markAttendance(
                                firebaseDatabase,
                                String.valueOf(eventId),
                                parts[0],
                                parts[1],
                                parts[2],
                                parts[3],
                                parts[4],
                                context,
                                getLayoutInflater(),
                                progressBarMarkAttendance,
                                binding.fabOpenCameraScannerQrCodeBack,
                                sharedViewModel
                        );
                        resultQrCode = result.getText();
                        beepManager.playBeepSoundAndVibrate();
                        Util.startVibration(context);
                    }
                } else
                    Toast.makeText(context, R.string.msg_qr_code_invalid, Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void possibleResultPoints(List<ResultPoint> resultPoints) {
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = requireContext();
        activity = requireActivity();
        scanOptions = new ScanOptions();
        scanOptions.setBeepEnabled(true);
        scanOptions.setOrientationLocked(false);
        scanOptions.setDesiredBarcodeFormats(ScanOptions.QR_CODE);
        scanOptions.setPrompt(getString(R.string.align_camera_qr_code));
        beepManager = new BeepManager(activity);
        firebaseDatabase = FirebaseDataBaseInstance.getInstance().database;
        sharedViewModel = new ViewModelProvider(this).get(SharedViewModel.class);
        eventId = AttendanceListFragmentArgs.fromBundle(requireArguments()).getEventId();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAttendanceListBinding.inflate(inflater, container, false);
        inflatedViewStub = binding.viewStub.inflate();
        decoratedBarcodeView = inflatedViewStub.findViewById(R.id.decoratedBarcodeView);
        progressBarMarkAttendance = activity.findViewById(R.id.progressBarMarkAttendance);
        inflatedViewStub.setVisibility(View.GONE);
        binding.fabOpenCameraScannerQrCodeFront.setOnClickListener(view -> openCameraScannerQrCodeEvent(0));
        binding.fabOpenCameraScannerQrCodeBack.setOnClickListener(view -> openCameraScannerQrCodeEvent(1));

        final long DOUBLE_CLICK_TIME_DELTA = 300; // Tempo máximo entre cliques (em milisegundos)
        final long[] lastClickTime = {0};
        final boolean[] isFlashLightOn = {false};
        inflatedViewStub.setOnLongClickListener(v -> {
            if (isFlashLightOn[0]) {
                isFlashLightOn[0] = false;
                decoratedBarcodeView.setTorchOff();
            } else {
                isFlashLightOn[0] = true;
                decoratedBarcodeView.setTorchOn();
            }
            return true;
        });
        inflatedViewStub.setOnClickListener(v -> {
            long clickTime = System.currentTimeMillis();
            if (clickTime - lastClickTime[0] < DOUBLE_CLICK_TIME_DELTA) {
                closeCamera(isFlashLightOn);
            }
            lastClickTime[0] = clickTime;
        });

        decoratedBarcodeView.setTorchListener(new DecoratedBarcodeView.TorchListener() {
            @Override
            public void onTorchOn() {
                Snackbar.make(requireView(), R.string.on_flashlight, Snackbar.LENGTH_LONG).show();
            }

            @Override
            public void onTorchOff() {
                Snackbar.make(requireView(), R.string.off_flashlight, Snackbar.LENGTH_LONG).show();
            }
        });
        return binding.getRoot();
    }

    private void openCameraScannerQrCodeEvent(int cameraId) {
        this.cameraId = cameraId;
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED)
            activityResultLauncher.launch(Manifest.permission.CAMERA);
        else
            openCamera();
    }

    private void openCamera() {
        scanOptions.setCameraId(cameraId);
        inflatedViewStub.setVisibility(View.VISIBLE);
        binding.linearLayoutButton.setVisibility(View.GONE);
        decoratedBarcodeView.initializeFromIntent(scanOptions.createScanIntent(context));
        decoratedBarcodeView.decodeContinuous(callback);
        decoratedBarcodeView.resume();
    }

    private void closeCamera(boolean[] isFlashLightOn) {
        binding.linearLayoutButton.setVisibility(View.VISIBLE);
        inflatedViewStub.setVisibility(View.GONE);
        if (isFlashLightOn[0]) {
            isFlashLightOn[0] = false;
            decoratedBarcodeView.setTorchOff();
        }
        decoratedBarcodeView.pause();
    }

    @Override
    public void onResume() {
        super.onResume();
        decoratedBarcodeView.resume();
    }

    @Override
    public void onPause() {
        super.onPause();
        decoratedBarcodeView.pause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        inflatedViewStub.setVisibility(View.GONE);
        if (decoratedBarcodeView != null) {
            decoratedBarcodeView.setTorchOff();
            decoratedBarcodeView.pause();
        }
        binding = null;
    }
}