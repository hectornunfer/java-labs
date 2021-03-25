package es.udc.redes.webserver;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Properties;

public class WebServer {
    public static int port;
    public static String BASE_DIRECTORY;
    public static String DEFAULT_FILE;
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Format: es.udc.redes.webserver.WebServer <port>");
            System.exit(-1);
        }
        InputStream inputStream = null;
        ServerSocket sSocket = null;
        Properties prop = new Properties();
        try {
            inputStream = new FileInputStream("p1-files/server.properties");
            prop.load(inputStream);
            port = Integer.parseInt(prop.getProperty("PORT"));
            BASE_DIRECTORY = prop.getProperty("BASE_DIRECTORY");
            DEFAULT_FILE = prop.getProperty("DEFAULT_FILE");
            sSocket = new ServerSocket(port);
            sSocket.setSoTimeout(300000);
            while (true) {
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