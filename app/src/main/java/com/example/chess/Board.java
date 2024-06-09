package com.example.chess;

import android.content.Context;
import android.graphics.BlendMode;
import android.graphics.BlendModeColorFilter;
import android.graphics.Color;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Board {

    public ImageView[][] boardSquares;
    public final int squareSize;
    public final GridLayout chessboardGridLayout;
    public Piece selectedPiece = null;
    public Piece  previousSelectedPiece = null;
    public Piece lastMovedPiece = null;
    public int lastMovedPieceFromCol;
    public int lastMovedPieceFromRow;
    private int previousPieceCol, previousPieceRow;
    public boolean whiteTurn = true;
    public ArrayList<Piece> pieceList = new ArrayList<>();
    public CheckScanner checkScanner = new CheckScanner(this);
    public int fiftyMoveRuleIndex = 0;
    public boolean isGameOver = false;
    public final List<String> positionHistory = new ArrayList<>();
    public boolean isCheckmate = false;
    public boolean isStalemate = false;
    public boolean isSwitchTurns = false;
    public int lastMovedPieceMovedToCol;
    public int lastMovedPieceMovedToRow;
    public boolean isCaptureMove = false;
    public String capturedPieceName;
    public boolean wasLastMoveEnPassant = false;
    public boolean canTwoPiecesMoveToSameCol = false;
    public boolean canTwoPiecesMoveToSameRow = false;



    public Board(Context context, GridLayout parentLayout, int squareSize) {
        this.squareSize = squareSize;
        this.chessboardGridLayout = parentLayout;
        initializeBoard(context, chessboardGridLayout);
        initializePieces(context);
    }

    private void initializeBoard(Context context, GridLayout parentLayout) {
        boardSquares = new ImageView[8][8];

        // Loop to dynamically create ImageViews for each square
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                ImageView square = new ImageView(context);
                square.setId(View.generateViewId()); // Generate unique ID for each square

                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = squareSize;
                params.height = squareSize;

                square.setLayoutParams(params);

                // Customize the appearance of each square
                int backgroundColor = (i + j) % 2 == 0 ? Color.rgb(227, 198, 181) : Color.rgb(157, 105, 53);
                square.setBackgroundColor(backgroundColor);

                parentLayout.addView(square);

                boardSquares[i][j] = square;

            }
        }

        // Attach touch event listener to each chessboard square or piece
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                final int col = i;
                final int row = j;

                boardSquares[i][j].setOnClickListener(v -> handleSquareClick(col, row));
            }
        }


    }

    private void initializePieces(Context context) {

        Pawn pawn;

        for(int i = 0; i < 8; i++) {
            pawn = new Pawn(context, this, 6, i, true);
            addPieceToChessboard(pawn, 6, i);
            pawn = new Pawn(context, this, 1, i, false);
            addPieceToChessboard(pawn, 1, i);
        }

        Rook rook = new Rook(context, this, 7, 0, true);
        addPieceToChessboard(rook, 7, 0);
        rook = new Rook(context, this, 7, 7, true);
        addPieceToChessboard(rook, 7, 7);
        rook = new Rook(context, this, 0, 0, false);
        addPieceToChessboard(rook, 0, 0);
        rook = new Rook(context, this, 0, 7, false);
        addPieceToChessboard(rook, 0, 7);

        Knight knight = new Knight(context, this, 7, 1, true);
        addPieceToChessboard(knight, 7, 1);
        knight = new Knight(context, this, 7, 6, true);
        addPieceToChessboard(knight, 7,  6);
        knight = new Knight(context, this, 0, 1, false);
        addPieceToChessboard(knight, 0, 1);
        knight = new Knight(context, this, 0, 6, false);
        addPieceToChessboard(knight, 0, 6);

        Bishop bishop = new Bishop(context, this, 7, 2, true);
        addPieceToChessboard(bishop, 7, 2);
        bishop = new Bishop(context, this, 7, 5, true);
        addPieceToChessboard(bishop, 7, 5);
        bishop = new Bishop(context, this,  0, 2, false);
        addPieceToChessboard(bishop, 0, 2);
        bishop = new Bishop(context, this, 0, 5, false);
        addPieceToChessboard(bishop, 0, 5);

        Queen queen = new Queen(context, this, 7, 3, true);
        addPieceToChessboard(queen, 7, 3);
        queen = new Queen(context, this, 0, 3, false);
        addPieceToChessboard(queen, 0, 3);

        King king = new King(context, this, 7, 4, true);
        addPieceToChessboard(king, 7, 4);
        king = new King(context, this, 0, 4, false);
        addPieceToChessboard(king, 0, 4);

        for(int c = 0; c < 8; c++) {
            for (int r = 0; r < 8; r++) {
                if(getPiece(c, r) != null){
                    pieceList.add(getPiece(c, r));
                }
            }
        }
    }

    public void addPieceToChessboard(Piece piece, int col, int row) {
        // Create a new ImageView for the piece
        ImageView pieceImageView = piece.getImageView();

        // Check if the pieceImageView already has a parent
        if (pieceImageView.getParent() != null) {
            // Remove it from the parent
            ((ViewGroup) pieceImageView.getParent()).removeView(pieceImageView);
        }

        // Set the Piece object as a tag for later retrieval
        pieceImageView.setTag(piece);
        boardSquares[col][row].setTag(piece);


        // Set layout parameters for the piece
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = squareSize;
        params.height = squareSize;
        params.columnSpec = GridLayout.spec(row);
        params.rowSpec = GridLayout.spec(col);

        // Set the layout parameters to the pieceImageView
        pieceImageView.setLayoutParams(params);
        // Add the pieceImageView to the chessboardGridLayout
        chessboardGridLayout.addView(pieceImageView);
    }

    public Piece getPiece(int col, int row) {
        if (col >= 0 && col < 8 && row >= 0 && row < 8) {
            ImageView square = boardSquares[col][row];
            if (square.getTag() instanceof Piece) {
                return (Piece) square.getTag();
            }
        }
        return null;
    }

    Piece findKing(boolean isWhite) {
        for(Piece piece : pieceList){
            if (piece.getName().equals("King") && piece.isWhite() == isWhite) {
                return piece;
            }
        }
        return null;
    }

    private String generatePositionHash() {
        StringBuilder hashBuilder = new StringBuilder();

        for (int c = 0; c < 8; c++) {
            for (int r = 0; r < 8; r++) {
                Piece piece = getPiece(c, r);
                if (piece == null) {
                    // Use a placeholder character to represent an empty square
                    hashBuilder.append(".");
                } else if(piece.isWhite()) {
                    // Append a unique character for each piece type
                    if (piece instanceof Pawn) {
                        hashBuilder.append("P");
                    } else if (piece instanceof Rook) {
                        hashBuilder.append("R");
                    } else if (piece instanceof Knight) {
                        hashBuilder.append("N");
                    } else if (piece instanceof Bishop) {
                        hashBuilder.append("B");
                    } else if (piece instanceof Queen) {
                        hashBuilder.append("Q");
                    } else if (piece instanceof King) {
                        hashBuilder.append("K");
                    }
                }
                else{
                    if (piece instanceof Pawn) {
                        hashBuilder.append("p");
                    } else if (piece instanceof Rook) {
                        hashBuilder.append("r");
                    } else if (piece instanceof Knight) {
                        hashBuilder.append("n");
                    } else if (piece instanceof Bishop) {
                        hashBuilder.append("b");
                    } else if (piece instanceof Queen) {
                        hashBuilder.append("q");
                    } else if (piece instanceof King) {
                        hashBuilder.append("k");
                    }
                }
            }
        }

        // Append information about the player to move
        hashBuilder.append(whiteTurn ? "W" : "B");

        return hashBuilder.toString();
    }
    public String startingPosition(){
        return "r1n1b1q1k1b1n1r1p1p1p1p1p1p1p1p1................................P1P1P1P1P1P1P1P1R1N1B1Q1K1B1N1R1W";
    }
    public boolean isRepetition() {
        // Check if the current position has occurred 3 times
        String currentPosition = generatePositionHash();
        int count = 0;
        for (String position : positionHistory) {
            if (position.equals(currentPosition)) {
                count++;
                if (count >= 3) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isInsufficientMaterialForWhite() {
        int sumBishops = 0;
        int sumKnights = 0;
        for (Piece piece : pieceList) {
            if (piece != null && piece.isWhite()) {
                if (piece.getName().equals("Bishop")) {
                    sumBishops++;
                }
                if (piece.getName().equals("Knight")) {
                    sumKnights++;
                }
                if (piece.getName().equals("Queen") || piece.getName().equals("Rook") || piece.getName().equals("Pawn")) {
                    return false;
                }
            }

        }
        if (sumBishops <= 1 && sumKnights == 0) {
            return true;
        }
        if (sumKnights <= 1 && sumBishops == 0) {
            return true;
        }

        return false;
    }

    public boolean isInsufficientMaterialForBlack() {
        int sumBishops = 0;
        int sumKnights = 0;
        for (Piece piece : pieceList) {
            if (piece != null && !piece.isWhite()) {
                if (piece.getName().equals("Bishop")) {
                    sumBishops++;
                }
                if (piece.getName().equals("Knight")) {
                    sumKnights++;
                }
                if (piece.getName().equals("Queen") || piece.getName().equals("Rook") || piece.getName().equals("Pawn")) {
                    return false;
                }
            }

        }
        if (sumBishops <= 1 && sumKnights == 0) {
            return true;
        }
        if (sumKnights <= 1 && sumBishops == 0) {
            return true;
        }

        return false;
    }

    public boolean isPieceStoppingCheck(Piece checkingPiece, int col, int row){

        int kingCol = findKing(whiteTurn).getCol();
        int kingRow = findKing(whiteTurn).getRow();

        int checkingPieceCol;
        int checkingPieceRow;

        if(checkingPiece != null) {
            checkingPieceCol = checkingPiece.getCol();
            checkingPieceRow = checkingPiece.getRow();

            // Check if the checking piece and the king are on the same diagonal
            if (Math.abs(checkingPieceCol - kingCol) == Math.abs(checkingPieceRow - kingRow)) {

                // Calculate the distance between the checking piece and the king
                int distanceCol = Math.abs(checkingPieceCol - kingCol);
                int distanceRow = Math.abs(checkingPieceRow - kingRow);

                // Calculate the direction of the movement
                int directionCol = (checkingPieceCol < kingCol) ? -1 : 1;
                int directionRow = (checkingPieceRow < kingRow) ? -1 : 1;

                // Check if the given col and row are within the range of the checking piece and the king
                for (int i = 1; i < Math.max(distanceCol, distanceRow); i++) {
                    int nextCol = kingCol + i * directionCol;
                    int nextRow = kingRow + i * directionRow;

                    // If the next position matches the given column and row, return true
                    if (nextCol == col && nextRow == row) {
                        return true;
                    }
                }
            }


                if (checkingPieceRow == kingRow) {
                    // Check if the given column is between the checking piece and the king
                    int minCol = Math.min(checkingPieceCol, kingCol);
                    int maxCol = Math.max(checkingPieceCol, kingCol);
                    if (col > minCol && col < maxCol) {
                        return true;
                    }
                }

                // Check if the checking piece and the king are on the same column
                if (checkingPieceCol == kingCol) {
                    // Check if the given row is between the checking piece and the king
                    int minRow = Math.min(checkingPieceRow, kingRow);
                    int maxRow = Math.max(checkingPieceRow, kingRow);
                    if (row > minRow && row < maxRow) {
                        return true;
                    }
                }

            }

        return false;
    }

    public boolean isProtectedPiece(Piece piece){
        for(Piece pieces : pieceList){
            if(piece.isWhite() == pieces.isWhite() && piece != pieces){
                if(pieces.isValidMovement(piece.getCol(), piece.getRow())){
                    return true;
                }
            }
        }

        return false;
    }

    public boolean hasEscapeMoves() {
        Piece checkingPiece;
        int pieceCol;
        int pieceRow;


        for (Piece piece : pieceList){
            if(piece.isWhite() == whiteTurn) {
                for (int c = 0; c < 8; c++) {
                    for (int r = 0; r < 8; r++) {
                        if (getPiece(c, r) == null || piece.isWhite() != getPiece(c, r).isWhite()) {
                            if (isValidMove(piece, c, r)) {
                                pieceCol = piece.getCol();
                                pieceRow = piece.getRow();

                                if (!checkScanner.isPlayerInDoubleCheck(whiteTurn) && checkScanner.isPlayerInCheck(whiteTurn)) {
                                    checkingPiece = checkScanner.findCheckingPiece();
                                    if(checkingPiece != null) {
                                        if (checkingPiece.getName().equals("Pawn")) {
                                            if (piece.getName().equals("King")) {
                                                if (!isProtectedPiece(checkingPiece) && isValidMove(piece, checkingPiece.getCol(), checkingPiece.getRow())) {
                                                    return true;
                                                }
                                                if (isProtectedPiece(checkingPiece) && isValidMove(piece, c, r) && !isValidMove(checkingPiece, c, r)) {
                                                    return true;
                                                }

                                            }
                                            else  if (checkingPiece.getCol() == c && checkingPiece.getRow() == r) {
                                                return true;
                                            }
                                        }
                                        else if (checkingPiece.getName().equals("Knight")) {
                                            if (piece.getName().equals("King")) {
                                                if (isValidMove(piece, c, r)) {
                                                    return true;
                                                }
                                            } else if (isValidMove(piece, checkingPiece.getCol(), checkingPiece.getRow())) {
                                                return true;
                                            }
                                        }

                                        else if ((checkingPiece.getName().equals("Rook") || checkingPiece.getName().equals("Bishop") || checkingPiece.getName().equals("Queen"))) {
                                            if (piece.getName().equals("King")) {
                                                if (!isProtectedPiece(checkingPiece) && isValidMove(piece, checkingPiece.getCol(), checkingPiece.getRow())) {
                                                    return true;
                                                }
                                                if (!isValidMove(checkingPiece, c, r)) {
                                                    return true;
                                                }
                                            }
                                            else if (checkingPiece.getCol() == c && checkingPiece.getRow() == r) {
                                                return true;
                                            }
                                            else if (isPieceStoppingCheck(checkingPiece, c, r)){
                                                return true;
                                            }
                                        }
                                    }
                                }

                                    piece.fakeMovePiece(c, r);
                                    if (!checkScanner.isPlayerInCheck(whiteTurn)) {
                                        piece.fakeMovePiece(pieceCol, pieceRow);
                                        return true;
                                    }
                                    piece.fakeMovePiece(pieceCol, pieceRow);
                            }
                        }
                    }
                }
            }
        }
        if(checkScanner.isPlayerInCheck(whiteTurn)){
            checkingPiece = checkScanner.findCheckingPiece();
            King king = (King) findKing(whiteTurn);

            if(isValidMove(king, checkingPiece.getCol(), checkingPiece.getRow()) &&  !isProtectedPiece(checkingPiece)){
                return true;
            }
            if(checkingPiece != lastMovedPiece) {
                for (int c = 0; c < 8; c++) {
                    for (int r = 0; r < 8; r++) {
                        if (isValidMove(king, c, r) && (!isValidMove(checkingPiece, c, r) || checkingPiece.getName().equals("Bishop"))) {
                            if(checkingPiece.getName().equals("Bishop")){
                                if(isPieceHaveValidMoves(king)){
                                    return true;
                                }
                            }
                            else {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    public boolean isCanTwoPiecesMoveToSameCol(int col, int row, int fromCol, String pieceType){
        int sum = 0;
            for(Piece piece : pieceList) {
                if (piece != null && (getPiece(col, row) == null || piece.isWhite() != getPiece(col, row).isWhite())) {
                    if (isValidMove(piece, col, row) && piece.getCol() == fromCol && piece.getName().equals(pieceType)) {
                        sum++;
                        if(sum >= 2){
                            return true;
                        }
                    }
                }
            }
            return false;
        }

    public boolean isCanTwoPiecesMoveToSameRow(int col, int row, int fromRow, String pieceType){
        int sum = 0;
        for(Piece piece : pieceList) {
            if (piece != null && (getPiece(col, row) == null || piece.isWhite() != getPiece(col, row).isWhite())) {
                if (isValidMove(piece, col, row) && piece.getRow() == fromRow && piece.getName().equals(pieceType)) {
                    sum++;
                    if(sum >= 2){
                        return true;
                    }
                }
            }
        }
        return false;
    }


    public boolean isValidMove(Piece piece, int col, int row){

        if(col > 8 || col < 0 || row > 8 || row < 0){
            return false;
        }
        if(checkScanner.isKingChecked(new Move(this, piece, col, row))){
            return false;
        }
        if(!piece.isValidMovement(col, row)){
            return false;
        }
        if(piece.moveCollidesWithPiece(col, row)){
            return false;
        }
        return true;
    }

    private void showValidMoves(Piece piece){
        if(piece != null){
            for(int c = 0; c < 8; c++){
                for(int r = 0; r < 8; r++){
                    if(getPiece(c, r) == null || piece.isWhite() != getPiece(c, r).isWhite()) {
                        if (isValidMove(piece, c ,r)) {
                            addValidMoveIndicator(c, r);
                        }
                    }
                }
            }
        }
    }

    private boolean isPieceHaveValidMoves(Piece piece){
        if(piece != null){
            for(int c = 0; c < 8; c++){
                for(int r = 0; r < 8; r++){
                    if(getPiece(c, r) == null || piece.isWhite() != getPiece(c, r).isWhite()) {
                        if (isValidMove(piece, c ,r)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private void handleSquareClick(int col, int row) {
        try {
            ImageView clickedSquare = boardSquares[col][row];

            resetSquareColors();

            // Retrieve the piece associated with the clicked square
            Piece clickedPiece = (Piece) clickedSquare.getTag();

            // Set the color filter to blend with the background color
            int blendColor = (col + row) % 2 == 0 ? blendColorForSquare(227, 198, 181) : blendColorForSquare(157, 105, 53);
            clickedSquare.getBackground().setColorFilter(new BlendModeColorFilter(blendColor, BlendMode.MULTIPLY));

            removeValidMoveIndicators();

            // Check if there is a piece on the clicked square
            if (clickedPiece != null && clickedPiece.isWhite() == whiteTurn) {

                previousSelectedPiece = clickedPiece;
                previousPieceCol = previousSelectedPiece.getCol();
                previousPieceRow = previousSelectedPiece.getRow();
                clickedSquare.setBackgroundResource(R.drawable.selected_square_border);
                clickedSquare.getBackground().setColorFilter(new BlendModeColorFilter(blendColor, BlendMode.MULTIPLY));
                selectedPiece = clickedPiece;

                if(previousSelectedPiece != null){
                    showValidMoves(previousSelectedPiece);
                }

            }
            else if (previousSelectedPiece != null && (getPiece(col, row) == null || previousSelectedPiece.isWhite() != getPiece(col, row).isWhite())) {
                if (isValidMove(previousSelectedPiece, col, row)) {

                    if(isCanTwoPiecesMoveToSameCol(col, row, previousSelectedPiece.getCol(), previousSelectedPiece.getName())){
                        canTwoPiecesMoveToSameCol = true;
                    }

                    if(isCanTwoPiecesMoveToSameRow(col, row, previousSelectedPiece.getRow(), previousSelectedPiece.getName())){
                        canTwoPiecesMoveToSameRow = true;
                    }

                    Piece capturedPiece = getPiece(col, row);
                    if (capturedPiece != null) {
                        capturedPieceName = capturedPiece.getName();
                        removePieceFromChessboard(capturedPiece);
                        isCaptureMove = true;
                    }
                    else if(previousSelectedPiece.getName().equals("King")){
                        King king = (King) previousSelectedPiece;
                        if(king.canCastle(col, row)){

                            int rookRow;
                            int rookTargetRow;
                            if (row == 6) { // King side castle
                                rookRow = 7;
                                rookTargetRow = 5;
                            }
                            else { // Queen side castle
                                rookRow = 0;
                                rookTargetRow = 3;
                            }

                            Rook rook = (Rook) getPiece(col, rookRow);
                            rook.movePiece(col, rookTargetRow);
                            boardSquares[col][rookRow].setTag(null);
                            boardSquares[col][rookTargetRow].setTag(rook);
                        }
                    }
                    if(previousSelectedPiece.getName().equals("Pawn")){
                        Pawn pawn = (Pawn) previousSelectedPiece;
                        int colorIndex = pawn.isWhite ? 1 : -1;
                        if(pawn.wasEnPassantMoveRight && row == pawn.getRow() + 1){
                            removePieceFromChessboard(getPiece((previousSelectedPiece.getCol()), pawn.isWhite ? previousSelectedPiece.getRow() + colorIndex : previousSelectedPiece.getRow() - colorIndex));
                            pawn.wasEnPassantMoveRight = false;
                            wasLastMoveEnPassant = true;
                        }
                        if(pawn.wasEnPassantMoveLeft && row == pawn.getRow() - 1){
                            removePieceFromChessboard(getPiece((previousSelectedPiece.getCol()),  pawn.isWhite ? previousSelectedPiece.getRow() - colorIndex : previousSelectedPiece.getRow() + colorIndex));
                            pawn.wasEnPassantMoveLeft = false;
                            wasLastMoveEnPassant = true;
                        }
                        if(col == (pawn.isWhite ? 0 : 7)){
                            // Animate the movement of the pawn to the promotion
                            final int finalCol = col;
                            final int finalRow = row;
                            final Piece finalPawn = pawn;
                            final Handler handler = new Handler();
                            final long startTime = System.currentTimeMillis();
                            final long duration = 170;
                            final Runnable animationRunnable = new Runnable() {
                                @Override
                                public void run() {
                                    long currentTime = System.currentTimeMillis();
                                    long elapsedTime = currentTime - startTime;
                                    if (elapsedTime < duration) {
                                        float progress = (float) elapsedTime / duration;
                                        float intermediateX = finalPawn.getImageView().getX();
                                        float intermediateY = finalPawn.getImageView().getY() + (finalRow - finalPawn.getRow()) * squareSize * progress;
                                        finalPawn.getImageView().setX(intermediateX);
                                        finalPawn.getImageView().setY(intermediateY);
                                        handler.postDelayed(this, 16);
                                    }
                                    else {
                                        finalPawn.getImageView().setX(pawn.calculateX(finalCol));
                                        finalPawn.getImageView().setY(pawn.calculateY(finalRow));
                                        // Remove the pawn from the chessboard
                                        removePieceFromChessboard(finalPawn);
                                        // Create a new queen object
                                        Queen queen = new Queen(chessboardGridLayout.getContext(), Board.this, finalCol, finalRow, pawn.isWhite());
                                        // Add the queen to the chessboard
                                        addPieceToChessboard(queen, finalCol, finalRow);
                                        // Add the queen to the piece list
                                        pieceList.add(queen);
                                        // Update the tag of the square
                                        boardSquares[finalCol][finalRow].setTag(queen);
                                    }
                                }
                            };

                            handler.post(animationRunnable);
                        }
                    }
                    if (capturedPiece != null || previousSelectedPiece.getName().equals("Pawn")){
                        fiftyMoveRuleIndex = 0;
                    }
                    else{
                        fiftyMoveRuleIndex++;
                    }

                    lastMovedPiece = previousSelectedPiece;
                    lastMovedPieceFromCol = previousSelectedPiece.getCol();
                    lastMovedPieceFromRow = previousSelectedPiece.getRow();
                    lastMovedPieceMovedToCol = col;
                    lastMovedPieceMovedToRow = row;

                    pieceList.remove(lastMovedPiece);

                    previousSelectedPiece.movePiece(col, row);

                    pieceList.add(previousSelectedPiece);
                    boardSquares[previousPieceCol][previousPieceRow].setTag(null);

                    boardSquares[col][row].setTag(previousSelectedPiece);

                    previousSelectedPiece = null;


                    whiteTurn = !whiteTurn;
                    isSwitchTurns = true;


                    if(checkScanner.isPlayerInCheck(whiteTurn)) {
                        if(!hasEscapeMoves()){
                            isCheckmate = true;
                        }
                        else {
                            Toast.makeText(chessboardGridLayout.getContext(), (whiteTurn ? "White" : "Black") + " player is in check!", Toast.LENGTH_SHORT).show();
                        }
                    }
                    else{
                        if(!hasEscapeMoves()){
                            isStalemate = true;
                        }
                    }

                    positionHistory.add(generatePositionHash());

                }
                else {
                    previousSelectedPiece = null;
                    Toast.makeText(chessboardGridLayout.getContext(), "Invalid move!", Toast.LENGTH_SHORT).show();
                }
            }
            else {
                // Clear the selected piece if the clicked square is empty or belong to the other player
                selectedPiece = null;
                if (clickedPiece != null && clickedPiece.isWhite() != whiteTurn) {
                    Toast.makeText(chessboardGridLayout.getContext(), "Clicked on the other's player piece", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(chessboardGridLayout.getContext(), "Clicked on empty square", Toast.LENGTH_SHORT).show();
                }
            }

        }
        catch (Exception e) {
            Log.e("ChessGameActivity", "Error in handleSquareClick", e);
        }
    }

    public void resetSquareColors() {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                ImageView square = boardSquares[i][j];
                int backgroundColor = (i + j) % 2 == 0 ? Color.rgb(227, 198, 181) : Color.rgb(157, 105, 53);
                square.setBackgroundColor(backgroundColor);
            }
        }
    }

    private int blendColorForSquare(int red, int green, int blue) {
        float factor = 0.7f;
        int blendedRed = (int) (red + (255 - red) * factor);
        int blendedGreen = (int) (green + (255 - green) * factor);
        int blendedBlue = (int) (blue + (255 - blue) * factor);
        return Color.rgb(blendedRed, blendedGreen, blendedBlue);
    }

    public void removeValidMoveIndicators() {
        // Find and remove all valid move indicators
        ArrayList<View> viewsToRemove = new ArrayList<>();
        for (int i = 0; i < chessboardGridLayout.getChildCount(); i++) {
            View child = chessboardGridLayout.getChildAt(i);
            if (child.getTag() != null && child.getTag().equals("valid_move_indicator")) {
                viewsToRemove.add(child);
            }
        }

        for (View view : viewsToRemove) {
            chessboardGridLayout.removeView(view);
        }
    }

    private void addValidMoveIndicator(int col, int row) {
        ImageView validMoveIndicator = new ImageView(chessboardGridLayout.getContext());


        if(getPiece(col, row) == null) {
            validMoveIndicator.setImageResource(R.drawable.dark_gray_circle);

            int indicatorSize = squareSize / 2;
            int horizontalMargin = (squareSize - indicatorSize) / 2;
            int verticalMargin = (squareSize - indicatorSize) / 2;

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = indicatorSize;
            params.height = indicatorSize;
            params.setMargins(horizontalMargin, verticalMargin, horizontalMargin, verticalMargin);
            params.columnSpec = GridLayout.spec(row);
            params.rowSpec = GridLayout.spec(col);
            validMoveIndicator.setLayoutParams(params);

            validMoveIndicator.setTag("valid_move_indicator");

            chessboardGridLayout.addView(validMoveIndicator);
        }
        else {
            validMoveIndicator.setImageResource(R.drawable.dark_gray_circle_capture);
            int indicatorSize = squareSize;

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = indicatorSize;
            params.height = indicatorSize;
            params.columnSpec = GridLayout.spec(row);
            params.rowSpec = GridLayout.spec(col);
            validMoveIndicator.setLayoutParams(params);

            validMoveIndicator.setTag("valid_move_indicator");

            chessboardGridLayout.addView(validMoveIndicator);
        }
    }

    public void removePieceFromChessboard(Piece piece) {
        ImageView pieceImageView = piece.getImageView();
        if (pieceImageView != null && pieceImageView.getParent() instanceof ViewGroup) {
            ViewGroup parent = (ViewGroup) pieceImageView.getParent();
            parent.removeView(pieceImageView);

            int col = piece.getCol();
            int row = piece.getRow();
            boardSquares[col][row].setTag(null);
            pieceList.remove(piece);
        }
    }

    public boolean sameTeam(Piece p1, Piece p2) {
        if(p1 == null || p2 == null) {
            return false;
        }
        return p1.isWhite == p2.isWhite;
    }

    public String getBoardPosition(){
        StringBuilder hashBuilder = new StringBuilder();

        for (int c = 0; c < 8; c++) {
            for (int r = 0; r < 8; r++) {
                Piece piece = getPiece(c, r);
                if (piece == null) {
                    // Use a placeholder character to represent an empty square
                    hashBuilder.append(".");
                } else {
                    char pieceChar = '.';
                    if (piece instanceof Pawn) {
                        pieceChar = piece.isWhite() ? 'P' : 'p';
                    } else if (piece instanceof Rook) {
                        pieceChar = piece.isWhite() ? 'R' : 'r';
                    } else if (piece instanceof Knight) {
                        pieceChar = piece.isWhite() ? 'N' : 'n';
                    } else if (piece instanceof Bishop) {
                        pieceChar = piece.isWhite() ? 'B' : 'b';
                    } else if (piece instanceof Queen) {
                        pieceChar = piece.isWhite() ? 'Q' : 'q';
                    } else if (piece instanceof King) {
                        pieceChar = piece.isWhite() ? 'K' : 'k';
                    }

                    // Append the piece character and the first move status
                    hashBuilder.append(pieceChar);
                    hashBuilder.append(piece.isFirstMove ? '1' : '0');
                }
            }
        }

        // Append information about the player to move
        hashBuilder.append(whiteTurn ? "W" : "B");

        return hashBuilder.toString();
    }

    public void setupBoardPosition(String position) {
        // Clear the board
        for (int c = 0; c < 8; c++) {
            for (int r = 0; r < 8; r++) {
                if (getPiece(c, r) != null) {
                    removePieceFromChessboard(getPiece(c, r));
                }
            }
        }

        // Index to keep track of position string
        int index = 0;

        // Iterate over the board and set up pieces according to the position string
        for (int c = 0; c < 8; c++) {
            for (int r = 0; r < 8; r++) {
                if (index >= position.length()) {
                    return;
                }

                char pieceChar = position.charAt(index);

                // Skip empty squares
                if (pieceChar == '.') {
                    index++;
                    continue;
                }

                // Determine the color of the piece
                boolean isWhite = Character.isUpperCase(pieceChar);

                // The next character indicates if the piece has moved
                index++;
                char moveStatusChar = position.charAt(index);
                boolean isFirstMove = moveStatusChar == '1';

                // Create the appropriate piece and place it on the board
                Piece piece = null;
                switch (Character.toLowerCase(pieceChar)) {
                    case 'p':
                        piece = new Pawn(chessboardGridLayout.getContext(), this, c, r, isWhite);
                        break;
                    case 'r':
                        piece = new Rook(chessboardGridLayout.getContext(), this, c, r, isWhite);
                        break;
                    case 'n':
                        piece = new Knight(chessboardGridLayout.getContext(), this, c, r, isWhite);
                        break;
                    case 'b':
                        piece = new Bishop(chessboardGridLayout.getContext(), this, c, r, isWhite);
                        break;
                    case 'q':
                        piece = new Queen(chessboardGridLayout.getContext(), this, c, r, isWhite);
                        break;
                    case 'k':
                        piece = new King(chessboardGridLayout.getContext(), this, c, r, isWhite);
                        break;
                    default:
                        Log.w("SetupBoard", "Unknown piece type: " + pieceChar);
                        break;
                }

                // Place the piece on the board and set its first move status
                if (piece != null) {
                    piece.isFirstMove = isFirstMove;
                    pieceList.add(piece);
                    addPieceToChessboard(piece, c, r);
                }

                // Move to the next character in the position string
                index++;
            }
        }
    }
    public void setPositionHistory(String positionHistoryString) {
        // Split the position history string into individual positions
        String[] positions = positionHistoryString.split(",");

        // Clear the existing position history
        positionHistory.clear();

        // Add each position to the position history list
        positionHistory.addAll(Arrays.asList(positions));
    }
}