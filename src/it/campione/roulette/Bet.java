/**
 * 
 * 
 * @author D. Campione
 *
 */
public class Bet {

    private int bet1;
    private int bet2;

    public Bet(int bet1, int bet2) {
        this.bet1 = bet1;
        this.bet2 = bet2;
    }

    public boolean shouldIgnore() {
        return bet1 == -1 && bet2 == -1;
    }

    public boolean isWinningNumber(int number) {
        return number == bet1 || number == bet2;
    }

    @Override
    public String toString() {
        if (shouldIgnore()) {
            return "ignora";
        } else {
            return bet1 + " " + bet2;
        }
    }
}
