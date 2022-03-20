package com.lzw.demo;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private TextView mTextView;
    private LZWAsyncLayoutInflater asyncLayoutInflater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        View view = LayoutInflater.from(this).inflate(R.layout.item_demo, null, false);
        Log.i(TAG, "(0) 同步渲染: " + view);

        asyncLayoutInflater = new LZWAsyncLayoutInflater(this);
        asyncLayoutInflater.inflate(R.layout.item_demo, null, new LZWAsyncLayoutInflater.OnInflateFinishedListener() {
            @Override
            public void onInflateFinished(@NonNull View view, int resid, @Nullable ViewGroup parent) {
                Log.i(TAG, "(1) onInflateFinished: " + view);
                setContentView(view);
                mTextView = findViewById(R.id.textview);
                mTextView.setText("Hello World");
            }
        });

        asyncLayoutInflater.inflate(R.layout.main_activity, null, new LZWAsyncLayoutInflater.OnInflateFinishedListener() {
            @Override
            public void onInflateFinished(@NonNull View view, int resid, @Nullable ViewGroup parent) {
                Log.i(TAG, "(2) onInflateFinished: " + view);
            }
        });
        asyncLayoutInflater.inflate(R.layout.main_activity, null, new LZWAsyncLayoutInflater.OnInflateFinishedListener() {
            @Override
            public void onInflateFinished(@NonNull View view, int resid, @Nullable ViewGroup parent) {
                Log.i(TAG, "(3) onInflateFinished: " + view);
            }
        });
        asyncLayoutInflater.inflate(R.layout.main_activity, null, new LZWAsyncLayoutInflater.OnInflateFinishedListener() {
            @Override
            public void onInflateFinished(@NonNull View view, int resid, @Nullable ViewGroup parent) {
                Log.i(TAG, "(4) onInflateFinished: " + view);
            }
        });
        asyncLayoutInflater.inflate(R.layout.main_activity, null, new LZWAsyncLayoutInflater.OnInflateFinishedListener() {
            @Override
            public void onInflateFinished(@NonNull View view, int resid, @Nullable ViewGroup parent) {
                Log.i(TAG, "(5) onInflateFinished: " + view);
                asyncLayoutInflater.cancel();
            }
        });



        asyncLayoutInflater.inflate(R.layout.main_activity, null, new LZWAsyncLayoutInflater.OnInflateFinishedListener() {
            @Override
            public void onInflateFinished(@NonNull View view, int resid, @Nullable ViewGroup parent) {
                Log.i(TAG, "(6) onInflateFinished: " + view);
            }
        });
        asyncLayoutInflater.inflate(R.layout.main_activity, null, new LZWAsyncLayoutInflater.OnInflateFinishedListener() {
            @Override
            public void onInflateFinished(@NonNull View view, int resid, @Nullable ViewGroup parent) {
                Log.i(TAG, "(7) onInflateFinished: " + view);
            }
        });
        asyncLayoutInflater.inflate(R.layout.main_activity, null, new LZWAsyncLayoutInflater.OnInflateFinishedListener() {
            @Override
            public void onInflateFinished(@NonNull View view, int resid, @Nullable ViewGroup parent) {
                Log.i(TAG, "(8) onInflateFinished: " + view);
            }
        });
        asyncLayoutInflater.inflate(R.layout.main_activity, null, new LZWAsyncLayoutInflater.OnInflateFinishedListener() {
            @Override
            public void onInflateFinished(@NonNull View view, int resid, @Nullable ViewGroup parent) {
                Log.i(TAG, "(9) onInflateFinished: " + view);
            }
        });
        asyncLayoutInflater.inflate(R.layout.main_activity, null, new LZWAsyncLayoutInflater.OnInflateFinishedListener() {
            @Override
            public void onInflateFinished(@NonNull View view, int resid, @Nullable ViewGroup parent) {
                Log.i(TAG, "(10) onInflateFinished: " + view);
                mTextView.setTextColor(Color.RED);
            }
        });

        asyncLayoutInflater.inflate(R.layout.main_activity, null, new LZWAsyncLayoutInflater.OnInflateFinishedListener() {
            @Override
            public void onInflateFinished(@NonNull View view, int resid, @Nullable ViewGroup parent) {
                Log.i(TAG, "(11) onInflateFinished: " + view);
            }
        });
        asyncLayoutInflater.inflate(R.layout.main_activity, null, new LZWAsyncLayoutInflater.OnInflateFinishedListener() {
            @Override
            public void onInflateFinished(@NonNull View view, int resid, @Nullable ViewGroup parent) {
                Log.i(TAG, "(12) onInflateFinished: " + view);
            }
        });
        asyncLayoutInflater.inflate(R.layout.main_activity, null, new LZWAsyncLayoutInflater.OnInflateFinishedListener() {
            @Override
            public void onInflateFinished(@NonNull View view, int resid, @Nullable ViewGroup parent) {
                Log.i(TAG, "(13) onInflateFinished: " + view);
            }
        });
        asyncLayoutInflater.inflate(R.layout.main_activity, null, new LZWAsyncLayoutInflater.OnInflateFinishedListener() {
            @Override
            public void onInflateFinished(@NonNull View view, int resid, @Nullable ViewGroup parent) {
                Log.i(TAG, "(14) onInflateFinished: " + view);
            }
        });
        asyncLayoutInflater.inflate(R.layout.main_activity, null, new LZWAsyncLayoutInflater.OnInflateFinishedListener() {
            @Override
            public void onInflateFinished(@NonNull View view, int resid, @Nullable ViewGroup parent) {
                Log.i(TAG, "(15) onInflateFinished: " + view);
            }
        });
        asyncLayoutInflater.inflate(R.layout.main_activity, null, new LZWAsyncLayoutInflater.OnInflateFinishedListener() {
            @Override
            public void onInflateFinished(@NonNull View view, int resid, @Nullable ViewGroup parent) {
                Log.i(TAG, "(16) onInflateFinished: " + view);
            }
        });
        asyncLayoutInflater.inflate(R.layout.main_activity, null, new LZWAsyncLayoutInflater.OnInflateFinishedListener() {
            @Override
            public void onInflateFinished(@NonNull View view, int resid, @Nullable ViewGroup parent) {
                Log.i(TAG, "(17) onInflateFinished: " + view);
            }
        });
        asyncLayoutInflater.inflate(R.layout.main_activity, null, new LZWAsyncLayoutInflater.OnInflateFinishedListener() {
            @Override
            public void onInflateFinished(@NonNull View view, int resid, @Nullable ViewGroup parent) {
                Log.i(TAG, "(18) onInflateFinished: " + view);
            }
        });
        asyncLayoutInflater.inflate(R.layout.main_activity, null, new LZWAsyncLayoutInflater.OnInflateFinishedListener() {
            @Override
            public void onInflateFinished(@NonNull View view, int resid, @Nullable ViewGroup parent) {
                Log.i(TAG, "(19) onInflateFinished: " + view);
            }
        });
        asyncLayoutInflater.inflate(R.layout.main_activity, null, new LZWAsyncLayoutInflater.OnInflateFinishedListener() {
            @Override
            public void onInflateFinished(@NonNull View view, int resid, @Nullable ViewGroup parent) {
                Log.i(TAG, "(20) onInflateFinished: " + view);
            }
        });

    }

}
