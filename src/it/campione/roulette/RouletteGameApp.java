package it.campione.roulette;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.effect.InnerShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * The method analyzed in this application is known as "Cover the Table" or
 * "Cover All Bases". This system involves covering almost all the numbers on
 * the roulette table, leaving only a few numbers uncovered, as for example two.
 * The goal is to maximize the chances of winning on each spin of the wheel,
 * although the profit for each win is generally low compared to the total bet.
 * The application proves that the dealer wins always and that he does it very
 * soon. Even the "Cover the table" method is therefore very risky for the
 * player.
 * 
 * @author D. Campione
 *
 */
public class RouletteGameApp extends Application {

    private Stage primaryStage;
    private Button startButton;
    private Button openRouletteButton;
    private TextArea seriesTextArea;
    private TextArea resultTextArea;
    private ComboBox<Integer> retryComboBox;
    private ComboBox<Integer> seriesComboBox;
    private ComboBox<String> betAmountComboBox;
    private ComboBox<Integer> attemptLimitComboBox;
    private VBox controlsBox;
    private VBox leftBox;
    private VBox rightBox;
    private List<Bet> bets;
    private Path seriesFilePath;
    private List<Integer> extractedNumbers;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        Locale locale = new Locale("en", "US"); // or "it", "IT" for Italian
        Messages.setLocale(locale);

        seriesFilePath = Paths.get("serie.txt");
        bets = loadBetsFromFile();
        primaryStage.setTitle(Messages.getString("applicationTitle"));

        seriesTextArea = new TextArea();
        seriesTextArea.setPromptText(Messages.getString("excludedCouples"));
        seriesTextArea.setText(loadSeriesFromFile());
        seriesTextArea.getStyleClass().add("text-area");
        seriesTextArea.setWrapText(true);

        resultTextArea = new TextArea();
        resultTextArea.setPromptText(Messages.getString("outcomeOfTheDraw"));
        resultTextArea.getStyleClass().add("text-area");
        resultTextArea.setWrapText(false);
        resultTextArea.setEditable(false);

        applyTransitions(seriesTextArea);
        applyTransitions(resultTextArea);

        retryComboBox = new ComboBox<>(FXCollections.observableArrayList(0, 1, 2, 3));
        retryComboBox.getSelectionModel().selectFirst();

        seriesComboBox = new ComboBox<>(FXCollections.observableArrayList(1, 2, 5, 10, 50, 100));
        seriesComboBox.getSelectionModel().selectFirst();

        betAmountComboBox = new ComboBox<>(FXCollections.observableArrayList(
                Messages.getString("EUR35_EUR1PerRouletteNumber"), Messages.getString("EUR70_EUR2PerRouletteNumber"),
                Messages.getString("EUR105_EUR3PerRouletteNumber"),
                Messages.getString("EUR3500_EUR100PerRouletteNumber"),
                Messages.getString("EUR5250_EUR150PerRouletteNumber"),
                Messages.getString("EUR7000_EUR200PerRouletteNumber")));
        betAmountComboBox.getSelectionModel().selectFirst();

        startButton = new Button(Messages.getString("startExtraction"));
        startButton.getStyleClass().add("button");
        startButton.setOnAction(e -> startExtraction());
        applyButtonEffects(startButton);

        openRouletteButton = new Button(Messages.getString("playRoulette"));
        openRouletteButton.getStyleClass().add("button");
        openRouletteButton.setOnAction(e -> openRouletteWindow());
        applyButtonEffects(openRouletteButton);

