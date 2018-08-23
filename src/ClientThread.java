import java.io.*;
import java.net.Socket;
import java.util.Observable;

public class ClientThread extends Observable implements Runnable {
    /**
     * For reading input from socket
     */
    private BufferedReader br;

    /**
     * For writing output to socket.
     */
    private PrintWriter pw;

    /**
     * Socket object representing client connection
     */
    private Socket socket;
    private boolean running;
    private Server server;
    public ClientThread(Socket socket, Server server) throws IOException {
        this.socket = socket;
        running = false;
        this.server = server;
        //get I/O from socket
        try {
            br = new BufferedReader(
                    new InputStreamReader(
                            socket.getInputStream()
                    )
            );

            pw = new PrintWriter(socket.getOutputStream(), true);
            running = true; //set status
        } catch (IOException ioe) {
            throw ioe;
        }
    }

    public void stopClient() {
        try {
            this.socket.close();
        } catch (IOException ioe) {
        }
        ;
    }

    private final int READY = 0, FILENAME = 1, STREAMING = 2;
    private String fileName;
    private String data = "";
    private boolean streaming = false;
    private int portInUse;
    @Override
    public void run() {
        String msg = ""; //will hold message sent from client

        //sent out initial welcome message etc. if required...
        //pw.println("Welcome to Java based Server");

        //start listening message from client//

        try {
            while ((msg = br.readLine()) != null && running) {
                //provide your server's logic here//
                if(msg.equals("GETPORT"))
                    portInUse = server.getAvailablePort();
                pw.println(portInUse); //echo msg back to client//
            }
            running = false;
        } catch (IOException ioe) {
            running = false;
        }
        //it's time to close the socket
        try {
            this.socket.close();
            server.returnPort(portInUse);
            System.out.println("Closing connection");
        } catch (IOException ioe) {
        }

        //notify the observers for cleanup etc.
        this.setChanged();              //inherit from Observable
        this.notifyObservers(this);     //inherit from Observable
    }

    private void writeData(String fileName, String data) {
        try {
            FileWriter writer = new FileWriter(fileName);
            writer.append(data);
            writer.flush();
            writer.close();
            System.out.println("Data written to " + fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
