import java.awt.BorderLayout;
import java.awt.Font;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Il metodo analizzato in questa applicazione è noto come "Cover the Table" o
 * "Cover All Bases". Questo sistema prevede di coprire quasi tutti i numeri sul
 * tavolo della roulette, lasciando scoperti solo pochissimi numeri, come ad
 * esempio due. L'obiettivo è massimizzare le probabilità di vincita su ogni
 * giro della ruota, anche se il profitto per ogni vincita è generalmente basso
 * rispetto alla puntata totale.
 * 
 * @author D. Campione
 *
 */
public class RouletteGameFrame extends JFrame {

    private static final long serialVersionUID = 5786059233469279478L;

    private JTextArea seriesTextArea;
    private JTextArea resultTextArea;
    private JButton startButton;
    private JComboBox<Integer> retryComboBox;
    private JComboBox<Integer> seriesComboBox;
    private List<Bet> bets;
    private Path seriesFilePath;

    public RouletteGameFrame(Path seriesFilePath) {
        this.seriesFilePath = seriesFilePath;
        this.bets = loadBetsFromFile();
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
        setTitle("Roulette Game - Cover the Table, Cover All Bases ");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        seriesTextArea = new JTextArea();
        seriesTextArea.setEditable(true);
        seriesTextArea.setFont(new Font("Courier New", Font.PLAIN, 16));
        JScrollPane seriesScrollPane = new JScrollPane(seriesTextArea);
        seriesScrollPane.setBorder(BorderFactory.createTitledBorder("Coppie escluse"));

        resultTextArea = new JTextArea();
        resultTextArea.setEditable(false);
        resultTextArea.setFont(new Font("Courier New", Font.PLAIN, 16));
        JScrollPane resultScrollPane = new JScrollPane(resultTextArea);
        resultScrollPane.setBorder(BorderFactory.createTitledBorder("Esito dell'estrazione"));

        startButton = new JButton("Avvia estrazione");
        startButton.setFont(new Font("Arial", Font.PLAIN, 16));
        startButton.addActionListener(e -> startExtraction());

        retryComboBox = new JComboBox<>(new Integer[] { 0, 1, 2, 3 });
        retryComboBox.setSelectedIndex(0);
        retryComboBox.setFont(new Font("Arial", Font.PLAIN, 16));
        JPanel retryPanel = new JPanel();
        retryPanel.setLayout(new BoxLayout(retryPanel, BoxLayout.Y_AXIS));
        retryPanel.add(new JLabel("Ritenta in caso di sconfitta"));
        retryPanel.add(retryComboBox);

        seriesComboBox = new JComboBox<>(new Integer[] { 1, 2, 5, 10, 50, 100 });
        seriesComboBox.setSelectedIndex(0);
        seriesComboBox.setFont(new Font("Arial", Font.PLAIN, 16));
        retryPanel.add(new JLabel("Giocate"));
        retryPanel.add(seriesComboBox);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, seriesScrollPane, resultScrollPane);
        splitPane.setResizeWeight(0.5);
        splitPane.setDividerLocation(300);

        setLayout(new BorderLayout());
        add(splitPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(retryPanel, BorderLayout.WEST);
        bottomPanel.add(startButton, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);

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
        if (!runtimeBets.equals(bets)) {
            saveBetsToFile(runtimeBets);
        }

        int retryCount = (int) retryComboBox.getSelectedItem();
        int seriesCount = (int) seriesComboBox.getSelectedItem();
        Roulette roulette = new Roulette();
        List<StringBuilder> results = new ArrayList<>();
        for (int i = 0; i < runtimeBets.size(); i++) {
            results.add(new StringBuilder());
        }

        int totalDots = 0;
        int totalBets = 0;
        int firstFailureRow = -1;
        int firstFailureSeries = -1;
        int totalProfitLoss = 0;

        List<Integer> columnProfits = new ArrayList<>();

        for (int series = 0; series < seriesCount; series++) {
            boolean stopGame = false;
            int attempts = 0;
            int columnProfitLoss = 0;

            for (int i = 0; i < runtimeBets.size(); i++) {
                Bet bet = runtimeBets.get(i);

                if (stopGame) {
                    if (attempts < retryCount) {
                        attempts++;
                        if (bet.shouldIgnore()) {
                            results.get(i).append("=");
                            continue;
                        } else {
                            stopGame = false;
                        }
                    } else {
                        results.get(i).append("X");
                        continue;
                    }
                }

                if (bet.shouldIgnore()) {
                    results.get(i).append("=");
                    continue;
                }

                int result = roulette.spin();
                if (bet.isWinningNumber(result)) {
                    results.get(i).append("X");
                    stopGame = true;
                    if (firstFailureRow == -1
                            || (i < firstFailureRow || (i == firstFailureRow && series < firstFailureSeries))) {
                        firstFailureRow = i;
                        firstFailureSeries = series;
                    }
                    columnProfitLoss -= 35;
                    if (retryCount == 0) {
                        for (int j = i + 1; j < runtimeBets.size(); j++) {
                            results.get(j).append("X");
                        }
                        break;
                    }
                } else {
                    results.get(i).append(".");
                    totalDots++;
                    columnProfitLoss += 1;
                }
            }

            columnProfits.add(columnProfitLoss);
            totalProfitLoss += columnProfitLoss;
            totalBets++;
        }

        StringBuilder resultText = new StringBuilder();
        for (int i = 0; i < runtimeBets.size(); i++) {
            resultText.append(results.get(i).toString()).append(" ").append(runtimeBets.get(i)).append("\n");
        }

        double averageDots = totalBets > 0 ? (double) totalDots / totalBets : 0;
        resultText.append("\nMedia dei punti (ovvero delle vittorie): ").append(averageDots);

        if (firstFailureRow != -1) {
            resultText.append("\nIl primo fallimento si registra dopo ").append(firstFailureRow + 1)
                    .append(" tentativi nella serie ").append((seriesCount - 1) - firstFailureSeries + 1).append(".");
        } else {
            resultText.append("\nNon ci sono stati fallimenti nelle serie.");
        }

        resultText.append("\n\n**Guadagno/Perdita per ciascuna serie**\n");
        for (int i = 0; i < columnProfits.size(); i++) {
            resultText.append("Serie ").append((seriesCount - 1) - i + 1).append(": ").append(columnProfits.get(i))
                    .append("€\n");
        }
        resultText.append("\nSomma complessiva: ").append(totalProfitLoss).append("€");

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

    private List<Bet> loadBetsFromFile() {
        List<Bet> loadedBets = new ArrayList<>();
        try {
            List<String> lines = Files.readAllLines(seriesFilePath);
            for (String line : lines) {
                String[] numbers = line.split(" ");
                if (numbers.length == 2) {
                    try {
                        int bet1 = Integer.parseInt(numbers[0].trim());
                        int bet2 = Integer.parseInt(numbers[1].trim());
                        loadedBets.add(new Bet(bet1, bet2));
                    } catch (NumberFormatException e) {
                        loadedBets.add(new Bet(-1, -1)); // Ignora
                    }
                } else {
                    loadedBets.add(new Bet(-1, -1)); // Ignora
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return loadedBets;
    }

    private void saveBetsToFile(List<Bet> bets) {
        try (BufferedWriter writer = Files.newBufferedWriter(seriesFilePath)) {
            for (Bet bet : bets) {
                writer.write(bet.toString());
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Path seriesFilePath = Paths.get("serie.txt");

        SwingUtilities.invokeLater(() -> {
            RouletteGameFrame frame = new RouletteGameFrame(seriesFilePath);
            frame.setVisible(true);
        });
    }
}