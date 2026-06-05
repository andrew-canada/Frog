import java.io.*;
public class FileIO {
    // read a file into the array data. the method works regarless of the
    // size of the array. if the file is larger that data can hold then
    // the method stops when data is full. if the file is smaller than data
    // then the method stops when the file has been fully loaded into data
    public static void readFileIntoArray(String fileName, String[] data) {
        String line = "";
        
        RandomAccessFile f;        
        try {
            f = new RandomAccessFile(fileName, "rw"); 
            int i = 0;
            line = f.readLine();
            while (i < data.length && line != null) {
                data[i] = line;
                line = f.readLine();
                i++;
            }            
            f.close();
        }
        catch(IOException e) {
            System.out.println("***error: unable to open file");
        }
    }

    // write the array data to the file fileName
    public static void writeArrayToFile(String[] data, String fileName) {
        String line = "";
        
        RandomAccessFile f;        
        try {
            f = new RandomAccessFile(fileName, "rw");
            f.setLength(0); 
            for (int i = 0; i < data.length; i++) {
                f.writeBytes(data[i] + "\n");
            }            
            f.close();
        }
        catch(IOException e) {
            System.out.println("***error: unable to open file");
        }
    }
}
