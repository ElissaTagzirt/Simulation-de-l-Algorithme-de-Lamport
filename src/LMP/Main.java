package LMP;

import java.io.IOException;
import java.net.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        int numberOfProcesses = 10;
        List<Processus> processes = new ArrayList<>();

        for (long i = 1; i <= numberOfProcesses; i++) {
            ServerSocket serverSocket = new ServerSocket((int) (5000 + i));
            List<Long> otherProcesses = new ArrayList<>();
            for (long j = 1; j <= numberOfProcesses; j++) {
                if (j != i) {
                    otherProcesses.add(j);
                }
            }
            Processus process = new Processus(i, otherProcesses, serverSocket);
            processes.add(process);
            process.start();
        }

        Watchdog watchdog = new Watchdog(processes);
        watchdog.start();
    }
}

