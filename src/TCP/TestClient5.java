package TCP;

import java.io.*;
import java.net.*;

public class TestClient5 {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    public TestClient5(String address, int port) throws IOException {
        socket = new Socket(address, port);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
    }

        public void start() throws IOException {
            StringBuilder filePartBuilder = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null && !line.equals("@@@END_OF_PART@@@")) {
                filePartBuilder.append(line).append("\n"); // Append each line received from the server
            }
            String filePart = filePartBuilder.toString();
            int wordCount = countWords(filePart);
           ;
            out.println(wordCount); // Send word count back to the server
            out.close(); // close output stream
            socket.close();
        }


    private int countWords(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        String[] words = text.split("\\s+"); // Split text into words
        return words.length;
    }

    public static void main(String[] args) throws IOException {
    	TestClient5 client = new TestClient5("localhost", 9999);
//    	  BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
//          PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
        client.start();
    }
}

