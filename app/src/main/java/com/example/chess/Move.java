package com.example.chess;

public class Move {
    int oldCol, oldRow;
    int newCol, newRow;
    Piece piece;
    Piece capture;
    private boolean promotion;
    public Move(Board board, Piece piece, int newCol, int newRow) {

        this.oldCol = piece.col;
        this.oldRow = piece.row;
        this.newCol = newCol;
        this.newRow = newRow;

        this.piece = piece;
        this.capture = board.getPiece(newCol, newRow);

        this.promotion = false;


    }

    public boolean isPromotion() {
        return promotion;
    }

    public void setPromotion(boolean promotion) {
        this.promotion = promotion;
    }


    @Override
    public String toString() {
        return piece.name + " from " + piece.col + piece.row + " to " + newCol + newRow;
    }

}