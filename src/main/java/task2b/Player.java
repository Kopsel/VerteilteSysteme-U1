package task2b;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Note: The logical clocks are only incremented
 * whenever messages are sent or received. This is
 * why according to the log messages a new round
 * starts supposedly at the same time that the last
 * one ended.
 */

public class Player {
    private static String SERVER_ADDRESS = "localhost";
    private static int SERVER_PORT = 12345;
    private static String NAME = "Player";
    // Latency in seconds
    private static int SPIELER_LATENZ = 5;
    private static final LamportClock clock = new LamportClock();

    public static void main(String[] args) {
        // Parse CLI args
        if (args.length != 0) {
            SERVER_ADDRESS = args[0];
            SERVER_PORT = Integer.parseInt(args[1]);
            NAME = args[2];
            SPIELER_LATENZ = Integer.parseInt(args[3]);
        }

        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT)) {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            while (true) {
                // Wait for the start message from the server
                String[] message;
                do {
                    message = in.readLine().split(";");
                    //Also triggers when "STOP" is sent
                    clock.receiveEvent(Integer.parseInt(message[1]));
                    System.out.println("Received message " + message[0] + " at time " + clock.getTime());
                } while (!message[0].equals("START"));

                // Simulate Player latency
                int latency = (int) (Math.random() * SPIELER_LATENZ) + 1;
                Thread.sleep(SPIELER_LATENZ * 1000L);

                // Roll the dice
                int WURF = (int) (Math.random() * 100) + 1;

                // Send submission to the server (includes logical timestamp)
                out.println(NAME + ";" + WURF + ";" + latency + ";" + clock.sendEvent());
                System.out.println("Player " + NAME + " rolled " + WURF + " with latency "
                        + latency + "s at time " + clock.getTime());
            }

            } catch (IOException | InterruptedException ex) {
                throw new RuntimeException(ex);
        }
    }
}
