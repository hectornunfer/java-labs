package es.udc.redes.webserver;


import java.net.*;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * ServerThread manages the responses to the HTTP requests.
 * The class logs every successful or failed petition.
 * @author 102
 */
public class ServerThread extends Thread {
    final Socket socket;
    private String files, CODE;
    private Date date;

    SimpleDateFormat dateformat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
    private final String BASE_DIRECTORY;
    private final String DEFAULT_FILE;
    String http = "HTTP/1.1";
    String machine = "WebServer_102"; // my DNI is 34287102J
    String codeOK = "200 OK";
    String codeNM = "304 Not Modified";
    String codeBR = "400 Bad Request";
    String codeF = "403 Forbidden";
    String codeNF = "404 Not Found";

    /**
     * Class contructor
     * @param s Socket received from WebServer
     * @param basedir Base directory which contains the files
     * @param basefile Default file if no one was requested
     */
    public ServerThread(Socket s, String basedir, String basefile) {
        // Store the socket s
        this.socket = s;
        this.BASE_DIRECTORY = basedir;
        this.DEFAULT_FILE = basefile;
    }

    /**
     * It generates the response to the request, generating the respective
     * success and errors messages and also logs them.
     * Error: It always throws an SocketException before requesting an order, the server works and responds well to the requests, but in
     * the .jar, I get an exception in every case, except the NotFound. I've making changes and debugging my code so many times, IDE tells
     * that the socket is closed, but if I print in that line the Socket status, it shows that its opened. I waste many time searching the bug but
     * I could not fix it. It could be an IntelliJ problem, because i have to run the server in the IDE, if i try to run it in a shell, it shows me that
     * ServerThread class was not recognised(this happens in the p0 too). If someone knows how to fix this error, contact me: hector.nunez.fernandez@udc.es.
     */

    public void run() {
        files = BASE_DIRECTORY;
        BufferedReader in;
        PrintWriter out;
        BufferedOutputStream outputStream;
        String[] parts;
        try {
            // This code processes HTTP requests and generates
            // HTTP responses
            while (true){ //makes the Tcp conection persistent, implementing http 1.1
                in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
                out = new PrintWriter(this.socket.getOutputStream());
                outputStream = new BufferedOutputStream(this.socket.getOutputStream());
                String message = in.readLine();
                if( message!= null) {
                    parts = message.split(" ");
                    String order = parts[0];
                    String file = parts[1];
                    if(file.equals("/")){
                        file = DEFAULT_FILE;
                        order = "GET";
                    }
                    if(!order.equals("GET") && !order.equals("HEAD")) { // no recognised request
                        CODE = codeBR;
                        file = BASE_DIRECTORY + "/error/error400.html";
                        GetandHead(file,order,out,outputStream);
                    }
                    else if (order.equals("GET")) {
                        String ims = null, aux = in.readLine();
                        if(aux != null) { // watch if the file implements If-modified-since
                            while (!aux.equals("")) {
                                if(aux.contains("If-Modified-Since")) {
                                    ims = aux;
                                    break;
                                } else {
                                    aux = in.readLine();
                                }
                            }
                        }
                        if(ims != null && ims.contains("If-Modified-Since")) {
                            String dateims = ims.substring(19);
                            File samepath = new File(BASE_DIRECTORY + file);
                            Date old = dateformat.parse(dateims);
                            Date last = dateformat.parse(dateformat.format((int)samepath.lastModified()));
                            if(old.after(last) || old.equals(last)) { //checking if the file was changed
                                CODE = codeNM;
                                GetandHead(file,order,out,outputStream);
                            } else {
                                CODE = codeOK;
                                GetandHead(file,order,out,outputStream);
                            }
                        }
                        else {
                            CODE = codeOK;
                            GetandHead(file,order,out,outputStream);
                        }
                    }
                    else  {
                        CODE = codeOK;
                        GetandHead(file,order,out,outputStream);
                    }
                }
                in.close();
                out.close();
                outputStream.close(); //closing streams and the thread doesn't end waiting to another conection
            }


        }
        catch (SocketTimeoutException e) {
            System.err.println("10 secs with no requests");
        }catch (IOException | ParseException e) { //it throws IOException with no apparently reason,
            // the code works and answer the requests, but in each petition it generates the called exception
            //i will comment that inusual mistake in other file
            e.printStackTrace();
        } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }

