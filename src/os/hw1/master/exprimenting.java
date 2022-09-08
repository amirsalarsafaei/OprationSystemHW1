package os.hw1.master;

import java.io.*;

public class exprimenting {
    public static  void main(String[] args ) {
        try {
            runProcess("javac -cp src src\\\\os\\\\hw1\\\\master\\\\tmp.java ");

            Process process = runProcess("java -cp src os.hw1.master.tmp ");
            BufferedWriter streamWriter = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));

            streamWriter.write("12");
            streamWriter.flush();
            streamWriter.close();
            process.waitFor();
            printLines("wtf", process.getInputStream());

        } catch (Exception e) {
            e.printStackTrace();
        }
           }
    private static Process runProcess(String command) throws Exception {
        Process pro = Runtime.getRuntime().exec(command);
        return pro;
    }
    private static void printLines(String name, InputStream ins) throws Exception {
        String line = null;
        BufferedReader in = new BufferedReader(
                new InputStreamReader(ins));
        while ((line = in.readLine()) != null) {
            System.out.println(name + " " + line);
        }
    }



}

