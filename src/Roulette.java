import java.util.Random;

public class Roulette {
    private Random random;
    private static final int[] numbers = {0, 32, 15, 19, 4, 21, 2, 25, 17, 34, 6, 27, 13, 36, 11, 30, 8, 23, 10, 5, 24, 16, 33, 1, 20, 14, 31, 9, 22, 18, 29, 7, 28, 12, 35, 3, 26};

    public Roulette() {
        random = new Random();
    }

    public int spin() {
        return numbers[random.nextInt(numbers.length)];
    }
}
