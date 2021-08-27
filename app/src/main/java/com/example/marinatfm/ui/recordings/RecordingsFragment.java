package com.example.marinatfm.ui.recordings;

import android.app.ProgressDialog;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import com.example.marinatfm.R;
import com.example.marinatfm.databinding.FragmentRecordingsBinding;
import com.example.marinatfm.ui.home.HomeFragmentDirections;
import com.example.marinatfm.ui.home.exercises.ExercisesFragment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RecordingsFragment extends Fragment {

    private RecordingsViewModel recordingsViewModel;
    private FragmentRecordingsBinding binding;

    private ArrayList<File> audioFiles;

    private ArrayList<String> exercises,blocks;
    private ArrayList<Integer> images;

    //Loading Dialog declaration
    private ProgressDialog dialogLoading;

    private MediaPlayer player;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        recordingsViewModel = new ViewModelProvider(this).get(RecordingsViewModel.class);
        binding = FragmentRecordingsBinding.inflate(inflater, container, false);

        audioFiles = new ArrayList<>();
        exercises = new ArrayList<>();
        blocks = new ArrayList<>();
        images = new ArrayList<>();

        player = new MediaPlayer();

        FirebaseStorage storage = FirebaseStorage.getInstance();

        StorageReference listRef = storage.getReference().child("audio");

        setLoadingDialog();

        dialogLoading.show();

        // Now we get the references of these images
        listRef.listAll().addOnSuccessListener(result -> {
            for(StorageReference fileRef : result.getItems()) {
                // All the items under listRef.
                File localFile = null;
                try {
                    localFile = File.createTempFile("audio", "3gpp");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                assert localFile != null;
                fileRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        // Local temp file has been created

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle any errors
                    }
                });
                audioFiles.add(localFile);
                String[] strings = fileRef.getName().split("_");
                exercises.add(strings[0]);
                blocks.add(strings[1]);
                images.add(android.R.drawable.ic_media_play);
            }

            MyAdapter adapter = new MyAdapter(requireContext(),exercises,blocks,images);
            binding.recordingsListView.setAdapter(adapter);

            dialogLoading.hide();



            binding.recordingsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String path = audioFiles.get(position).getAbsolutePath();
                    if(player.isPlaying()){
                        adapter.rImgs.set(position, android.R.drawable.ic_media_play);
                        MyAdapter adapter = new MyAdapter(requireContext(),exercises,blocks,images);
                        binding.recordingsListView.setAdapter(adapter);
                        player.stop();
                        player.reset();
                    }else{
                        try {
                            adapter.rImgs.set(position,android.R.drawable.ic_media_pause);
                            MyAdapter adapter = new MyAdapter(requireContext(),exercises,blocks,images);
                            binding.recordingsListView.setAdapter(adapter);
                            player.setDataSource(path);
                            player.prepare();
                            player.start();
                        } catch (IOException e) {
                            Log.e("Audio test", "prepare() failed");
                        }

                    }
                    player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mediaPlayer) {
                            player.reset();
                            adapter.rImgs.set(position,android.R.drawable.ic_media_play);
                            MyAdapter adapter = new MyAdapter(requireContext(),exercises,blocks,images);
                            binding.recordingsListView.setAdapter(adapter);
                        }
                    });

                }
            });
        }).addOnFailureListener(exception -> {
            // Handle any errors
        });


        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        player.stop();
        player.reset();
        player = null;
    }

    private void setLoadingDialog() {
        dialogLoading = new ProgressDialog(requireContext());
        dialogLoading.setMessage("Loading...");
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