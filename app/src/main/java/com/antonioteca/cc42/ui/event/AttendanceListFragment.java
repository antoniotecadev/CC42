package com.antonioteca.cc42.ui.event;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.antonioteca.cc42.R;
import com.antonioteca.cc42.databinding.FragmentAttendanceListBinding;
import com.antonioteca.cc42.utility.Util;
import com.google.android.material.snackbar.Snackbar;
import com.google.zxing.ResultPoint;
import com.google.zxing.client.android.BeepManager;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.List;

public class AttendanceListFragment extends Fragment {

    private Context context;
    private Integer cameraId;
    private String resultQrCode;
    private View inflatedViewStub;
    private BeepManager beepManager;
    private ScanOptions scanOptions;
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
                if (!result.getText().startsWith("cc42user")) {
                    resultQrCode = result.getText();
                    Toast.makeText(context, resultQrCode, Toast.LENGTH_LONG).show();
                    beepManager.playBeepSoundAndVibrate();
                    Util.startVibration(context);
                }
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
        scanOptions = new ScanOptions();
        scanOptions.setBeepEnabled(true);
        scanOptions.setOrientationLocked(false);
        scanOptions.setDesiredBarcodeFormats(ScanOptions.QR_CODE);
        scanOptions.setPrompt(getString(R.string.align_camera_qr_code));
        beepManager = new BeepManager(requireActivity());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAttendanceListBinding.inflate(inflater, container, false);
        inflatedViewStub = binding.viewStub.inflate();
        decoratedBarcodeView = inflatedViewStub.findViewById(R.id.decoratedBarcodeView);
        inflatedViewStub.setVisibility(View.GONE);
        binding.fabOpenCameraScannerQrCodeBack.setOnClickListener(view -> openCameraScannerQrCodeEvent(0));
        binding.fabOpenCameraScannerQrCodeFront.setOnClickListener(view -> openCameraScannerQrCodeEvent(1));

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
        if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED)
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