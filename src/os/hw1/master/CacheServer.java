package os.hw1.master;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class CacheServer {
    private static Map<SubProcess, Integer> answer;
    static DataInputStream dataIn;
    static DataOutputStream dataOut;
    static Scanner scanner;
    static PrintStream printStream;
    public static void main(String[] args) {
        int port = Integer.parseInt(args[0]);
        Socket socket = null;
        try {
            socket = new Socket(InetAddress.getLocalHost(), port);
            scanner = new Scanner(socket.getInputStream());
            printStream = new PrintStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        answer = new HashMap<>();
        while (true) {
            String resp = StreamHandler.getResponse();
            String stat = resp.split("_")[0];
            String data = resp.split("_")[1];
            int programId = Integer.parseInt(data.split("/")[0]), input = Integer.parseInt(data.split("/")[1]);
            if (stat.equals("get")) {
                StreamHandler.sendMessage(String.valueOf(answer.get(new SubProcess(input,programId))));
            }
            else {
                int ans = Integer.parseInt(data.split("/")[2]);
                answer.put(new SubProcess(input, programId), ans);
                StreamHandler.sendMessage("done");
            }
        }
    }


    private static class SubProcess implements Comparable<SubProcess> {
        private int input, programId;

        public SubProcess(int input, int programId) {
            this.input = input;
            this.programId = programId;
        }

        public int getInput() {
            return input;
        }

        public void setInput(int input) {
            this.input = input;
        }

        public int getProgramId() {
            return programId;
        }

        public void setProgramId(int programId) {
            this.programId = programId;
        }

        @Override
        public int compareTo(SubProcess o) {
            if (programId == o.programId)
                return Integer.compare(input, o.input);
            return Integer.compare(programId, o.programId);
        }
        @Override
        public boolean equals(Object o) {
            if (getClass() != o.getClass())
                return false;
            return input == ((SubProcess) o).input && programId == ((SubProcess) o).programId;
        }
        @Override
        public int hashCode() {
            return input + programId;
        }
    }


    private static class StreamHandler {
      public static void sendMessage( String message) {
                printStream.println(message);
        }

        public static String getResponse() {
            return   scanner.nextLine();
        }
    }

}
