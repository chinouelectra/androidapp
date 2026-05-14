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
import android.widget.EditText;


public class MainActivity extends AppCompatActivity {
    private TextView resultText;
    private Button listGamesButton;
    private Button searchGamesButton;
    private Button addBalanceButton;
    private EditText balanceInput;
    private Button playButton;
    private Button playerStatsButton;

    private EditText playerIdInput;
    private EditText providerInput;
    private EditText riskInput;
    private EditText categoryInput;
    private EditText minStarsInput;



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
        listGamesButton = findViewById(R.id.listGamesButton);
        searchGamesButton = findViewById(R.id.searchGamesButton);
        addBalanceButton = findViewById(R.id.addBalanceButton);
        playButton = findViewById(R.id.playButton);
        playerStatsButton = findViewById(R.id.playerStatsButton);

        listGamesButton.setOnClickListener(v -> listGames());
        searchGamesButton.setOnClickListener(v -> searchGames());
        addBalanceButton.setOnClickListener(v -> addBalance());
        playButton.setOnClickListener(v -> play());
        playerStatsButton.setOnClickListener(v -> playerStats());

        playerIdInput = findViewById(R.id.playerIdInput);
        providerInput = findViewById(R.id.providerInput);
        riskInput = findViewById(R.id.riskInput);
        categoryInput = findViewById(R.id.categoryInput);
        minStarsInput = findViewById(R.id.minStarsInput);

        balanceInput = findViewById(R.id.balanceInput);


        searchGamesButton.setOnClickListener(v -> searchGames());

        balanceInput = findViewById(R.id.balanceInput);

        addBalanceButton = findViewById(R.id.addBalanceButton);
        addBalanceButton.setOnClickListener(v -> addBalance());


    }
    private void listGames() {
        resultText.setText("Loading games...");

        new Thread(() -> {
            TcpClient client = new TcpClient("10.0.2.2", 5001);

            Response res = client.sendRequest(Request.getAllGames());

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
                            .append(" | provider=")
                            .append(game.getProviderName())
                            .append(" | stars=")
                            .append(game.getStars())
                            .append(" | risk=")
                            .append(game.getRiskLevel())
                            .append(" | range=[")
                            .append(game.getMinBet())
                            .append("..")
                            .append(game.getMaxBet())
                            .append("]\n");
                }

                resultText.setText(builder.length() == 0 ? "No games found" : builder.toString());
            });
        }).start();
    }



    private void searchGames() {

        String playerId = playerIdInput.getText().toString().trim();
        String provider = providerInput.getText().toString().trim();
        String risk = riskInput.getText().toString().trim();
        String category = categoryInput.getText().toString().trim();
        String minStarsText = minStarsInput.getText().toString().trim();

        Integer minStars = null;

        if (!minStarsText.isEmpty()) {
            minStars = Integer.parseInt(minStarsText);
        }

        resultText.setText("Searching games...");

        Integer finalMinStars = minStars;

        new Thread(() -> {

            TcpClient client = new TcpClient("10.0.2.2", 5001);

            Request req = Request.searchGames(
                    playerId.isEmpty() ? null : playerId,
                    provider.isEmpty() ? null : provider,
                    risk.isEmpty() ? null : risk,
                    category.isEmpty() ? null : category,
                    finalMinStars
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
                            .append(" | provider=")
                            .append(game.getProviderName())
                            .append(" | stars=")
                            .append(game.getStars())
                            .append(" | risk=")
                            .append(game.getRiskLevel())
                            .append(" | bet=")
                            .append(game.getBetCategory())
                            .append(" | range=[")
                            .append(game.getMinBet())
                            .append("..")
                            .append(game.getMaxBet())
                            .append("]\n\n");
                }

                resultText.setText(
                        builder.length() == 0
                                ? "No matching games found"
                                : builder.toString()
                );
            });

        }).start();
    }
    private void addBalance() {
        String playerId = playerIdInput.getText().toString().trim();
        String amountText = balanceInput.getText().toString().trim();

        if (playerId.isEmpty()) {
            resultText.setText("Please enter Player ID");
            return;
        }

        if (amountText.isEmpty()) {
            resultText.setText("Please enter amount");
            return;
        }

        double amount;

        try {
            amount = Double.parseDouble(amountText);
        } catch (NumberFormatException e) {
            resultText.setText("Invalid amount");
            return;
        }

        resultText.setText("Adding balance...");

        new Thread(() -> {
            TcpClient client = new TcpClient("10.0.2.2", 5001);

            Request req = Request.addBalance(playerId, amount);
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
    private void play() {
        resultText.setText("Play not implemented yet");
    }

    private void playerStats() {
        resultText.setText("Player stats not implemented yet");
    }}



