package org.pat.causeconnect.service;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

@Service
public class InternetCheckService {
    private static final String TEST_ADDRESS = "8.8.8.8";
    private static final int TEST_PORT = 53;
    private static final int TIMEOUT_MS = 1500;

    public boolean hasInternetConnection() {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(TEST_ADDRESS, TEST_PORT), TIMEOUT_MS);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
