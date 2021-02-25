package es.udc.redes.tutorial.udp.server;

import java.net.*;

/**
 * Implements a UDP echo sqerver.
 */
public class UdpServer {

    public static void main(String argv[]) {
        if (argv.length != 1) {
            System.err.println("Format: es.udc.redes.tutorial.udp.server.UdpServer <port_number>");
            System.exit(-1);
        }
        DatagramSocket s = null;
        try {
            // Create a server socket
            s = new DatagramSocket(Integer.parseInt(argv[0]));
            // Set max. timeout to 300 secs
            s.setSoTimeout(300000);
            byte[] packet = new byte[1024];
            while (true) {
                // Prepare datagram for reception
                DatagramPacket r = new DatagramPacket(packet,packet.length);
                // Receive the message
                s.receive(r);
                System.out.println("SERVER: Received "
                        + new String(r.getData(), 0, r.getLength())
                        + " from " + r.getAddress().toString() + ":"
                        + r.getPort());
                // Prepare datagram to send response
                DatagramPacket send = new DatagramPacket(r.getData(),r.getLength(),r.getAddress(),r.getPort());
                // Send response
                s.send(send);
                System.out.println("SERVER: Sending "
                        + new String(send.getData(),0,r.getLength()) + " to "
                        + send.getAddress().toString() + ":" + send.getPort());
            }
          
        } catch (SocketTimeoutException e) {
            System.err.println("No requests received in 300 secs ");
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
        // Close the socket
            if(s!=null) s.close();
        }
    }
}
