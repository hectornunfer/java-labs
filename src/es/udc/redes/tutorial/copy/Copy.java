package es.udc.redes.tutorial.copy;

import java.io.*;

public class Copy {
    public Copy(String sourceFile, String destinationFile) throws IOException{
        BufferedInputStream inputStream = null;
        BufferedOutputStream outputStream = null;

        try {
            inputStream = new BufferedInputStream(new FileInputStream(sourceFile));
            outputStream = new BufferedOutputStream(new FileOutputStream(destinationFile));
            int c;

            while ((c = inputStream.read()) != -1) {
                outputStream.write(c);
            }
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
            if (outputStream != null) {
                outputStream.close();
            }
        }
    }
    public static void main(String[] args) throws IOException {
        if(args.length == 2) {
            new Copy(args[0], args[1]);
        } else System.out.println("Error de parámetros.");
    }
}