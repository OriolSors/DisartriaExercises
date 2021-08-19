package com.example.marinatfm.ui.home.exercises;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.marinatfm.R;
import com.example.marinatfm.databinding.ExercisesFragmentBinding;

public class ExercisesFragment extends Fragment {

    private ExercisesViewModel exercisesViewModel;
    private ExercisesFragmentBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        exercisesViewModel = new ViewModelProvider(this).get(ExercisesViewModel.class);

        binding = ExercisesFragmentBinding.inflate(inflater, container, false);


        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


}