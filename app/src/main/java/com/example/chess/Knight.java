package com.example.chess;

import android.content.Context;
import android.graphics.Bitmap;
public class Knight extends Piece {

    public Knight(Context context, Board board, int col, int row, boolean isWhite) {
        super(context, board, col, row, isWhite, R.drawable.chess_pieces);
        this.board = board;
        this.name = "Knight";
    }

    @Override
    public void determinePieceImageResource() {
        int spriteSize = spriteSheet.getWidth() / 6;

        int xOffset = 3 * spriteSize;
        int yOffset = isWhite ? 0 : spriteSize;

        // Extract the piece image from the sprite sheet
        Bitmap pieceBitmap = Bitmap.createBitmap(spriteSheet, xOffset + 3, yOffset, spriteSize, spriteSize);

        // Set the image to the ImageView
        imageView.setImageBitmap(pieceBitmap);
    }

    @Override
    public boolean isValidMovement(int col, int row) {
        int colDiff = Math.abs(col - this.col);
        int rowDiff = Math.abs(row - this.row);

        // Knights move in an "L" shape: 2 squares in one direction and 1 square perpendicular
        return (colDiff == 2 && rowDiff == 1) || (colDiff == 1 && rowDiff == 2);
    }

    @Override
    protected int calculateXOffsetBasedOnTypeAndColor() {
        return 2 * pieceSize;
    }

    @Override
    protected int calculateYOffsetBasedOnTypeAndColor() {
        return isWhite ? 0 : pieceSize;
    }

    @Override
    protected String determinePieceType() {
        return "Knight";
    }
}