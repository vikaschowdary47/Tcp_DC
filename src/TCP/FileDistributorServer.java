package TCP;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


public class FileDistributorServer {
//    private static final int PORT = 9999;
    private List<Socket> clientSockets = new ArrayList<>();
    private ServerSocket serverSocket;
    private int noOfClients = 5;

    public FileDistributorServer(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        System.out.println("Server started on port " + port);
    }

    public void acceptClients() throws IOException {
        while (clientSockets.size() < noOfClients) { // Accept exactly 5 clients
            Socket socket = serverSocket.accept();
            System.out.println("Client connected: " + socket);
            clientSockets.add(socket);
        }
    }
    
    

    public void distributeFileAndCollectCounts(String filePath) throws IOException, InterruptedException {
        // Split file into parts
        List<String> fileParts = splitFileIntoParts(filePath, noOfClients);

        // Create a thread pool with a fixed number of threads
        ExecutorService executor = Executors.newFixedThreadPool(noOfClients);

        AtomicInteger totalWordCount = new AtomicInteger(0); // Atomic variable to store the total word count received from all clients

        // Submit tasks to the thread pool
        for (int i = 0; i < clientSockets.size(); i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    PrintWriter out = new PrintWriter(clientSockets.get(index).getOutputStream(), true);
                    for (String line : fileParts.get(index).split("\n")) {
                        out.println(line); // Send each line of the file part to client
                    }
                    out.println("@@@END_OF_PART@@@"); // indicate end of file part

                    // Collect word count from client
                    BufferedReader in = new BufferedReader(new InputStreamReader(clientSockets.get(index).getInputStream()));
                    String countStr = in.readLine();
                    int count = Integer.parseInt(countStr);
                    totalWordCount.addAndGet(count); // Add word count to the total
                    System.out.println("Client's word count: " + count);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }

        // Shutdown the executor and wait for all tasks to complete
        executor.shutdown();
        executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);

        System.out.println("Total word count received from all clients: " + totalWordCount.get());
        
        // Other cleanup tasks...
    }


    private List<String> splitFileIntoParts(String filePath, int parts) throws IOException {
        File file = new File(filePath);
        BufferedReader reader = new BufferedReader(new FileReader(file));

        List<String> fileParts = new ArrayList<>();
        StringBuilder part = new StringBuilder();
        String line;
        int lineCount = 0;
        int totalLines = getTotalLines(filePath);
        int linesPerPart = (int) Math.ceil((double) totalLines / parts); // Calculate roughly how many lines each part should have
        System.out.println("linesPerPartt: " + linesPerPart);
        
        while ((line = reader.readLine()) != null) {
            part.append(line).append("\n");
            lineCount++;
            if (lineCount % linesPerPart == 0) { // Check if current line count equals lines per part
                fileParts.add(part.toString());
                part = new StringBuilder(); // Reset for next part
            }
        }
        if (!part.toString().isEmpty()) {
            fileParts.add(part.toString()); // Add any remaining content as a part
        }

        reader.close();
//        System.out.println("fileParts: " + fileParts);
        return fileParts;
    }

    private int getTotalLines(String filePath) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        int lines = 0;
        while (reader.readLine() != null) lines++;
        reader.close();
        return lines;
    }



    public static void main(String[] args) throws IOException, InterruptedException {
    	
    	if (args.length != 2) {
            System.err.println("Usage: java FileDistributorServer <Port> <File Path>");
            System.exit(1);
        }
    	
    	int port = Integer.parseInt(args[0]);
    	String filePath = args[1];
    	
        long start, end, executionTime;
        
        FileDistributorServer server = new FileDistributorServer(port);
        server.acceptClients(); // Wait for clients to connect before measuring time

        start = System.currentTimeMillis(); // Start time after clients have connected
        server.distributeFileAndCollectCounts(filePath);
        end = System.currentTimeMillis(); // End time after file distribution and word counting

        executionTime = end - start; // Calculate execution time
        System.out.println("File distribution and word counting execution time: " + executionTime + " milliseconds");
    }


}
