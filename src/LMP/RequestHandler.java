package LMP;

import java.io.*;
import java.net.*;

class RequestHandler implements Runnable {
    private final Socket socket;
    private final Processus processus;

    public RequestHandler(Socket socket, Processus processus) {
        this.socket = socket;
        this.processus = processus;
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            String message;
            while ((message = in.readLine()) != null) {
                processus.handleRequest(message);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
