package com.example.chess;

import android.content.Context;
import android.graphics.Bitmap;

public class Queen extends Piece {

    private final Board board;
    public Queen(Context context, Board board, int col, int row, boolean isWhite) {
        super(context, board, col, row, isWhite, R.drawable.chess_pieces);
        this.board = board;
        this.name = "Queen";
    }

    @Override
    public void determinePieceImageResource() {
        int spriteSize = spriteSheet.getWidth() / 6;

        int yOffset = isWhite ? 0 : spriteSize;

        // Extract the piece image from the sprite sheet
        Bitmap pieceBitmap = Bitmap.createBitmap(spriteSheet, spriteSize + 3, yOffset, spriteSize, spriteSize);

        // Set the image to the ImageView
        imageView.setImageBitmap(pieceBitmap);
    }

    @Override
    public boolean isValidMovement(int col, int row) {
        return this.col == col || this.row == row || Math.abs(this.col - col) == Math.abs(this.row - row);
    }

    public boolean moveCollidesWithPiece(int col, int row) {

        if (this.col == col || this.row == row) {

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
        }
        else {
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
        }


        return false;
    }

    @Override
    protected int calculateXOffsetBasedOnTypeAndColor() {
        return pieceSize;
    }

    @Override
    protected int calculateYOffsetBasedOnTypeAndColor() {
        return isWhite ? 0 : pieceSize;
    }

    @Override
    protected String determinePieceType() {
        return "Queen";
    }
}