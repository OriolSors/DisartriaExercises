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
import java.util.ArrayList;
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

    //Custom Adapter declaration
    private MyAdapter adapter;

    //Blocks, Exercises and Images Strings declarations
    private String[] blocks, exercises;
    private ArrayList<String> imagesURL;

    //Bitmaps from URL images declarations
    private ArrayList<Bitmap> images;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        //ViewModel and Binding initializations
        exercisesViewModel = new ViewModelProvider(this).get(ExercisesViewModel.class);
        binding = ExercisesFragmentBinding.inflate(inflater, container, false);

        //Loading Dialog initialization
        setLoadingDialog();

        return binding.getRoot();
    }


    @Override
    public void onResume(){
        super.onResume();
        mRootChild.addValueEventListener(dataListener= new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //Getting Blocks and Exercises in Real-Time
                blocks = getBlockNames(dataSnapshot).toArray(new String[0]);
                exercises = getExerciseNames(dataSnapshot).toArray(new String[0]);
                imagesURL = getImages(dataSnapshot);

                images = new ArrayList<>();

                dialogLoading.show();

                ExecutorService executor = Executors.newSingleThreadExecutor();
                Handler handler = new Handler(Looper.getMainLooper());
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            for (String imageURL: imagesURL) {
                                images.add(Picasso.get().load(imageURL).resize(100,100).get());
                            }

                        } catch (IOException ignored) {

                        }

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                adapter = new MyAdapter(requireContext(),exercises,blocks,images);
                                binding.exercisesListView.setAdapter(adapter);
                                dialogLoading.hide();
                            }
                        });

                    }
                });

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

    private ArrayList<String> getExerciseNames(@NonNull DataSnapshot dataSnapshot) {
        ArrayList<String> exerciseList = new ArrayList<>();
        for(DataSnapshot block: dataSnapshot.getChildren()){
            for (DataSnapshot exercise : block.getChildren()){
                if(Objects.equals(exercise.getValue(String.class), "enabled")){
                    exerciseList.add(exercise.getKey());
                }
            }
        }
        return exerciseList;
    }

    private ArrayList<String> getBlockNames(@NonNull DataSnapshot dataSnapshot) {
        ArrayList<String> blockList = new ArrayList<>();
        for(DataSnapshot block: dataSnapshot.getChildren()){
            for (DataSnapshot exercise : block.getChildren()){
                if(Objects.equals(exercise.getValue(String.class), "enabled")){
                    blockList.add(block.getKey());
                }
            }
        }
        return blockList;
    }

    private ArrayList<String> getImages(@NonNull DataSnapshot dataSnapshot){
        //TODO: Hierarchize the RealTime DB and set correctly the images
        ArrayList<String> imagesList = new ArrayList<>();
        for(DataSnapshot block: dataSnapshot.getChildren()){
            boolean found = false;
            for (DataSnapshot exercise : block.getChildren()){
                String value = exercise.getValue(String.class);
                if(Objects.equals(value, "enabled"))found=true;
                if (found && !Objects.equals(value, "enabled") && !Objects.equals(value, "disabled")) {
                    imagesList.add(value);
                }
            }
        }
        return imagesList;
    }

    static class MyAdapter extends ArrayAdapter<String> {

        Context context;
        String[] rTitle;
        String[] rDescription;
        ArrayList<Bitmap> rImgs;

        MyAdapter (Context c, String[] title, String[] description, ArrayList<Bitmap> imgs) {
            super(c, R.layout.row, R.id.textView1, title);
            this.context = c;
            this.rTitle = title;
            this.rDescription = description;
            this.rImgs = imgs;

        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater layoutInflater = (LayoutInflater) context.getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View row = layoutInflater.inflate(R.layout.row, parent, false);
            ImageView images = row.findViewById(R.id.image);
            TextView myTitle = row.findViewById(R.id.textView1);
            TextView myDescription = row.findViewById(R.id.textView2);

            // now set our resources on views
            images.setImageBitmap(rImgs.get(position));
            myTitle.setText(rTitle[position]);
            myDescription.setText(rDescription[position]);

            return row;
        }
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }


}