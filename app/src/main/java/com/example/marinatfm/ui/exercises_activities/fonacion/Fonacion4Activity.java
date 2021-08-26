package com.example.marinatfm.ui.exercises_activities.fonacion;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Space;
import android.widget.TextView;

import com.example.marinatfm.MainActivity;
import com.example.marinatfm.R;
import com.example.marinatfm.databinding.ActivityFonacion4Binding;
import com.example.marinatfm.databinding.ActivityMainBinding;
import com.example.marinatfm.ui.home.HomeFragmentDirections;

public class Fonacion4Activity extends AppCompatActivity {

    private ActivityFonacion4Binding binding;

    private CharSequence[] sentences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFonacion4Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupMainWindowDisplayMode();
        sentences = getResources().getTextArray(R.array.sentences_fonacion_4);

        binding.startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //TODO: Start recording audio

                loadSentences();
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
                                binding.sentencesLayout.removeAllViews();
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

                AlertDialog.Builder builder = new AlertDialog.Builder(Fonacion4Activity.this);
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
                                Intent intent = new Intent(Fonacion4Activity.this,MainActivity.class);
                                startActivity(intent);


                                //TODO: Safe to Cloud Storage the recorded audio

                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                //No button clicked
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(Fonacion4Activity.this);
                builder.setMessage("VAS A FINALIZAR EL EJERCICIO:").setPositiveButton("SI", dialogClickListener)
                        .setNegativeButton("NO", dialogClickListener).show();

            }
        });


    }

    private void loadSentences() {
        binding.scrollView2.scrollTo(0,0);
        for (CharSequence sentence: sentences){
            TextView textView = new TextView(this);
            textView.setText(sentence);
            textView.setTextColor(getResources().getColor(R.color.purple_500));
            textView.setTextSize(24);
            binding.sentencesLayout.addView(textView);

            Space space = new Space(this);
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            space.setLayoutParams(params);
            space.getLayoutParams().height = 110;
            binding.sentencesLayout.addView(space);
        }
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