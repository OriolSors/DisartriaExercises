package com.example.marinatfm.ui.exercises_activities.fonorespiratoria;

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
import android.widget.ImageView;
import android.widget.TextView;

import com.example.marinatfm.MainActivity;
import com.example.marinatfm.R;
import com.example.marinatfm.databinding.ActivityFonorespiratoria1Binding;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Fonorespiratoria1Activity extends AppCompatActivity {

    //Binding declaration
    private ActivityFonorespiratoria1Binding binding;

    //Images, numbers, days and months declarations
    int[] images;
    CharSequence[] numbers, days, months;


    //MediaRecorder and File declaration
    private MediaRecorder recorder;
    private String fileName = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Binding initialization
        binding = ActivityFonorespiratoria1Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Settings for hide Bar Navigation
        setupMainWindowDisplayMode();

        //Retrieving all data for the exercise
        images = new int[]{R.drawable.fonoimage1,
                R.drawable.fonoimage2,
                R.drawable.fonoimage3,
                R.drawable.fonoimage4,
                R.drawable.fonoimage5};
        numbers = getResources().getTextArray(R.array.numeros_fonorespiratoria_1);
        days = getResources().getTextArray(R.array.dias_fonorespiratoria_1);
        months = getResources().getTextArray(R.array.meses_fonorespiratoria_1);

        binding.startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startRecording();
                loadGroups();
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
                                binding.groupLayout.removeAllViews();
                                stopRecording();
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

                AlertDialog.Builder builder = new AlertDialog.Builder(Fonorespiratoria1Activity.this);
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

                AlertDialog.Builder builder = new AlertDialog.Builder(Fonorespiratoria1Activity.this);
                builder.setMessage("VAS A FINALIZAR EL EJERCICIO:").setPositiveButton("SI", dialogClickListener)
                        .setNegativeButton("NO", dialogClickListener).show();

            }
        });

    }

    private void loadGroups() {
        TextView textView = new TextView(this);
        binding.groupLayout.addView(textView);
        binding.groupLayout.post(new Runnable() {
            int idx_images = 0;
            int idx_numbers = 0;
            int idx_days = 0;
            int idx_months = 0;
            @Override
            public void run() {
                if(idx_numbers < numbers.length){
                    textView.setText(numbers[idx_numbers]);
                    textView.setTextColor(getResources().getColor(R.color.purple_500));
                    textView.setTextSize(24);
                    textView.setGravity(Gravity.CENTER);
                    idx_numbers++;
                    textView.postDelayed(this,idx_numbers*500L);
                }else if(idx_days < days.length){
                    textView.setText(days[idx_days]);
                    textView.setTextColor(getResources().getColor(R.color.purple_500));
                    textView.setTextSize(24);
                    textView.setGravity(Gravity.CENTER);
                    idx_days++;
                    textView.postDelayed(this,idx_days* 600L);
                }else if(idx_months < months.length){
                    textView.setText(months[idx_months]);
                    textView.setTextColor(getResources().getColor(R.color.purple_500));
                    textView.setTextSize(24);
                    textView.setGravity(Gravity.CENTER);
                    idx_months++;
                    textView.postDelayed(this,idx_months* 700L);
                }else{
                    textView.setText("Fin del ejercicio");
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
        Intent intent = new Intent(Fonorespiratoria1Activity.this, MainActivity.class);
        startActivity(intent);
    }

    private void safeToCloudStorage() throws FileNotFoundException {
        Date currentTime = Calendar.getInstance().getTime();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference().child("audio/Grupo fónico_"+currentTime.toString());
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
        fileName += "/audiorecordfonorespiratoria1.3gp";
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