        attemptLimitComboBox = new ComboBox<>(
                FXCollections.observableArrayList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 15, 20, 25, 30, 35, 40, 45, 50));
        attemptLimitComboBox.getSelectionModel().selectFirst();
        attemptLimitComboBox.setDisable(false); // Initially enabled

        controlsBox = new VBox(10, new Label(Messages.getString("plays")), seriesComboBox,
                new Label(Messages.getString("retryOnLoss")), retryComboBox,
                new Label(Messages.getString("betsOnTheTable")), betAmountComboBox,
                new Label(Messages.getString("sumEURUpTo")), attemptLimitComboBox, startButton, openRouletteButton);
        controlsBox.setPadding(new Insets(10));

        seriesComboBox.getSelectionModel().selectedItemProperty().addListener((options, oldValue, newValue) -> {
            if (newValue.equals(1)) {
                attemptLimitComboBox.setDisable(false);
            } else {
                attemptLimitComboBox.setDisable(true);
                attemptLimitComboBox.getSelectionModel().selectFirst();
            }
        });

        VBox.setVgrow(seriesTextArea, Priority.ALWAYS);
        VBox.setVgrow(resultTextArea, Priority.ALWAYS);

        leftBox = new VBox(10, new Label(Messages.getString("excludedCouples")), seriesTextArea);
        leftBox.getChildren().get(0).getStyleClass().add("label");

        rightBox = new VBox(10, new Label(Messages.getString("outcomeOfTheDraw")), resultTextArea);
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

        // Set the flag panel for language switching within controlsBox
        setupLanguageSwitcher(controlsBox);

        Scene scene = new Scene(root, 800, 600);
        scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
        primaryStage.setScene(scene);

        primaryStage.setOnCloseRequest(event -> {
            event.consume(); // Consume the close event to handle it manually
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

        // Add neon effect to text area borders
        addNeonEffect(seriesTextArea);
        addNeonEffect(resultTextArea);

        int retryCount = retryComboBox.getValue();
        int seriesCount = seriesComboBox.getValue();
        int betAmount = getBetAmount(); // Get your selected bet amount
        int betUnit = betAmount / 35; // Calculate the amount per bet number
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

                // Always pull out the number
                int result = roulette.spin();
                extractedNumbers.add(result); // Add the drawn number to the list

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
                    columnProfitLoss += betUnit; // Increment with amount by number
                } else {
                    results.get(i).append("X");
                    stopGame = true;
                    failuresCount++; // Increase failure count
                    columnProfitLoss -= betUnit * 35; // Multiply by Amount by Number
                    if (firstFailureRow == -1
                            || (i < firstFailureRow || (i == firstFailureRow && series < firstFailureSeries))) {
                        firstFailureRow = i;
                        firstFailureSeries = series;
                    }
                    if (failuresCount > retryCount + 1) {
                        // Consider everything that follows as failure
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
                resultText.append(", " + Messages.getString("numberDrawn") + ": ").append(extractedNumber).append(" (")
                        .append(characteristics).append(")");
            }
            resultText.append("\n");
        }

        double averageDots = seriesCount > 0 ? (double) totalDots / seriesCount : 0;
        resultText.append("\n" + Messages.getString("averagePoints") + ": ").append(averageDots);

        if (firstFailureRow != -1) {
            resultText.append("\n" + Messages.getString("theFirstFailureIsRecordedAfter") + " ")
                    .append(firstFailureRow + 1).append(" " + Messages.getString("attemptsInTheSeries") + " ")
                    .append(firstFailureSeries + 1).append(".");
        } else {
            resultText.append("\n" + Messages.getString("thereWereNoFailuresInTheSeries"));
        }

        resultText.append("\n\n**" + Messages.getString("gainLossForEachSeries") + "**\n");
        for (int i = 0; i < columnProfits.size(); i++) {
            resultText.append(Messages.getString("series") + " ").append(i + 1).append(": ")
                    .append(columnProfits.get(i)).append(Messages.getString("euro") + "\n");
        }
        resultText.append("\n" + Messages.getString("totalSum") + ": ").append(totalProfitLoss)
                .append(Messages.getString("euro"));

        // Calculate gain/loss until set attempt
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
                        limitedProfitLoss += betUnit; // Increment with amount by number
                    } else if (c == 'X') {
                        limitedProfitLoss -= betUnit * 35; // Multiply by Amount by Number
                    }
                    attempts++;
                }
            }

            // If the capped sum is less than the aggregate sum and the aggregate sum is
            // strictly negative, use the aggregate sum
            if (limitedProfitLoss < totalProfitLoss && totalProfitLoss < 0) {
                limitedProfitLoss = totalProfitLoss;
            }

            resultText.append("\n" + Messages.getString("gainLossUpToAttempt") + " ").append(attemptLimit).append(": ")
                    .append(limitedProfitLoss).append(Messages.getString("euro"));
        }

        resultTextArea.setText(resultText.toString());

        // Remove the neon effect after completing the extraction
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
            textArea.setStyle(""); // Restore the original style from the CSS file
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
        case "70 \u20AC (2 \u20AC per numero)":
        case "70 \u20AC (2 \u20AC per number)":
            return 70;
        case "105 \u20AC (3 \u20AC per numero)":
        case "105 \u20AC (3 \u20AC per number)":
            return 105;
        case "3.500 \u20AC (100 \u20AC per numero)":
        case "3.500 \u20AC (100 \u20AC per number)":
            return 3500;
        case "5.250 \u20AC (150 \u20AC per numero)":
        case "5.250 \u20AC (150 \u20AC per number)":
            return 5250;
        case "7.000 \u20AC (200 \u20AC per numero)":
        case "7.000 \u20AC (200 \u20AC per number)":
            return 7000;
        case "35 \u20AC (1 \u20AC per numero)":
        case "35 \u20AC (1 \u20AC per number)":
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
                        runtimeBets.add(new Bet(-1, -1)); // Ignore
                    }
                } catch (NumberFormatException e) {
                    runtimeBets.add(new Bet(-1, -1)); // Ignore
                }
            } else {
                runtimeBets.add(new Bet(-1, -1)); // Ignore
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
                        loadedBets.add(new Bet(-1, -1)); // Ignore
                    }
                } else {
                    loadedBets.add(new Bet(-1, -1)); // Ignore
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
        // Let's create a rotation transition
        RotateTransition rotateTransition = new RotateTransition(Duration.millis(1000),
                primaryStage.getScene().getRoot());
        rotateTransition.setFromAngle(0); // Let's start from 0 degrees
        rotateTransition.setToAngle(360); // We rotate 360 degrees

        // Let's create a fade transition
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(1000), primaryStage.getScene().getRoot());
        fadeTransition.setFromValue(1.0); // Partiamo da opacità piena
        fadeTransition.setToValue(0.0); // Arriviamo a opacità zero

        // Let's create a zoom out transition (reduce size)
        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(1000), primaryStage.getScene().getRoot());
        scaleTransition.setFromX(1.0); // Let's start from the original size
        scaleTransition.setFromY(1.0);
        scaleTransition.setToX(0.5); // Let's reduce the size in half
        scaleTransition.setToY(0.5);

        // We perform the transitions in parallel
        ParallelTransition parallelTransition = new ParallelTransition(rotateTransition, fadeTransition,
                scaleTransition);
        parallelTransition.setOnFinished(event -> {
            Platform.runLater(() -> {
                primaryStage.close(); // We close the window when the animation is completed
                System.gc(); // We invoke the garbage collector to free memory
            });
        });

        // Let's start the animation
        parallelTransition.play();
    }

    // Modify the openRouletteWindow method to include opening with effects
    private void openRouletteWindow() {
        // Create a new window
        Stage rouletteStage = new Stage();
        rouletteStage.initModality(Modality.APPLICATION_MODAL);
        rouletteStage.setTitle("Gioca alla Roulette");

        // Create a WebView and upload the HTML file
        WebView webView = new WebView();
        webView.getEngine().load(getClass().getResource("roulette.html").toExternalForm());

        // Add the WebView to the scene and configure the window
        Scene scene = new Scene(webView, 800, 800);
        rouletteStage.setScene(scene);

        // Show the window immediately
        rouletteStage.show();

        // Add closing behavior with effects
        rouletteStage.setOnCloseRequest(event -> {
            event.consume(); // Consume the close event to handle it manually
            closeRouletteWindow(rouletteStage);
        });
    }

    private void closeRouletteWindow(Stage rouletteStage) {
        // Let's create a rotation transition
        RotateTransition rotateTransition = new RotateTransition(Duration.millis(1000),
                rouletteStage.getScene().getRoot());
        rotateTransition.setFromAngle(0);
        rotateTransition.setToAngle(360);

        // Let's create a scale transition
        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(1000),
                rouletteStage.getScene().getRoot());
        scaleTransition.setFromX(1.0);
        scaleTransition.setFromY(1.0);
        scaleTransition.setToX(0.5);
        scaleTransition.setToY(0.5);

        // Let's create a fade transition
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(1000), rouletteStage.getScene().getRoot());
        fadeTransition.setFromValue(1.0);
        fadeTransition.setToValue(0.0);

        // We perform transitions in parallel
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
            color = Messages.getString("green");
        } else if ((number >= 1 && number <= 10) || (number >= 19 && number <= 28)) {
            color = (number % 2 == 0) ? Messages.getString("black") : Messages.getString("red");
        } else {
            color = (number % 2 == 0) ? Messages.getString("red") : Messages.getString("black");
        }

        String parity = (number % 2 == 0) ? Messages.getString("even") : Messages.getString("odd");
        String range = (number >= 1 && number <= 18) ? Messages.getString("low")
                : (number >= 19 && number <= 36) ? Messages.getString("high") : "";

        return color + ", " + parity + ", " + range;
    }

    private void setupLanguageSwitcher(VBox controlsBox) {
        // Upload flag images
        ImageView itFlag = new ImageView(new Image(getClass().getResourceAsStream("/images/it_flag.png")));
        ImageView enFlag = new ImageView(new Image(getClass().getResourceAsStream("/images/en_flag.png")));

        // Size Flag Images
        itFlag.setFitWidth(30);
        itFlag.setFitHeight(20);
        enFlag.setFitWidth(30);
        enFlag.setFitHeight(20);

        // Create an HBox to hold flags
        HBox flagBox = new HBox(10, enFlag, itFlag);
        flagBox.setAlignment(Pos.CENTER_RIGHT);

        // Add HBox to VBox of controls
        controlsBox.getChildren().add(0, flagBox); // Add the Box flag to the top of the controls

        // Add EventHandler for language switching
        enFlag.setOnMouseClicked(event -> switchLanguage("en", "US"));
        itFlag.setOnMouseClicked(event -> switchLanguage("it", "IT"));

        // Animations for when the mouse enters the flag
        enFlag.setOnMouseEntered(event -> {
            enFlag.setStyle("-fx-cursor: hand;");
            animateFlag(enFlag, 1.2); // Scale the flag to 120% of its original size
        });
        itFlag.setOnMouseEntered(event -> {
            itFlag.setStyle("-fx-cursor: hand;");
            animateFlag(itFlag, 1.2); // Scale the flag to 120% of its original size
        });

        // Animations for when the mouse leaves the flag
        enFlag.setOnMouseExited(event -> {
            enFlag.setStyle("-fx-cursor: default;");
            animateFlag(enFlag, 1.0); // Scale the flag to its original size
        });
        itFlag.setOnMouseExited(event -> {
            itFlag.setStyle("-fx-cursor: default;");
            animateFlag(itFlag, 1.0); // Scale the flag to its original size
        });
    }

    private void animateFlag(ImageView flag, double scale) {
        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(150), flag); // 150 milliseconds for a
                                                                                           // better fluidity
        scaleTransition.setToX(scale);
        scaleTransition.setToY(scale);
        scaleTransition.play();
    }

    private void switchLanguage(String lang, String country) {
        Locale locale = new Locale(lang, country);
        Messages.setLocale(locale);

        // Update the interface texts
        updateTexts();
    }

    private void updateTexts() {
        primaryStage.setTitle(Messages.getString("applicationTitle"));
        seriesTextArea.setPromptText(Messages.getString("excludedCouples"));
        resultTextArea.setPromptText(Messages.getString("outcomeOfTheDraw"));
        startButton.setText(Messages.getString("startExtraction"));
        openRouletteButton.setText(Messages.getString("playRoulette"));

        // Update texts in ComboBoxes
        betAmountComboBox.setItems(FXCollections.observableArrayList(Messages.getString("EUR35_EUR1PerRouletteNumber"),
                Messages.getString("EUR70_EUR2PerRouletteNumber"), Messages.getString("EUR105_EUR3PerRouletteNumber"),
                Messages.getString("EUR3500_EUR100PerRouletteNumber"),
                Messages.getString("EUR5250_EUR150PerRouletteNumber"),
                Messages.getString("EUR7000_EUR200PerRouletteNumber")));
        betAmountComboBox.getSelectionModel().selectFirst();

        // Update texts in Vboxes
        controlsBox.getChildren().clear(); // First we clean the existing controls

        // Let's create the flag panel again
        setupLanguageSwitcher(controlsBox);

        // Now let's add all the controls with the updated texts again
        controlsBox.getChildren().addAll(new Label(Messages.getString("plays")), seriesComboBox,
                new Label(Messages.getString("retryOnLoss")), retryComboBox,
                new Label(Messages.getString("betsOnTheTable")), betAmountComboBox,
                new Label(Messages.getString("sumEURUpTo")), attemptLimitComboBox, startButton, openRouletteButton);

        leftBox.getChildren().setAll(new Label(Messages.getString("excludedCouples")), seriesTextArea);
        rightBox.getChildren().setAll(new Label(Messages.getString("outcomeOfTheDraw")), resultTextArea);
    }

    public static void main(String... args) {
        launch(args);
    }
}