package TCP;

import java.io.*;
import java.net.*;

public class TestClient2 {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    public TestClient2(String address, int port) throws IOException {
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
//    	TestClient1 client = new TestClient1("localhost", 9999);
//        client.start();
    	
    	if (args.length != 2) {
            System.err.println("Usage: java TestClient1 <IP Address> <Port>");
            System.exit(1);
        }

        String ipAddress = args[0];
        int port = Integer.parseInt(args[1]);

        
//    	System.out.println("ipAddress >> " + args[0]);
//    	System.out.println("Port >> "+ args[1]);
    	
        TestClient2 client = new TestClient2(ipAddress, port);
        client.start();
    }
}

