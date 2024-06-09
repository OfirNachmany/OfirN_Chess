package com.example.chess;

import android.content.Context;
import android.graphics.Bitmap;

public class King extends Piece {

    private final Board board;

    public King(Context context, Board board,  int col, int row, boolean isWhite) {
        super(context, board, col, row, isWhite, R.drawable.chess_pieces);
        this.board = board;
        this.name = "King";
    }

    @Override
    public void determinePieceImageResource() {
        int spriteSize = spriteSheet.getWidth() / 6;

        int xOffset = 0;
        int yOffset = isWhite ? 0 : spriteSize;

        // Extract the piece image from the sprite sheet
        Bitmap pieceBitmap = Bitmap.createBitmap(spriteSheet, xOffset + 3, yOffset, spriteSize, spriteSize);

        // Set the image to the ImageView
        imageView.setImageBitmap(pieceBitmap);
    }

    @Override
    public boolean isValidMovement(int col, int row) {
        return Math.abs((col - this.col) * (row - this.row)) == 1 || Math.abs(col - this.col) + Math.abs(row - this.row) == 1 || canCastle(col, row);
    }

    public boolean canCastle(int col, int row) {

        if(this.col == col && !board.checkScanner.isPlayerInCheck(this.isWhite())) {

            if(row == 6) {
                Piece rook = board.getPiece(col, 7);
                if(rook != null && rook.isFirstMove && isFirstMove && !board.checkScanner.isKingChecked(new Move(board, this, col, 5))) {
                    return board.getPiece(col, 5) == null &&
                            board.getPiece(col, 6) == null;
                }
            }
            else if(row == 2) {
                Piece rook = board.getPiece(col, 0);
                if(rook != null && rook.isFirstMove && isFirstMove && !board.checkScanner.isKingChecked(new Move(board, this, col, 3))) {
                    return board.getPiece(col, 3) == null &&
                            board.getPiece(col, 2) == null &&
                            board.getPiece(col, 1) == null;
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
        int spriteSize = spriteSheet.getWidth() / 6;
        return isWhite ? 0 : spriteSize;
    }

    @Override
    protected String determinePieceType() {
        return "King";
    }
}