package com.kdp.tptdial;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.widget.SeekBar;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity{
    TptDialView tptDialView;
    private TextView tvValue,tv_min,tv_max;
    private SeekBar startAngle,sweepAngle;
    private SeekBar startR,startG,startB,endR,endG,endB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        initDatas();


        tptDialView.setOnSlideChangedListener(new TptDialView.OnSlideChangedListener() {
            @Override
            public void onSlideChanged(int position, float value) {
                tvValue.setText(value+"");
            }
        });


        startAngle.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tptDialView.setDialStartAngle(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        sweepAngle.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tptDialView.setDialSweepAngle(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        startR.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tptDialView.setScaleStartColor(progress, Color.green(tptDialView.getScaleStartColor()),Color.blue(tptDialView.getScaleEndColor()));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        startG.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tptDialView.setScaleStartColor(Color.red(tptDialView.getScaleStartColor()),progress,Color.blue(tptDialView.getScaleEndColor()));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        startB.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tptDialView.setScaleStartColor(Color.red(tptDialView.getScaleStartColor()),Color.green(tptDialView.getScaleStartColor()),progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        endR.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tptDialView.setScaleEndColor(progress, Color.green(tptDialView.getScaleEndColor()),Color.blue(tptDialView.getScaleEndColor()));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        endG.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tptDialView.setScaleEndColor(Color.red(tptDialView.getScaleEndColor()),progress,Color.blue(tptDialView.getScaleEndColor()));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        endB.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tptDialView.setScaleEndColor(Color.red(tptDialView.getScaleEndColor()),Color.green(tptDialView.getScaleEndColor()),progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


    }

    private void initDatas() {
        tvValue.setText(tptDialView.getMinValue() + "");
        tv_min.setText("最小刻度值： " +tptDialView.getMinValue());
        tv_max.setText("最大刻度值： " +tptDialView.getMaxValue());
        startAngle.setProgress(tptDialView.getDialStartAngle());
        sweepAngle.setProgress(tptDialView.getDialSweepAngle());
        startR.setProgress(Color.red(tptDialView.getScaleStartColor()));
        startG.setProgress(Color.green(tptDialView.getScaleStartColor()));
        startB.setProgress(Color.blue(tptDialView.getScaleStartColor()));
        endR.setProgress(Color.red(tptDialView.getScaleStartColor()));
        endG.setProgress(Color.green(tptDialView.getScaleStartColor()));
        endB.setProgress(Color.blue(tptDialView.getScaleStartColor()));
    }

    private void initViews() {
        tptDialView = findViewById(R.id.tptdial);
        tvValue = findViewById(R.id.tv_value);
        tv_min = findViewById(R.id.tv_min);
        tv_max = findViewById(R.id.tv_max);
        startAngle = findViewById(R.id.startAngle);
        sweepAngle = findViewById(R.id.sweepAngle);
        startR = findViewById(R.id.startR);
        startG = findViewById(R.id.startG);
        startB = findViewById(R.id.startB);
        endR = findViewById(R.id.endR);
        endG = findViewById(R.id.endG);
        endB = findViewById(R.id.endB);

    }


}
