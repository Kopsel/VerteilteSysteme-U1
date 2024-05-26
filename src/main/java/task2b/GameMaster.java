package task2b;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Note: The logical clocks are only incremented
 * whenever messages are sent or received. This is
 * why according to the log messages a new round
 * starts supposedly at the same time that the last
 * one ended.
 */

public class GameMaster {
    private static int PORT = 12345;
    private static int DAUER_DER_RUNDE = 10;
    private final List<Socket> playerSockets = new CopyOnWriteArrayList<>();
    private final List<GameRound> roundLog = new ArrayList<>();
    private List<PlayerSubmission> currentSubmissions;
    private static final String JSON_FILE_PATH = "game_rounds.json";
    private int roundID = 0;
    private final LamportClock clock = new LamportClock();

    public static void main(String[] args) {
        // Parse CLI args
        if (args.length != 0) {
            PORT = Integer.parseInt(args[0]);
            DAUER_DER_RUNDE = Integer.parseInt(args[1]);
        }

        GameMaster gameMaster = new GameMaster();
        gameMaster.startConnectionListener();

        // Add shutdown hook so log is persisted when user decides to terminate program
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
                gameMaster.writeRoundLog();
            }
        }));

        gameMaster.runGame();
    }

    /**
     * Starts a thread that continuously listens for new
     * clients connections
     */
    public void startConnectionListener() {
        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            System.out.println("GameMaster started. Listening on port " + PORT);
            Thread acceptThread = new Thread(new ConnectionListener(this, serverSocket));
            acceptThread.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Contains the main game loop
     */
    public void runGame() {
        while (true) {
            System.out.println("Round started at time " + clock.getTime());
            currentSubmissions = new ArrayList<>();
            int roundStart = clock.getTime();
            broadcast("START");
            try {
                Thread.sleep(DAUER_DER_RUNDE * 1000L);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            broadcast("STOP");
            int roundEnd = clock.getTime();
            System.out.println("Round ended at time " + clock.getTime());
            PlayerSubmission winner = currentSubmissions.stream()
                    .max(Comparator.comparing(PlayerSubmission::diceValue)).orElse(null);

            roundLog.add(new GameRound(++roundID, roundStart, roundEnd, currentSubmissions, winner));
        }
    }

    /**
     * Sends a message to all connected client sockets
     * @param message
     */
    public void broadcast(String message) {
        for (Socket socket : playerSockets) {
            try {
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                out.println(message + ";" + clock.sendEvent());

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void registerPlayer(Socket clientSocket) {
        playerSockets.add(clientSocket);
    }

    public void logPlayerSubmission(PlayerSubmission submission) {
        currentSubmissions.add(submission);
    }

    public void writeRoundLog() {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(JSON_FILE_PATH), java.nio.file.StandardOpenOption.CREATE)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(roundLog, writer);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public LamportClock getClock() {
        return clock;
    }

    /**
     * Whenever a new clients connects, the ConnectionListener creates
     * a new client thread that handles the communication with the client
     * @param gameMaster
     * @param serverSocket
     */
    private record ConnectionListener(GameMaster gameMaster, ServerSocket serverSocket) implements Runnable {

        @Override
            public void run() {
                while (true) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        System.out.println("Player connected: " + clientSocket.getInetAddress());
                        gameMaster.registerPlayer(clientSocket);
                        Thread clientThread = new Thread(new ClientHandler(gameMaster, clientSocket));
                        clientThread.start();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    /**
     * Handles the main logic of managing a Player connection. The ClientHandler listens
     * for incoming messages from the client and adds them to the submission lag.
     * @param gameMaster
     * @param clientSocket
     */
    private record ClientHandler(GameMaster gameMaster, Socket clientSocket) implements Runnable {

        @Override
            public void run() {
                try {
                    BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        String[] message = inputLine.split(";");
                        System.out.println(message[0] + " rolled " + message[1]);
                        gameMaster.getClock().receiveEvent(Integer.parseInt(message[3]));
                        gameMaster.logPlayerSubmission(new PlayerSubmission(message[0], message[1], message[2]
                                + "s", gameMaster.getClock().getTime()));
                    }
                    System.out.println("Player disconnected: " + clientSocket.getInetAddress());
                    clientSocket.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    // Data structure that represents a game round
    private record GameRound(
            int roundID,
            int roundStartTime,
            int roundEndTime,
            List<PlayerSubmission> playerSubmissions,
            PlayerSubmission winningSubmission) {}

    // Data structure that represents a submission by the player
    public record PlayerSubmission(String playerName,
                                   String diceValue,
                                   String latency,
                                   int receiveTimestamp) {}
}
