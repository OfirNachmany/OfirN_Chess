package com.example.chess;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class ChessGameActivity extends AppCompatActivity {

    private CountDownTimer player1Timer;
    private CountDownTimer player2Timer;
    private long bonusTimeMillis;
    private TextView player1MainTimeTextView;
    private TextView player2MainTimeTextView;
    private long player1TimeLeft;
    private long player2TimeLeft;
    private boolean lastTurnWhiteTurn;
    private Board board = null;
    private CountDownTimer infinityCheckTimer;
    int squareSize = 125;
    private PopupWindow popupWindow;
    private GridLayout chessboardGridLayout;
    private int mainTimeInMinutes;
    private int bonusTimeInSeconds;
    private View popupView;
    private TextToSpeech textToSpeech;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private boolean isResetting = false;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ShakeDetector mShakeDetector;
    private boolean isShakingPhoneDialogShown = false;
    private AlertDialog resetGameDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("ChessGameActivity", "onCreate called");
        setContentView(R.layout.activity_chess_game);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


        ImageButton menuButton = findViewById(R.id.menuButton);

        // Set an OnClickListener for the menu button
        menuButton.setOnClickListener(v -> {
            // Inflate the popup_menu.xml layout
            @SuppressLint("InflateParams") View popupView = getLayoutInflater().inflate(R.layout.popup_menu, null);

            // Create the PopupWindow with the inflated view
            popupWindow = new PopupWindow(
                    popupView,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    true);

            // Set a black background for the PopupWindow
            popupWindow.setBackgroundDrawable(new ColorDrawable(Color.BLACK));

            // Set the popup animation style (optional)
            popupWindow.setAnimationStyle(android.R.style.Animation_Dialog);

            // Show the PopupWindow at the center of the screen
            popupWindow.showAtLocation(v, Gravity.CENTER, 0, 0);

            Button logoutButton = popupView.findViewById(R.id.logout);
            logoutButton.setOnClickListener(logOutClickListener);

            Button changeTimerDuration = popupView.findViewById(R.id.changeTimerDuration);
            changeTimerDuration.setOnClickListener(changeTimerDurationClickListener);

            Button resetGame = popupView.findViewById(R.id.resetGame);
            resetGame.setOnClickListener(resetGameClickListener);

            // Set an OnClickListener for the close button in the popup
            Button closeButton = popupView.findViewById(R.id.closeButton);
            closeButton.setOnClickListener(closeClickListener);


        });

        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.setLanguage(Locale.US);
                textToSpeech.setPitch(1.0f);
                textToSpeech.setSpeechRate(1.2f);

                String startingMessage = "The game has been started";
                textToSpeech.speak(startingMessage, TextToSpeech.QUEUE_FLUSH, null, null);
            } else {
                // Initialization failed, handle error
                Log.e("TTS", "Initialization failed");
            }
        });

        mSensorManager = (SensorManager) getSystemService(this.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mShakeDetector = new ShakeDetector(new ShakeDetector.OnShakeListener() {
            @Override
            public void onShake(int count) {
                if (!isShakingPhoneDialogShown) {
                    showResetGameDialog();
                } else if (resetGameDialog != null && resetGameDialog.isShowing()) {
                    resetGame();
                    isShakingPhoneDialogShown = false;
                    resetGameDialog.dismiss();
                }
            }
        });


        // Declare chessboardGridLayout at the class level
        chessboardGridLayout = findViewById(R.id.chessboardGridLayout);


        board = new Board(this, chessboardGridLayout, squareSize);

        player1MainTimeTextView = findViewById(R.id.player1MainTimeTextView);
        player2MainTimeTextView = findViewById(R.id.player2MainTimeTextView);


        // Retrieve selected times from the Intent
        Intent intent = getIntent();
        mainTimeInMinutes = intent.getIntExtra("MAIN_TIME", -1);
        bonusTimeInSeconds = intent.getIntExtra("BONUS_TIME", -1);

        if (mainTimeInMinutes != -1 || bonusTimeInSeconds != -1) {
            bonusTimeMillis = bonusTimeInSeconds * 1000L; // Convert seconds to milliseconds
            if (bonusTimeInSeconds == -1) {
                bonusTimeMillis = 0;
            }

            player1TimeLeft = (long) mainTimeInMinutes * 60 * 1000;
            player2TimeLeft = (long) mainTimeInMinutes * 60 * 1000;

            lastTurnWhiteTurn = board.whiteTurn;

            startChessClock();

        } else {
            player1MainTimeTextView.setVisibility(View.GONE);
            player2MainTimeTextView.setVisibility(View.GONE);
        }

        loadRecentGameFromFirestore();

        startInfinityCheckTimer();
    }

    private void startChessClock() {
        if (board.whiteTurn) {
            startPlayer1Timer();
        } else {
            startPlayer2Timer();
        }
    }

    private void startPlayer1Timer() {
        updateClockDisplay(player1TimeLeft, player2TimeLeft);
        player1Timer = new CountDownTimer(player1TimeLeft, 10) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (isResetting) {
                    isResetting = false;
                    player2TimeLeft = (long) mainTimeInMinutes * 60 * 1000;
                }
                player1TimeLeft = millisUntilFinished;
                updateClockDisplay(player1TimeLeft, player2TimeLeft);

            }

            @Override
            public void onFinish() {
                player1TimeLeft = 0;
                updateClockDisplay(player1TimeLeft, player2TimeLeft);

                if (board.isInsufficientMaterialForBlack()) {
                    String text = "Draw by insufficient materials versus timeout";
                    textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
                    declareGameMessage(null, "insufficient materials vs timeout", "Draw");
                } else {
                    String text = "Black won by timeout";
                    textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
                    declareGameMessage("Black", "timeout", "won");
                }
                board.isGameOver = true;
            }
        }.start();
    }

    private void startPlayer2Timer() {
        player2Timer = new CountDownTimer(player2TimeLeft, 10) {
            @Override
            public void onTick(long millisUntilFinished) {
                player2TimeLeft = millisUntilFinished;
                updateClockDisplay(player1TimeLeft, player2TimeLeft);
            }

            @Override
            public void onFinish() {
                player2TimeLeft = 0;
                updateClockDisplay(player1TimeLeft, player2TimeLeft);
                if (board.isInsufficientMaterialForWhite()) {
                    String text = "Draw by insufficient materials versus timeout";
                    textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
                    declareGameMessage(null, "insufficient materials vs timeout", "Draw");
                } else {
                    String text = "White won by timeout";
                    textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
                    declareGameMessage("White", "timeout", "won");
                }
                board.isGameOver = true;
            }
        }.start();
    }

    private void updateClockDisplay(long player1Time, long player2Time) {

        updatePlayerClock(player1MainTimeTextView, player1Time);
        updatePlayerClock(player2MainTimeTextView, player2Time);

        if (lastTurnWhiteTurn != board.whiteTurn) {
            lastTurnWhiteTurn = board.whiteTurn;
            if (lastTurnWhiteTurn) {
                player2TimeLeft += bonusTimeMillis;
                startPlayer1Timer();
                player2Timer.cancel();
            } else {
                player1TimeLeft += bonusTimeMillis;
                startPlayer2Timer();
                player1Timer.cancel();
            }
        }
        lastTurnWhiteTurn = board.whiteTurn;
    }

    private void updatePlayerClock(TextView mainTimeTextView, long remainingMillis) {
        long hours = remainingMillis / 3600000;
        long minutes = remainingMillis % (3600000) / 60000;
        long seconds = (remainingMillis % 60000) / 1000;
        long milliseconds = remainingMillis % 1000;

        String displayTime;

        if (remainingMillis >= 60 * 60 * 1000) {
            // If remaining time is more than 1 hour, display as h:mm:ss
            displayTime = String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, seconds);
        } else if (remainingMillis >= 20 * 1000) {
            displayTime = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        } else {
            // If remaining time is less than 20 seconds, display in red as mm:ss:msms
            displayTime = String.format(Locale.getDefault(), "%02d:%02d:%03d", minutes, seconds, milliseconds);
            mainTimeTextView.setTextColor(Color.RED);
        }

        // Update UI with displayTime for the specific player
        mainTimeTextView.setText(displayTime);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Cancel the timer to prevent memory leaks
        if (player1Timer != null) {
            player1Timer.cancel();
        }
        if (player2Timer != null) {
            player2Timer.cancel();
        }
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }

    private void startInfinityCheckTimer() {
        infinityCheckTimer = new CountDownTimer(Long.MAX_VALUE, 75) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (board.fiftyMoveRuleIndex > 100) {
                    String text = "Draw by fifty moves rule";
                    textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
                    declareGameMessage(null, "50 moves rule", "Draw");
                }
                if (board.isRepetition()) {
                    String text = "Draw by repetition";
                    textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
                    declareGameMessage(null, "repetition", "Draw");
                }
                if (board.isInsufficientMaterialForBlack() && board.isInsufficientMaterialForWhite()) {
                    String text = "Draw by insufficient materials";
                    textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
                    declareGameMessage(null, "insufficient materials", "Draw");
                }
                if (board.isCheckmate) {
                    String text = (board.whiteTurn ? "Black" : "White") + " won by checkmate";
                    textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
                    declareGameMessage((board.whiteTurn ? "Black" : "White"), "checkmate", "won");
                }
                if (board.isStalemate) {
                    String text = "Draw by stalemate";
                    textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
                    declareGameMessage(null, "stalemate", "Draw");
                }
                if (popupView != null && !popupView.isClickable()) {
                    popupView.setClickable(true);
                }
                if (board.isSwitchTurns && !board.isGameOver) {
                    String text;

                    String toColInWords = null;
                    String fromColInWords = null;
                    String toRowInLetter = null;
                    String fromRowInLetter = null;

                    String[] rowInLetter = {"a", "b", "c", "d", "e", "f", "g", "h"};
                    String[] colInWords = {"eight", "seven", "six", "five", "four", "three", "two", "one"};
                    if (board.lastMovedPiece != null) {
                        for (int i = 0; i < 8; i++) {
                            if (board.lastMovedPieceMovedToCol == i) {
                                toColInWords = colInWords[i];
                            }
                            if (board.lastMovedPieceFromCol == i) {
                                fromColInWords = colInWords[i];
                            }
                            if (board.lastMovedPieceMovedToRow == i) {
                                toRowInLetter = rowInLetter[i];
                            }
                            if (board.lastMovedPieceFromRow == i) {
                                fromRowInLetter = rowInLetter[i];
                            }
                        }
                    }
                    if (board.checkScanner.isPlayerInCheck(board.whiteTurn)) {
                        text = (board.whiteTurn ? "White" : "Black") + " is in check!";
                    } else {
                        text = (board.whiteTurn ? "Black" : "White");
                    }
                    if (board.lastMovedPiece.getName().equals("Bishop")) {
                        text += " Bishop";
                    } else if (board.lastMovedPiece.getName().equals("King")) {
                        text += " King";
                    } else if (board.lastMovedPiece.getName().equals("Queen")) {
                        text += " Queen";
                    } else if (board.lastMovedPiece.getName().equals("Knight")) {
                        text += " Knight";
                    } else if (board.lastMovedPiece.getName().equals("Rook")) {
                        text += " Rook";
                    }

                    if (board.canTwoPiecesMoveToSameCol) {
                        text += " " + fromColInWords;
                    }
                    if (board.canTwoPiecesMoveToSameRow) {
                        text += " " + fromRowInLetter;
                    }

                    if (board.isCaptureMove && board.lastMovedPiece != null) {
                        text += " captured " + board.capturedPieceName + " in " + toRowInLetter + " " + toColInWords;
                        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
                    } else if (board.lastMovedPiece != null && board.wasLastMoveEnPassant) {
                        text += " did En Passant to " + toRowInLetter + " " + toColInWords;
                        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
                    } else if (board.lastMovedPiece != null) {
                        text += " moved to " + toRowInLetter + " " + toColInWords;
                        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
                    }
                }
                board.isCaptureMove = false;
                board.isSwitchTurns = false;
                board.wasLastMoveEnPassant = false;
                board.canTwoPiecesMoveToSameCol = false;
                board.canTwoPiecesMoveToSameRow = false;
            }

            @Override
            public void onFinish() {
                infinityCheckTimer.start();
            }
        }.start();
    }

    private void declareGameMessage(String winner, String reason, String result) {

        String message;
        if (winner != null) {
            message = winner + " " + result + "\nby " + reason;
        } else {
            message = result + "\nby " + reason;
        }
        popupView = getLayoutInflater().inflate(R.layout.popup_message, null);
        TextView popupMessageTextView = popupView.findViewById(R.id.popupMessageTextView);
        popupMessageTextView.setText(message);

        PopupWindow popupWindow = new PopupWindow(
                popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true
        );

        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.BLACK));

        int yOffset = 200;
        int[] location = new int[2];
        getWindow().getDecorView().getLocationOnScreen(location);
        int screenHeight = getWindow().getDecorView().getHeight();
        int popupHeight = popupView.getHeight();
        int y = (location[1] + (screenHeight - popupHeight) / 2 - yOffset) / 3;

        popupWindow.showAtLocation(getWindow().getDecorView(), Gravity.CENTER, 0, -y);

        // Stop both timers
        if (player1Timer != null) {
            player1Timer.cancel();
        }
        if (player2Timer != null) {
            player2Timer.cancel();
        }
        infinityCheckTimer.cancel();
        board.isGameOver = true;
    }


    private final View.OnClickListener closeClickListener = v -> {
        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.dismiss();
        }
    };

    View.OnClickListener logOutClickListener = view -> {
        if(board.isGameOver){
            deleteRecentGameFromFirestore();
        }
        else if(!board.getBoardPosition().equals(board.startingPosition())) {
            saveRecentGameToFirestore();
        }
        AppConstants.clearLoginInfo(this);
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(intent);
        finish(); // Close the current activity (optional, depending on your use case)
        Toast.makeText(getApplicationContext(), "Log Out clicked", Toast.LENGTH_SHORT).show();
    };

    View.OnClickListener changeTimerDurationClickListener = view -> {
        if(board.isGameOver){
            deleteRecentGameFromFirestore();
        }
        else if(!board.getBoardPosition().equals(board.startingPosition())) {
            saveRecentGameToFirestore();
        }
        saveRecentGameToFirestore();
        Intent intent = new Intent(getApplicationContext(), SelectTimeDurationActivity.class);
        startActivity(intent);
        finish(); // Close the current activity (optional, depending on your use case)
        Toast.makeText(getApplicationContext(), "Change Timer Duration clicked", Toast.LENGTH_SHORT).show();
    };

    View.OnClickListener resetGameClickListener = view -> {
        resetGame();


        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.dismiss();
        }
    };

    private void resetGame() {
        isResetting = true;

        String startingMessage = "The game has been restarted";
        textToSpeech.speak(startingMessage, TextToSpeech.QUEUE_FLUSH, null, null);

        if (player1Timer != null) {
            player1Timer.cancel();
        }
        if (player2Timer != null) {
            player2Timer.cancel();
        }
        if (mainTimeInMinutes != -1 || bonusTimeInSeconds != -1) {
            bonusTimeMillis = bonusTimeInSeconds * 1000L; // Convert seconds to milliseconds

            if (bonusTimeInSeconds == -1) {
                bonusTimeMillis = 0;
            }

            player1TimeLeft = (long) mainTimeInMinutes * 60 * 1000;
            player2TimeLeft = (long) mainTimeInMinutes * 60 * 1000;

            lastTurnWhiteTurn = board.whiteTurn;

            startChessClock();
        }
        else {
            player1MainTimeTextView.setVisibility(View.GONE);
            player2MainTimeTextView.setVisibility(View.GONE);
        }
        deleteRecentGameFromFirestore();

        startInfinityCheckTimer();

        chessboardGridLayout.removeAllViews();

        board = new Board(this, chessboardGridLayout, squareSize);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(mShakeDetector, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(!board.isGameOver && !board.getBoardPosition().equals(board.startingPosition())) {
            saveRecentGameToFirestore();
        }
        else if(!board.getBoardPosition().equals(board.startingPosition())){
            deleteRecentGameFromFirestore();
        }
        mSensorManager.unregisterListener(mShakeDetector);
    }

    private void showResetGameDialog() {

        if (isShakingPhoneDialogShown) {
            resetGame();
            return;
        }

        isShakingPhoneDialogShown = true;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Do you want to reset the game?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Reset the game
                        resetGame();
                        isShakingPhoneDialogShown = false;
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Close the dialog
                        dialog.dismiss();
                        isShakingPhoneDialogShown = false;
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        // Reset the dialog flag if canceled
                        isShakingPhoneDialogShown = false;
                    }
                });
        resetGameDialog = builder.create();
        resetGameDialog.show();

        Button positiveButton = resetGameDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        Button negativeButton = resetGameDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        if (positiveButton != null && negativeButton != null) {
            positiveButton.setTextColor(Color.WHITE);
            negativeButton.setTextColor(Color.WHITE);
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) positiveButton.getLayoutParams();
            params.gravity = Gravity.START;
            positiveButton.setLayoutParams(params);
            negativeButton.setLayoutParams(params);
        }
    }

    private void saveRecentGameToFirestore() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            String uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

            // Gather the position history from the Board class
            List<String> positionHistory = board.positionHistory;

            // Initialize a StringBuilder to construct the position history string
            StringBuilder positionHistoryStringBuilder = new StringBuilder();

            // Concatenate each board position into the StringBuilder
            for (String position : positionHistory) {
                positionHistoryStringBuilder.append(position);
                positionHistoryStringBuilder.append(",");
            }

            // Convert the StringBuilder to a string
            String positionHistoryString = positionHistoryStringBuilder.toString();

            // Check if the document exists
            DocumentReference docRef = db.collection("recentGame").document(uid);
            docRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        // Document exists, update its fields
                        docRef.update("boardPosition", board.getBoardPosition(),
                                        "whiteTurn", board.whiteTurn,
                                        "player1TimeLeft", player1TimeLeft,
                                        "player2TimeLeft", player2TimeLeft,
                                        "positionHistory", positionHistoryString)
                                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Game state successfully written!"))
                                .addOnFailureListener(e -> Log.w("Firestore", "Error writing document", e));

                    } else {

                        // Document doesn't exist, create it and set its fields
                        db.collection("recentGame").document(uid)
                                .set(new HashMap<>())
                                .addOnSuccessListener(aVoid -> {
                                    docRef.update("boardPosition", board.getBoardPosition(),
                                                    "whiteTurn", board.whiteTurn,
                                                    "player1TimeLeft", player1TimeLeft,
                                                    "player2TimeLeft", player2TimeLeft,
                                                    "positionHistory", positionHistoryString)
                                            .addOnSuccessListener(aVoid1 -> Log.d("Firestore", "Game state successfully written!"))
                                            .addOnFailureListener(e -> Log.w("Firestore", "Error writing document", e));
                                })
                                .addOnFailureListener(e -> Log.w("Firestore", "Error creating document", e));
                    }
                } else {
                    Log.d("Firestore", "get failed with ", task.getException());
                }
            });
        }
    }

    private void loadRecentGameFromFirestore() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            String uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

            DocumentReference docRef = db.collection("recentGame").document(uid);
            docRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {

                        // Extract data from the document
                        String boardPosition = document.getString("boardPosition");
                        String positionHistory = document.getString("positionHistory");
                        // Check if whiteTurn is not null before unboxing
                        Boolean whiteTurnObject = document.getBoolean("whiteTurn");
                        boolean whiteTurn = whiteTurnObject != null ? whiteTurnObject : false;

                        // Check if player1TimeLeft is not null before unboxing
                        Long player1TimeLeftObject = document.getLong("player1TimeLeft");
                        long player1TimeLeft = player1TimeLeftObject != null ? player1TimeLeftObject : 0;

                        // Check if player2TimeLeft is not null before unboxing
                        Long player2TimeLeftObject = document.getLong("player2TimeLeft");
                        long player2TimeLeft = player2TimeLeftObject != null ? player2TimeLeftObject : 0;

                        if (boardPosition == null || whiteTurnObject == null || player1TimeLeftObject == null || player2TimeLeftObject == null || positionHistory == null) {
                            deleteRecentGameFromFirestore();
                        }
                        else {
                            showResumeGameDialog(boardPosition, whiteTurn, player1TimeLeft, player2TimeLeft, positionHistory);
                        }

                    } else {
                        Log.d("Firestore", "No recent game found.");
                    }
                } else {
                    Log.d("Firestore", "get failed with ", task.getException());
                }
            });
        }
    }

    private void showResumeGameDialog(String boardPosition, boolean whiteTurn, long player1Time, long player2Time, String positionHistory) {
        if(player1Timer != null){
            player1Timer.cancel();
        }
        if(player2Timer != null){
            player2Timer.cancel();
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("Resume Game")
                .setMessage("Do you want to resume to your last game?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Set up the board with the loaded data
                    board.setupBoardPosition(boardPosition);
                    board.setPositionHistory(positionHistory);
                    board.whiteTurn = whiteTurn;
                    if(player1Timer != null){
                        player1Timer.cancel();
                    }
                    if(player2Timer != null){
                        player2Timer.cancel();
                    }
                    player1TimeLeft = player1Time;
                    player2TimeLeft = player2Time;
                    if (player1TimeLeft == 0 && player2TimeLeft == 0) {
                        mainTimeInMinutes = -1;
                        bonusTimeInSeconds = -1;
                        player1MainTimeTextView.setVisibility(View.GONE);
                        player2MainTimeTextView.setVisibility(View.GONE);
                    }
                    else{
                        lastTurnWhiteTurn = whiteTurn;
                        startChessClock();
                    }

                    if(board.previousSelectedPiece != null) {
                        board.previousSelectedPiece = null;
                        board.removeValidMoveIndicators();
                        board.resetSquareColors();
                    }
                })
                .setNegativeButton("No", (dialog, which) -> {
                    startChessClock();
                })
                .setCancelable(false);

        AlertDialog dialog = builder.create();
        dialog.show();

        // Change the button text color to white
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.WHITE);
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.WHITE);
    }
    private void deleteRecentGameFromFirestore() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            String uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

            db.collection("recentGame").document(uid)
                    .delete()
                    .addOnSuccessListener(aVoid -> Log.d("Firestore", "Recent game successfully deleted!"))
                    .addOnFailureListener(e -> deleteRecentGameFromFirestore());
        }
    }
}