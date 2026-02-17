package com.ricedotwho.rsm.module.impl.dungeon.puzzle.ticktactoe;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Board {

    public enum State {
        Blank, X, O
    }

    public static final int BOARD_WIDTH = 3;

    private State[][] board = new State[BOARD_WIDTH][BOARD_WIDTH];

    public State turn = State.X;
    public State winner = State.Blank;
    public boolean isGameOver = false;
    public int algorithmBestMove = -1;

    public Set<Integer> availableMoves = new HashSet<>();
    private int moveCount = 0;

    public Board() {
        reset();
    }

    /**
     * Initialize board and available moves.
     */
    private void initialize() {
        for (int row = 0; row < BOARD_WIDTH; row++) {
            for (int col = 0; col < BOARD_WIDTH; col++) {
                board[row][col] = State.Blank;
            }
        }
        availableMoves.clear();
        for (int i = 0; i < BOARD_WIDTH * BOARD_WIDTH; i++) {
            availableMoves.add(i);
        }
    }

    /**
     * Restart the game with a new blank board.
     */
    public void reset() {
        moveCount = 0;
        isGameOver = false;
        turn = State.X;
        winner = State.Blank;
        initialize();
    }

    /**
     * Places an X or an O on the specified index depending on whose turn it is.
     */
    public boolean move(int index) {
        return move(index % BOARD_WIDTH, index / BOARD_WIDTH);
    }

    /**
     * Places an X or an O on the specified coordinates depending on whose turn it is.
     */
    public boolean move(int x, int y) {
        if (isGameOver) {
            throw new IllegalStateException("TicTacToe is over. No moves can be played.");
        }
        if (board[y][x] == State.Blank) {
            board[y][x] = turn;
        } else {
            return false;
        }

        moveCount++;
        availableMoves.remove(y * BOARD_WIDTH + x);

        // Check draw
        if (moveCount == BOARD_WIDTH * BOARD_WIDTH) {
            winner = State.Blank;
            isGameOver = true;
        }

        // Check for winner
        checkRow(y);
        checkColumn(x);
        checkDiagonalFromTopLeft(x, y);
        checkDiagonalFromTopRight(x, y);

        turn = (turn == State.X) ? State.O : State.X;
        return true;
    }

    /**
     * Places an X or an O based on explicit player.
     */
    public boolean place(int x, int y, State player) {
        if (isGameOver) {
            throw new IllegalStateException("TicTacToe is over. No moves can be played.");
        }
        if (board[y][x] == State.Blank) {
            board[y][x] = player;
        } else {
            return false;
        }

        moveCount++;
        availableMoves.remove(y * BOARD_WIDTH + x);

        // Check draw
        if (moveCount == BOARD_WIDTH * BOARD_WIDTH) {
            winner = State.Blank;
            isGameOver = true;
        }

        // Check for winner
        checkRow(y);
        checkColumn(x);
        checkDiagonalFromTopLeft(x, y);
        checkDiagonalFromTopRight(x, y);

        turn = (turn == State.X) ? State.O : State.X;
        return true;
    }

    /**
     * Get a copy of the array that represents the board.
     */
    public State[][] toArray() {
        return board.clone();
    }

    private void checkRow(int row) {
        for (int i = 1; i < BOARD_WIDTH; i++) {
            if (board[row][i] != board[row][i - 1]) {
                return;
            }
            if (i == BOARD_WIDTH - 1) {
                winner = turn;
                isGameOver = true;
            }
        }
    }

    private void checkColumn(int column) {
        for (int i = 1; i < BOARD_WIDTH; i++) {
            if (board[i][column] != board[i - 1][column]) {
                return;
            }
            if (i == BOARD_WIDTH - 1) {
                winner = turn;
                isGameOver = true;
            }
        }
    }

    private void checkDiagonalFromTopLeft(int x, int y) {
        if (x == y) {
            for (int i = 1; i < BOARD_WIDTH; i++) {
                if (board[i][i] != board[i - 1][i - 1]) {
                    return;
                }
                if (i == BOARD_WIDTH - 1) {
                    winner = turn;
                    isGameOver = true;
                }
            }
        }
    }

    private void checkDiagonalFromTopRight(int x, int y) {
        if (BOARD_WIDTH - 1 - x == y) {
            for (int i = 1; i < BOARD_WIDTH; i++) {
                if (board[BOARD_WIDTH - 1 - i][i] != board[BOARD_WIDTH - i][i - 1]) {
                    return;
                }
                if (i == BOARD_WIDTH - 1) {
                    winner = turn;
                    isGameOver = true;
                }
            }
        }
    }

    /**
     * Get a deep copy of the Tic Tac Toe board.
     */
    public Board deepCopy() {
        Board copy = new Board();
        for (int i = 0; i < BOARD_WIDTH; i++) {
            copy.board[i] = Arrays.copyOf(this.board[i], BOARD_WIDTH);
        }
        copy.turn = this.turn;
        copy.winner = this.winner;
        copy.availableMoves = new HashSet<>(this.availableMoves);
        copy.moveCount = this.moveCount;
        copy.isGameOver = this.isGameOver;
        return copy;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int y = 0; y < BOARD_WIDTH; y++) {
            for (int x = 0; x < BOARD_WIDTH; x++) {
                sb.append(board[y][x] == State.Blank ? "-" : board[y][x].name());
                sb.append(" ");
            }
            if (y != BOARD_WIDTH - 1) sb.append("\n");
        }
        return sb.toString();
    }
}
