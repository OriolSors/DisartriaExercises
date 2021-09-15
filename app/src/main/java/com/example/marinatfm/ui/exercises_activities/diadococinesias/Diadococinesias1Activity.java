package com.example.marinatfm.ui.exercises_activities.diadococinesias;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Space;
import android.widget.TextView;

import com.example.marinatfm.MainActivity;
import com.example.marinatfm.R;
import com.example.marinatfm.databinding.ActivityDiadococinesias1Binding;
import com.example.marinatfm.ui.exercises_activities.fonacion.Fonacion4Activity;
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

public class Diadococinesias1Activity extends AppCompatActivity {

    //Binding declaration
    private ActivityDiadococinesias1Binding binding;

    //CharSequence Words declaration
    private CharSequence[] words;

    //MediaRecorder and File declaration
    private MediaRecorder recorder;
    private String fileName = null;

    //Boolean Recording mode
    private boolean recording = false;

    //Long milliseconds number
    private long millis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Binding initialization
        binding = ActivityDiadococinesias1Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Settings for hide Bar Navigation
        setupMainWindowDisplayMode();

        //Retrieving words from strings file
        words = getResources().getTextArray(R.array.words_diadococinesias_1);

        binding.startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                //Yes button clicked
                                startRecording();
                                recording = true;
                                loadWords(millis);
                                binding.startBtn.setEnabled(false);
                                binding.restartBtn.setEnabled(true);
                                binding.finishBtn.setEnabled(true);
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                //No button clicked
                                loadWords(millis);
                                binding.startBtn.setEnabled(false);
                                binding.restartBtn.setEnabled(true);
                                binding.finishBtn.setEnabled(true);
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(Diadococinesias1Activity.this);
                builder.setMessage("Va a empezar el ejercicio. ¿Deseas grabar la actividad?").setPositiveButton("SI", dialogClickListener)
                        .setNegativeButton("NO", dialogClickListener);

                String[] modes = {"Lento", "Normal", "Rápido"};

                AlertDialog.Builder modeBuilder = new AlertDialog.Builder(Diadococinesias1Activity.this);
                modeBuilder.setTitle("Escoge una velocidad");
                modeBuilder.setItems(modes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (modes[which]) {
                            case "Lento":
                                millis = 4500;
                                break;
                            case "Normal":
                                millis = 3500;
                                break;
                            case "Rápido":
                                millis = 2500;
                                break;
                        }

                        builder.show();
                    }
                });
                modeBuilder.show();



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
                                binding.wordsLayout.removeAllViews();
                                if (recording) stopRecording();
                                binding.startBtn.setEnabled(true);
                                binding.restartBtn.setEnabled(false);
                                binding.finishBtn.setEnabled(false);
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                //No button clicked
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(Diadococinesias1Activity.this);
                builder.setMessage("¿Seguro que quieres reiniciar el ejercicio?").setPositiveButton("SI", dialogClickListener)
                        .setNegativeButton("NO", dialogClickListener).show();

            }
        });


        binding.finishBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finishingDialog();

            }
        });
    }

    private void finishingDialog(){
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

        AlertDialog.Builder builder = new AlertDialog.Builder(Diadococinesias1Activity.this);
        builder.setMessage("VAS A FINALIZAR EL EJERCICIO:").setPositiveButton("SI", dialogClickListener)
                .setNegativeButton("NO", dialogClickListener).show();
    }

    private void finishExercise() {
        if(recording){
            stopRecording();
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which){
                        case DialogInterface.BUTTON_POSITIVE:
                            //Yes button clicked
                            try {
                                safeToCloudStorage();
                                Intent intent = new Intent(Diadococinesias1Activity.this, MainActivity.class);
                                startActivity(intent);
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }
                            break;

                        case DialogInterface.BUTTON_NEGATIVE:
                            //No button clicked
                            Intent intent = new Intent(Diadococinesias1Activity.this, MainActivity.class);
                            startActivity(intent);

                            break;
                    }
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(Diadococinesias1Activity.this);
            builder.setMessage("¿Deseas subir la grabación de voz a la base de datos?").setPositiveButton("SI", dialogClickListener)
                    .setNegativeButton("NO", dialogClickListener).show();
        }else{
            Intent intent = new Intent(Diadococinesias1Activity.this, MainActivity.class);
            startActivity(intent);
        }


    }

    private void safeToCloudStorage() throws FileNotFoundException {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault());
        String currentDate = sdf.format(new Date());

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference().child("audio/Diadococinesias verbales_"+currentDate);
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

    private void loadWords(long millis) {
        TextView textView = new TextView(this);
        binding.wordsLayout.addView(textView);
        textView.post(new Runnable() {
            int i = 0;
            @Override
            public void run() {
                if(i >= words.length){
                    textView.setText("");
                    finishExercise();
                }else{
                    textView.setText(words[i]);
                    textView.setTextColor(getResources().getColor(R.color.purple_500));
                    textView.setTextSize(32);

                    i++;
                    textView.postDelayed(this, millis);
                }
            }
        });


    }

    private void startRecording() {
        //File Audio initialization
        fileName = getExternalCacheDir().getAbsolutePath();
        fileName += "/audiorecorddiadococinesias1.3gp";
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