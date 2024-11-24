package com.antonioteca.cc42.ui.event;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.antonioteca.cc42.databinding.FragmentAttendanceListBinding;

public class AttendanceListFragment extends Fragment {


    private FragmentAttendanceListBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentAttendanceListBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }
}