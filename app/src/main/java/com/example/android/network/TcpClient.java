package com.example.android.network;

import android.util.Log;

import common.Request;
import common.Response;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public class TcpClient {

    private final String host;
    private final int port;

    public TcpClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public Response sendRequest(Request request) {

        Socket socket = null;
        ObjectOutputStream out = null;
        ObjectInputStream in = null;

        try {
            Log.d("TCP", "Connecting to " + host + ":" + port);

            socket = new Socket();
            socket.connect(new InetSocketAddress(host, port), 3000); // timeout 3s

            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();

            in = new ObjectInputStream(socket.getInputStream());

            Log.d("TCP", "Sending request...");
            out.writeObject(request);
            out.flush();

            Log.d("TCP", "Waiting for response...");
            Response response = (Response) in.readObject();

            Log.d("TCP", "Response received: " + response.getMessage());

            return response;

        } catch (Exception e) {
            Log.e("TCP", "Error: " + e.getMessage());
            e.printStackTrace();

            return new Response(false, "Connection error: " + e.getMessage());

        } finally {
            try {
                if (in != null) in.close();
            } catch (Exception ignored) {}

            try {
                if (out != null) out.close();
            } catch (Exception ignored) {}

            try {
                if (socket != null && !socket.isClosed()) socket.close();
            } catch (Exception ignored) {}
        }
    }
}