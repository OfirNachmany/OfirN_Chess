package com.example.chess;

import android.content.Context;
import android.graphics.Bitmap;

public class Bishop extends Piece {

    private final Board board;

    public Bishop(Context context, Board board, int col, int row, boolean isWhite) {
        super(context, board, col, row, isWhite, R.drawable.chess_pieces);
        this.board = board;
        this.name = "Bishop";
    }

    @Override
    public void determinePieceImageResource() {
        int spriteSize = spriteSheet.getWidth() / 6;

        int xOffset = 2 * spriteSize;
        int yOffset = isWhite ? 0 : spriteSize;

        // Extract the piece image from the sprite sheet
        Bitmap pieceBitmap = Bitmap.createBitmap(spriteSheet, xOffset + 3, yOffset, spriteSize, spriteSize);

        // Set the image to the ImageView
        imageView.setImageBitmap(pieceBitmap);
    }

    public boolean isValidMovement(int col, int row) {
        return Math.abs(this.col - col) == Math.abs(this.row - row);
    }

    public boolean moveCollidesWithPiece(int col, int row) {

        //up left

        if(this.col > col && this.row > row)
            for(int i = 1; i < Math.abs(this.col - col); i++)
                if(board.getPiece(this.col - i, this.row - i) != null)
                    return true;


        //up right

        if(this.col < col && this.row > row)
            for(int i = 1; i < Math.abs(this.col - col); i++)
                if(board.getPiece(this.col + i, this.row - i) != null)
                    return true;

        //down left

        if(this.col > col && this.row < row)
            for(int i = 1; i < Math.abs(this.col - col); i++)
                if(board.getPiece(this.col - i, this.row + i) != null)
                    return true;


        //down right

        if(this.col < col && this.row < row)
            for(int i = 1; i < Math.abs(this.col - col); i++)
                if(board.getPiece(this.col + i, this.row + i) != null)
                    return true;



        return false;
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
        return "Bishop";
    }

}