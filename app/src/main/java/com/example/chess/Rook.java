package com.example.chess;

import android.content.Context;
import android.graphics.Bitmap;

public class Rook extends Piece {

    private final Board board;

    public Rook(Context context, Board board, int col, int row, boolean isWhite) {
        super(context, board, col, row, isWhite, R.drawable.chess_pieces);
        this.board = board;
        this.name = "Rook";
    }

    @Override
    public boolean isValidMovement(int col, int row) {
        return this.col == col || this.row == row;
    }

    public boolean moveCollidesWithPiece(int col, int row) {

        //left
        if (this.col > col)
            for(int c = this.col - 1; c > col; c--)
                if(board.getPiece(c, this.row) != null)
                    return true;


        //right
        if (this.col < col)
            for(int c = this.col + 1; c < col; c++)
                if(board.getPiece(c, this.row) != null)
                    return true;

        //up
        if (this.row > row)
            for(int r = this.row - 1; r > row; r--)
                if(board.getPiece(this.col, r) != null)
                    return true;


        //down
        if (this.row < row)
            for(int r = this.row + 1; r < row; r++)
                if(board.getPiece(this.col, r) != null)
                    return true;


        return false;
    }


    @Override
    protected int calculateXOffsetBasedOnTypeAndColor() {
        return 0;
    }

    @Override
    protected int calculateYOffsetBasedOnTypeAndColor() {
        return 0;
    }

    @Override
    public void determinePieceImageResource() {
        int spriteSize = spriteSheet.getWidth() / 6;

        int xOffset = 4 * spriteSize;
        int yOffset = isWhite ? 0 : spriteSize;

        // Extract the piece image from the sprite sheet
        Bitmap pieceBitmap = Bitmap.createBitmap(spriteSheet, xOffset + 3, yOffset, spriteSize, spriteSize);

        // Set the image to the ImageView
        imageView.setImageBitmap(pieceBitmap);
    }

    @Override
    protected String determinePieceType() {
        return "Rook";
    }
}