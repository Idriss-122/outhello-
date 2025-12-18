import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {

    private enum Cell {
        EMPTY, BLACK, WHITE;
        static Cell fromPlayer(Player p) {
            return p == Player.BLACK ? BLACK : WHITE;
        }
    }

    private enum Player {
        BLACK, WHITE;
        Player opposite() {
            return this == BLACK ? WHITE : BLACK;
        }
    }

    private static class Move {
        final int row;
        final int col;
        Move(int row, int col) {
            this.row = row;
            this.col = col;
        }
    }

    private static class Board {
        private static final int SIZE = 8;
        private final Cell[][] grid = new Cell[SIZE][SIZE];

        Board() {
            for (int r = 0; r < SIZE; r++) {
                for (int c = 0; c < SIZE; c++) {
                    grid[r][c] = Cell.EMPTY;
                }
            }
            grid[3][3] = Cell.WHITE;
            grid[3][4] = Cell.BLACK;
            grid[4][3] = Cell.BLACK;
            grid[4][4] = Cell.WHITE;
        }

        Board(Board other) {
            for (int r = 0; r < SIZE; r++) {
                System.arraycopy(other.grid[r], 0, this.grid[r], 0, SIZE);
            }
        }

        boolean inBounds(int r, int c) {
            return r >= 0 && r < SIZE && c >= 0 && c < SIZE;
        }

        Cell at(int r, int c) {
            return grid[r][c];
        }

        int count(Player p) {
            Cell target = Cell.fromPlayer(p);
            int total = 0;
            for (Cell[] cells : grid) {
                for (Cell cell : cells) {
                    if (cell == target) total++;
                }
            }
            return total;
        }

        List<Move> getLegalMoves(Player p) {
            List<Move> moves = new ArrayList<>();
            for (int r = 0; r < SIZE; r++) {
                for (int c = 0; c < SIZE; c++) {
                    if (grid[r][c] == Cell.EMPTY && canFlip(p, r, c)) {
                        moves.add(new Move(r, c));
                    }
                }
            }
            return moves;
        }

        boolean hasAnyMoves(Player p) {
            return !getLegalMoves(p).isEmpty();
        }

        int applyMove(Player p, Move move) {
            if (grid[move.row][move.col] != Cell.EMPTY) return 0;
            int flipped = 0;
            for (int dr = -1; dr <= 1; dr++) {
                for (int dc = -1; dc <= 1; dc++) {
                    if (dr == 0 && dc == 0) continue;
                    flipped += flipDirection(p, move.row, move.col, dr, dc, true);
                }
            }
            if (flipped > 0) {
                grid[move.row][move.col] = Cell.fromPlayer(p);
            }
            return flipped;
        }

        private boolean canFlip(Player p, int row, int col) {
            if (grid[row][col] != Cell.EMPTY) return false;
            for (int dr = -1; dr <= 1; dr++) {
                for (int dc = -1; dc <= 1; dc++) {
                    if (dr == 0 && dc == 0) continue;
                    if (flipDirection(p, row, col, dr, dc, false) > 0) return true;
                }
            }
            return false;
        }

        private int flipDirection(Player p, int row, int col, int dr, int dc, boolean performFlip) {
            int r = row + dr;
            int c = col + dc;
            int captured = 0;
            Cell me = Cell.fromPlayer(p);
            Cell opp = Cell.fromPlayer(p.opposite());
            while (inBounds(r, c) && grid[r][c] == opp) {
                captured++;
                r += dr;
                c += dc;
            }
            if (captured == 0) return 0;
            if (!inBounds(r, c) || grid[r][c] != me) return 0;
            if (performFlip) {
                int fr = row + dr;
                int fc = col + dc;
                for (int i = 0; i < captured; i++) {
                    grid[fr][fc] = me;
                    fr += dr;
                    fc += dc;
                }
            }
            return captured;
        }

        boolean isTerminal() {
            return !hasAnyMoves(Player.BLACK) && !hasAnyMoves(Player.WHITE);
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Othello - Mode: 1) Human vs Human  2) Human vs Computer");
        int mode = scanner.nextInt();
        int depth = 2;
        Player aiSide = Player.WHITE;
        if (mode == 2) {
            System.out.print("Profondeur (np): ");
            depth = scanner.nextInt();
            System.out.print("IA joue 1) Noir  2) Blanc: ");
            aiSide = scanner.nextInt() == 1 ? Player.BLACK : Player.WHITE;
        }

        Board board = new Board();
        Player current = Player.BLACK;
        while (true) {
            if (board.isTerminal()) break;
            List<Move> legal = board.getLegalMoves(current);
            if (legal.isEmpty()) {
                System.out.println(current + " passe son tour");
                if (!board.hasAnyMoves(current.opposite())) break;
                current = current.opposite();
                continue;
            }

            System.out.println("\nTour: " + current);
            printBoard(board, legal);
            Move chosen;
            if (mode == 2 && current == aiSide) {
                chosen = chooseBestMove(board, current, depth);
                if (chosen == null) chosen = legal.get(0);
                System.out.println("IA choisit: " + toCoord(chosen));
            } else {
                chosen = getHumanMove(scanner, legal);
            }
            board.applyMove(current, chosen);
            current = current.opposite();
        }

        System.out.println("\n=== FIN DE PARTIE ===");
        printBoard(board, null);
        int black = board.count(Player.BLACK);
        int white = board.count(Player.WHITE);
        if (black > white) System.out.println("Noir gagne: " + black + " vs " + white);
        else if (white > black) System.out.println("Blanc gagne: " + white + " vs " + black);
        else System.out.println("Egalite: " + black + " vs " + white);
        scanner.close();
    }

    private static void printBoard(Board board, List<Move> highlights) {
        boolean[][] mark = new boolean[8][8];
        if (highlights != null) {
            for (Move m : highlights) {
                mark[m.row][m.col] = true;
            }
        }
        System.out.println("  a b c d e f g h");
        for (int r = 0; r < 8; r++) {
            System.out.print((r + 1) + " ");
            for (int c = 0; c < 8; c++) {
                char ch;
                if (board.at(r, c) == Cell.BLACK) ch = 'B';
                else if (board.at(r, c) == Cell.WHITE) ch = 'W';
                else ch = mark[r][c] ? '*' : '.';
                System.out.print(ch + " ");
            }
            System.out.println();
        }
        System.out.println("Noir: " + board.count(Player.BLACK) + "  Blanc: " + board.count(Player.WHITE));
    }

    private static Move getHumanMove(Scanner scanner, List<Move> legal) {
        while (true) {
            System.out.print("Votre coup (ex: d3): ");
            String input = scanner.next().trim().toLowerCase();
            if (input.length() == 2) {
                int col = input.charAt(0) - 'a';
                int row = input.charAt(1) - '1';
                for (Move m : legal) {
                    if (m.row == row && m.col == col) {
                        return m;
                    }
                }
            }
            System.out.print("Coup invalide. Coups possibles: ");
            for (int i = 0; i < legal.size(); i++) {
                if (i > 0) System.out.print(", ");
                System.out.print(toCoord(legal.get(i)));
            }
            System.out.println();
        }
    }

    private static String toCoord(Move m) {
        return "" + (char)('a' + m.col) + (m.row + 1);
    }

    private static int Max(Board board, Player ai, int depth) {
        List<Move> legal = board.getLegalMoves(ai);
        if (depth == 0 || board.isTerminal()) {
            return V(board, ai);
        }
        if (legal.isEmpty()) {
            if (!board.hasAnyMoves(ai.opposite())) {
                return V(board, ai);
            }
            return Min(board, ai, depth - 1);
        }
        int maximum = Integer.MIN_VALUE;
        for (Move move : legal) {
            Board next = new Board(board);
            next.applyMove(ai, move);
            int t = Min(next, ai, depth - 1);
            if (t > maximum) {
                maximum = t;
            }
        }
        return maximum;
    }

    private static int Min(Board board, Player ai, int depth) {
        Player minPlayer = ai.opposite();
        List<Move> legal = board.getLegalMoves(minPlayer);
        if (depth == 0 || board.isTerminal()) {
            return V(board, ai);
        }
        if (legal.isEmpty()) {
            if (!board.hasAnyMoves(ai)) {
                return V(board, ai);
            }
            return Max(board, ai, depth - 1);
        }
        int minimum = Integer.MAX_VALUE;
        for (Move move : legal) {
            Board next = new Board(board);
            next.applyMove(minPlayer, move);
            int t = Max(next, ai, depth - 1);
            if (t < minimum) {
                minimum = t;
            }
        }
        return minimum;
    }

    private static int V(Board board, Player ai) {
        int myDiscs = board.count(ai);
        int oppDiscs = board.count(ai.opposite());
        int discDiff = myDiscs - oppDiscs;
        int mobility = board.getLegalMoves(ai).size() - board.getLegalMoves(ai.opposite()).size();
        int cornerScore = 0;
        int[][] corners = {{0, 0}, {0, 7}, {7, 0}, {7, 7}};
        for (int[] c : corners) {
            Cell cell = board.at(c[0], c[1]);
            if (cell == Cell.fromPlayer(ai)) cornerScore += 25;
            else if (cell == Cell.fromPlayer(ai.opposite())) cornerScore -= 25;
        }
        return 10 * discDiff + 5 * mobility + cornerScore;
    }

    private static Move chooseBestMove(Board board, Player ai, int depth) {
        List<Move> legal = board.getLegalMoves(ai);
        if (legal.isEmpty()) return null;
        Move best = legal.get(0);
        int bestScore = Integer.MIN_VALUE;
        for (Move move : legal) {
            Board next = new Board(board);
            next.applyMove(ai, move);
            int score = Min(next, ai, depth - 1);
            if (score > bestScore) {
                bestScore = score;
                best = move;
            }
        }
        return best;
    }
}
