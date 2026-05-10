package com.example.android;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.example.android.network.TcpClient;
import common.Request;
import common.Response;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import common.GameInfo;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.json.JSONObject;
import android.widget.Spinner;
import android.widget.ArrayAdapter;


public class MainActivity extends AppCompatActivity {
    private TextView resultText;
    private Button testButton;
    private Button addGameButton;
    private Spinner gameFileSpinner;
    private Button searchGamesButton;
    private Button addBalanceButton;

    private String readAssetFile(String fileName) throws Exception {
        InputStream is = getAssets().open(fileName);
        int size = is.available();
        byte[] buffer = new byte[size];
        is.read(buffer);
        is.close();
        return new String(buffer, StandardCharsets.UTF_8);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        resultText = findViewById(R.id.resultText);
        testButton = findViewById(R.id.testButton);

        testButton.setOnClickListener(v -> testConnection());
        addGameButton = findViewById(R.id.addGameButton);

        addGameButton.setOnClickListener(v -> addGame());
        gameFileSpinner = findViewById(R.id.gameFileSpinner);

        String[] gameFiles = {"game1.json", "game2.json"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                gameFiles
        );

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        gameFileSpinner.setAdapter(adapter);
        searchGamesButton = findViewById(R.id.searchGamesButton);

        searchGamesButton.setOnClickListener(v -> searchGames());

        addBalanceButton = findViewById(R.id.addBalanceButton);
        addBalanceButton.setOnClickListener(v -> addBalance());

    }

    private void testConnection() {
        resultText.setText("Button clicked...");

        new Thread(() -> {
            TcpClient client = new TcpClient("10.0.2.2", 5001);

            Request req = Request.healthCheck();
            Response res = client.sendRequest(req);

            runOnUiThread(() -> {
                if (res != null) {
                    resultText.setText(res.getMessage());
                } else {
                    resultText.setText("No response");
                }
            });

        }).start();


    }
    private void addGame() {
        resultText.setText("Adding game from assets...");

        new Thread(() -> {
            try {
                String selectedFile = gameFileSpinner.getSelectedItem().toString();
                String jsonText = readAssetFile(selectedFile);

                JSONObject json = new JSONObject(jsonText);

                GameInfo gameInfo = new GameInfo(
                        json.getString("GameName"),
                        json.getString("ProviderName"),
                        json.getInt("Stars"),
                        json.getInt("NoOfVotes"),
                        json.getString("GameLogo"),
                        json.getDouble("MinBet"),
                        json.getDouble("MaxBet"),
                        json.getString("RiskLevel"),
                        json.getString("HashKey")
                );

                TcpClient client = new TcpClient("10.0.2.2", 5001);
                Request req = Request.addGame(gameInfo);
                Response res = client.sendRequest(req);

                runOnUiThread(() -> {
                    if (res != null) {
                        resultText.setText(res.getMessage());
                    } else {
                        resultText.setText("No response");
                    }
                });

            } catch (Exception e) {
                runOnUiThread(() -> resultText.setText("Error: " + e.getMessage()));
            }
        }).start();
    }
    private void searchGames() {

        resultText.setText("Searching games...");

        new Thread(() -> {

            TcpClient client = new TcpClient("10.0.2.2", 5001);

            Request req = Request.searchGames(
                    null,
                    null,
                    null,
                    null,
                    null
            );

            Response res = client.sendRequest(req);

            runOnUiThread(() -> {

                if (res == null) {
                    resultText.setText("No response");
                    return;
                }

                if (!res.isSuccess()) {
                    resultText.setText(res.getMessage());
                    return;
                }

                StringBuilder builder = new StringBuilder();

                for (GameInfo game : res.getGames()) {
                    builder.append(game.getGameName())
                            .append(" - ")
                            .append(game.getProviderName())
                            .append("\n");
                }

                resultText.setText(builder.toString());
            });

        }).start();
    }
    private void addBalance() {
        resultText.setText("Adding balance...");

        new Thread(() -> {
            TcpClient client = new TcpClient("10.0.2.2", 5001);

            Request req = Request.addBalance("player1", 100.0);
            Response res = client.sendRequest(req);

            runOnUiThread(() -> {
                if (res != null) {
                    resultText.setText(res.getMessage());
                } else {
                    resultText.setText("No response");
                }
            });

        }).start();
    }}


