package com.example;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class Main extends Application {

    private static final int TOTAL_ROOMS     = 100;
    private static final int FLOORS          = 10;
    private static final int ROOMS_PER_FLOOR = 10;

    private static final String[] FLOOR_COLORS = {
        "#e74c3c", "#e67e22", "#f1c40f", "#2ecc71",
        "#1abc9c", "#3498db", "#9b59b6", "#e91e63",
        "#00bcd4", "#ff5722"
    };

    private static final String BG_DARK     = "#0d1117";
    private static final String BG_CARD     = "#161b22";
    private static final String BG_CARD2    = "#21262d";
    private static final String ACCENT_GOLD = "#f0a500";
    private static final String BTN_GREEN   = "#238636";
    private static final String BTN_BLUE    = "#1f6feb";
    private static final String BTN_RED     = "#da3633";
    private static final String BTN_PURPLE  = "#8957e5";
    private static final String BTN_ORANGE  = "#e67e22";
    private static final String TEXT_LIGHT  = "#e6edf3";
    private static final String TEXT_MUTED  = "#8b949e";
    private static final String AVAILABLE   = "#238636";
    private static final String BOOKED      = "#da3633";
    private static final String BORDER      = "#30363d";

    @Override
    public void start(Stage stage) {
        if (DB.adminExists()) showLoginScreen(stage);
        else showSetupScreen(stage);
    }

    // ── Setup Screen ──────────────────────────────────────────────────────────
    private void showSetupScreen(Stage stage) {
        VBox root = new VBox();
        root.setStyle("-fx-background-color: " + BG_DARK + ";");
        root.setAlignment(Pos.CENTER);

        HBox banner = gradientBanner("🏨  SwiftStay Hotel Management",
                "Create your admin account to get started", "#1f6feb", "#8957e5");

        VBox formCard = new VBox(16);
        formCard.setMaxWidth(420);
        formCard.setStyle(cardStyle(BG_CARD));
        formCard.setPadding(new Insets(32));

        TextField     userField    = styledTextField("Choose a username");
        PasswordField passField    = styledPasswordField("Choose a password");
        PasswordField confirmField = styledPasswordField("Confirm password");
        Label statusLabel = statusLabel();
        Button createBtn  = bigButton("🚀  Create Account & Launch", BTN_BLUE);

        formCard.getChildren().addAll(sectionLabel("⚙  Admin Setup"),
                fieldGroup("Username", userField),
                fieldGroup("Password", passField),
                fieldGroup("Confirm Password", confirmField),
                createBtn, statusLabel);

        StackPane center = new StackPane(formCard);
        center.setPadding(new Insets(40));
        VBox.setVgrow(center, Priority.ALWAYS);
        root.getChildren().addAll(banner, center);

        createBtn.setOnAction(e -> {
            String user = userField.getText().trim();
            String pass = passField.getText();
            String conf = confirmField.getText();
            if (user.isEmpty()) { setStatus(statusLabel, "⚠  Enter a username.", "#f0a500"); return; }
            if (pass.isEmpty()) { setStatus(statusLabel, "⚠  Enter a password.", "#f0a500"); return; }
            if (!pass.equals(conf)) { setStatus(statusLabel, "⚠  Passwords do not match.", BOOKED); return; }
            DB.registerAdmin(user, pass);
            setStatus(statusLabel, "✅  Account created! Launching...", AVAILABLE);
            javafx.animation.PauseTransition p =
                new javafx.animation.PauseTransition(javafx.util.Duration.seconds(1));
            p.setOnFinished(ev -> showLoginScreen(stage));
            p.play();
        });

        stage.setTitle("SwiftStay — Setup");
        stage.setScene(new Scene(root, 600, 560));
        stage.setResizable(false);
        stage.show();
    }

    // ── Login Screen ──────────────────────────────────────────────────────────
    private void showLoginScreen(Stage stage) {
        VBox root = new VBox();
        root.setStyle("-fx-background-color: " + BG_DARK + ";");
        root.setAlignment(Pos.CENTER);

        HBox banner = gradientBanner("🏨  SwiftStay",
                "Hotel Management System — Admin Portal", "#1f6feb", "#8957e5");

        VBox formCard = new VBox(16);
        formCard.setMaxWidth(400);
        formCard.setStyle(cardStyle(BG_CARD));
        formCard.setPadding(new Insets(32));

        TextField     userField = styledTextField("Username");
        PasswordField passField = styledPasswordField("Password");
        Label statusLabel = statusLabel();
        Button loginBtn   = bigButton("🔐  Login to Dashboard", BTN_BLUE);

        formCard.getChildren().addAll(sectionLabel("🔐  Admin Login"),
                fieldGroup("Username", userField),
                fieldGroup("Password", passField),
                loginBtn, statusLabel);

        StackPane center = new StackPane(formCard);
        center.setPadding(new Insets(40));
        VBox.setVgrow(center, Priority.ALWAYS);
        root.getChildren().addAll(banner, center);

        loginBtn.setOnAction(e -> {
            if (userField.getText().trim().isEmpty() || passField.getText().isEmpty()) {
                setStatus(statusLabel, "⚠  Enter username and password.", "#f0a500"); return;
            }
            if (DB.validateLogin(userField.getText().trim(), passField.getText())) {
                showDashboard(stage);
            } else {
                setStatus(statusLabel, "❌  Invalid credentials.", BOOKED);
                passField.clear();
            }
        });
        passField.setOnAction(e -> loginBtn.fire());

        stage.setTitle("SwiftStay — Login");
        stage.setScene(new Scene(root, 600, 500));
        stage.setResizable(false);
        stage.show();
    }

    // ── Dashboard ─────────────────────────────────────────────────────────────
    private void showDashboard(Stage stage) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + BG_DARK + ";");

        HBox navbar = new HBox();
        navbar.setStyle("-fx-background-color: " + BG_CARD + ";" +
                        "-fx-border-color: " + BORDER + "; -fx-border-width: 0 0 1 0;");
        navbar.setPadding(new Insets(14, 24, 14, 24));
        navbar.setAlignment(Pos.CENTER_LEFT);
        navbar.setSpacing(10);

        Label logo = new Label("🏨  SwiftStay");
        logo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        logo.setTextFill(Color.web("#58a6ff"));

        Label tagline = new Label("Hotel Management System");
        tagline.setFont(Font.font("Segoe UI", 13));
        tagline.setTextFill(Color.web(TEXT_MUTED));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button newBookingBtn  = navButton("➕  New Booking",  BTN_GREEN);
        Button allBookingsBtn = navButton("📋  All Bookings", BTN_BLUE);
        Button feedbackBtn    = navButton("⭐  Feedback",      BTN_PURPLE);
        Button pricesBtn      = navButton("💲  Edit Prices",   BTN_ORANGE);
        Button logoutBtn      = navButton("🚪  Logout",        "#6e7681");

        navbar.getChildren().addAll(logo, tagline, spacer,
                newBookingBtn, allBookingsBtn, feedbackBtn, pricesBtn, logoutBtn);

        List<String> booked = DB.getBookedRooms();
        int bookedCount   = booked.size();
        int availCount    = TOTAL_ROOMS - bookedCount;
        int totalRevenue  = DB.getTotalRevenue();
        int totalBookings = DB.getTotalBookings();
        double avgRating  = DB.getAverageRating();

        HBox statsBar = new HBox(12);
        statsBar.setPadding(new Insets(16, 24, 16, 24));
        statsBar.setStyle("-fx-background-color: " + BG_DARK + ";");
        statsBar.getChildren().addAll(
            statCard("🏠  Total Rooms",  String.valueOf(TOTAL_ROOMS),  "#58a6ff"),
            statCard("✅  Available",     String.valueOf(availCount),   AVAILABLE),
            statCard("🔴  Occupied",      String.valueOf(bookedCount),  BOOKED),
            statCard("📦  Bookings",      String.valueOf(totalBookings), BTN_PURPLE),
            statCard("💰  Revenue",       "₹" + String.format("%,d", totalRevenue), ACCENT_GOLD),
            statCard("⭐  Avg Rating",    avgRating > 0
                    ? String.format("%.1f / 5", avgRating) : "No ratings", "#f1c40f")
        );

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setStyle("-fx-background: " + BG_DARK +
                            "; -fx-background-color: " + BG_DARK + ";");
        scrollPane.setFitToWidth(true);

        VBox floorsBox = new VBox(20);
        floorsBox.setPadding(new Insets(20, 24, 20, 24));
        floorsBox.setStyle("-fx-background-color: " + BG_DARK + ";");

        Label gridTitle = new Label("🗺  Room Availability Map");
        gridTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        gridTitle.setTextFill(Color.web(TEXT_LIGHT));

        HBox legend = new HBox(20);
        legend.setAlignment(Pos.CENTER_LEFT);
        legend.getChildren().addAll(
            legendItem("🟢  Available — click to book", AVAILABLE),
            legendItem("🔴  Occupied — click to view guest", BOOKED)
        );

        floorsBox.getChildren().addAll(gridTitle, legend);

        for (int floor = 1; floor <= FLOORS; floor++) {
            String floorColor = FLOOR_COLORS[floor - 1];

            Label floorLabel = new Label("  Floor " + floor +
                    "  (Rooms " + (floor * 100 + 1) + " – " + (floor * 100 + 10) + ")");
            floorLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
            floorLabel.setTextFill(Color.WHITE);
            floorLabel.setStyle("-fx-background-color: " + floorColor + ";" +
                                "-fx-background-radius: 8 8 0 0;" +
                                "-fx-padding: 8 16;");
            floorLabel.setMaxWidth(Double.MAX_VALUE);

            HBox roomRow = new HBox(10);
            roomRow.setPadding(new Insets(14));
            roomRow.setStyle("-fx-background-color: " + BG_CARD + ";" +
                             "-fx-background-radius: 0 0 10 10;" +
                             "-fx-border-color: " + floorColor + ";" +
                             "-fx-border-width: 0 1 1 1;" +
                             "-fx-border-radius: 0 0 10 10;");
            roomRow.setAlignment(Pos.CENTER_LEFT);

            for (int r = 1; r <= ROOMS_PER_FLOOR; r++) {
                String  roomNum  = String.valueOf(floor * 100 + r);
                boolean isBooked = booked.contains(roomNum);
                String  roomType = getRoomType(r);
                String  typeEmoji = getTypeEmoji(roomType);

                VBox roomCard = buildRoomCard(roomNum, roomType, typeEmoji, isBooked);

                final String  rn = roomNum;
                final boolean rb = isBooked;
                roomCard.setOnMouseClicked(ev -> {
                    if (rb) showRoomDetails(stage, rn);
                    else    showBookingForm(stage, rn, () -> showDashboard(stage));
                });
                roomCard.setOnMouseEntered(ev -> applyRoomHover(roomCard, rb));
                roomCard.setOnMouseExited(ev  -> applyRoomNormal(roomCard, rb));

                roomRow.getChildren().add(roomCard);
            }

            floorsBox.getChildren().add(new VBox(0, floorLabel, roomRow));
        }

        scrollPane.setContent(floorsBox);

        VBox mainContent = new VBox(0, statsBar, scrollPane);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        root.setTop(navbar);
        root.setCenter(mainContent);

        newBookingBtn.setOnAction(e  -> showBookingForm(stage, null, () -> showDashboard(stage)));
        allBookingsBtn.setOnAction(e -> showAllBookings(stage));
        feedbackBtn.setOnAction(e    -> showAllFeedback(stage));
        pricesBtn.setOnAction(e      -> showEditPrices(stage));
        logoutBtn.setOnAction(e      -> showLoginScreen(stage));

        stage.setTitle("SwiftStay — Dashboard");
        stage.setScene(new Scene(root, 1100, 750));
        stage.setResizable(true);
        stage.show();
    }

    // ── Room Card ─────────────────────────────────────────────────────────────
    private VBox buildRoomCard(String roomNum, String roomType,
                                String typeEmoji, boolean isBooked) {
        VBox card = new VBox(4);
        card.setAlignment(Pos.CENTER);
        card.setPrefSize(82, 72);
        applyRoomNormal(card, isBooked);

        Label numLabel = new Label(roomNum);
        numLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        numLabel.setTextFill(Color.web(isBooked ? BOOKED : AVAILABLE));

        Label typeLabel = new Label(typeEmoji + " " + roomType);
        typeLabel.setFont(Font.font("Segoe UI", 10));
        typeLabel.setTextFill(Color.web(TEXT_MUTED));

        Label statusLabel = new Label(isBooked ? "● Booked" : "● Free");
        statusLabel.setFont(Font.font("Segoe UI", 10));
        statusLabel.setTextFill(Color.web(isBooked ? BOOKED : AVAILABLE));

        card.getChildren().addAll(numLabel, typeLabel, statusLabel);
        return card;
    }

    private void applyRoomNormal(VBox card, boolean isBooked) {
        card.setStyle(
            "-fx-background-color: " + (isBooked ? "#2d1b1b" : "#1b2d1b") + ";" +
            "-fx-background-radius: 8;" +
            "-fx-border-color: " + (isBooked ? BOOKED : AVAILABLE) + ";" +
            "-fx-border-width: 2; -fx-border-radius: 8; -fx-cursor: hand;"
        );
    }

    private void applyRoomHover(VBox card, boolean isBooked) {
        card.setStyle(
            "-fx-background-color: " + (isBooked ? "#3d1b1b" : "#1b3d1b") + ";" +
            "-fx-background-radius: 8;" +
            "-fx-border-color: " + (isBooked ? "#ff6b6b" : "#40c057") + ";" +
            "-fx-border-width: 2; -fx-border-radius: 8; -fx-cursor: hand;" +
            "-fx-effect: dropshadow(gaussian, " +
                (isBooked ? "rgba(218,54,51,0.5)" : "rgba(35,134,54,0.5)") +
                ", 12, 0.3, 0, 0);"
        );
    }

    // ── Room Details ──────────────────────────────────────────────────────────
    private void showRoomDetails(Stage owner, String roomNum) {
        Booking b = DB.getBookingByRoom(roomNum);
        if (b == null) return;

        Stage dialog = new Stage();
        dialog.initOwner(owner);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Room " + roomNum + " — Guest Details");

        VBox box = new VBox(16);
        box.setPadding(new Insets(28));
        box.setStyle("-fx-background-color: " + BG_CARD + ";");
        box.setPrefWidth(440);

        Label title = new Label("🛏  Room " + roomNum + " — Current Guest");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        title.setTextFill(Color.web("#58a6ff"));

        GridPane grid = new GridPane();
        grid.setVgap(10); grid.setHgap(16);
        grid.setStyle("-fx-background-color: " + BG_CARD2 + ";" +
                      "-fx-background-radius: 8; -fx-padding: 16;");

        String[][] rows = {
            {"👤 Guest",     b.getName()},
            {"📱 Phone",     b.getPhone()},
            {"📧 Email",     b.getEmail()},
            {"🛏 Room Type", b.getRoom()},
            {"📅 Check-in",  b.getCheckIn()},
            {"📅 Check-out", b.getCheckOut()},
            {"🌙 Nights",    String.valueOf(b.getDays())},
            {"💰 Total",     "₹" + String.format("%,d", b.getTotal())}
        };
        for (int i = 0; i < rows.length; i++) {
            Label k = new Label(rows[i][0]);
            k.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
            k.setTextFill(Color.web(TEXT_MUTED));
            Label v = new Label(rows[i][1] != null ? rows[i][1] : "—");
            v.setFont(Font.font("Segoe UI", 13));
            v.setTextFill(Color.web(TEXT_LIGHT));
            grid.add(k, 0, i); grid.add(v, 1, i);
        }

        Button checkoutBtn = bigButton("✅  Checkout & Collect Feedback", BTN_RED);
        Button closeBtn    = bigButton("Close", "#6e7681");

        checkoutBtn.setOnAction(e -> {
            DB.deleteByRoom(roomNum);
            dialog.close();
            showFeedbackForm(owner, b.getName(), roomNum, () -> showDashboard(owner));
        });
        closeBtn.setOnAction(e -> dialog.close());

        HBox btns = new HBox(10, checkoutBtn, closeBtn);
        box.getChildren().addAll(title, grid, btns);

        dialog.setScene(new Scene(box));
        dialog.show();
    }

    // ── Feedback Form ─────────────────────────────────────────────────────────
    private void showFeedbackForm(Stage owner, String guestName,
                                   String roomNumber, Runnable onDone) {
        Stage dialog = new Stage();
        dialog.initOwner(owner);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Guest Feedback — SwiftStay");

        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: " + BG_DARK + ";");

        HBox banner = gradientBanner("⭐  Guest Feedback",
                "How was your stay at SwiftStay?", BTN_PURPLE, "#e91e63");

        VBox formCard = new VBox(18);
        formCard.setPadding(new Insets(28));
        formCard.setStyle("-fx-background-color: " + BG_CARD + ";");

        Label guestLbl = new Label("Guest: " + guestName + "   |   Room: " + roomNumber);
        guestLbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        guestLbl.setTextFill(Color.web("#58a6ff"));

        Label ratingLbl = new Label("Rate your experience:");
        ratingLbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        ratingLbl.setTextFill(Color.web(TEXT_LIGHT));

        HBox stars = new HBox(8);
        stars.setAlignment(Pos.CENTER_LEFT);
        final int[] selectedRating = {0};
        Button[] starBtns = new Button[5];

        for (int i = 0; i < 5; i++) {
            Button sb = new Button("☆");
            sb.setStyle("-fx-background-color: transparent; -fx-text-fill: #f1c40f;" +
                        "-fx-font-size: 28px; -fx-cursor: hand; -fx-padding: 0 4;");
            starBtns[i] = sb;
            stars.getChildren().add(sb);
        }

        Label selectedLbl = new Label("No rating selected");
        selectedLbl.setFont(Font.font("Segoe UI", 12));
        selectedLbl.setTextFill(Color.web(TEXT_MUTED));

        for (int i = 0; i < 5; i++) {
            final int star = i + 1;
            starBtns[i].setOnAction(e -> {
                selectedRating[0] = star;
                String[] labels = {"", "Poor 😞", "Fair 😐",
                        "Good 🙂", "Very Good 😊", "Excellent 🤩"};
                selectedLbl.setText(star + " / 5 — " + labels[star]);
                selectedLbl.setTextFill(Color.web("#f1c40f"));
                for (int j = 0; j < 5; j++)
                    starBtns[j].setText(j < star ? "★" : "☆");
            });
        }

        Label commentLbl = new Label("Leave a comment (optional):");
        commentLbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        commentLbl.setTextFill(Color.web(TEXT_LIGHT));

        TextArea commentArea = new TextArea();
        commentArea.setPromptText("Tell us about your experience...");
        commentArea.setPrefRowCount(4);
        commentArea.setStyle("-fx-control-inner-background: " + BG_CARD2 + ";" +
                             "-fx-text-fill: " + TEXT_LIGHT + ";" +
                             "-fx-prompt-text-fill: " + TEXT_MUTED + ";" +
                             "-fx-background-radius: 8; -fx-border-color: " + BORDER + ";" +
                             "-fx-border-radius: 8; -fx-font-size: 13px;");

        Label statusLabel = statusLabel();
        Button submitBtn  = bigButton("⭐  Submit Feedback", BTN_PURPLE);
        Button skipBtn    = bigButton("Skip", "#6e7681");

        HBox btnRow = new HBox(10, submitBtn, skipBtn);
        HBox.setHgrow(submitBtn, Priority.ALWAYS);
        HBox.setHgrow(skipBtn, Priority.ALWAYS);

        formCard.getChildren().addAll(guestLbl, new Separator(),
                ratingLbl, stars, selectedLbl,
                commentLbl, commentArea, btnRow, statusLabel);

        root.getChildren().addAll(banner, formCard);

        submitBtn.setOnAction(e -> {
            if (selectedRating[0] == 0) {
                setStatus(statusLabel, "⚠  Please select a star rating.", "#f0a500"); return;
            }
            DB.insertFeedback(guestName, roomNumber,
                    selectedRating[0], commentArea.getText().trim());
            setStatus(statusLabel, "✅  Thank you for your feedback!", AVAILABLE);
            javafx.animation.PauseTransition p =
                new javafx.animation.PauseTransition(javafx.util.Duration.seconds(1));
            p.setOnFinished(ev -> { dialog.close(); if (onDone != null) onDone.run(); });
            p.play();
        });

        skipBtn.setOnAction(e -> { dialog.close(); if (onDone != null) onDone.run(); });

        dialog.setScene(new Scene(root, 520, 560));
        dialog.show();
    }

    // ── All Feedback ──────────────────────────────────────────────────────────
    private void showAllFeedback(Stage owner) {
        Stage s = new Stage();
        s.initOwner(owner);
        s.setTitle("Guest Feedback — SwiftStay");

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + BG_DARK + ";");

        HBox banner = gradientBanner("⭐  Guest Feedback",
                "All ratings and reviews from guests", BTN_PURPLE, "#e91e63");

        double avgRating = DB.getAverageRating();
        Label avgLbl = new Label(String.format(
            "  ⭐  Average Rating: %.1f / 5.0   |   Total Reviews: %d  ",
            avgRating, DB.getAllFeedback().size()));
        avgLbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        avgLbl.setTextFill(Color.web("#f1c40f"));
        avgLbl.setStyle("-fx-background-color: " + BG_CARD2 + "; -fx-padding: 14 20;");
        avgLbl.setMaxWidth(Double.MAX_VALUE);

        VBox feedbackList = new VBox(12);
        feedbackList.setPadding(new Insets(16, 20, 16, 20));
        feedbackList.setStyle("-fx-background-color: " + BG_DARK + ";");

        List<String[]> feedbacks = DB.getAllFeedback();
        if (feedbacks.isEmpty()) {
            Label empty = new Label("No feedback yet.");
            empty.setFont(Font.font("Segoe UI", 14));
            empty.setTextFill(Color.web(TEXT_MUTED));
            feedbackList.getChildren().add(empty);
        } else {
            for (String[] f : feedbacks) {
                int    rating  = Integer.parseInt(f[2]);
                String comment = f[3] != null && !f[3].isEmpty() ? f[3] : "No comment";
                String stars   = "★".repeat(rating) + "☆".repeat(5 - rating);

                VBox card = new VBox(6);
                card.setPadding(new Insets(14, 18, 14, 18));
                card.setStyle("-fx-background-color: " + BG_CARD + ";" +
                              "-fx-background-radius: 10;" +
                              "-fx-border-color: " + BORDER + ";" +
                              "-fx-border-radius: 10; -fx-border-width: 1;");

                HBox topRow = new HBox(10);
                Label nameLbl = new Label("👤 " + f[0]);
                nameLbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
                nameLbl.setTextFill(Color.web(TEXT_LIGHT));

                Label roomLbl = new Label("Room " + f[1]);
                roomLbl.setFont(Font.font("Segoe UI", 12));
                roomLbl.setTextFill(Color.web(TEXT_MUTED));
                roomLbl.setStyle("-fx-background-color: " + BG_CARD2 + ";" +
                                 "-fx-background-radius: 4; -fx-padding: 2 8;");

                Region sp2 = new Region();
                HBox.setHgrow(sp2, Priority.ALWAYS);

                Label dateLbl = new Label(f[4]);
                dateLbl.setFont(Font.font("Segoe UI", 11));
                dateLbl.setTextFill(Color.web(TEXT_MUTED));

                topRow.setAlignment(Pos.CENTER_LEFT);
                topRow.getChildren().addAll(nameLbl, roomLbl, sp2, dateLbl);

                Label starsLbl = new Label(stars);
                starsLbl.setFont(Font.font("Segoe UI", 18));
                starsLbl.setTextFill(Color.web("#f1c40f"));

                Label commentLbl = new Label("💬 " + comment);
                commentLbl.setFont(Font.font("Segoe UI", 13));
                commentLbl.setTextFill(Color.web(TEXT_MUTED));
                commentLbl.setWrapText(true);

                card.getChildren().addAll(topRow, starsLbl, commentLbl);
                feedbackList.getChildren().add(card);
            }
        }

        ScrollPane sp = new ScrollPane(feedbackList);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background: " + BG_DARK +
                    "; -fx-background-color: " + BG_DARK + ";");

        root.setTop(new VBox(banner, avgLbl));
        root.setCenter(sp);

        s.setScene(new Scene(root, 680, 600));
        s.show();
    }

    // ── Edit Prices ───────────────────────────────────────────────────────────
    private void showEditPrices(Stage owner) {
        Stage dialog = new Stage();
        dialog.initOwner(owner);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Edit Room Prices — SwiftStay");

        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: " + BG_DARK + ";");

        HBox banner = gradientBanner("💲  Edit Room Prices",
                "Update nightly rates for each room type", BTN_ORANGE, ACCENT_GOLD);

        VBox formCard = new VBox(20);
        formCard.setPadding(new Insets(28));
        formCard.setStyle("-fx-background-color: " + BG_CARD + ";");

        int curSingle = DB.getPrice("Single");
        int curDouble = DB.getPrice("Double");
        int curSuite  = DB.getPrice("Suite");

        TextField singleField = styledTextField("Current: ₹" + curSingle);
        TextField doubleField = styledTextField("Current: ₹" + curDouble);
        TextField suiteField  = styledTextField("Current: ₹" + curSuite);

        singleField.setText(String.valueOf(curSingle));
        doubleField.setText(String.valueOf(curDouble));
        suiteField.setText(String.valueOf(curSuite));

        VBox singleCard = priceCard("🛏  Single Room",
                "Rooms x1, x2, x3, x4 on each floor", singleField, "#3498db");
        VBox doubleCard = priceCard("🛏🛏  Double Room",
                "Rooms x5, x6, x7, x8 on each floor", doubleField, "#2ecc71");
        VBox suiteCard  = priceCard("👑  Suite",
                "Rooms x9, x10 on each floor", suiteField, "#9b59b6");

        Label statusLabel = statusLabel();
        Button saveBtn    = bigButton("💾  Save All Prices", BTN_GREEN);
        Button cancelBtn  = bigButton("Cancel", "#6e7681");

        HBox btnRow = new HBox(10, saveBtn, cancelBtn);
        HBox.setHgrow(saveBtn, Priority.ALWAYS);
        HBox.setHgrow(cancelBtn, Priority.ALWAYS);

        formCard.getChildren().addAll(
            sectionLabel("💲  Set Nightly Rates"),
            singleCard, doubleCard, suiteCard,
            btnRow, statusLabel
        );

        root.getChildren().addAll(banner, formCard);

        saveBtn.setOnAction(e -> {
            try {
                int sp = Integer.parseInt(singleField.getText().trim());
                int dp = Integer.parseInt(doubleField.getText().trim());
                int su = Integer.parseInt(suiteField.getText().trim());
                if (sp <= 0 || dp <= 0 || su <= 0) {
                    setStatus(statusLabel, "⚠  Prices must be greater than 0.", "#f0a500"); return;
                }
                DB.updatePrice("Single", sp);
                DB.updatePrice("Double", dp);
                DB.updatePrice("Suite",  su);
                setStatus(statusLabel, "✅  Prices updated successfully!", AVAILABLE);
                javafx.animation.PauseTransition p =
                    new javafx.animation.PauseTransition(javafx.util.Duration.seconds(1));
                p.setOnFinished(ev -> dialog.close());
                p.play();
            } catch (NumberFormatException ex) {
                setStatus(statusLabel, "⚠  Please enter valid numbers only.", BOOKED);
            }
        });
        cancelBtn.setOnAction(e -> dialog.close());

        dialog.setScene(new Scene(root, 520, 620));
        dialog.show();
    }

    private VBox priceCard(String title, String subtitle,
                            TextField field, String color) {
        Label t = new Label(title);
        t.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        t.setTextFill(Color.web(color));
        Label s = new Label(subtitle);
        s.setFont(Font.font("Segoe UI", 12));
        s.setTextFill(Color.web(TEXT_MUTED));
        Label rupee = new Label("₹");
        rupee.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        rupee.setTextFill(Color.web(color));
        HBox inputRow = new HBox(6, rupee, field);
        inputRow.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(field, Priority.ALWAYS);
        Label perNight = new Label("per night");
        perNight.setFont(Font.font("Segoe UI", 12));
        perNight.setTextFill(Color.web(TEXT_MUTED));
        VBox card = new VBox(6, t, s, inputRow, perNight);
        card.setPadding(new Insets(14));
        card.setStyle("-fx-background-color: " + BG_CARD2 + ";" +
                      "-fx-background-radius: 8;" +
                      "-fx-border-color: " + color + ";" +
                      "-fx-border-radius: 8; -fx-border-width: 1;");
        return card;
    }

    // ── Booking Form ──────────────────────────────────────────────────────────
    private void showBookingForm(Stage owner, String preselectedRoom, Runnable onSuccess) {
        Stage dialog = new Stage();
        dialog.initOwner(owner);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("New Booking — SwiftStay");

        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: " + BG_DARK + ";");

        HBox banner = gradientBanner("➕  New Booking",
                "Fill in the guest details below", BTN_GREEN, "#1abc9c");

        VBox formCard = new VBox(14);
        formCard.setPadding(new Insets(28));
        formCard.setStyle("-fx-background-color: " + BG_CARD + ";");

        TextField     nameField  = styledTextField("Guest full name");
        TextField     phoneField = styledTextField("10-digit phone number");
        TextField     emailField = styledTextField("Email address");

        int pSingle = DB.getPrice("Single");
        int pDouble = DB.getPrice("Double");
        int pSuite  = DB.getPrice("Suite");

        ComboBox<String> roomBox = new ComboBox<>();
        roomBox.getItems().addAll(
            "Single — ₹" + String.format("%,d", pSingle) + "/night",
            "Double — ₹" + String.format("%,d", pDouble) + "/night",
            "Suite  — ₹" + String.format("%,d", pSuite)  + "/night"
        );
        roomBox.setPromptText("Select room type");
        roomBox.setMaxWidth(Double.MAX_VALUE);
        roomBox.setStyle(comboStyle());

        // ── Lock room type if preselected ─────────────────────────────────────
        if (preselectedRoom != null) {
            int roomInFloor = Integer.parseInt(preselectedRoom) % 100;
            String autoType = getRoomType(roomInFloor);
            String autoValue = autoType.equals("Single")
                    ? "Single — ₹" + String.format("%,d", pSingle) + "/night"
                    : autoType.equals("Double")
                    ? "Double — ₹" + String.format("%,d", pDouble) + "/night"
                    : "Suite  — ₹" + String.format("%,d", pSuite)  + "/night";
            roomBox.setValue(autoValue);
            roomBox.setDisable(true);
            roomBox.setStyle(comboStyle() +
                    "-fx-opacity: 0.7;");
        }

        List<String> booked = DB.getBookedRooms();
        ComboBox<String> roomNumBox = new ComboBox<>();
        for (int floor = 1; floor <= FLOORS; floor++)
            for (int r = 1; r <= ROOMS_PER_FLOOR; r++) {
                String rn = String.valueOf(floor * 100 + r);
                if (!booked.contains(rn)) roomNumBox.getItems().add(rn);
            }

        // ── Lock room number if preselected ───────────────────────────────────
        if (preselectedRoom != null) {
            roomNumBox.setValue(preselectedRoom);
            roomNumBox.setDisable(true);
            roomNumBox.setStyle(comboStyle() +
                    "-fx-opacity: 0.7;");
        }

        roomNumBox.setPromptText("Select room number");
        roomNumBox.setMaxWidth(Double.MAX_VALUE);
        if (!roomNumBox.isDisabled()) roomNumBox.setStyle(comboStyle());

        DatePicker checkInPicker  = styledDatePicker("Check-in date");
        DatePicker checkOutPicker = styledDatePicker("Check-out date");

        Label nightsInfo   = new Label("");
        nightsInfo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        nightsInfo.setTextFill(Color.web("#58a6ff"));

        Label estimateInfo = new Label("");
        estimateInfo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        estimateInfo.setTextFill(Color.web(ACCENT_GOLD));

        Runnable updateEstimate = () -> {
            LocalDate in   = checkInPicker.getValue();
            LocalDate out  = checkOutPicker.getValue();
            String roomRaw = roomBox.getValue();
            if (in != null && out != null && out.isAfter(in)) {
                long nights = ChronoUnit.DAYS.between(in, out);
                nightsInfo.setText("📅  " + nights + " night" + (nights > 1 ? "s" : ""));
                if (roomRaw != null) {
                    int price = roomRaw.startsWith("Single") ? pSingle :
                                roomRaw.startsWith("Double") ? pDouble : pSuite;
                    estimateInfo.setText("💰  Estimated Total: ₹" +
                            String.format("%,d", price * (int) nights));
                }
            } else {
                nightsInfo.setText("");
                estimateInfo.setText("");
            }
        };

        checkInPicker.valueProperty().addListener((o, ov, nv)  -> updateEstimate.run());
        checkOutPicker.valueProperty().addListener((o, ov, nv) -> updateEstimate.run());
        roomBox.valueProperty().addListener((o, ov, nv)        -> updateEstimate.run());

        Label statusLabel = statusLabel();
        Button bookBtn    = bigButton("✔  Confirm Booking", BTN_GREEN);

        GridPane grid = new GridPane();
        grid.setVgap(12); grid.setHgap(16);
        ColumnConstraints c0 = new ColumnConstraints(130);
        ColumnConstraints c1 = new ColumnConstraints();
        c1.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(c0, c1);

        grid.add(formLabel("Guest Name"), 0, 0); grid.add(nameField,      1, 0);
        grid.add(formLabel("Phone"),      0, 1); grid.add(phoneField,     1, 1);
        grid.add(formLabel("Email"),      0, 2); grid.add(emailField,     1, 2);
        grid.add(formLabel("Room Type"),  0, 3); grid.add(roomBox,        1, 3);
        grid.add(formLabel("Room No."),   0, 4); grid.add(roomNumBox,     1, 4);
        grid.add(formLabel("Check-in"),   0, 5); grid.add(checkInPicker,  1, 5);
        grid.add(formLabel("Check-out"),  0, 6); grid.add(checkOutPicker, 1, 6);
        grid.add(formLabel("Duration"),   0, 7); grid.add(nightsInfo,     1, 7);
        grid.add(formLabel("Estimate"),   0, 8); grid.add(estimateInfo,   1, 8);

        // Show lock indicator if room is preselected
        if (preselectedRoom != null) {
            Label lockNote = new Label("🔒  Room number and type are locked from your selection");
            lockNote.setFont(Font.font("Segoe UI", 12));
            lockNote.setTextFill(Color.web("#58a6ff"));
            lockNote.setStyle("-fx-background-color: #0d2137;" +
                              "-fx-background-radius: 6; -fx-padding: 8 12;");
            lockNote.setMaxWidth(Double.MAX_VALUE);
            formCard.getChildren().addAll(sectionLabel("📋  Guest Information"),
                    lockNote, grid, bookBtn, statusLabel);
        } else {
            formCard.getChildren().addAll(sectionLabel("📋  Guest Information"),
                    grid, bookBtn, statusLabel);
        }

        ScrollPane sp = new ScrollPane(formCard);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background: " + BG_DARK +
                    "; -fx-background-color: " + BG_DARK + ";");
        VBox.setVgrow(sp, Priority.ALWAYS);

        root.getChildren().addAll(banner, sp);

        bookBtn.setOnAction(e -> {
            String name    = nameField.getText().trim();
            String phone   = phoneField.getText().trim();
            String email   = emailField.getText().trim();
            String roomRaw = roomBox.getValue();
            String roomNum = roomNumBox.getValue();
            LocalDate in   = checkInPicker.getValue();
            LocalDate out  = checkOutPicker.getValue();

            if (name.isEmpty())  { setStatus(statusLabel, "⚠  Enter guest name.", "#f0a500"); return; }
            if (phone.isEmpty()) { setStatus(statusLabel, "⚠  Enter phone number.", "#f0a500"); return; }
            if (!phone.matches("\\d{10}")) { setStatus(statusLabel, "⚠  Phone must be 10 digits.", BOOKED); return; }
            if (email.isEmpty()) { setStatus(statusLabel, "⚠  Enter email.", "#f0a500"); return; }
            if (!email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) { setStatus(statusLabel, "⚠  Invalid email.", BOOKED); return; }
            if (roomRaw == null) { setStatus(statusLabel, "⚠  Select room type.", "#f0a500"); return; }
            if (roomNum == null) { setStatus(statusLabel, "⚠  Select room number.", "#f0a500"); return; }
            if (in == null)      { setStatus(statusLabel, "⚠  Select check-in date.", "#f0a500"); return; }
            if (out == null)     { setStatus(statusLabel, "⚠  Select check-out date.", "#f0a500"); return; }
            if (!out.isAfter(in)){ setStatus(statusLabel, "⚠  Check-out must be after check-in.", BOOKED); return; }

            int days = (int) ChronoUnit.DAYS.between(in, out);
            String room;
            int price;
            if (roomRaw.startsWith("Single"))      { room = "Single"; price = pSingle; }
            else if (roomRaw.startsWith("Double")) { room = "Double"; price = pDouble; }
            else                                   { room = "Suite";  price = pSuite;  }

            int total = price * days;
            boolean saved = DB.insert(name, room, roomNum, days, total,
                    in.toString(), out.toString(), phone, email);

            if (!saved) { setStatus(statusLabel, "❌  Database error.", BOOKED); return; }

            showReceipt(dialog, name, room, roomNum, days, price, total,
                    in.toString(), out.toString(), phone, email);
            dialog.close();
            if (onSuccess != null) onSuccess.run();
        });

        dialog.setScene(new Scene(root, 580, 680));
        dialog.show();
    }

    // ── All Bookings ──────────────────────────────────────────────────────────
    private void showAllBookings(Stage owner) {
        Stage s = new Stage();
        s.initOwner(owner);
        s.setTitle("All Bookings — SwiftStay");

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + BG_DARK + ";");

        HBox banner = gradientBanner("📋  All Bookings",
                "View, search and manage guest bookings", BTN_BLUE, BTN_PURPLE);

        TableColumn<Booking, String>  nameCol    = tableCol("Guest Name", "name",       130);
        TableColumn<Booking, String>  phoneCol   = tableCol("Phone",      "phone",      100);
        TableColumn<Booking, String>  emailCol   = tableCol("Email",      "email",      160);
        TableColumn<Booking, String>  roomNumCol = tableCol("Room No.",   "roomNumber", 75);
        TableColumn<Booking, String>  roomCol    = tableCol("Type",       "room",       70);
        TableColumn<Booking, String>  inCol      = tableCol("Check-in",   "checkIn",    95);
        TableColumn<Booking, String>  outCol     = tableCol("Check-out",  "checkOut",   95);
        TableColumn<Booking, Integer> daysCol    = tableCol("Nights",     "days",       60);
        TableColumn<Booking, Integer> totalCol   = tableCol("Total (₹)",  "total",      90);

        TableView<Booking> table = new TableView<>();
        table.getColumns().addAll(nameCol, phoneCol, emailCol,
                roomNumCol, roomCol, inCol, outCol, daysCol, totalCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setStyle("-fx-background-color: " + BG_CARD +
                       "; -fx-text-fill: " + TEXT_LIGHT + ";");
        table.setPlaceholder(new Label("No bookings yet."));
        table.setItems(FXCollections.observableArrayList(DB.getAllBookings()));

        TextField search = styledTextField("🔍  Search by guest name...");
        search.textProperty().addListener((obs, o, n) ->
            table.setItems(FXCollections.observableArrayList(
                n.trim().isEmpty() ? DB.getAllBookings() : DB.searchBookings(n.trim())))
        );

        int rev = DB.getTotalRevenue();
        Label summary = new Label(String.format(
            "  📦 Bookings: %d     🏠 Occupied: %d / %d     💰 Revenue: ₹%,d  ",
            DB.getTotalBookings(), DB.getBookedRooms().size(), TOTAL_ROOMS, rev));
        summary.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        summary.setTextFill(Color.web(TEXT_LIGHT));
        summary.setStyle("-fx-background-color: " + BG_CARD2 + "; -fx-padding: 12 20;");
        summary.setMaxWidth(Double.MAX_VALUE);

        Button deleteBtn  = navButton("🗑  Delete Selected", BTN_RED);
        Button refreshBtn = navButton("🔄  Refresh", BTN_BLUE);

        deleteBtn.setOnAction(e -> {
            Booking sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) return;
            DB.deleteByRoom(sel.getRoomNumber());
            table.setItems(FXCollections.observableArrayList(DB.getAllBookings()));
        });
        refreshBtn.setOnAction(e ->
            table.setItems(FXCollections.observableArrayList(DB.getAllBookings())));

        HBox toolbar = new HBox(10, search, refreshBtn, deleteBtn);
        toolbar.setPadding(new Insets(12, 16, 12, 16));
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setStyle("-fx-background-color: " + BG_CARD2 + ";");
        HBox.setHgrow(search, Priority.ALWAYS);

        VBox center = new VBox(toolbar, table);
        VBox.setVgrow(table, Priority.ALWAYS);

        root.setTop(new VBox(banner, summary));
        root.setCenter(center);

        s.setScene(new Scene(root, 900, 550));
        s.show();
    }

    // ── Receipt ───────────────────────────────────────────────────────────────
    private void showReceipt(Stage owner, String name, String room, String roomNumber,
                              int days, int price, int total, String in, String out,
                              String phone, String email) {
        Stage d = new Stage();
        d.initOwner(owner);
        d.initModality(Modality.APPLICATION_MODAL);
        d.setTitle("Receipt — SwiftStay");

        String text =
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
            "        🏨  SWIFTSTAY RECEIPT        \n" +
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
            String.format("  Guest     : %s%n", name)       +
            String.format("  Phone     : %s%n", phone)      +
            String.format("  Email     : %s%n", email)      +
            String.format("  Room No.  : %s%n", roomNumber) +
            String.format("  Room Type : %s%n", room)       +
            String.format("  Check-in  : %s%n", in)         +
            String.format("  Check-out : %s%n", out)        +
            String.format("  Nights    : %d%n", days)       +
            String.format("  Rate      : ₹%,d/night%n", price) +
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
            String.format("  TOTAL     : ₹%,d%n", total)    +
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
            "    Thank you for choosing us! 🙏  ";

        TextArea ta = new TextArea(text);
        ta.setEditable(false);
        ta.setFont(Font.font("Courier New", 13));
        ta.setStyle("-fx-control-inner-background: " + BG_DARK +
                    "; -fx-text-fill: #58a6ff;");
        ta.setPrefSize(400, 330);

        Button pdfBtn   = bigButton("📄  Download PDF", BTN_BLUE);
        Button closeBtn = bigButton("✖  Close", "#6e7681");

        pdfBtn.setOnAction(e -> {
            generatePDF(name, room, roomNumber, days, price, total, in, out, phone, email);
            new Alert(Alert.AlertType.INFORMATION,
                    "Saved to Desktop!", ButtonType.OK).showAndWait();
        });
        closeBtn.setOnAction(e -> d.close());

        HBox btns = new HBox(10, pdfBtn, closeBtn);
        btns.setAlignment(Pos.CENTER);

        VBox box = new VBox(14, ta, btns);
        box.setPadding(new Insets(24));
        box.setStyle("-fx-background-color: " + BG_CARD + ";");

        d.setScene(new Scene(box));
        d.showAndWait();
    }

    // ── PDF ───────────────────────────────────────────────────────────────────
    private void generatePDF(String name, String room, String roomNumber,
                              int days, int price, int total,
                              String in, String out, String phone, String email) {
        String path = System.getProperty("user.home") +
                      "/Desktop/Receipt_" + name.replaceAll("\\s+", "_") + ".pdf";
        try {
            Document doc = new Document();
            PdfWriter.getInstance(doc, new FileOutputStream(path));
            doc.open();
            com.itextpdf.text.Font tf =
                new com.itextpdf.text.Font(
                    com.itextpdf.text.Font.FontFamily.HELVETICA, 18,
                    com.itextpdf.text.Font.BOLD);
            com.itextpdf.text.Font bf =
                new com.itextpdf.text.Font(
                    com.itextpdf.text.Font.FontFamily.HELVETICA, 12);
            Paragraph t = new Paragraph("SWIFTSTAY — BOOKING RECEIPT", tf);
            t.setAlignment(Element.ALIGN_CENTER);
            doc.add(t); doc.add(new Paragraph(" "));
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(80);
            table.setHorizontalAlignment(Element.ALIGN_CENTER);
            String[][] rows = {
                {"Guest Name", name},  {"Phone", phone},
                {"Email", email},      {"Room No.", roomNumber},
                {"Room Type", room},   {"Check-in", in},
                {"Check-out", out},    {"Nights", String.valueOf(days)},
                {"Rate/Night", "Rs. " + price}, {"TOTAL", "Rs. " + total}
            };
            for (String[] row : rows) {
                table.addCell(new PdfPCell(new Phrase(row[0], bf)));
                table.addCell(new PdfPCell(new Phrase(row[1], bf)));
            }
            doc.add(table); doc.add(new Paragraph(" "));
            Paragraph ty = new Paragraph("Thank you for choosing SwiftStay!", bf);
            ty.setAlignment(Element.ALIGN_CENTER);
            doc.add(ty); doc.close();
        } catch (Exception e) { System.err.println("PDF error: " + e.getMessage()); }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private static String getRoomType(int r) {
        if (r <= 4) return "Single";
        if (r <= 8) return "Double";
        return "Suite";
    }

    private static String getTypeEmoji(String type) {
        return switch (type) {
            case "Single" -> "🛏";
            case "Double" -> "🛏🛏";
            default       -> "👑";
        };
    }

    private static HBox gradientBanner(String title, String subtitle,
                                        String c1, String c2) {
        Label t = new Label(title);
        t.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        t.setTextFill(Color.WHITE);
        Label s = new Label(subtitle);
        s.setFont(Font.font("Segoe UI", 13));
        s.setTextFill(Color.web("rgba(255,255,255,0.75)"));
        VBox v = new VBox(4, t, s);
        v.setAlignment(Pos.CENTER_LEFT);
        HBox box = new HBox(v);
        box.setPadding(new Insets(22, 28, 22, 28));
        box.setStyle("-fx-background-color: linear-gradient(to right, " + c1 + ", " + c2 + ");");
        return box;
    }

    private static VBox statCard(String label, String value, String color) {
        Label val = new Label(value);
        val.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        val.setTextFill(Color.web(color));
        Label lbl = new Label(label);
        lbl.setFont(Font.font("Segoe UI", 12));
        lbl.setTextFill(Color.web(TEXT_MUTED));
        VBox card = new VBox(4, val, lbl);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(14, 18, 14, 18));
        card.setStyle("-fx-background-color: " + BG_CARD + ";" +
                      "-fx-background-radius: 10;" +
                      "-fx-border-color: " + BORDER + ";" +
                      "-fx-border-radius: 10; -fx-border-width: 1;");
        HBox.setHgrow(card, Priority.ALWAYS);
        return card;
    }

    private static HBox legendItem(String text, String color) {
        Label l = new Label(text);
        l.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        l.setTextFill(Color.web(color));
        return new HBox(l);
    }

    private static VBox fieldGroup(String labelText, javafx.scene.Node field) {
        Label lbl = new Label(labelText);
        lbl.setFont(Font.font("Segoe UI", 12));
        lbl.setTextFill(Color.web(TEXT_MUTED));
        VBox g = new VBox(4, lbl, field);
        if (field instanceof TextField f)     f.setMaxWidth(Double.MAX_VALUE);
        if (field instanceof PasswordField f) f.setMaxWidth(Double.MAX_VALUE);
        return g;
    }

    private static Label sectionLabel(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        l.setTextFill(Color.web(TEXT_LIGHT));
        return l;
    }

    private static Label statusLabel() {
        Label l = new Label("");
        l.setFont(Font.font("Segoe UI", 12));
        l.setWrapText(true);
        return l;
    }

    private static <T> TableColumn<Booking, T> tableCol(String title,
                                                          String prop, double width) {
        TableColumn<Booking, T> col = new TableColumn<>(title);
        col.setCellValueFactory(new PropertyValueFactory<>(prop));
        col.setPrefWidth(width);
        return col;
    }

    private static TextField styledTextField(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setStyle("-fx-background-color: " + BG_CARD2 +
                    "; -fx-text-fill: " + TEXT_LIGHT + ";" +
                    "-fx-prompt-text-fill: " + TEXT_MUTED +
                    "; -fx-background-radius: 6;" +
                    "-fx-border-color: " + BORDER +
                    "; -fx-border-radius: 6; -fx-border-width: 1;" +
                    "-fx-padding: 9 12; -fx-font-size: 13px;");
        return tf;
    }

    private static PasswordField styledPasswordField(String prompt) {
        PasswordField pf = new PasswordField();
        pf.setPromptText(prompt);
        pf.setStyle("-fx-background-color: " + BG_CARD2 +
                    "; -fx-text-fill: " + TEXT_LIGHT + ";" +
                    "-fx-prompt-text-fill: " + TEXT_MUTED +
                    "; -fx-background-radius: 6;" +
                    "-fx-border-color: " + BORDER +
                    "; -fx-border-radius: 6; -fx-border-width: 1;" +
                    "-fx-padding: 9 12; -fx-font-size: 13px;");
        return pf;
    }

    private static DatePicker styledDatePicker(String prompt) {
        DatePicker dp = new DatePicker();
        dp.setPromptText(prompt);
        dp.setMaxWidth(Double.MAX_VALUE);
        dp.setStyle("-fx-background-color: " + BG_CARD2 +
                    "; -fx-text-fill: " + TEXT_LIGHT + ";" +
                    "-fx-background-radius: 6; -fx-border-color: " + BORDER + ";" +
                    "-fx-border-radius: 6; -fx-border-width: 1; -fx-font-size: 13px;");
        return dp;
    }

    private static Button bigButton(String text, String color) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setStyle("-fx-background-color: " + color +
                     "; -fx-text-fill: white; -fx-font-size: 14px;" +
                     "-fx-font-weight: bold; -fx-background-radius: 8;" +
                     "-fx-padding: 11 20; -fx-cursor: hand;");
        return btn;
    }

    private static Button navButton(String text, String color) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: " + color +
                     "; -fx-text-fill: white; -fx-font-size: 13px;" +
                     "-fx-font-weight: bold; -fx-background-radius: 7;" +
                     "-fx-padding: 8 16; -fx-cursor: hand;");
        return btn;
    }

    private static Label formLabel(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        l.setTextFill(Color.web(TEXT_MUTED));
        return l;
    }

    private static String comboStyle() {
        return "-fx-background-color: " + BG_CARD2 +
               "; -fx-text-fill: " + TEXT_LIGHT + ";" +
               "-fx-background-radius: 6; -fx-border-color: " + BORDER + ";" +
               "-fx-border-radius: 6; -fx-border-width: 1;" +
               "-fx-font-size: 13px; -fx-padding: 4 8;";
    }

    private static String cardStyle(String bg) {
        return "-fx-background-color: " + bg + ";" +
               "-fx-background-radius: 12;" +
               "-fx-border-color: " + BORDER +
               "; -fx-border-radius: 12; -fx-border-width: 1;" +
               "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 16, 0.2, 0, 4);";
    }

    private static void setStatus(Label label, String message, String color) {
        label.setText(message);
        if (!color.isEmpty()) label.setTextFill(Color.web(color));
    }

    public static void main(String[] args) { launch(); }
}