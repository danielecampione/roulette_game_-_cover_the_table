package it.campione.roulette;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

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
public class RouletteGameApp extends Application {
    private TextArea seriesTextArea;
    private TextArea resultTextArea;
    private ComboBox<Integer> retryComboBox;
    private ComboBox<Integer> seriesComboBox;
    private ComboBox<String> betAmountComboBox;
    private List<Bet> bets;
    private Path seriesFilePath;

    @Override
    public void start(Stage primaryStage) {
        seriesFilePath = Paths.get("serie.txt");
        bets = loadBetsFromFile();
        primaryStage.setTitle("Roulette Game - Cover the Table, Cover All Bases");

        seriesTextArea = new TextArea();
        seriesTextArea.setPromptText("Coppie escluse");
        seriesTextArea.setText(loadSeriesFromFile());
        seriesTextArea.setFont(javafx.scene.text.Font.font("Courier New", 12));
        seriesTextArea.setWrapText(true);

        resultTextArea = new TextArea();
        resultTextArea.setPromptText("Esito dell'estrazione");
        resultTextArea.setFont(javafx.scene.text.Font.font("Courier New", 12));
        resultTextArea.setWrapText(true);
        resultTextArea.setEditable(false);

        retryComboBox = new ComboBox<>(FXCollections.observableArrayList(0, 1, 2, 3));
        retryComboBox.getSelectionModel().selectFirst();

        seriesComboBox = new ComboBox<>(FXCollections.observableArrayList(1, 2, 5, 10, 50, 100));
        seriesComboBox.getSelectionModel().selectFirst();

        betAmountComboBox = new ComboBox<>(FXCollections.observableArrayList("35 € (1 € per numero)",
                "70 € (2 € per numero)", "105 € (3 € per numero)", "3.500 € (100 € per numero)",
                "5.250 € (150 € per numero)", "7.000 € (200 € per numero)"));
        betAmountComboBox.getSelectionModel().selectFirst();

        Button startButton = new Button("Avvia estrazione");
        startButton.setOnAction(e -> startExtraction());

        VBox controlsBox = new VBox(10, new Label("Ritenta in caso di sconfitta"), retryComboBox, new Label("Giocate"),
                seriesComboBox, new Label("Puntate sul tavolo"), betAmountComboBox, startButton);
        controlsBox.setPadding(new Insets(10));

        VBox.setVgrow(seriesTextArea, Priority.ALWAYS);
        VBox.setVgrow(resultTextArea, Priority.ALWAYS);

        VBox leftBox = new VBox(10, new Label("Coppie escluse"), seriesTextArea);
        VBox rightBox = new VBox(10, new Label("Esito dell'estrazione"), resultTextArea);
        SplitPane splitPane = new SplitPane(leftBox, rightBox);
        splitPane.setDividerPositions(0.5);

        BorderPane root = new BorderPane();
        root.setCenter(splitPane);
        root.setRight(controlsBox);

        Scene scene = new Scene(root, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void startExtraction() {
        List<Bet> runtimeBets = parseBetsFromTextArea();
        if (!runtimeBets.equals(bets)) {
            saveBetsToFile(runtimeBets);
        }

        int retryCount = retryComboBox.getValue();
        int seriesCount = seriesComboBox.getValue();
        int betAmount = getBetAmount(); // Ottieni l'importo della puntata selezionato
        int betUnit = betAmount / 35; // Calcola l'importo per numero puntato
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
                    columnProfitLoss -= betUnit * 35; // Moltiplica per l'importo per numero
                    if (firstFailureRow == -1
                            || (i < firstFailureRow || (i == firstFailureRow && series < firstFailureSeries))) {
                        firstFailureRow = i;
                        firstFailureSeries = series;
                    }
                    if (retryCount == 0) {
                        for (int j = i + 1; j < runtimeBets.size(); j++) {
                            results.get(j).append("X");
                        }
                        break;
                    }
                } else {
                    results.get(i).append(".");
                    totalDots++;
                    columnProfitLoss += betUnit; // Incrementa con l'importo per numero
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

        double averageDots = seriesCount > 0 ? (double) totalDots / seriesCount : 0;
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

    private int getBetAmount() {
        String selectedBet = betAmountComboBox.getValue();
        switch (selectedBet) {
        case "70 € (2 € per numero)":
            return 70;
        case "105 € (3 € per numero)":
            return 105;
        case "3.500 € (100 € per numero)":
            return 3500;
        case "5.250 € (150 € per numero)":
            return 5250;
        case "7.000 € (200 € per numero)":
            return 7000;
        case "35 € (1 € per numero)":
        default:
            return 35;
        }
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

    private String loadSeriesFromFile() {
        StringBuilder contentBuilder = new StringBuilder();
        try {
            List<String> lines = Files.readAllLines(seriesFilePath);
            for (String line : lines) {
                contentBuilder.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return contentBuilder.toString();
    }

    public static void main(String[] args) {
        launch(args);
    }
}