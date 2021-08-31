package com.example.marinatfm.ui.exercises_activities.fonacion;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Space;
import android.widget.TextView;

import com.example.marinatfm.MainActivity;
import com.example.marinatfm.R;
import com.example.marinatfm.databinding.ActivityFonacion2Binding;
import com.example.marinatfm.databinding.ActivityFonacion4Binding;
import com.example.marinatfm.databinding.ActivityMainBinding;
import com.example.marinatfm.ui.exercises_activities.fonorespiratoria.Fonorespiratoria1Activity;
import com.example.marinatfm.ui.exercises_activities.respiracion.Respiracion1Activity;
import com.example.marinatfm.ui.home.HomeFragmentDirections;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class Fonacion2Activity extends AppCompatActivity {

    //Binding declaration
    private ActivityFonacion2Binding binding;

    //Integer Images declaration
    private int[] images;

    //MediaRecorder and File declaration
    private MediaRecorder recorder;
    private String fileName = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Binding initialization
        binding = ActivityFonacion2Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Settings for hide Bar Navigation
        setupMainWindowDisplayMode();

        //Retrieving images from drawable
        images = new int[]{R.drawable.mountain_up,R.drawable.mountain_down,R.drawable.mountain};

        binding.startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startRecording();
                loadMountains();
                binding.startBtn.setEnabled(false);
                binding.restartBtn.setEnabled(true);
                binding.finishBtn.setEnabled(true);
            }
        });

        binding.restartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                //Yes button clicked
                                stopRecording();
                                Intent intent = new Intent(Fonacion2Activity.this,Fonacion2Activity.class);
                                startActivity(intent);
                                finish();
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                //No button clicked
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(Fonacion2Activity.this);
                builder.setMessage("¿Seguro que quieres reiniciar el ejercicio?").setPositiveButton("SI", dialogClickListener)
                        .setNegativeButton("NO", dialogClickListener).show();

            }
        });


        binding.finishBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                //Yes button clicked
                                finishExercise();
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                //No button clicked
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(Fonacion2Activity.this);
                builder.setMessage("VAS A FINALIZAR EL EJERCICIO:").setPositiveButton("SI", dialogClickListener)
                        .setNegativeButton("NO", dialogClickListener).show();

            }
        });


    }

    private void loadMountains() {
        binding.mountainsLayout.post(new Runnable() {
            final String[] sounds = new String[]{"Z","M","R"};
            int round = 0;
            int idx_sound = 0;

            boolean transition = true;
            @Override
            public void run() {
                if(idx_sound < sounds.length){
                    round++;
                    binding.mountainsLayout.removeAllViews();
                    binding.textView.setText(sounds[idx_sound]);
                    if (transition){
                        round--;
                        transition = false;
                        binding.mountainsLayout.postDelayed(this,1000);
                    }else if(round == 1){
                        ImageView imageView = new ImageView(Fonacion2Activity.this);
                        binding.mountainsLayout.addView(imageView);
                        imageView.setImageResource(images[0]);
                        transition = true;
                        binding.mountainsLayout.postDelayed(this,2000);
                    }else if(round == 2){
                        ImageView imageView = new ImageView(Fonacion2Activity.this);
                        binding.mountainsLayout.addView(imageView);
                        imageView.setImageResource(images[1]);
                        transition = true;
                        binding.mountainsLayout.postDelayed(this,2000);
                    }else if(round == 3){
                        ImageView imageView = new ImageView(Fonacion2Activity.this);
                        binding.mountainsLayout.addView(imageView);
                        imageView.setImageResource(images[2]);
                        transition = true;
                        binding.mountainsLayout.postDelayed(this,3500);
                    }else if(round == 4){
                        ImageView imageView = new ImageView(Fonacion2Activity.this);
                        binding.mountainsLayout.addView(imageView);
                        imageView.setImageResource(images[2]);

                        ImageView imageView2 = new ImageView(Fonacion2Activity.this);
                        binding.mountainsLayout.addView(imageView2);
                        imageView2.setImageResource(images[2]);

                        transition = true;
                        binding.mountainsLayout.postDelayed(this,5500);
                    }else{
                        round = 0;
                        idx_sound++;
                        if (idx_sound == sounds.length){
                            binding.textView.setText("Fin del ejercicio");
                        }else{
                            binding.textView.setText("Siguiente sonido");
                            binding.mountainsLayout.postDelayed(this,1000);
                        }

                    }
                }

            }
        });
    }

    private void finishExercise() {
        stopRecording();
        try {
            safeToCloudStorage();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Intent intent = new Intent(Fonacion2Activity.this,MainActivity.class);
        startActivity(intent);
    }

    private void safeToCloudStorage() throws FileNotFoundException {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault());
        String currentDate = sdf.format(new Date());

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference().child("audio/Montaña rusa_"+currentDate);
        InputStream stream = new FileInputStream(new File(fileName));
        UploadTask uploadTask = storageRef.putStream(stream);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                // ...
            }
        });


    }

    private void startRecording() {
        //File Audio initialization
        fileName = getExternalCacheDir().getAbsolutePath();
        fileName += "/audiorecordfonacion2.3gp";
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setOutputFile(fileName);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            recorder.prepare();
        } catch (IOException e) {
            Log.e("Audio Record test", "prepare() failed");
        }

        recorder.start();
    }

    private void stopRecording() {
        recorder.stop();
        recorder.release();
        recorder = null;
    }

    private void setupMainWindowDisplayMode() {
        Objects.requireNonNull(getSupportActionBar()).hide();
        View decorView = setSystemUiVisilityMode();
        decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                setSystemUiVisilityMode(); // Needed to avoid exiting immersive_sticky when keyboard is displayed
            }
        });
    }

    private View setSystemUiVisilityMode() {
        View decorView = getWindow().getDecorView();
        int options;
        options =
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

        decorView.setSystemUiVisibility(options);
        return decorView;
    }

}