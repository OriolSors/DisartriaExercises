package com.example.marinatfm.ui.home.exercises;

import androidx.lifecycle.ViewModelProvider;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.marinatfm.R;
import com.example.marinatfm.databinding.ExercisesFragmentBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.InputStream;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExercisesFragment extends Fragment {

    //ViewModel and Binding declarations
    private ExercisesViewModel exercisesViewModel;
    private ExercisesFragmentBinding binding;

    //Real-time Database declarations
    private final DatabaseReference mDatabase = FirebaseDatabase.getInstance("https://disartriaexercises-default-rtdb.europe-west1.firebasedatabase.app/").getReference();
    private final DatabaseReference mRootChild = mDatabase.child("planning");

    //ValueListener declaration
    private ValueEventListener dataListener;

    //Loading Dialog declaration
    private ProgressDialog dialogLoading;

    //Blocks, Exercises and Images Strings declarations
    private ArrayList<String> blocks, exercises;
    private ArrayList<Integer> images;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        //ViewModel and Binding initializations
        exercisesViewModel = new ViewModelProvider(this).get(ExercisesViewModel.class);
        binding = ExercisesFragmentBinding.inflate(getLayoutInflater());

        //Loading Dialog initialization
        setLoadingDialog();

        return binding.getRoot();
    }


    @Override
    public void onResume(){
        super.onResume();
        dialogLoading.show();
        mRootChild.addValueEventListener(dataListener= new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //Getting Blocks and Exercises in Real-Time
                getBlocksAndExercises(dataSnapshot);

                MyAdapter adapter = new MyAdapter(requireContext(),exercises,blocks,images);
                binding.exercisesListView.setAdapter(adapter);
                dialogLoading.hide();
                binding.exercisesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        switch (position){
                            case 0:
                                Toast.makeText(requireContext(),"testing",Toast.LENGTH_SHORT).show();
                        }
                    }
                });


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

    private void getBlocksAndExercises(@NonNull DataSnapshot dataSnapshot) {
        exercises = new ArrayList<>();
        blocks = new ArrayList<>();
        images = new ArrayList<>();
        for(DataSnapshot block: dataSnapshot.getChildren()){
            for (DataSnapshot exercise : block.getChildren()){
                if(Objects.equals(exercise.getValue(String.class), "enabled")){
                    exercises.add(exercise.getKey());
                    blocks.add(block.getKey());
                    String imageName = Normalizer.normalize(exercise.getKey(), Normalizer.Form.NFD);
                    imageName = imageName.replaceAll("[^\\p{ASCII}]", "");
                    imageName = imageName.replaceAll(" ","");
                    imageName = imageName.toLowerCase(Locale.ROOT);
                    images.add(requireContext().getResources()
                            .getIdentifier(imageName,"drawable",requireContext().getPackageName()));
                }
            }
        }
    }


    static class MyAdapter extends ArrayAdapter<String> {

        Context context;
        ArrayList<String> exercises, blocks;
        ArrayList<Integer> rImgs;

        MyAdapter (Context c, ArrayList<String> exercises, ArrayList<String> blocks, ArrayList<Integer> imgs) {
            super(c, R.layout.row, R.id.textView1, exercises);
            this.context = c;
            this.exercises = exercises;
            this.blocks = blocks;
            this.rImgs = imgs;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater layoutInflater = (LayoutInflater) context.getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View row = layoutInflater.inflate(R.layout.row, parent, false);
            ImageView image = row.findViewById(R.id.image);
            TextView exercise = row.findViewById(R.id.textView1);
            TextView block = row.findViewById(R.id.textView2);

            // now set our resources on views
            image.setImageResource(rImgs.get(position));
            image.getLayoutParams().height = 250;
            image.getLayoutParams().width = 250;
            exercise.setText(exercises.get(position));
            block.setText(blocks.get(position));

            return row;
        }
    }


}