package it.campione.roulette;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.effect.InnerShadow;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Il metodo analizzato in questa applicazione è noto come "Cover the Table" o
 * "Cover All Bases". Questo sistema prevede di coprire quasi tutti i numeri sul
 * tavolo della roulette, lasciando scoperti solo pochissimi numeri, come ad
 * esempio due. L'obiettivo è massimizzare le probabilità di vincita su ogni
 * giro della ruota, anche se il profitto per ogni vincita è generalmente basso
 * rispetto alla puntata totale. L'applicazione dimostra che il banco vince
 * sempre e che lo fa anche molto presto. Persino il metodo "Cover the table" è
 * pertanto molto rischioso per il giocatore.
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
    private ComboBox<Integer> attemptLimitComboBox;
    private List<Bet> bets;
    private Path seriesFilePath;
    private List<Integer> extractedNumbers;

    @Override
    public void start(Stage primaryStage) {
        seriesFilePath = Paths.get("serie.txt");
        bets = loadBetsFromFile();
        primaryStage.setTitle("Roulette Game - Cover the Table, Cover All Bases");

        seriesTextArea = new TextArea();
        seriesTextArea.setPromptText("Coppie escluse");
        seriesTextArea.setText(loadSeriesFromFile());
        seriesTextArea.getStyleClass().add("text-area");
        seriesTextArea.setWrapText(true);

        resultTextArea = new TextArea();
        resultTextArea.setPromptText("Esito dell'estrazione");
        resultTextArea.getStyleClass().add("text-area");
        resultTextArea.setWrapText(false);
        resultTextArea.setEditable(false);

        applyTransitions(seriesTextArea);
        applyTransitions(resultTextArea);

        retryComboBox = new ComboBox<>(FXCollections.observableArrayList(0, 1, 2, 3));
        retryComboBox.getSelectionModel().selectFirst();

        seriesComboBox = new ComboBox<>(FXCollections.observableArrayList(1, 2, 5, 10, 50, 100));
        seriesComboBox.getSelectionModel().selectFirst();

        betAmountComboBox = new ComboBox<>(FXCollections.observableArrayList("35 € (1 € per numero)",
                "70 € (2 € per numero)", "105 € (3 € per numero)", "3.500 € (100 € per numero)",
                "5.250 € (150 € per numero)", "7.000 € (200 € per numero)"));
        betAmountComboBox.getSelectionModel().selectFirst();

        Button startButton = new Button("Avvia estrazione");
        startButton.getStyleClass().add("button");
        startButton.setOnAction(e -> startExtraction());
        applyButtonEffects(startButton);

        // Aggiungi il pulsante per aprire la finestra di gioco della roulette
        Button openRouletteButton = new Button("Gioca alla Roulette");
        openRouletteButton.getStyleClass().add("button");
        openRouletteButton.setOnAction(e -> openRouletteWindow());
        applyButtonEffects(openRouletteButton);

        attemptLimitComboBox = new ComboBox<>(
                FXCollections.observableArrayList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 15, 20, 25, 30, 35, 40, 45, 50));
        attemptLimitComboBox.getSelectionModel().selectFirst();
        attemptLimitComboBox.setDisable(false); // Abilitata inizialmente

        VBox controlsBox = new VBox(10, new Label("Giocate"), seriesComboBox, new Label("Ritenta in caso di sconfitta"),
                retryComboBox, new Label("Puntate sul tavolo"), betAmountComboBox,
                new Label("Somma € fino a (0 = no limite)"), attemptLimitComboBox, startButton, openRouletteButton);
        controlsBox.setPadding(new Insets(10));

        // Aggiungi il listener per la ComboBox "Giocate"
        seriesComboBox.getSelectionModel().selectedItemProperty().addListener((options, oldValue, newValue) -> {
            if (newValue.equals(1)) {
                attemptLimitComboBox.setDisable(false);
            } else {
                attemptLimitComboBox.setDisable(true);
                attemptLimitComboBox.getSelectionModel().selectFirst(); // Resetta a "Nessun limite imposto" se
                                                                        // disabilitato
            }
        });

        VBox.setVgrow(seriesTextArea, Priority.ALWAYS);
        VBox.setVgrow(resultTextArea, Priority.ALWAYS);

        VBox leftBox = new VBox(10, new Label("Coppie escluse"), seriesTextArea);
        leftBox.getChildren().get(0).getStyleClass().add("label");
        VBox rightBox = new VBox(10, new Label("Esito dell'estrazione"), resultTextArea);
        rightBox.getChildren().get(0).getStyleClass().add("label");
        SplitPane splitPane = new SplitPane(leftBox, rightBox);
        splitPane.setDividerPositions(0.5);

        retryComboBox.setOnAction(e -> applyListBoxAnimation(retryComboBox));
        seriesComboBox.setOnAction(e -> applyListBoxAnimation(seriesComboBox));
        betAmountComboBox.setOnAction(e -> applyListBoxAnimation(betAmountComboBox));
        attemptLimitComboBox.setOnAction(e -> applyListBoxAnimation(attemptLimitComboBox));

        BorderPane root = new BorderPane();
        root.setCenter(splitPane);
        root.setRight(controlsBox);

        Scene scene = new Scene(root, 800, 600);
        scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
        primaryStage.setScene(scene);

        primaryStage.setOnCloseRequest(event -> {
            event.consume(); // Consuma l'evento di chiusura per gestirlo manualmente
            closeApp(primaryStage);
        });
        primaryStage.show();
    }

    private void applyTransitions(TextArea textArea) {
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(1000), textArea);
        fadeTransition.setFromValue(0.0);
        fadeTransition.setToValue(1.0);

        TranslateTransition translateTransition = new TranslateTransition(Duration.millis(1000), textArea);
        translateTransition.setFromX(-50);
        translateTransition.setToX(0);

        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(1000), textArea);
        scaleTransition.setFromX(0.8);
        scaleTransition.setFromY(0.8);
        scaleTransition.setToX(1.0);
        scaleTransition.setToY(1.0);

        RotateTransition rotateTransition = new RotateTransition(Duration.millis(1000), textArea);
        rotateTransition.setByAngle(360);

        ParallelTransition parallelTransition = new ParallelTransition(fadeTransition, translateTransition,
                scaleTransition, rotateTransition);
        parallelTransition.play();
    }

    private void applyButtonEffects(Button button) {
        button.setOnMouseEntered(e -> {
            button.setStyle(
                    "-fx-background-color: #45a049; -fx-effect: dropshadow(three-pass-box, rgba(0, 0, 0, 0.2), 10, 0, 0, 5); -fx-cursor: hand;");
            ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(200), button);
            scaleTransition.setToX(1.1);
            scaleTransition.setToY(1.1);
            scaleTransition.play();
        });
        button.setOnMouseExited(e -> {
            button.setStyle(
                    "-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-background-radius: 5px; -fx-padding: 10 20; -fx-font-size: 14px;");
            ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(200), button);
            scaleTransition.setToX(1.0);
            scaleTransition.setToY(1.0);
            scaleTransition.play();
        });
        button.setOnMousePressed(e -> {
            RotateTransition rotateTransition = new RotateTransition(Duration.millis(100), button);
            rotateTransition.setByAngle(5);
            rotateTransition.setCycleCount(2);
            rotateTransition.setAutoReverse(true);
            rotateTransition.play();
        });
    }

    private void startExtraction() {
        List<Bet> runtimeBets = parseBetsFromTextArea();
        if (!runtimeBets.equals(bets)) {
            saveBetsToFile(runtimeBets);
        }

        // Aggiungi l'effetto neon ai bordi delle text area
        addNeonEffect(seriesTextArea);
        addNeonEffect(resultTextArea);

        int retryCount = retryComboBox.getValue();
        int seriesCount = seriesComboBox.getValue();
        int betAmount = getBetAmount(); // Ottieni l'importo della puntata selezionato
        int betUnit = betAmount / 35; // Calcola l'importo per numero puntato
        Roulette roulette = new Roulette();
        List<StringBuilder> results = new ArrayList<>();
        for (int i = 0; i < runtimeBets.size(); i++) {
            results.add(new StringBuilder());
        }
        extractedNumbers = new ArrayList<>();
        int totalDots = 0;
        int firstFailureRow = -1;
        int firstFailureSeries = -1;
        int totalProfitLoss = 0;

        List<Integer> columnProfits = new ArrayList<>();

        for (int series = 0; series < seriesCount; series++) {
            boolean stopGame = false;
            int failuresCount = 0;
            int columnProfitLoss = 0;

            for (int i = 0; i < runtimeBets.size(); i++) {
                Bet bet = runtimeBets.get(i);

                // Estrarre sempre il numero
                int result = roulette.spin();
                extractedNumbers.add(result); // Aggiungi il numero estratto alla lista

                if (stopGame) {
                    if (failuresCount < retryCount + 1) {
                        if (bet.shouldIgnore()) {
                            results.get(i).append("=");
                            continue;
                        } else {
                            stopGame = false;
                        }
                    } else {
                        if (bet.shouldIgnore()) {
                            results.get(i).append("=");
                        } else {
                            results.get(i).append("X");
                        }
                        continue;
                    }
                }

                if (bet.shouldIgnore()) {
                    results.get(i).append("=");
                    continue;
                }

                if (bet.isWinningNumber(result)) {
                    results.get(i).append(".");
                    totalDots++;
                    columnProfitLoss += betUnit; // Incrementa con l'importo per numero
                } else {
                    results.get(i).append("X");
                    stopGame = true;
                    failuresCount++; // Incrementa il conteggio dei fallimenti
                    columnProfitLoss -= betUnit * 35; // Moltiplica per l'importo per numero
                    if (firstFailureRow == -1
                            || (i < firstFailureRow || (i == firstFailureRow && series < firstFailureSeries))) {
                        firstFailureRow = i;
                        firstFailureSeries = series;
                    }
                    if (failuresCount > retryCount + 1) {
                        // Considera come fallimento tutto ciò che segue
                        for (int j = i + 1; j < runtimeBets.size(); j++) {
                            Bet nextBet = runtimeBets.get(j);
                            if (nextBet.shouldIgnore()) {
                                results.get(j).append("=");
                            } else {
                                results.get(j).append("X");
                            }
                        }
                        break;
                    }
                }
            }

            columnProfits.add(columnProfitLoss);
            totalProfitLoss += columnProfitLoss;
        }

        StringBuilder resultText = new StringBuilder();
        for (int i = 0; i < runtimeBets.size(); i++) {
            resultText.append(results.get(i).toString()).append(" ").append(runtimeBets.get(i));
            if (seriesCount == 1 && i < extractedNumbers.size()) {
                int extractedNumber = extractedNumbers.get(i);
                String characteristics = getNumberCharacteristics(extractedNumber);
                resultText.append(", numero estratto: ").append(extractedNumber).append(" (").append(characteristics)
                        .append(")");
            }
            resultText.append("\n");
        }

        double averageDots = seriesCount > 0 ? (double) totalDots / seriesCount : 0;
        resultText.append("\nMedia dei punti (ovvero delle vittorie): ").append(averageDots);

        if (firstFailureRow != -1) {
            resultText.append("\nIl primo fallimento si registra dopo ").append(firstFailureRow + 1)
                    .append(" tentativi nella serie ").append(firstFailureSeries + 1).append(".");
        } else {
            resultText.append("\nNon ci sono stati fallimenti nelle serie.");
        }

        resultText.append("\n\n**Guadagno/Perdita per ciascuna serie**\n");
        for (int i = 0; i < columnProfits.size(); i++) {
            resultText.append("Serie ").append(i + 1).append(": ").append(columnProfits.get(i)).append("€\n");
        }
        resultText.append("\nSomma complessiva: ").append(totalProfitLoss).append("€");

        // Calcola il guadagno/perdita fino al tentativo impostato
        int attemptLimit = attemptLimitComboBox.getValue();
        if (attemptLimit != 0) {
            int limitedProfitLoss = 0;
            int attempts = 0;

            for (int i = 0; i < results.size(); i++) {
                String column = results.get(i).toString();
                for (char c : column.toCharArray()) {
                    if (attempts >= attemptLimit) {
                        break;
                    }
                    if (c == '.') {
                        limitedProfitLoss += betUnit; // Incrementa con l'importo per numero
                    } else if (c == 'X') {
                        limitedProfitLoss -= betUnit * 35; // Moltiplica per l'importo per numero
                    }
                    attempts++;
                }
            }

            // Se la somma limitata è inferiore alla somma complessiva e la somma
            // complessiva è strettamente negativa, usa la somma complessiva
            if (limitedProfitLoss < totalProfitLoss && totalProfitLoss < 0) {
                limitedProfitLoss = totalProfitLoss;
            }

            resultText.append("\nGuadagno/Perdita fino al tentativo ").append(attemptLimit).append(": ")
                    .append(limitedProfitLoss).append("€");
        }

        resultTextArea.setText(resultText.toString());

        // Rimuovi l'effetto neon dopo aver completato l'estrazione
        removeNeonEffect(seriesTextArea);
        removeNeonEffect(resultTextArea);
    }

    private void addNeonEffect(TextArea textArea) {
        InnerShadow innerShadow = new InnerShadow();
        innerShadow.setColor(Color.TRANSPARENT);

        textArea.setEffect(innerShadow);

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(innerShadow.colorProperty(), Color.TRANSPARENT)),
                new KeyFrame(Duration.seconds(1), new KeyValue(innerShadow.colorProperty(), Color.BLUE)));
        timeline.play();
    }

    private void removeNeonEffect(TextArea textArea) {
        InnerShadow innerShadow = (InnerShadow) textArea.getEffect();

        if (innerShadow == null) {
            innerShadow = new InnerShadow();
            innerShadow.setColor(Color.BLUE);
            textArea.setEffect(innerShadow);
        }

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(innerShadow.colorProperty(), Color.BLUE)),
                new KeyFrame(Duration.seconds(1), new KeyValue(innerShadow.colorProperty(), Color.TRANSPARENT)));
        timeline.setOnFinished(e -> {
            textArea.setEffect(null);
            textArea.setStyle(""); // Ripristina lo stile originale dal file CSS
        });
        timeline.play();
    }

    private void applyListBoxAnimation(ComboBox<?> comboBox) {
        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(500), comboBox);
        scaleTransition.setFromX(1.0);
        scaleTransition.setFromY(1.0);
        scaleTransition.setToX(1.1);
        scaleTransition.setToY(1.1);
        scaleTransition.setAutoReverse(true);
        scaleTransition.setCycleCount(2);
        scaleTransition.play();
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

    private void closeApp(Stage primaryStage) {
        // Creiamo una transizione di dissolvenza
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(1000), primaryStage.getScene().getRoot());
        fadeTransition.setFromValue(1.0);
        fadeTransition.setToValue(0.0);

        // Creiamo una transizione di spostamento laterale
        TranslateTransition translateTransition = new TranslateTransition(Duration.millis(1000),
                primaryStage.getScene().getRoot());
        translateTransition.setFromX(0);
        translateTransition.setToX(-primaryStage.getWidth());

        // Quando la transizione è completata, chiudiamo l'app
        fadeTransition.setOnFinished(event -> {
            Platform.runLater(() -> {
                primaryStage.close();
                System.gc(); // Richiama il Garbage Collector per pulire la memoria
            });
        });

        // Avvia le transizioni
        fadeTransition.play();
        translateTransition.play();
    }

    // Modifica il metodo openRouletteWindow per includere l'apertura con effetti
    private void openRouletteWindow() {
        // Crea una nuova finestra
        Stage rouletteStage = new Stage();
        rouletteStage.initModality(Modality.APPLICATION_MODAL);
        rouletteStage.setTitle("Gioca alla Roulette");

        // Crea un WebView e carica il file HTML
        WebView webView = new WebView();
        webView.getEngine().load(getClass().getResource("roulette.html").toExternalForm());

        // Aggiungi il WebView alla scena e configura la finestra
        Scene scene = new Scene(webView, 800, 800);
        rouletteStage.setScene(scene);

        // Mostra la finestra immediatamente
        rouletteStage.show();

        // Aggiungi il comportamento di chiusura con effetti
        rouletteStage.setOnCloseRequest(event -> {
            event.consume(); // Consuma l'evento di chiusura per gestirlo manualmente
            closeRouletteWindow(rouletteStage);
        });
    }

    private void closeRouletteWindow(Stage rouletteStage) {
        // Creiamo una transizione di rotazione
        RotateTransition rotateTransition = new RotateTransition(Duration.millis(1000),
                rouletteStage.getScene().getRoot());
        rotateTransition.setFromAngle(0);
        rotateTransition.setToAngle(360);

        // Creiamo una transizione di scala
        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(1000),
                rouletteStage.getScene().getRoot());
        scaleTransition.setFromX(1.0);
        scaleTransition.setFromY(1.0);
        scaleTransition.setToX(0.5);
        scaleTransition.setToY(0.5);

        // Creiamo una transizione di dissolvenza
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(1000), rouletteStage.getScene().getRoot());
        fadeTransition.setFromValue(1.0);
        fadeTransition.setToValue(0.0);

        // Eseguiamo le transizioni in parallelo
        ParallelTransition parallelTransition = new ParallelTransition(rotateTransition, scaleTransition,
                fadeTransition);
        parallelTransition.setOnFinished(event -> {
            rouletteStage.close();
        });

        parallelTransition.play();
    }

    private String getNumberCharacteristics(int number) {
        String color;
        if (number == 0) {
            color = "verde";
        } else if ((number >= 1 && number <= 10) || (number >= 19 && number <= 28)) {
            color = (number % 2 == 0) ? "nero" : "rosso";
        } else {
            color = (number % 2 == 0) ? "rosso" : "nero";
        }

        String parity = (number % 2 == 0) ? "pari" : "dispari";
        String range = (number >= 1 && number <= 18) ? "basso" : (number >= 19 && number <= 36) ? "alto" : "";

        return color + ", " + parity + ", " + range;
    }

    public static void main(String... args) {
        launch(args);
    }
}