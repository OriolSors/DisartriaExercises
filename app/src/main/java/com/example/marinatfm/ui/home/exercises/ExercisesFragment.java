package com.example.marinatfm.ui.home.exercises;

import androidx.lifecycle.ViewModelProvider;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.marinatfm.MainActivity;
import com.example.marinatfm.R;
import com.example.marinatfm.databinding.ExercisesFragmentBinding;

public class ExercisesFragment extends Fragment {

    private ExercisesViewModel exercisesViewModel;
    private ExercisesFragmentBinding binding;

    ListView listView;

    String[] blocks = {"block1", "block2", "dslkjgsa","dsagas","dgasga","block2", "dslkjgsa","dsagas","dgasga"};
    String[] exercises = {"block1DESC", "block2DESC", "klasdghg","dfhda","dsagag","block2", "dslkjgsa","dsagas","dgasga"};
    int[] images = {R.drawable.ic_home_black_24dp,
            R.drawable.ic_notifications_black_24dp,
            R.drawable.ic_launcher_background,
            R.drawable.ic_dashboard_black_24dp,
            R.drawable.ic_launcher_foreground,
            R.drawable.ic_home_black_24dp,
            R.drawable.ic_dashboard_black_24dp,
            R.drawable.ic_launcher_foreground,
            R.drawable.ic_home_black_24dp};

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        exercisesViewModel = new ViewModelProvider(this).get(ExercisesViewModel.class);

        binding = ExercisesFragmentBinding.inflate(inflater, container, false);

        listView = binding.listView;

        /*
        blocks = getBlockNames();
        exercises = getExerciseNames();
        images = getExerciseImages();
         */

        MyAdapter adapter = new MyAdapter(requireActivity(), blocks, exercises, images);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position ==  0) {
                    Toast.makeText(requireContext(), "Facebook Description", Toast.LENGTH_SHORT).show();
                }
                if (position ==  1) {
                    Toast.makeText(requireContext(), "Whatsapp Description", Toast.LENGTH_SHORT).show();
                }
            }
        });


        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


    static class MyAdapter extends ArrayAdapter<String> {

        Context context;
        String[] rTitle;
        String[] rDescription;
        int[] rImgs;

        MyAdapter (Context c, String[] title, String[] description, int[] imgs) {
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
            images.setImageResource(rImgs[position]);
            myTitle.setText(rTitle[position]);
            myDescription.setText(rDescription[position]);

            return row;
        }
    }


}