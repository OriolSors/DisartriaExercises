package com.example.marinatfm.ui.home.planning;

import androidx.lifecycle.ViewModelProvider;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Space;
import android.widget.TextView;

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

    //ViewModel and Binding declarations
    private PlanningViewModel planningViewModel;
    private PlanningFragmentBinding binding;

    //Real-time Database declarations
    private final DatabaseReference mDatabase = FirebaseDatabase.getInstance("https://disartriaexercises-default-rtdb.europe-west1.firebasedatabase.app/").getReference();
    private final DatabaseReference mRootChild = mDatabase.child("planning");

    //ValueListener declaration
    private ValueEventListener dataListener;

    //Loading Dialog declaration
    private ProgressDialog dialogLoading;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        //ViewModel and Binding initializations
        planningViewModel = new ViewModelProvider(this).get(PlanningViewModel.class);
        binding = PlanningFragmentBinding.inflate(inflater, container, false);

        //Loading Dialog initialization
        setLoadingDialog();




        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        dialogLoading.show();
        mRootChild.addValueEventListener(dataListener= new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                binding.blockLayout.removeAllViews();
                for (DataSnapshot block: dataSnapshot.getChildren() ) {
                    TextView blockText = new TextView(binding.blockLayout.getContext());
                    blockText.setTextSize(18);
                    blockText.setText(block.getKey());
                    blockText.setTextColor(getResources().getColor(R.color.purple_500));
                    binding.blockLayout.addView(blockText);

                    for(DataSnapshot exercise: block.getChildren()){
                        String value = exercise.getValue(String.class);
                        if (Objects.equals(value, "enabled") || Objects.equals(value, "disabled")) {

                            LinearLayout checkLayout = new LinearLayout(binding.blockLayout.getContext());
                            checkLayout.setOrientation(LinearLayout.HORIZONTAL);

                            //TODO: Make correct Space as left margin
                            TextView blankSpace = new TextView(checkLayout.getContext());
                            blankSpace.setText("      ");


                            CheckBox exerciseCheck = new CheckBox(binding.blockLayout.getContext());
                            exerciseCheck.setText(exercise.getKey());
                            exerciseCheck.setTextSize(16);
                            exerciseCheck.setButtonTintList(ColorStateList.valueOf(getResources().getColor(R.color.purple_700)));

                            checkLayout.addView(blankSpace);
                            checkLayout.addView(exerciseCheck);

                            boolean check = Objects.equals(exercise.getValue(String.class), "enabled");
                            exerciseCheck.setChecked(check);

                            binding.blockLayout.addView(checkLayout);

                            dialogLoading.hide();

                            //TODO: Separate writing to DB from ValueListener
                            exerciseCheck.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    if(exerciseCheck.isChecked()){
                                        mRootChild.child(Objects.requireNonNull(block.getKey()))
                                                .child(Objects.requireNonNull(exercise.getKey()))
                                                .setValue("enabled");
                                    }else{
                                        mRootChild.child(Objects.requireNonNull(block.getKey()))
                                                .child(Objects.requireNonNull(exercise.getKey()))
                                                .setValue("disabled");
                                    }

                                }
                            });
                        }


                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

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



    private void setLoadingDialog() {
        dialogLoading = new ProgressDialog(requireContext());
        dialogLoading.setMessage("Loading...");
    }

}