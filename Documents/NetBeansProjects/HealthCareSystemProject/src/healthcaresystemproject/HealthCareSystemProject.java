package healthcaresystemproject;

import java.time.LocalDate;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import javafx.scene.layout.*;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.Node;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class HealthCareSystemProject extends Application {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/pure health clinic";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";
    private static final String UPDATE_PASSWORD = "UPDATE USERS SET PASSWORD = ? WHERE USERNAME = ?";
    private Scene homeScene;
    private Scene loginScene;
    private Scene settingsScene;
    private Scene dashboardScene;
    private Scene notificationsScene;
    private Scene themeSelectionScene;
    private Scene changePasswordScene;
    private Stage primaryStage;
    private TextField userTextField;
    private PasswordField passField;
    private Label messageLabel;
    private String loggedInUsername;
    private Button dashboardButton;
    private Button settingsButton;
    private Button notificationsButton;
    private Button logoutButton;

    private ListView<String> notificationsList;

    private void deleteUserAccount() {
        String deleteQuery = "DELETE FROM users WHERE username = ?";

        try (Connection con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD); PreparedStatement stmt = con.prepareStatement(deleteQuery)) {

            stmt.setString(1, loggedInUsername);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                showAlert(AlertType.INFORMATION, "Success", "Account deleted successfully.");
                primaryStage.setScene(loginScene);
                primaryStage.setTitle("Login Page");
                userTextField.clear();
                passField.clear();
                messageLabel.setText("");
            } else {
                showAlert(AlertType.ERROR, "Error", "Account deletion failed. User not found.");
            }

        } catch (SQLException ex) {
            showAlert(AlertType.ERROR, "Error", "Sorry, you cannot delete your account after booking appointments.");
            ex.printStackTrace();
        }
    }

    private boolean registerUser(String username, String password) {
        String checkQuery = "SELECT * FROM users WHERE username = ?";
        String insertQuery = "INSERT INTO users (username, password) VALUES (?, ?)";

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {

            try (PreparedStatement checkStmt = connection.prepareStatement(checkQuery)) {
                checkStmt.setString(1, username);
                try (ResultSet resultSet = checkStmt.executeQuery()) {
                    if (resultSet.next()) {

                        showAlert(Alert.AlertType.ERROR, "Error", "Username already exists.");
                        return false;
                    }
                }
            }

            try (PreparedStatement insertStmt = connection.prepareStatement(insertQuery)) {
                insertStmt.setString(1, username);
                insertStmt.setString(2, password);
                int rowsAffected = insertStmt.executeUpdate();
                if (rowsAffected > 0) {

                    showAlert(Alert.AlertType.INFORMATION, "Success", "User registered successfully.");
                    return true;
                } else {

                    showAlert(Alert.AlertType.ERROR, "Error", "Registration failed. Please try again.");
                    return false;
                }
            }
        } catch (SQLException e) {

            showAlert(Alert.AlertType.ERROR, "Error", "An error occurred: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private boolean validateCredentials(String username, String password) {
        boolean isValid = false;
        String query = "SELECT * FROM users WHERE username = ? AND password = ?";

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD); PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, username);
            statement.setString(2, password);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    isValid = true;
                    loggedInUsername = username;
                }
            }

        } catch (SQLException e) {
            System.out.println(e);
        }

        return isValid;
    }

    private void changePassword(String newPassword) {
        if (newPassword.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please fill in all fields.");
            return;
        }

        try (Connection con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD); PreparedStatement st = con.prepareStatement(UPDATE_PASSWORD)) {

            st.setString(1, newPassword);
            st.setString(2, loggedInUsername);
            int rowsAffected = st.executeUpdate();

            if (rowsAffected > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Password changed successfully.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Password change failed. User not found.");
            }

        } catch (SQLException ex) {
            showAlert(Alert.AlertType.ERROR, "Error", "An error occurred: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void saveAppointment(String title, LocalDate date) {
        String query = "INSERT INTO appointments (usernae, appointment_title, appointment_date) VALUES (?, ?, ?)";

        try (Connection con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD); PreparedStatement stmt = con.prepareStatement(query)) {

            stmt.setString(1, loggedInUsername);
            stmt.setString(2, title);
            stmt.setDate(3, java.sql.Date.valueOf(date));

            stmt.executeUpdate();
            showAlert(AlertType.INFORMATION, "Success", "Appointment booked successfully.");

        } catch (SQLException ex) {
            showAlert(AlertType.ERROR, "Error", "An error occurred: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private List<String> getAppointments() {
        List<String> appointments = new ArrayList<>();
        String query = "SELECT appointment_title, appointment_date FROM appointments WHERE username = ?";

        try (Connection con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD); PreparedStatement stmt = con.prepareStatement(query)) {

            stmt.setString(1, loggedInUsername);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String title = rs.getString("appointment_title");
                    LocalDate date = rs.getDate("appointment_date").toLocalDate();
                    appointments.add(title + " on " + date);
                }
            }

        } catch (SQLException ex) {
            showAlert(AlertType.ERROR, "Error", "An error occurred: " + ex.getMessage());
            ex.printStackTrace();
        }

        return appointments;
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("Login Page");

        userTextField = new TextField();
        passField = new PasswordField();
        messageLabel = new Label();

        Label healthCenterName = new Label("Pure Health Clinic");
        healthCenterName.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        Image healthCenterLogo = new Image("https://cdn.iconscout.com/icon/premium/png-256-thumb/clinic-2054876-1730482.png");
        ImageView healthCenterLogoView = new ImageView(healthCenterLogo);
        healthCenterLogoView.setFitWidth(100);
        healthCenterLogoView.setFitHeight(100);

        GridPane loginGridPane = new GridPane();
        loginGridPane.setAlignment(Pos.CENTER);
        loginGridPane.setPadding(new Insets(10));
        loginGridPane.setHgap(10);
        loginGridPane.setVgap(10);

        Label userLabel = new Label("Username:");
        Label passLabel = new Label("Password:");
        Button loginButton = new Button("Login");
        Button registerButton = new Button("Register");

        userLabel.setStyle("-fx-font-weight: bold;");
        passLabel.setStyle("-fx-font-weight: bold;");
        userTextField.setStyle("-fx-pref-width: 200px; -fx-padding: 5px;");
        passField.setStyle("-fx-pref-width: 200px; -fx-padding: 5px;");
        loginButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 10px 20px; -fx-border-radius: 5px; -fx-background-radius: 5px; -fx-font-weight: bold;");
        registerButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 10px 20px; -fx-border-radius: 5px; -fx-background-radius: 5px; -fx-font-weight: bold;");
        messageLabel.setStyle("-fx-font-weight: bold;");

        loginButton.setOnMouseEntered(e -> loginButton.setStyle("-fx-background-color: #45a049; -fx-text-fill: white; -fx-padding: 10px 20px; -fx-border-radius: 5px; -fx-background-radius: 5px; -fx-font-weight: bold;"));
        loginButton.setOnMouseExited(e -> loginButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 10px 20px; -fx-border-radius: 5px; -fx-background-radius: 5px; -fx-font-weight: bold;"));
        registerButton.setOnMouseEntered(e -> registerButton.setStyle("-fx-background-color: #45a049; -fx-text-fill: white; -fx-padding: 10px 20px; -fx-border-radius: 5px; -fx-background-radius: 5px; -fx-font-weight: bold;"));
        registerButton.setOnMouseExited(e -> registerButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 10px 20px; -fx-border-radius: 5px; -fx-background-radius: 5px; -fx-font-weight: bold;"));
        loginButton.setOnAction(e -> {
            String username = userTextField.getText();
            String password = passField.getText();

            if (validateCredentials(username, password)) {

                loggedInUsername = username;

                Label welcomeLabel = new Label("Welcome, " + loggedInUsername + "!");
                welcomeLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

                VBox profileBox = new VBox(10);
                profileBox.setAlignment(Pos.CENTER);
                profileBox.getChildren().addAll(welcomeLabel);

                VBox navBox = new VBox(10);
                navBox.setAlignment(Pos.CENTER);
                navBox.getChildren().addAll(dashboardButton, settingsButton, notificationsButton, logoutButton);

                VBox mainBox = new VBox(20);
                mainBox.setAlignment(Pos.CENTER);
                mainBox.setPadding(new Insets(20));
                mainBox.getChildren().addAll(profileBox, navBox);

                homeScene = new Scene(createSceneWithScrollPane(mainBox), 400, 400);

                primaryStage.setScene(homeScene);
                primaryStage.setTitle("Home Page");
            } else {
                messageLabel.setText("Invalid username or password");
                messageLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
            }
        });
        registerButton.setOnAction(e -> {
            String username = userTextField.getText();
            String password = passField.getText();

            if (username.isEmpty() || password.isEmpty()) {
                messageLabel.setText("Invalid username or password");
                messageLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
            } else {
                registerUser(username, password);

            }
        });

        loginGridPane.add(userLabel, 0, 0);
        loginGridPane.add(userTextField, 1, 0);
        loginGridPane.add(passLabel, 0, 1);
        loginGridPane.add(passField, 1, 1);
        loginGridPane.add(loginButton, 1, 2);
        loginGridPane.add(registerButton, 1, 3);

        VBox loginVBox = new VBox(20);
        loginVBox.setAlignment(Pos.CENTER);
        loginVBox.setPadding(new Insets(20));
        loginVBox.getChildren().addAll(healthCenterLogoView, healthCenterName, loginGridPane, messageLabel);

        loginScene = new Scene(createSceneWithScrollPane(loginVBox), 300, 300);

        Label welcomeLabel = new Label("Welcome, " + loggedInUsername + "!");
        welcomeLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        dashboardButton = new Button("Dashboard");
        settingsButton = new Button("Settings");
        notificationsButton = new Button("Notifications");
        logoutButton = new Button("Logout");

        applyButtonStyles(dashboardButton, settingsButton, notificationsButton, logoutButton);

        dashboardButton.setOnAction(e -> openDashboardPage());
        settingsButton.setOnAction(e -> openSettingsPage());
        notificationsButton.setOnAction(e -> openNotificationsPage());
        logoutButton.setOnAction(e -> showLogoutConfirmation());

        VBox profileBox = new VBox(10);
        profileBox.setAlignment(Pos.CENTER);
        profileBox.getChildren().addAll(welcomeLabel);

        VBox navBox = new VBox(10);
        navBox.setAlignment(Pos.CENTER);
        navBox.getChildren().addAll(dashboardButton, settingsButton, notificationsButton, logoutButton);

        VBox mainBox = new VBox(20);
        mainBox.setAlignment(Pos.CENTER);
        mainBox.setPadding(new Insets(20));
        mainBox.getChildren().addAll(profileBox, navBox);

        homeScene = new Scene(createSceneWithScrollPane(mainBox), 400, 400);
        settingsScene = new Scene(createSceneWithScrollPane(createSettingsScene()), 400, 400);
        dashboardScene = new Scene(createSceneWithScrollPane(createDashboardScene()), 400, 400);
        notificationsScene = new Scene(createSceneWithScrollPane(createNotificationsScene()), 400, 400);
        themeSelectionScene = new Scene(createSceneWithScrollPane(createThemeSelectionScene()), 400, 400);
        changePasswordScene = new Scene(createSceneWithScrollPane(createChangePasswordScene()), 400, 400);

        primaryStage.setScene(loginScene);
        primaryStage.show();
    }

    private VBox createDashboardScene() {
        Label dashboardLabel = new Label("Dashboard");
        dashboardLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        GridPane appointmentBox = new GridPane();
        appointmentBox.setAlignment(Pos.CENTER);

        appointmentBox.add(createAppointmentComponent("Dentist Appointment", 30, "https://cdn-icons-png.flaticon.com/512/5498/5498914.png"), 0, 0);
        appointmentBox.add(createAppointmentComponent("Internist Appointment", 50, "https://cdn-icons-png.flaticon.com/512/5745/5745085.png"), 1, 0);
        appointmentBox.add(createAppointmentComponent("Radiologist Appointment", 20, "https://cdn-icons-png.freepik.com/512/6024/6024968.png"), 0, 1);
        appointmentBox.add(createAppointmentComponent("Plastic Surgery Appointment", 100, "https://cdn-icons-png.flaticon.com/512/4856/4856562.png"), 1, 1);
        Button backButton = new Button("Back");
        applyButtonStyles(backButton);
        backButton.setOnAction(e -> {
            primaryStage.setScene(homeScene);
            primaryStage.setTitle("Home Page");

        });

        VBox dashboardBox = new VBox(20);
        dashboardBox.setAlignment(Pos.CENTER);
        dashboardBox.setPadding(new Insets(20));
        dashboardBox.getChildren().addAll(dashboardLabel, appointmentBox, backButton);

        return dashboardBox;
    }

    private VBox createAppointmentComponent(String title, int price, String imageUrl) {
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Label priceLabel = new Label("Price: $" + price);

        ImageView imageView = new ImageView(new Image(imageUrl));
        imageView.setFitWidth(100);
        imageView.setFitHeight(100);

        DatePicker datePicker = new DatePicker();
        Button bookButton = new Button("Book Appointment");
        applyButtonStyles(bookButton);

        bookButton.setOnAction(e -> {
            if (datePicker.getValue() == null) {
                showAlert(AlertType.ERROR, "Error", "Please select a date.");
            } else {
                LocalDate selectedDate = datePicker.getValue();
                LocalDate today = LocalDate.now();

                if (selectedDate.isBefore(today)) {
                    showAlert(AlertType.ERROR, "Error", "Cannot book an appointment in the past.");
                } else if (selectedDate.isEqual(today)) {
                    showAlert(AlertType.ERROR, "Error", "Cannot book an appointment for today.");
                } else {
                    String date = selectedDate.toString();
                    saveAppointment(title, selectedDate);
                    updateListView(notificationsList);
                }
            }
        });

        VBox appointmentBox = new VBox(10);
        appointmentBox.setAlignment(Pos.CENTER);
        appointmentBox.getChildren().addAll(imageView, titleLabel, priceLabel, datePicker, bookButton);

        return appointmentBox;
    }

    private VBox createSettingsScene() {
        Button changeThemeButton = new Button("Change Theme");
        Button changePasswordButton = new Button("Change Password");
        Button deleteAccountButton = new Button("Delete Account");
        Button backButton = new Button("Back");

        applyButtonStyles(changeThemeButton, changePasswordButton, deleteAccountButton, backButton);

        changeThemeButton.setOnAction(e -> openThemeSelectionPage());
        changePasswordButton.setOnAction(e -> openChangePasswordPage());
        deleteAccountButton.setOnAction(e -> {
            Alert deleteAlert = new Alert(AlertType.CONFIRMATION, "Are you sure you want to delete your account?", ButtonType.YES, ButtonType.NO);
            deleteAlert.setTitle("Confirm Delete");
            deleteAlert.showAndWait();

            if (deleteAlert.getResult() == ButtonType.YES) {
                deleteUserAccount();
            }
        });
        backButton.setOnAction(e -> {
            primaryStage.setScene(homeScene);
            primaryStage.setTitle("Home Page");
        });

        VBox settingsBox = new VBox(20);
        settingsBox.setAlignment(Pos.CENTER);
        settingsBox.setPadding(new Insets(20));
        settingsBox.getChildren().addAll(changeThemeButton, changePasswordButton, deleteAccountButton, backButton);

        return settingsBox;
    }

    private VBox createNotificationsScene() {
        notificationsList = new ListView<>();
        updateListView(notificationsList);
        Button backButton = new Button("Back");
        applyButtonStyles(backButton);
        backButton.setOnAction(e -> {
            primaryStage.setScene(homeScene);
            primaryStage.setTitle("Home Page");
        });

        VBox notificationsBox = new VBox(20);
        notificationsBox.setAlignment(Pos.CENTER);
        notificationsBox.setPadding(new Insets(20));
        notificationsBox.getChildren().addAll(new Label("Notifications"), notificationsList, backButton);

        return notificationsBox;
    }

    private void applyWhiteTheme() {

        applyThemeToScene(homeScene);
        applyThemeToScene(loginScene);
        applyThemeToScene(settingsScene);
        applyThemeToScene(dashboardScene);
        applyThemeToScene(notificationsScene);
        applyThemeToScene(themeSelectionScene);
        applyThemeToScene(changePasswordScene);
    }

    private void applyThemeToScene(Scene scene) {

        if (scene.getRoot() instanceof ScrollPane) {
            ScrollPane scrollPane = (ScrollPane) scene.getRoot();
            scrollPane.setStyle("-fx-background-color: #FFFAF0; -fx-border-color: transparent;");

            Node content = scrollPane.getContent();
            if (content instanceof Region) {
                ((Region) content).setStyle("-fx-background-color: #FFFAF0;");
            }
        }
    }

    private void applyLightBlueTheme() {

        applyThemeToScene(homeScene, "#ADD8E6");
        applyThemeToScene(loginScene, "#ADD8E6");
        applyThemeToScene(settingsScene, "#ADD8E6");
        applyThemeToScene(dashboardScene, "#ADD8E6");
        applyThemeToScene(notificationsScene, "#ADD8E6");
        applyThemeToScene(themeSelectionScene, "#ADD8E6");
        applyThemeToScene(changePasswordScene, "#ADD8E6");
    }

    private void applyThemeToScene(Scene scene, String color) {

        if (scene.getRoot() instanceof ScrollPane) {
            ScrollPane scrollPane = (ScrollPane) scene.getRoot();
            scrollPane.setStyle("-fx-background-color: " + color + "; -fx-border-color: transparent;");

            Node content = scrollPane.getContent();
            if (content instanceof Region) {
                ((Region) content).setStyle("-fx-background-color: " + color + ";");
            }
        }
    }

    private void applyLightGreenTheme() {

        applyThemeToScene(homeScene, "#90EE90");
        applyThemeToScene(loginScene, "#90EE90");
        applyThemeToScene(settingsScene, "#90EE90");
        applyThemeToScene(dashboardScene, "#90EE90");
        applyThemeToScene(notificationsScene, "#90EE90");
        applyThemeToScene(themeSelectionScene, "#90EE90");
        applyThemeToScene(changePasswordScene, "#90EE90");
    }

    private VBox createThemeSelectionScene() {
        Label themeLabel = new Label("Select Theme");
        themeLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        ToggleGroup themeGroup = new ToggleGroup();

        RadioButton whiteTheme = new RadioButton("White");
        RadioButton blueTheme = new RadioButton("Blue");
        RadioButton greenTheme = new RadioButton("Green");

        whiteTheme.setToggleGroup(themeGroup);
        blueTheme.setToggleGroup(themeGroup);
        greenTheme.setToggleGroup(themeGroup);

        Button applyThemeButton = new Button("Apply");

        applyButtonStyles(applyThemeButton);

        applyThemeButton.setOnAction(e -> {
            if (whiteTheme.isSelected()) {
                applyWhiteTheme();
            } else if (blueTheme.isSelected()) {
                applyLightBlueTheme();
            } else if (greenTheme.isSelected()) {
                applyLightGreenTheme();
            }
        });

        Button backButton = new Button("Back");
        applyButtonStyles(backButton);
        backButton.setOnAction(e -> {
            primaryStage.setScene(settingsScene);
            primaryStage.setTitle("Settings");
        });

        VBox themeSelectionBox = new VBox(20);
        themeSelectionBox.setAlignment(Pos.CENTER);
        themeSelectionBox.setPadding(new Insets(20));
        themeSelectionBox.getChildren().addAll(themeLabel, whiteTheme, blueTheme, greenTheme, applyThemeButton, backButton);

        return themeSelectionBox;
    }

    private VBox createChangePasswordScene() {
        Label newPasswordLabel = new Label("New Password:");
        PasswordField newPasswordField = new PasswordField();
        Button changePasswordButton = new Button("Change Password");
        Button backButton = new Button("Back");

        applyButtonStyles(changePasswordButton, backButton);

        changePasswordButton.setOnAction(e -> {
            String newPassword = newPasswordField.getText();
            changePassword(newPassword);
        });

        backButton.setOnAction(e -> {
            newPasswordField.clear();
            primaryStage.setScene(settingsScene);
            primaryStage.setTitle("Settings");
        });

        VBox changePasswordBox = new VBox(10);
        changePasswordBox.setAlignment(Pos.CENTER);
        changePasswordBox.setPadding(new Insets(20));
        changePasswordBox.getChildren().addAll(
                newPasswordLabel, newPasswordField,
                changePasswordButton, backButton
        );

        return changePasswordBox;
    }

    private void showLogoutConfirmation() {
        Alert logoutAlert = new Alert(AlertType.CONFIRMATION, "Are you sure you want to logout?", ButtonType.YES, ButtonType.NO);
        logoutAlert.setTitle("Confirm Logout");
        logoutAlert.showAndWait();

        if (logoutAlert.getResult() == ButtonType.YES) {
            primaryStage.setScene(loginScene);
            primaryStage.setTitle("Login Page");
            userTextField.clear();
            passField.clear();
            messageLabel.setText("");

        }
    }

    private void openDashboardPage() {
        primaryStage.setScene(dashboardScene);
        primaryStage.setTitle("Dashboard");
    }

    private void openSettingsPage() {
        primaryStage.setScene(settingsScene);
        primaryStage.setTitle("Settings");
    }

    private void openNotificationsPage() {
        updateListView(notificationsList);
        primaryStage.setScene(notificationsScene);
        primaryStage.setTitle("Notifications");
    }

    private void openThemeSelectionPage() {
        primaryStage.setScene(themeSelectionScene);
        primaryStage.setTitle("Select Theme");
    }

    private void openChangePasswordPage() {
        primaryStage.setScene(changePasswordScene);
        primaryStage.setTitle("Change Password");
    }

    private void applyButtonStyles(Button... buttons) {
        for (Button button : buttons) {
            button.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 10px 20px; -fx-border-radius: 5px; -fx-background-radius: 5px; -fx-font-weight: bold;");
            button.setOnMouseEntered(e -> button.setStyle("-fx-background-color: #45a049; -fx-text-fill: white; -fx-padding: 10px 20px; -fx-border-radius: 5px; -fx-background-radius: 5px; -fx-font-weight: bold;"));
            button.setOnMouseExited(e -> button.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 10px 20px; -fx-border-radius: 5px; -fx-background-radius: 5px; -fx-font-weight: bold;"));
        }
    }

    private void showAlert(AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void updateListView(ListView<String> listView) {
        listView.getItems().clear();
        List<String> appointments = getAppointments();
        listView.getItems().addAll(appointments);
    }

    private ScrollPane createSceneWithScrollPane(VBox vbox) {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(vbox);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        return scrollPane;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
