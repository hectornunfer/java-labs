package es.udc.redes.tutorial.tcp.server;
import java.net.*;
import java.io.*;

/** Thread that processes an echo server connection. */

public class ServerThread extends Thread {

  public Socket socket;

  public ServerThread(Socket s) {
    // Store the socket s
    this.socket = s;
  }

  public void run() {
    try {
      // Set the input channel
      BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      // Set the output channel
      PrintWriter out = new PrintWriter(new PrintWriter(socket.getOutputStream(),true));
      // Receive the message from the client
      String message = in.readLine();
      System.out.println("SERVER: Received "
              + message
              + " from " + socket.getInetAddress() + ":"
              + socket.getPort());
      // Sent the echo message to the client
      out.println(message);
      System.out.println("SERVER: Sending "
              + message
              + " to " + socket.getInetAddress() + ":"
              + socket.getPort());
      // Close the streams
      in.close();
      out.close();
    } catch (SocketTimeoutException e) {
      System.err.println("Nothing received in 300 secs");
    } catch (Exception e) {
      System.err.println("Error: " + e.getMessage());
    } finally {
	// Close the socket
      try {
        if(socket!=null) socket.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
    }
  }
}
