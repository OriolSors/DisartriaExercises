package com.example.marinatfm.ui.recordings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.marinatfm.databinding.FragmentRecordingsBinding;

public class RecordingsFragment extends Fragment {

    private RecordingsViewModel recordingsViewModel;
    private FragmentRecordingsBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        recordingsViewModel =
                new ViewModelProvider(this).get(RecordingsViewModel.class);

        binding = FragmentRecordingsBinding.inflate(inflater, container, false);


        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}