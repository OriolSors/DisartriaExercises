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
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Space;
import android.widget.TextView;

import com.example.marinatfm.MainActivity;
import com.example.marinatfm.R;
import com.example.marinatfm.databinding.ActivityFonorespiratoria1Binding;
import com.example.marinatfm.databinding.ActivityRespiracion1Binding;
import com.example.marinatfm.ui.exercises_activities.diadococinesias.Diadococinesias1Activity;
import com.example.marinatfm.ui.exercises_activities.fonacion.Fonacion4Activity;
import com.example.marinatfm.ui.exercises_activities.prosodia.Prosodia1Activity;
import com.example.marinatfm.ui.exercises_activities.respiracion.Respiracion1Activity;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Fonorespiratoria1Activity extends AppCompatActivity {

    //Binding declaration
    private ActivityFonorespiratoria1Binding binding;

    //Objects, numbers, days and months declarations
    CharSequence[] objects, numbers, days, months;

    //MediaRecorder and File declaration
    private MediaRecorder recorder;
    private String fileName = null;

    //Boolean Recording mode
    private boolean recording = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Binding initialization
        binding = ActivityFonorespiratoria1Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Settings for hide Bar Navigation
        setupMainWindowDisplayMode();

        //Retrieving all data for the exercise
        objects = getResources().getTextArray(R.array.objetos_fonorespiratoria_1);
        numbers = getResources().getTextArray(R.array.numeros_fonorespiratoria_1);
        days = getResources().getTextArray(R.array.dias_fonorespiratoria_1);
        months = getResources().getTextArray(R.array.meses_fonorespiratoria_1);

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
                                loadGroups();
                                binding.startBtn.setEnabled(false);
                                binding.restartBtn.setEnabled(true);
                                binding.finishBtn.setEnabled(true);
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                //No button clicked
                                loadGroups();
                                binding.startBtn.setEnabled(false);
                                binding.restartBtn.setEnabled(true);
                                binding.finishBtn.setEnabled(true);
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(Fonorespiratoria1Activity.this);
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
                                stopRecording();
                                Intent intent = new Intent(Fonorespiratoria1Activity.this,Fonorespiratoria1Activity.class);
                                startActivity(intent);
                                finish();
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
        binding.groupLayout.post(new Runnable() {
            int idx_objects = 0;
            int idx_numbers = 0;
            int idx_days = 0;
            int idx_months = 0;

            boolean transition = true;
            @Override
            public void run() {
                if (transition){
                    binding.groupLayout.removeAllViews();
                    ImageView imageView = new ImageView(Fonorespiratoria1Activity.this);
                    binding.groupLayout.addView(imageView);
                    Space space = new Space(Fonorespiratoria1Activity.this);
                    ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    space.setLayoutParams(params);
                    space.getLayoutParams().width = 110;
                    binding.groupLayout.addView(space);
                    TextView textView = new TextView(Fonorespiratoria1Activity.this);
                    binding.groupLayout.addView(textView);

                    transition = false;
                    imageView.setImageResource(R.drawable.flower_fonorespiratoria);
                    imageView.getLayoutParams().height = 250;
                    imageView.getLayoutParams().width = 250;
                    textView.setText("*Inhale*");
                    textView.setTextColor(getResources().getColor(R.color.purple_500));
                    textView.setTextSize(24);
                    textView.setGravity(Gravity.CENTER);
                    binding.groupLayout.postDelayed(this,1500);
                }else if(idx_objects < objects.length){
                    binding.groupLayout.removeAllViews();
                    TextView textView = new TextView(Fonorespiratoria1Activity.this);
                    binding.groupLayout.addView(textView);

                    textView.setText(objects[idx_objects]);
                    textView.setTextColor(getResources().getColor(R.color.purple_500));
                    textView.setTextSize(24);
                    textView.setGravity(Gravity.CENTER);
                    idx_objects++;
                    transition = true;
                    binding.groupLayout.postDelayed(this,1000 + idx_objects*400L);
                }else if(idx_numbers < numbers.length){
                    binding.groupLayout.removeAllViews();
                    TextView textView = new TextView(Fonorespiratoria1Activity.this);
                    binding.groupLayout.addView(textView);

                    textView.setText(numbers[idx_numbers]);
                    textView.setTextColor(getResources().getColor(R.color.purple_500));
                    textView.setTextSize(24);
                    textView.setGravity(Gravity.CENTER);
                    idx_numbers++;
                    transition = true;
                    binding.groupLayout.postDelayed(this,1000 + idx_numbers*350L);
                }else if(idx_days < days.length){
                    binding.groupLayout.removeAllViews();
                    TextView textView = new TextView(Fonorespiratoria1Activity.this);
                    binding.groupLayout.addView(textView);

                    textView.setText(days[idx_days]);
                    textView.setTextColor(getResources().getColor(R.color.purple_500));
                    textView.setTextSize(24);
                    textView.setGravity(Gravity.CENTER);
                    idx_days++;
                    transition = true;
                    binding.groupLayout.postDelayed(this,1000 + idx_days* 400L);
                }else if(idx_months < months.length){
                    binding.groupLayout.removeAllViews();
                    TextView textView = new TextView(Fonorespiratoria1Activity.this);
                    binding.groupLayout.addView(textView);

                    textView.setText(months[idx_months]);
                    textView.setTextColor(getResources().getColor(R.color.purple_500));
                    textView.setTextSize(24);
                    textView.setGravity(Gravity.CENTER);
                    idx_months++;
                    transition = true;
                    binding.groupLayout.postDelayed(this,1000 + idx_months* 450L);
                }else{
                    binding.groupLayout.removeAllViews();
                    TextView textView = new TextView(Fonorespiratoria1Activity.this);
                    binding.groupLayout.addView(textView);

                    textView.setText("Fin del ejercicio");
                }
            }
        });
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
                                Intent intent = new Intent(Fonorespiratoria1Activity.this, MainActivity.class);
                                startActivity(intent);
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }
                            break;

                        case DialogInterface.BUTTON_NEGATIVE:
                            //No button clicked
                            Intent intent = new Intent(Fonorespiratoria1Activity.this, MainActivity.class);
                            startActivity(intent);

                            break;
                    }
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(Fonorespiratoria1Activity.this);
            builder.setMessage("¿Deseas subir la grabación de voz a la base de datos?").setPositiveButton("SI", dialogClickListener)
                    .setNegativeButton("NO", dialogClickListener).show();
        }else{
            Intent intent = new Intent(Fonorespiratoria1Activity.this, MainActivity.class);
            startActivity(intent);
        }

    }


    private void safeToCloudStorage() throws FileNotFoundException {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault());
        String currentDate = sdf.format(new Date());

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference().child("audio/Grupo fónico_"+currentDate);
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