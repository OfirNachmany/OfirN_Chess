package com.example.chess;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

public class SelectTimeDurationActivity extends AppCompatActivity {

    private SeekBar mainTimeSeekBar;
    private SeekBar bonusTimeSeekBar;
    private TextView selectedTimeLabel;

    private boolean isInfinityTime = false;


    private int selectedMainTime = 10; // Initial main time in minutes
    private int selectedBonusTime = 0; // Initial bonus time in seconds


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_time_duration);

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


        mainTimeSeekBar = findViewById(R.id.mainTimeSeekBar);
        bonusTimeSeekBar = findViewById(R.id.bonusTimeSeekBar);
        selectedTimeLabel = findViewById(R.id.selectedTimeLabel);
        Button incrementMainTimeButton = findViewById(R.id.incrementMainTimeButton);
        Button decrementMainTimeButton = findViewById(R.id.decrementMainTimeButton);
        Button incrementBonusTimeButton = findViewById(R.id.incrementBonusTimeButton);
        Button decrementBonusTimeButton = findViewById(R.id.decrementBonusTimeButton);
        Button startGameButton = findViewById(R.id.startGameButton);
        CheckBox infinityTimeButton = findViewById(R.id.infinityTimeCheckBox);

        infinityTimeButton.setOnCheckedChangeListener((buttonView, isChecked) -> handleInfinityTimeCheck(isChecked));

        Button logoutButton = findViewById(R.id.logoutButton);


        logoutButton.setOnClickListener(v -> {
            logout();
        });


        mainTimeSeekBar.setMax(90); // Set the maximum main time to 90 minutes
        mainTimeSeekBar.setMin(1); // Set the minimum main time to 1 minute
        mainTimeSeekBar.setProgress(selectedMainTime);

        bonusTimeSeekBar.setMax(60); // Set the maximum bonus time to 60 seconds
        bonusTimeSeekBar.setProgress(selectedBonusTime);


        mainTimeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                selectedMainTime = progress;
                updateTimeLabel(selectedMainTime, selectedBonusTime);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // no need for this (prevents error)
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // no need for this (prevents error)
            }
        });

        bonusTimeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                selectedBonusTime = progress;
                updateTimeLabel(selectedMainTime, selectedBonusTime);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // no need for this (prevents error)
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // no need for this (prevents error)
            }
        });

        incrementMainTimeButton.setOnClickListener(v -> {
            if (selectedMainTime < 90) {
                selectedMainTime++;
                mainTimeSeekBar.setProgress(selectedMainTime);
                updateTimeLabel(selectedMainTime, selectedBonusTime);
            }
        });

        decrementMainTimeButton.setOnClickListener(v -> {
            if (selectedMainTime > 1) {
                selectedMainTime--;
                mainTimeSeekBar.setProgress(selectedMainTime);
                updateTimeLabel(selectedMainTime, selectedBonusTime);
            }
        });

        incrementBonusTimeButton.setOnClickListener(v -> {
            if (selectedBonusTime < 60) {
                selectedBonusTime++;
                bonusTimeSeekBar.setProgress(selectedBonusTime);
                updateTimeLabel(selectedMainTime, selectedBonusTime);
            }
        });

        decrementBonusTimeButton.setOnClickListener(v -> {
            if (selectedBonusTime > 0) {
                selectedBonusTime--;
                bonusTimeSeekBar.setProgress(selectedBonusTime);
                updateTimeLabel(selectedMainTime, selectedBonusTime);
            }
        });

        // Update the label initially
        updateTimeLabel(selectedMainTime, selectedBonusTime);

        startGameButton.setOnClickListener(v -> startGame());
    }

    private void handleInfinityTimeCheck(boolean isChecked) {
        // Restore the previous label text when unchecking the "Infinity Time" checkbox
        isInfinityTime = isChecked;
        updateTimeLabel(selectedMainTime, selectedBonusTime);
    }

    private void logout(){
        AppConstants.clearLoginInfo(this);
        // Start the LoginActivity
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @SuppressLint("SetTextI18n")
    private void updateTimeLabel(int mainTime, int bonusTime) {
        if(isInfinityTime){
            selectedTimeLabel.setText("Infinity Time");
        }
        else {
            selectedTimeLabel.setText("Main Time: " + mainTime + " minutes\nBonus Time: " + bonusTime + " seconds per move");
        }
    }

    private void startGame() {
        // Start the game with the selectedDuration
        Intent intent = new Intent(this, ChessGameActivity.class);

        if (isInfinityTime) {
            // Pass some special values to indicate infinity time
            intent.putExtra("MAIN_TIME", -1);
            intent.putExtra("BONUS_TIME", -1);
        }
        else {
            // Pass the selected time values
            intent.putExtra("MAIN_TIME", selectedMainTime);
            intent.putExtra("BONUS_TIME", selectedBonusTime);
        }

        startActivity(intent);
        finish();
    }
}