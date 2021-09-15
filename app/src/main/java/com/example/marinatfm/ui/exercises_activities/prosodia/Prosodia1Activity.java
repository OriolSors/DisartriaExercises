package com.example.marinatfm.ui.exercises_activities.prosodia;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.marinatfm.MainActivity;
import com.example.marinatfm.R;
import com.example.marinatfm.databinding.ActivityDiadococinesias1Binding;
import com.example.marinatfm.databinding.ActivityProsodia1Binding;
import com.example.marinatfm.ui.exercises_activities.diadococinesias.Diadococinesias1Activity;
import com.example.marinatfm.ui.exercises_activities.fonorespiratoria.Fonorespiratoria2Activity;
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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class Prosodia1Activity extends AppCompatActivity {

    //Binding declaration
    private ActivityProsodia1Binding binding;

    //CharSequence Trios declarations
    private CharSequence[] trios;
    private ArrayList<CharSequence> triosShuffled;

    //MediaRecorder and File declaration
    private MediaRecorder recorder;
    private String fileName = null;

    //Boolean Recording mode
    private boolean recording = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Binding initialization
        binding = ActivityProsodia1Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Settings for hide Bar Navigation
        setupMainWindowDisplayMode();

        //Retrieving trios from strings file
        trios = getResources().getTextArray(R.array.trios_prosodia_1);

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
                                shuffleGroupOfThree(trios);
                                loadWords();
                                binding.startBtn.setEnabled(false);
                                binding.restartBtn.setEnabled(true);
                                binding.finishBtn.setEnabled(true);
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                //No button clicked
                                shuffleGroupOfThree(trios);
                                loadWords();
                                binding.startBtn.setEnabled(false);
                                binding.restartBtn.setEnabled(true);
                                binding.finishBtn.setEnabled(true);
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(Prosodia1Activity.this);
                builder.setMessage("Va a empezar el ejercicio. ¿Deseas grabar la actividad?").setPositiveButton("SI", dialogClickListener)
                        .setNegativeButton("NO", dialogClickListener).show();

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
                                binding.triosLayout.removeAllViews();
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

                AlertDialog.Builder builder = new AlertDialog.Builder(Prosodia1Activity.this);
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

        AlertDialog.Builder builder = new AlertDialog.Builder(Prosodia1Activity.this);
        builder.setMessage("VAS A FINALIZAR EL EJERCICIO:").setPositiveButton("SI", dialogClickListener)
                .setNegativeButton("NO", dialogClickListener).show();
    }

    private void shuffleGroupOfThree(CharSequence[] trios) {
        triosShuffled = new ArrayList<>();
        ArrayList<CharSequence> trio_aux = new ArrayList<>();
        for (CharSequence trio : trios) {
            trio_aux.add(trio);
            if (trio_aux.size() % 3 == 0 && trio_aux.size() != 0) {
                Collections.shuffle(trio_aux);
                triosShuffled.addAll(trio_aux);
                trio_aux = new ArrayList<>();
            }
        }

    }

    private void finishExercise() {
        if (recording){
            stopRecording();
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which){
                        case DialogInterface.BUTTON_POSITIVE:
                            //Yes button clicked
                            try {
                                safeToCloudStorage();
                                Intent intent = new Intent(Prosodia1Activity.this, MainActivity.class);
                                startActivity(intent);
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }
                            break;

                        case DialogInterface.BUTTON_NEGATIVE:
                            //No button clicked
                            Intent intent = new Intent(Prosodia1Activity.this, MainActivity.class);
                            startActivity(intent);

                            break;
                    }
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(Prosodia1Activity.this);
            builder.setMessage("¿Deseas subir la grabación de voz a la base de datos?").setPositiveButton("SI", dialogClickListener)
                    .setNegativeButton("NO", dialogClickListener).show();
        }else{
            Intent intent = new Intent(Prosodia1Activity.this, MainActivity.class);
            startActivity(intent);
        }


    }


    private void safeToCloudStorage() throws FileNotFoundException {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault());
        String currentDate = sdf.format(new Date());

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference().child("audio/Marcar sílaba_"+currentDate);
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

    private void loadWords() {
        TextView textView = new TextView(this);
        binding.triosLayout.addView(textView);
        textView.post(new Runnable() {
            int i = 0;
            @Override
            public void run() {
                if(i >= triosShuffled.size()){
                    textView.setText("");
                    finishExercise();
                }else{
                    textView.setText(triosShuffled.get(i));
                    textView.setTextColor(getResources().getColor(R.color.purple_500));
                    textView.setTextSize(26);

                    i++;
                    textView.postDelayed(this, 2000);
                }


            }
        });

    }

    private void startRecording() {
        //File Audio initialization
        fileName = getExternalCacheDir().getAbsolutePath();
        fileName += "/audiorecordprosodia1.3gp";
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