package es.udc.redes.webserver;


import java.net.*;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ServerThread extends Thread {

    private final Socket socket;
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

    public ServerThread(Socket s, String basedir, String basefile) {
        // Store the socket s
        this.socket = s;
        this.BASE_DIRECTORY = basedir;
        this.DEFAULT_FILE = basefile;
    }


    public void run() {
        files = BASE_DIRECTORY;
        BufferedReader in = null;
        PrintWriter out = null;
        BufferedOutputStream outputStream = null;
        String[] parts = null;
        try {
          // This code processes HTTP requests and generates 
          // HTTP responses
            while (true){
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream());
                outputStream = new BufferedOutputStream(socket.getOutputStream());
                String message = in.readLine();
                if( message!= null) {
                    parts = message.split(" ");
                    String order = parts[0];
                    String file = parts[1];
                    if(file.equals("/")){
                        file = DEFAULT_FILE;
                        order = "GET";
                    }
                    if(!order.equals("GET") && !order.equals("HEAD")) {
                        CODE = codeBR;
                        file = BASE_DIRECTORY + "/error/error400.html";
                        GetandHead(file,order,out,outputStream);
                    }
                    else if (order.equals("GET")) {
                        String ims = null, aux = in.readLine();
                        if(aux != null) {
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
                            if(old.after(last) || old.equals(last)) {
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
                outputStream.close();
            }



        }
        catch (SocketTimeoutException e) {
            System.err.println("60 segs sin recibir nada");
        }  catch (IOException | ParseException e) {
            e.printStackTrace();
        } finally {

                if(socket != null) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
        }
    }
    private void GetandHead(String file, String order, PrintWriter writer, OutputStream outputStream){
        File path = new File(BASE_DIRECTORY + file);
        if (CODE.equals(codeOK)){
            if(path.exists()) {
                CODE = codeOK;
            } else{
                CODE = codeNF;
                path = new File(BASE_DIRECTORY + "error/error404.html");
            }
        }
        date = new Date();
        if(CODE.equals(codeBR)) {
            String print = http + " " + CODE + "\n" + "Date: " + dateformat.format(date) + "\n"
            + "Server: " + machine + "\n";
            writer.println(print);
            writer.flush();
        } else {
             String print = http + " " + CODE + "\n" + "Date: " + dateformat.format(date) + "\n"
             + "Server: " + machine + "\n" + "Last-Modified: " + dateformat.format(path.lastModified()) + "\n"
             + "Content-Length: " + path.length() + "\n" + "Content-type: " + FileType(path.getName()) + "\n";
            writer.println(print);
            writer.flush();
        }
        if(order.equals("GET") ) {
            try {
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
        logger(order,file,(int)path.length());
    }

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
    private void logger(String order, String file, int size) {
        File access = new File(BASE_DIRECTORY + "log/access.log");
        File errors = new File(BASE_DIRECTORY + "log/error.log");
        try {
            PrintWriter printer;
            if( CODE.contains("2") || CODE.startsWith("3")) {
                printer = new PrintWriter(new FileOutputStream(access,true),true);
                String print = "Access order: " + order + file + http  + "\n" + "Client IP: " + socket.getInetAddress().toString()  +
                "\n" + "Date and hour: " + date  + "\n" + "Code status: " + CODE  + "\n" + "Size: " + size + "bytes" + "\n\n";
                printer.println(print);
            } else {
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
