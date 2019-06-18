package com.kdp.tptdial;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    TptDialView tptDialView;
//    SeekBar seekBar;
    private TextView tvValue,tv_min,tv_max;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tptDialView = findViewById(R.id.tptdial);

        tvValue = findViewById(R.id.tv_value);
        tv_min = findViewById(R.id.tv_min);
        tv_max = findViewById(R.id.tv_max);
        tv_min.setText(tptDialView.getMinValue()+"");
        tv_max.setText(tptDialView.getMaxValue()+"");
        tptDialView.setOnSlideChangedListener(new TptDialView.OnSlideChangedListener() {
            @Override
            public void onSlideChanged(int position, float value) {
                tvValue.setText("位置： " + position + "  数值为:   " +value);
            }
        });



    }
}
