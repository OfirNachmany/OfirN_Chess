package com.example.chess;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.ImageView;

import android.os.Handler;


public abstract class Piece implements Cloneable {
    public int col, row;

    public boolean isWhite;
    public String name;
    public int value;

    public boolean isFirstMove = true;
    protected ImageView imageView;
    protected Bitmap spriteSheet;
    protected static final int pieceSize = 100;
    protected abstract int calculateXOffsetBasedOnTypeAndColor();
    protected abstract int calculateYOffsetBasedOnTypeAndColor();
    public Board board;

    private float initialX, initialY;
    private float targetX, targetY;


    // Define animation duration
    private static final int animationDuration = 170;
    private Handler handler;


    public Piece(Context context, Board board, int col, int row, boolean isWhite, int spriteSheetResId) {
        this.col = col;
        this.row = row;
        this.isWhite = isWhite;
        this.imageView = new ImageView(context);
        this.spriteSheet = BitmapFactory.decodeResource(context.getResources(), spriteSheetResId);
        determinePieceImageResource();
        this.board = board;
    }

    public void determinePieceImageResource() {
        int xOffset = calculateXOffsetBasedOnTypeAndColor();
        int yOffset = calculateYOffsetBasedOnTypeAndColor();

        Bitmap pieceImage = Bitmap.createBitmap(spriteSheet, xOffset, yOffset, pieceSize, pieceSize);

        imageView.setImageBitmap(pieceImage);
    }

    public boolean isValidMovement(int targetCol, int targetRow) {
        Log.d("Piece", "isValidMovement called");

        return true;

    }
    public boolean moveCollidesWithPiece(int col, int row) {
        return false;
    }

    public ImageView getImageView() {
        return imageView;
    }

    public int getCol() {
        return col;
    }
    public int getRow() {
        return row;
    }

    public boolean isWhite() {
        return this.isWhite;
    }


    public void movePiece(int newCol, int newRow) {
        boolean isFakeMove = this.col == newCol && this.row == newRow;
        this.col = newCol;
        this.row = newRow;

        this.isFirstMove = false;
        if(!isFakeMove) {
            animatePieceMovement(row, col);
        }
    }

    public void fakeMovePiece(int newCol, int newRow){
        this.col = newCol;
        this.row = newRow;
    }

    private void updatePiecePosition(int newCol, int newRow) {
        col = newCol;
        row = newRow;
    }


    public String getName() {

        name = determinePieceType();
        return name;
    }

    public void animatePieceMovement(int newCol, int newRow) {
        handler = new Handler();
        targetX = calculateX(newCol);
        targetY = calculateY(newRow);
        initialX = imageView.getX();
        initialY = imageView.getY();

        // Calculate total distance to move
        final float totalDistanceX = targetX - initialX;
        final float totalDistanceY = targetY - initialY;
        final long startTime = System.currentTimeMillis();

        Runnable animationRunnable = new Runnable() {
            @Override
            public void run() {
                long currentTime = System.currentTimeMillis();
                long elapsedTime = currentTime - startTime;
                if (elapsedTime < animationDuration) {
                    // Calculate progress based on elapsed time and total duration
                    float progress = (float) elapsedTime / animationDuration;

                    // Calculate intermediate position based on progress
                    float intermediateX = initialX + (totalDistanceX * progress);
                    float intermediateY = initialY + (totalDistanceY * progress);
                    imageView.setX(intermediateX);
                    imageView.setY(intermediateY);
                    handler.postDelayed(this, 16);
                }
                else {
                    imageView.setX(targetX);
                    imageView.setY(targetY);
                    updatePiecePosition(newRow, newCol);
                }
            }
        };
        // Start the animation loop
        handler.post(animationRunnable);
    }
    protected float calculateX(int col) {
        return col * board.squareSize;
    }
    protected float calculateY(int row) {
        return row * board.squareSize;
    }


    protected abstract String determinePieceType();



}