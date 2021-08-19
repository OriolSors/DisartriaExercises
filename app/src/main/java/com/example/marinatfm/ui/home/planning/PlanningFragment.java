package com.example.marinatfm.ui.home.planning;

import androidx.lifecycle.ViewModelProvider;

import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.marinatfm.MainActivity;
import com.example.marinatfm.R;
import com.example.marinatfm.databinding.ExercisesFragmentBinding;
import com.example.marinatfm.databinding.PlanningFragmentBinding;
import com.example.marinatfm.ui.home.exercises.ExercisesViewModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class PlanningFragment extends Fragment {

    private PlanningViewModel planningViewModel;
    private PlanningFragmentBinding binding;

    private final DatabaseReference mDatabase = FirebaseDatabase.getInstance("https://disartriaexercises-default-rtdb.europe-west1.firebasedatabase.app/").getReference();

    private final DatabaseReference mRootChild = mDatabase.child("user");

    private ValueEventListener dataListener;

    private ProgressDialog dialogLoading;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        planningViewModel = new ViewModelProvider(this).get(PlanningViewModel.class);

        binding = PlanningFragmentBinding.inflate(inflater, container, false);

        dialogLoading = new ProgressDialog(requireContext());
        dialogLoading.setMessage("Loading...");

        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        dialogLoading.show();
        mRootChild.addValueEventListener(dataListener= new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                String text = Objects.requireNonNull(dataSnapshot.getValue()).toString();
                binding.planningText.setText(text);
                dialogLoading.hide();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void onPause() {
        super.onPause();
        mRootChild.removeEventListener(dataListener);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}