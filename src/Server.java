import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Observable;
import java.util.Observer;
import java.util.Stack;
import java.util.Vector;

public class Server implements Observer {
    public static void main(String[] args) {
        Server newServer = new Server(5555);
        newServer.startServer();
    }

    private Socket socket;
    private Stack<Integer> availablePorts = new Stack<>();
    /**
     * This vector holds all connected clients.
     * May be used for broadcasting, etc.
     */
    private Vector clients;
    private ServerSocket ssocket;  //Server Socket
    private StartServerThread sst; //inner class

    /**
     * Represents each currently connected client.
     *
     * @label initiates
     * @clientCardinality 1
     * @supplierCardinality 0..*
     */
    private ClientThread clientThread;

    private void populatePortStack() {
        availablePorts.push(5000);
        availablePorts.push(5001);
        availablePorts.push(5002);
        availablePorts.push(5003);
        availablePorts.push(5004);
        availablePorts.push(5005);
    }

    public void returnPort(int portNumber) {
        availablePorts.push(portNumber);
    }

    public int getAvailablePort() {
        return (availablePorts.pop());
    }

    /**
     * Port number of Server.
     */
    private int port;
    private boolean listening; //status for listening

    public Server(int port) {
        this.clients = new Vector();
        this.port = port; //default port
        this.listening = false;
        populatePortStack();
    }

    public void startServer() {
        if (!listening) {
            this.sst = new StartServerThread();
            this.sst.start();
            this.listening = true;
        }
    }

    public void stopServer() {
        if (this.listening) {
            this.sst.stopServerThread();
            //close all connected clients//

            java.util.Enumeration e = this.clients.elements();
            while (e.hasMoreElements()) {
                ClientThread ct = (ClientThread) e.nextElement();
                ct.stopClient();
            }
            this.listening = false;
        }
    }

    //observer interface//
    public void update(Observable observable, Object object) {
        //notified by observables, do cleanup here//
        this.clients.removeElement(observable);
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }


    /**
     * This inner class will keep listening to incoming connections,
     * and initiating a ClientThread object for each connection.
     */

    private class StartServerThread extends Thread {
        private boolean listen;

        public StartServerThread() {
            this.listen = false;
        }

        public void run() {
            System.out.println("Server Started");
            this.listen = true;
            try {

/**The following constructor provides a default number of
 * connections -- 50, according to Java's documentation.
 * An overloaded constructor is available for providing a
 * specific number, more or less, about connections. */

                Server.this.ssocket =
                        new ServerSocket(Server.this.port);


                while (this.listen) {
                    //wait for client to connect//

                    Server.this.socket = Server.this.ssocket.accept();
                    System.out.println("New Client Thread Started");
                    try {
                        Server.this.clientThread =
                                new ClientThread(Server.this.socket, Server.this);
                        Thread t =
                                new Thread(Server.this.clientThread);
                        Server.this.clientThread.addObserver(Server.this);
                        Server.this.clients.addElement(
                                Server.this.clientThread
                        );
                        t.start();
                    } catch (IOException ioe) {
                        //some error occurred in ClientThread //
                    }
                }
            } catch (IOException ioe) {
                //I/O error in ServerSocket//
                this.stopServerThread();
            }
        }

        public void stopServerThread() {
            try {
                Server.this.ssocket.close();
            } catch (IOException ioe) {
                //unable to close ServerSocket
            }

            this.listen = false;
        }
    }

}
