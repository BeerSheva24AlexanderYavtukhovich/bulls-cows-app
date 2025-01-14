package telran.games;

public final class Params {
    public final static String HOST = System.getProperty("host", "54.91.101.91");
    final static int PORT = 5011;
    final static String LINE = "-----------------";
    final static String TABLE_BORDER = "+-----+-------+-------+------------+------------+";
    final static String TABLE_HEADER = "| #   | Bulls | Cows  | Sequence   | Gamer      |";
    final static String TABLE_ROW_FORMAT = "| %-3d | %-5d | %-5d | %-10s | %-10s |";
    final static String NO_GAMES_FOUND = "Games not found";

    private Params() {
    }
}