    /**
     * Prints the headers of the request
     * If its an GET order, we get and read de file.
     * @param file Variable which tells us what file was requested
     * @param order Represents the order request (GET, HEAD,...)
     * @param writer Variable initialized where we want to print the headers
     * @param outputStream Allows us to tranfer the file if we received a GET order.
     */
    private void GetandHead(String file, String order, PrintWriter writer, OutputStream outputStream){
        File path = new File(BASE_DIRECTORY + file);
        if (CODE.equals(codeOK)){
            if(path.exists()) {
                CODE = codeOK;
            } else{
                CODE = codeNF;
                path = new File(BASE_DIRECTORY + "error/error404.html"); //it will show an html with the 404 error
            }
        }
        date = new Date();
        if(CODE.equals(codeBR)) {
            String print = http + " " + CODE + "\n" + "Date: " + dateformat.format(date) + "\n"
            + "Server: " + machine + "\n"; //low headers than other requests, the rest are not necessary
            writer.println(print);
            writer.flush();
        } else { //printing the headers
             String print = http + " " + CODE + "\n" + "Date: " + dateformat.format(date) + "\n"
             + "Server: " + machine + "\n" + "Last-Modified: " + dateformat.format(path.lastModified()) + "\n"
             + "Content-Length: " + path.length() + "\n" + "Content-type: " + FileType(path.getName()) + "\n";
            writer.println(print);
            writer.flush();
        }
        if(order.equals("GET") ) {
            try { // transfer the file to the outputStream wanted
                FileInputStream transfer = null;
                byte[] content = new byte[(int)path.length()];
                transfer = new FileInputStream(path);
                transfer.read(content);
                outputStream.write(content, 0, (int) path.length());
                outputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        logger(order,file,(int)path.length()); //logs every event
    }

    /**
     * It tells the type of the file requested
     * @param name Name of the file searched
     * @return String with the type of the file
     */
    public String FileType(String name) {
        String aux, type = name.substring(name.lastIndexOf(".")+1);
        aux = switch (type) {
            case "html" -> "text/html";
            case "txt" -> "text/plain";
            case "gif" -> "image/gif";
            case "png" -> "image/png";
            default -> "application/octet-stream";
        };
        return aux;
    }

    /**
     * It logs every request, saving it in a different file depending the result of the request,
     * if it was a success, it saves the information in /p1-files/log/acces.log If the result was an error,
     * it saves in /p1-files/log/error.log
     * @param order Request to log
     * @param file File requested
     * @param size Size of the file requested
     */
    private void logger(String order, String file, int size) {
        File access = new File(BASE_DIRECTORY + "log/access.log");
        File errors = new File(BASE_DIRECTORY + "log/error.log");
        try {
            PrintWriter printer;
            if( CODE.startsWith("2") || CODE.startsWith("3")) { //if success
                printer = new PrintWriter(new FileOutputStream(access,true),true);
                String print = "Access order: " + order + file + http  + "\n" + "Client IP: " + socket.getInetAddress().toString()  +
                "\n" + "Date and hour: " + date  + "\n" + "Code status: " + CODE  + "\n" + "Size: " + size + "bytes" + "\n\n";
                printer.println(print);
            } else { //if fails
                printer = new PrintWriter(new FileOutputStream(errors,true),true);
                String print = "Access order: " + order + file + http  + "\n" + "Client IP: " + socket.getInetAddress().toString()  +
                        "\n" + "Date and hour: " + date  + "\n" + "Error code: " + CODE + "\n\n";
                printer.println(print);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }
}
