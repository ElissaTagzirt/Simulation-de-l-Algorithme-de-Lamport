package LMP;

import java.util.*;

class Watchdog extends Thread {
    private final List<Processus> processes;

    public Watchdog(List<Processus> processes) {
        this.processes = processes;
    }

    @Override
    public void run() {
        while (true) {
            for (Processus process : processes) {
                if (process.isInCriticalSection()) {
                    continue;
                }
                try {
                    process.join(10000); // Wait for the process to complete
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
