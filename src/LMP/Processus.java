package LMP;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

class Processus extends Thread {
    private final long id;
    private final List<Long> otherProcesses;
    private final Map<Long, LamportClock> timestamps;
    private final AtomicBoolean inCriticalSection;
    private final Random random;
    private final ServerSocket serverSocket;
    private final PriorityQueue<Request> requestQueue;
    private final Map<Long, Boolean> acksReceived;

    public Processus(long id, List<Long> otherProcesses, ServerSocket serverSocket) {
        this.id = id;
        this.otherProcesses = otherProcesses;
        this.timestamps = new ConcurrentHashMap<>();
        this.inCriticalSection = new AtomicBoolean(false);
        this.random = new Random();
        this.serverSocket = serverSocket;
        this.requestQueue = new PriorityQueue<>();
        this.acksReceived = new ConcurrentHashMap<>();

        for (long processId : otherProcesses) {
            timestamps.put(processId, new LamportClock());
            acksReceived.put(processId, false);
        }
        timestamps.put(id, new LamportClock());
    }

    @Override
    public void run() {
        new Thread(this::handleRequests).start();
        while (true) {
            try {
                System.out.println("Process " + id + " waiting to enter critical section.");
                Thread.sleep(random.nextInt(9000) + 1000);
                requestCriticalSection();
                waitForAcks();
                enterCriticalSection();
                System.out.println("Process " + id + " in critical section.");
                Thread.sleep(random.nextInt(1000) + 1000);
                System.out.println("Process " + id + " exiting critical section.");
                releaseCriticalSection();
                clearAcks();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleRequests() {
        try {
            while (true) {
                Socket socket = serverSocket.accept();
                new Thread(new RequestHandler(socket, this)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void requestCriticalSection() {
        LamportClock myClock = timestamps.get(id);
        long timestamp = myClock.increment();
        synchronized (requestQueue) {
            requestQueue.add(new Request(id, timestamp));
        }
        for (long processId : otherProcesses) {
            sendMessage(processId, "REQUEST " + id + " " + timestamp);
        }
    }

    private void waitForAcks() {
        while (!hasAllAcks()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void enterCriticalSection() {
        synchronized (requestQueue) {
            while (requestQueue.peek() != null && requestQueue.peek().processId != id) {
                try {
                    requestQueue.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            requestQueue.poll();
            inCriticalSection.set(true);
        }
    }

    private void releaseCriticalSection() {
        synchronized (requestQueue) {
            inCriticalSection.set(false);
            requestQueue.notifyAll();
        }
        LamportClock myClock = timestamps.get(id);
        for (long processId : otherProcesses) {
            sendMessage(processId, "RELEASE " + id + " " + myClock.increment());
        }
    }

    private void sendMessage(long processId, String message) {
        try (Socket socket = new Socket("localhost", (int)(5000 + processId));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
            out.println(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handleRequest(String message) {
        String[] parts = message.split(" ");
        String type = parts[0];
        long senderId = Long.parseLong(parts[1]);
        long timestamp = Long.parseLong(parts[2]);

        LamportClock senderClock = timestamps.get(senderId);
        senderClock.update(timestamp);

        synchronized (requestQueue) {
            if (type.equals("REQUEST")) {
                requestQueue.add(new Request(senderId, timestamp));
                requestQueue.notifyAll();
                sendMessage(senderId, "ACK " + id + " " + timestamps.get(id).increment());
            } else if (type.equals("RELEASE")) {
                requestQueue.removeIf(r -> r.processId == senderId);
                requestQueue.notifyAll();
            } else if (type.equals("ACK")) {
                acksReceived.put(senderId, true);
            }
        }
    }

    private boolean hasAllAcks() {
        return acksReceived.values().stream().allMatch(Boolean::booleanValue);
    }

    private void clearAcks() {
        acksReceived.replaceAll((k, v) -> false);
    }

    public boolean isInCriticalSection() {
        return inCriticalSection.get();
    }

    public long getId() {
        return id;
    }
}

class Request implements Comparable<Request> {
    long processId;
    long timestamp;

    public Request(long processId, long timestamp) {
        this.processId = processId;
        this.timestamp = timestamp;
    }

    @Override
    public int compareTo(Request other) {
        if (this.timestamp == other.timestamp) {
            return Long.compare(this.processId, other.processId);
        }
        return Long.compare(this.timestamp, other.timestamp);
    }
}
