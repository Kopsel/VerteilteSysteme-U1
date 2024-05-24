import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Player {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;
    private static final String NAME = "Player1";
    // Latency in seconds
    private static final int SPIELER_LATENZ = 5;

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT)) {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            while (true){
                // Wait for the start message from the server
                String startMessage;
                do {
                    startMessage = in.readLine();
                } while (!startMessage.equals("START"));

                // Generate random latency
                int latency = (int) (Math.random() * SPIELER_LATENZ) + 1;
                System.out.println("Latenz " + NAME + ": " + latency);

                //Simulate latency
                Thread.sleep(SPIELER_LATENZ * 1000);

                // Start message received, proceed to roll the dice
                int WURF = (int) (Math.random() * 100) + 1; // Simulate rolling a dice
                // Also include timestamp of sending the message from the player
                out.println(NAME + ";" + WURF + ";" + latency + ";" + LocalDateTime.now());
                System.out.println("Player " + NAME + " rolled: " + WURF);
            }

            } catch (IOException | InterruptedException ex) {
            throw new RuntimeException(ex);
        }
    }
}
