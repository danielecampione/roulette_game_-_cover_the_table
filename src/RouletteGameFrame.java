import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class RouletteGameFrame extends JFrame {
    private JTextArea seriesTextArea;
    private JTextArea resultTextArea;
    private JButton startButton;
    private List<Bet> bets;

    public RouletteGameFrame(List<Bet> bets) {
        this.bets = bets;
        setLookAndFeel();
        initializeUI();
    }

    private void setLookAndFeel() {
        try {
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initializeUI() {
        setTitle("Roulette Game");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        seriesTextArea = new JTextArea();
        seriesTextArea.setEditable(true);
        seriesTextArea.setFont(new Font("Courier New", Font.PLAIN, 16));
        JScrollPane seriesScrollPane = new JScrollPane(seriesTextArea);
        seriesScrollPane.setBorder(BorderFactory.createTitledBorder("Serie"));

        resultTextArea = new JTextArea();
        resultTextArea.setEditable(false);
        resultTextArea.setFont(new Font("Courier New", Font.PLAIN, 16));
        JScrollPane resultScrollPane = new JScrollPane(resultTextArea);
        resultScrollPane.setBorder(BorderFactory.createTitledBorder("Esito dell'estrazione"));

        startButton = new JButton("Avvia estrazione");
        startButton.setFont(new Font("Arial", Font.PLAIN, 16));
        startButton.addActionListener(e -> startExtraction());

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, seriesScrollPane, resultScrollPane);
        splitPane.setResizeWeight(0.5);
        splitPane.setDividerLocation(300);

        setLayout(new BorderLayout());
        add(splitPane, BorderLayout.CENTER);
        add(startButton, BorderLayout.SOUTH);

        displaySeries();
    }

    private void displaySeries() {
        StringBuilder seriesBuilder = new StringBuilder();
        for (Bet bet : bets) {
            seriesBuilder.append(bet.toString()).append("\n");
        }
        seriesTextArea.setText(seriesBuilder.toString());
    }

    private void startExtraction() {
        List<Bet> runtimeBets = parseBetsFromTextArea();
        Roulette roulette = new Roulette();
        List<StringBuilder> results = new ArrayList<>();
        for (int i = 0; i < runtimeBets.size(); i++) {
            results.add(new StringBuilder());
        }

        int totalDots = 0;
        int totalBets = 0;

        for (int round = 0; round < 100; round++) {
            boolean stopGame = false;
            for (int i = 0; i < runtimeBets.size(); i++) {
                Bet bet = runtimeBets.get(i);
                if (bet.shouldIgnore()) {
                    results.get(i).insert(0, stopGame ? "X" : ".");
                    continue;
                }

                if (stopGame) {
                    results.get(i).insert(0, "X");
                    continue;
                }

                int result = roulette.spin();
                if (bet.isWinningNumber(result)) {
                    results.get(i).insert(0, "X");
                    stopGame = true;
                } else {
                    results.get(i).insert(0, ".");
                }
            }

            // Conta i punti per ogni round
            if (!stopGame) {
                totalDots += runtimeBets.size();
            } else {
                for (int i = 0; i < runtimeBets.size(); i++) {
                    if (results.get(i).charAt(0) == '.') {
                        totalDots++;
                    } else {
                        break;
                    }
                }
            }

            totalBets++;
        }

        // Aggiungi la scommessa originale alla fine di ogni risultato
        for (int i = 0; i < runtimeBets.size(); i++) {
            results.get(i).append(" ").append(runtimeBets.get(i));
        }

        // Stampa i risultati finali nell'area di testo
        StringBuilder resultText = new StringBuilder();
        for (StringBuilder result : results) {
            resultText.append(result.toString()).append("\n");
        }

        double averageDots = (double) totalDots / totalBets;
        resultText.append("\nMedia dei punti (ovvero delle vittorie): ").append(averageDots);

        resultTextArea.setText(resultText.toString());
    }

    private List<Bet> parseBetsFromTextArea() {
        List<Bet> runtimeBets = new ArrayList<>();
        String[] lines = seriesTextArea.getText().split("\\n");
        for (String line : lines) {
            String[] numbers = line.split(" ");
            if (numbers.length == 2) {
                try {
                    int bet1 = Integer.parseInt(numbers[0].trim());
                    int bet2 = Integer.parseInt(numbers[1].trim());
                    if ((bet1 >= 0 && bet1 <= 36) && (bet2 >= 0 && bet2 <= 36)) {
                        runtimeBets.add(new Bet(bet1, bet2));
                    } else {
                        runtimeBets.add(new Bet(-1, -1)); // Ignora
                    }
                } catch (NumberFormatException e) {
                    runtimeBets.add(new Bet(-1, -1)); // Ignora
                }
            } else {
                runtimeBets.add(new Bet(-1, -1)); // Ignora
            }
        }
        return runtimeBets;
    }

    public static void main(String[] args) {
        List<Bet> bets = new ArrayList<>();
        bets.add(new Bet(17, 34));
        bets.add(new Bet(2, 25));
        bets.add(new Bet(4, 21));
        bets.add(new Bet(6, 27));
        bets.add(new Bet(13, 36));
        bets.add(new Bet(-1, -1)); // Ignora
        bets.add(new Bet(22, 18));
        bets.add(new Bet(31, 9));
        bets.add(new Bet(20, 14));
        bets.add(new Bet(29, 7));
        bets.add(new Bet(28, 12));
        bets.add(new Bet(-1, -1)); // Ignora
        bets.add(new Bet(17, 34));
        bets.add(new Bet(2, 25));
        bets.add(new Bet(4, 21));
        bets.add(new Bet(6, 27));
        bets.add(new Bet(13, 36));
        bets.add(new Bet(-1, -1)); // Ignora
        bets.add(new Bet(22, 18));
        bets.add(new Bet(31, 9));
        bets.add(new Bet(20, 14));
        bets.add(new Bet(29, 7));
        bets.add(new Bet(28, 12));
        bets.add(new Bet(-1, -1)); // Ignora
        bets.add(new Bet(17, 34));
        bets.add(new Bet(2, 25));
        bets.add(new Bet(4, 21));
        bets.add(new Bet(6, 27));
        bets.add(new Bet(13, 36));
        bets.add(new Bet(-1, -1)); // Ignora
        bets.add(new Bet(22, 18));
        bets.add(new Bet(31, 9));
        bets.add(new Bet(20, 14));
        bets.add(new Bet(29, 7));
        bets.add(new Bet(28, 12));
        bets.add(new Bet(-1, -1)); // Ignora
        bets.add(new Bet(17, 34));
        bets.add(new Bet(2, 25));
        bets.add(new Bet(4, 21));
        bets.add(new Bet(6, 27));
        bets.add(new Bet(13, 36));
        bets.add(new Bet(-1, -1)); // Ignora
        bets.add(new Bet(22, 18));
        bets.add(new Bet(31, 9));
        bets.add(new Bet(20, 14));
        bets.add(new Bet(29, 7));
        bets.add(new Bet(28, 12));
        bets.add(new Bet(-1, -1)); // Ignora
        bets.add(new Bet(17, 34));
        bets.add(new Bet(2, 25));
        bets.add(new Bet(4, 21));
        bets.add(new Bet(6, 27));
        bets.add(new Bet(13, 36));
        bets.add(new Bet(-1, -1)); // Ignora
        bets.add(new Bet(22, 18));
        bets.add(new Bet(31, 9));
        bets.add(new Bet(20, 14));
        bets.add(new Bet(29, 7));
        bets.add(new Bet(28, 12));
        bets.add(new Bet(-1, -1)); // Ignora
        bets.add(new Bet(17, 34));
        bets.add(new Bet(2, 25));
        bets.add(new Bet(4, 21));
        bets.add(new Bet(6, 27));
        bets.add(new Bet(13, 36));
        bets.add(new Bet(-1, -1)); // Ignora
        bets.add(new Bet(22, 18));
        bets.add(new Bet(31, 9));
        bets.add(new Bet(20, 14));
        bets.add(new Bet(29, 7));
        bets.add(new Bet(28, 12));
        bets.add(new Bet(-1, -1)); // Ignora
        bets.add(new Bet(17, 34));
        bets.add(new Bet(2, 25));
        bets.add(new Bet(4, 21));
        bets.add(new Bet(6, 27));
        bets.add(new Bet(13, 36));
        bets.add(new Bet(-1, -1)); // Ignora
        bets.add(new Bet(22, 18));
        bets.add(new Bet(31, 9));
        bets.add(new Bet(20, 14));
        bets.add(new Bet(29, 7));
        bets.add(new Bet(28, 12));
        bets.add(new Bet(-1, -1)); // Ignora
        bets.add(new Bet(17, 34));
        bets.add(new Bet(2, 25));
        bets.add(new Bet(4, 21));
        bets.add(new Bet(6, 27));
        bets.add(new Bet(13, 36));
        bets.add(new Bet(-1, -1)); // Ignora
        bets.add(new Bet(22, 18));
        bets.add(new Bet(31, 9));
        bets.add(new Bet(20, 14));
        bets.add(new Bet(29, 7));
        bets.add(new Bet(28, 12));
        bets.add(new Bet(-1, -1)); // Ignora
        bets.add(new Bet(17, 34));
        bets.add(new Bet(2, 25));
        bets.add(new Bet(4, 21));
        bets.add(new Bet(6, 27));
        bets.add(new Bet(13, 36));
        bets.add(new Bet(-1, -1)); // Ignora
        bets.add(new Bet(22, 18));
        bets.add(new Bet(31, 9));
        bets.add(new Bet(20, 14));
        bets.add(new Bet(29, 7));
        bets.add(new Bet(28, 12));
        bets.add(new Bet(-1, -1)); // Ignora
        bets.add(new Bet(17, 34));
        bets.add(new Bet(2, 25));
        bets.add(new Bet(4, 21));
        bets.add(new Bet(6, 27));
        bets.add(new Bet(13, 36));
        bets.add(new Bet(-1, -1)); // Ignora
        bets.add(new Bet(22, 18));
        bets.add(new Bet(31, 9));
        bets.add(new Bet(20, 14));
        bets.add(new Bet(29, 7));
        bets.add(new Bet(28, 12));
        bets.add(new Bet(-1, -1)); // Ignora
        bets.add(new Bet(17, 34));
        bets.add(new Bet(2, 25));
        bets.add(new Bet(4, 21));
        bets.add(new Bet(6, 27));
        bets.add(new Bet(13, 36));
        bets.add(new Bet(-1, -1)); // Ignora
        bets.add(new Bet(22, 18));
        bets.add(new Bet(31, 9));
        bets.add(new Bet(20, 14));
        bets.add(new Bet(29, 7));
        bets.add(new Bet(28, 12));
        bets.add(new Bet(-1, -1)); // Ignora


        SwingUtilities.invokeLater(() -> {
            RouletteGameFrame frame = new RouletteGameFrame(bets);
            frame.setVisible(true);
        });
    }
}
