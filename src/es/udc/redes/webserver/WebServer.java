package es.udc.redes.webserver;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Properties;

/**
 * This class manages the server and its socket, waiting 300 seconds to a new request.
 * The server reads the configuration in a file called p1-files/server.properties
 * If it was accepted, new ServerThread will be created, doing the work described in the same class.
 * @author 102
 */

public class WebServer {
    public static int port;
    public static String BASE_DIRECTORY;
    public static String DEFAULT_FILE;
    private static ServerSocket sSocket;
    /**
     * main method of the class that initializes the server reading the file server.properties
     * @param args the arguments, in that case we don't need anyone because we read the settings from a file
     */
    public static void main(String[] args) {

        Properties prop = new Properties();
        try {
            InputStream inputStream;
            inputStream = new FileInputStream("p1-files/server.properties");
            prop.load(inputStream); //charge the server configuration
            port = Integer.parseInt(prop.getProperty("PORT"));
            BASE_DIRECTORY = prop.getProperty("BASE_DIRECTORY");
            DEFAULT_FILE = prop.getProperty("DEFAULT_FILE");
            sSocket = new ServerSocket(port);
            sSocket.setSoTimeout(300000);
            while (true) { // waiting to new conections
                Socket accept = sSocket.accept();
                ServerThread server = new ServerThread(accept, BASE_DIRECTORY, DEFAULT_FILE);
                server.start();
            }
        } catch (SocketTimeoutException e) {
            System.err.println("300 segs sin recibir nada");
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                //Cerramos el socket
                if(sSocket!=null)
                    sSocket.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}