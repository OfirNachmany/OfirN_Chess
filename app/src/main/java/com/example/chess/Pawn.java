package com.example.chess;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;

public class Pawn extends Piece {

    private final Board board;
    public boolean wasEnPassantMoveRight = false;
    public boolean wasEnPassantMoveLeft = false;

    public Pawn(Context context, Board board, int col, int row, boolean isWhite) {
        super(context, board, col, row, isWhite, R.drawable.chess_pieces);
        this.board = board;
        this.name = "Pawn";
    }

    public ImageView getImageView() {
        return imageView;
    }

    @Override
    public void determinePieceImageResource() {
        int spriteSize = spriteSheet.getWidth() / 6;

        int xOffset = 5 * spriteSize;
        int yOffset = isWhite ? 0 : spriteSize;

        // Extract the piece image from the sprite sheet
        Bitmap pieceBitmap = Bitmap.createBitmap(spriteSheet, xOffset + 3, yOffset, spriteSize, spriteSize);

        // Set the image to the ImageView
        imageView.setImageBitmap(pieceBitmap);
    }

    @Override
    public boolean isValidMovement(int col, int row) {
        int colorIndex = isWhite ? 1 : -1;

        // Push pawn 1
        if (this.row == row && col == this.col - colorIndex && board.getPiece(col, row) == null) {
            return true;
        }

        // Push pawn 2
        if (isFirstMove && this.row == row && col == this.col - colorIndex * 2 && board.getPiece(col, row) == null && board.getPiece(col + colorIndex, row) == null) {
            return true;
        }

        // Capture left
        if (row == this.row - 1 && col == this.col - colorIndex && board.getPiece(col, row) != null) {
            return true;
        }

        // Capture right
        if (row == this.row + 1 && col == this.col - colorIndex && board.getPiece(col, row) != null) {
            return true;
        }
        // En passant left
        if (row == this.row - 1 && col == this.col - colorIndex && board.getPiece(this.col, this.row - 1) != null && this.col == (isWhite ? 3 : 4)) {
            if(board.getPiece(this.col, (this.row - 1)).name.equals("Pawn") && board.lastMovedPiece != null && board.lastMovedPiece.getName().equals("Pawn")){
                if(board.lastMovedPiece.getCol() == this.col && board.lastMovedPiece.getRow() == this.row - 1 && board.lastMovedPieceFromCol == (isWhite ? 1 : 6)) {
                    wasEnPassantMoveLeft = true;
                    return true;
                }
            }
        }

        // En passant right
        if (row == this.row + 1 && col == this.col - colorIndex && board.getPiece(this.col, this.row + 1) != null && this.col == (isWhite ? 3 : 4)) {
            if(board.getPiece(this.col, (this.row + 1)).name.equals("Pawn") && board.lastMovedPiece != null && board.lastMovedPiece.getName().equals("Pawn") && board.lastMovedPieceFromCol == (isWhite ? 1 : 6)) {
                if(board.lastMovedPiece.getCol() == this.col && board.lastMovedPiece.getRow() == this.row + 1) {
                    wasEnPassantMoveRight = true;
                    return true;
                }
            }
        }


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
    protected String determinePieceType() {
        return "Pawn";
    }
}