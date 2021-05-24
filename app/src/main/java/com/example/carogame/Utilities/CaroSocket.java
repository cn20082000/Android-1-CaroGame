package com.example.carogame.Utilities;


import android.util.Log;

import java.net.URI;
import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;

public class CaroSocket {

    public static Socket socket;

    public static void init() {
        URI uri = URI.create(Constants.SERVER_URL);
        socket = IO.socket(uri);
    }

    public static void disconnect() {
        socket.disconnect();
    }
}
