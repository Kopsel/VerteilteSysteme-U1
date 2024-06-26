package task2a;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalDateTime;

public class Player {
    private static String SERVER_ADDRESS = "localhost";
    private static int SERVER_PORT = 12345;
    private static String NAME = "Player";
    // Latency in seconds
    private static int SPIELER_LATENZ = 5;

    public static void main(String[] args) {
        // Parse params for CLI
        if (args.length != 0) {
            SERVER_ADDRESS = args[0];
            SERVER_PORT = Integer.parseInt(args[1]);
            NAME = args[2];
            SPIELER_LATENZ = Integer.parseInt(args[3]);
        }

        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT)) {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            while (true){
                // Wait for the start message from the server
                String message;
                do {
                    message = in.readLine();
                    System.out.println("Received message " + message + " at time " + LocalDateTime.now());
                } while (!message.equals("START"));

                // Simulate Player latency
                int latency = (int) (Math.random() * SPIELER_LATENZ) + 1;
                Thread.sleep(SPIELER_LATENZ * 1000L);

                // Roll the dice
                int WURF = (int) (Math.random() * 100) + 1;

                // Send submission to the server (no timestamp)
                out.println(NAME + ";" + WURF + ";" + latency);
                System.out.println("Player " + NAME + " rolled " + WURF + " with latency "
                        + latency + "s at time " + LocalDateTime.now());
            }

            } catch (IOException | InterruptedException ex) {
            throw new RuntimeException(ex);
        }
    }
}
