package com.ricedotwho.rsm.module.impl.dungeon.puzzle.ticktactoe;

public class AlphaBetaAdvanced {
    private static double maxPly = 0.0;

    /**
     * Play using the Alpha-Beta Pruning algorithm. Include depth in the
     * evaluation function and a depth limit.
     * @param board the Tic Tac Toe board to play on
     * @param ply   the maximum depth
     * @return the score of the move
     */
    public static int run(Board board, double ply) {
        return run(board.turn, board, ply);
    }

    /**
     * Overload with infinite ply by default.
     */
    public static int run(Board board) {
        return run(board.turn, board, Double.POSITIVE_INFINITY);
    }

    /**
     * Execute the algorithm.
     * @param player the player that the AI will identify as
     * @param board  the Tic Tac Toe board to play on
     * @param maxPly the maximum depth
     * @return the score of the move
     */
    private static int run(Board.State player, Board board, double maxPly) {
        if (maxPly < 1) {
            throw new IllegalArgumentException("Maximum depth must be greater than 0.");
        }
        AlphaBetaAdvanced.maxPly = maxPly;
        return alphaBetaPruning(player, board, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 0);
    }

    /**
     * The core recursive Alpha-Beta function.
     */
    private static int alphaBetaPruning(Board.State player, Board board, double alpha, double beta, int currentPly) {
        int cp = currentPly;
        if (++cp == maxPly || board.isGameOver) {
            return score(player, board, cp);
        }

        if (board.turn == player) {
            return getMax(player, board, alpha, beta, cp);
        } else {
            return getMin(player, board, alpha, beta, cp);
        }
    }

    /**
     * Play the move with the highest score.
     */
    private static int getMax(Board.State player, Board board, double alpha, double beta, int currentPly) {
        double a = alpha;
        int indexOfBestMove = -1;

        for (int theMove : board.availableMoves) {
            Board modifiedBoard = board.deepCopy();
            modifiedBoard.move(theMove);
            int score = alphaBetaPruning(player, modifiedBoard, a, beta, currentPly);

            if (score > a) {
                a = score;
                indexOfBestMove = theMove;
            }

            if (a >= beta) {
                break;
            }
        }

        if (indexOfBestMove != -1) {
            board.algorithmBestMove = indexOfBestMove;
        }

        return (int) a;
    }

    /**
     * Play the move with the lowest score.
     */
    private static int getMin(Board.State player, Board board, double alpha, double beta, int currentPly) {
        double b = beta;
        int indexOfBestMove = -1;

        for (int theMove : board.availableMoves) {
            Board modifiedBoard = board.deepCopy();
            modifiedBoard.move(theMove);
            int score = alphaBetaPruning(player, modifiedBoard, alpha, b, currentPly);

            if (score < b) {
                b = score;
                indexOfBestMove = theMove;
            }

            if (alpha >= b) {
                break;
            }
        }

        if (indexOfBestMove != -1) {
            board.algorithmBestMove = indexOfBestMove;
        }

        return (int) b;
    }

    /**
     * Get the score of the board. Takes depth into account.
     */
    private static int score(Board.State player, Board board, int currentPly) {
        if (player == Board.State.Blank) {
            throw new IllegalArgumentException("Player must be X or O.");
        }

        Board.State opponent = (player == Board.State.X) ? Board.State.O : Board.State.X;

        if (board.isGameOver && board.winner == player) {
            return 10 - currentPly;
        } else if (board.isGameOver && board.winner == opponent) {
            return -10 + currentPly;
        } else {
            return 0;
        }
    }
}
