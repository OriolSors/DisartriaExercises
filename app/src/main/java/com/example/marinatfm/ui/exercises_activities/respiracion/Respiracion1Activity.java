package com.example.marinatfm.ui.exercises_activities.respiracion;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;

import com.example.marinatfm.MainActivity;
import com.example.marinatfm.R;
import com.example.marinatfm.databinding.ActivityRespiracion1Binding;
import com.example.marinatfm.ui.exercises_activities.prosodia.Prosodia1Activity;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Respiracion1Activity extends AppCompatActivity {

    //Binding declaration
    private ActivityRespiracion1Binding binding;

    //MediaRecorder and File declaration
    private MediaRecorder recorder;
    private String fileName = null;

    //Runnable object to start the exercise declaration
    private Runnable myRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Binding initialization
        binding = ActivityRespiracion1Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Settings for hide Bar Navigation
        setupMainWindowDisplayMode();

        binding.startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startRecording();
                startPlaying(new int[]{3000,3000,3000,5000},new int[]{1000,3000,3000,3000},new int[]{3000,3000,7000,10000});
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
                                Intent intent = new Intent(Respiracion1Activity.this,Respiracion1Activity.class);
                                startActivity(intent);
                                finish();
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                //No button clicked
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(Respiracion1Activity.this);
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

                AlertDialog.Builder builder = new AlertDialog.Builder(Respiracion1Activity.this);
                builder.setMessage("VAS A FINALIZAR EL EJERCICIO:").setPositiveButton("SI", dialogClickListener)
                        .setNegativeButton("NO", dialogClickListener).show();

            }
        });

    }

    private void startPlaying(int[] increasings, int[] constants, int[] decreasings) {
        binding.imagesLayout.post(myRunnable = new Runnable() {
            int idx_increase = increasings[0];
            int idx_constant = constants[0];
            int idx_decrease = decreasings[0];
            int round = 1;
            @Override
            public void run() {
                if(idx_increase > 0){
                    binding.increasingImage.setVisibility(View.VISIBLE);
                    binding.textView.setText(String.valueOf(idx_increase/1000));
                    binding.textView.setTextSize(30);
                    binding.textView.setGravity(Gravity.LEFT);
                    idx_increase -= 1000;
                    binding.increasingImage.postDelayed(this,1000);
                }else if(idx_constant > 0){
                    binding.constantImage.setVisibility(View.VISIBLE);
                    binding.textView.setText(String.valueOf(idx_constant/1000));
                    binding.textView.setTextSize(30);
                    binding.textView.setGravity(Gravity.CENTER);
                    idx_constant -= 1000;
                    binding.constantImage.postDelayed(this,1000);
                }else if(idx_decrease > 0){
                    binding.decreasingImage.setVisibility(View.VISIBLE);
                    binding.textView.setText(String.valueOf(idx_decrease/1000));
                    binding.textView.setTextSize(30);
                    binding.textView.setGravity(Gravity.RIGHT);
                    idx_decrease -= 1000;
                    binding.decreasingImage.postDelayed(this,1000);
                }else{
                    binding.increasingImage.setVisibility(View.INVISIBLE);
                    binding.constantImage.setVisibility(View.INVISIBLE);
                    binding.decreasingImage.setVisibility(View.INVISIBLE);
                    round++;
                    binding.textView.setGravity(Gravity.CENTER);
                    if(round < increasings.length){
                        binding.textView.setText("Siguiente ronda");
                        idx_increase=increasings[round];
                        idx_constant=constants[round];
                        idx_decrease=decreasings[round];
                        binding.increasingImage.postDelayed(this,1000);
                    }else{
                        binding.textView.setText("Fin del ejercicio");
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
        Intent intent = new Intent(Respiracion1Activity.this, MainActivity.class);
        startActivity(intent);
    }

    private void safeToCloudStorage() throws FileNotFoundException {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault());
        String currentDate = sdf.format(new Date());

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference().child("audio/Tiempo respiratorio_"+currentDate);
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
        fileName += "/audiorecordrespiracion1.3gp";
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