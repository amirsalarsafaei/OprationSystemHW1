package os.hw1.master;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SubServer {

    public static int CurWeight;
    public static ArrayList<SubProcess> subProcesses;
    public static int port, id;
    public static Socket socket;
    public static ProcessBuilder processBuilder = new ProcessBuilder();
    public static DataInputStream dataIn;
    public static DataOutputStream dataOut;
    public static SubServerShutDownHook subServerShutDownHook;
    public static List<String> comArgs = new ArrayList<>();
    public static PrintStream printStream;
    public static Scanner scanner;
    private static Program[]programs;
    private static ArrayList<Process> activeProcess = new ArrayList<>();
    static final ExecutorService executorService = Executors.newFixedThreadPool(50);
    public static void main(String[] args) {
        CurWeight = 0;
        subProcesses = new ArrayList<>();
        port = Integer.parseInt(args[0]);
        id = Integer.parseInt(args[1]);
        try {
            socket = new Socket(InetAddress.getLocalHost(), port);
            scanner = new Scanner(socket.getInputStream());
            printStream = new PrintStream(socket.getOutputStream());
        } catch (IOException e) {
            System.out.println("Couldn't Connect to Master");
            System.exit(-1);
        }
        int cntComArgs = Integer.parseInt(StreamHandler.getResponse());
        for (int i = 0; i < cntComArgs; i++) {
            String s = StreamHandler.getResponse();
            comArgs.add(s);
        }
        int cntPrograms = Integer.parseInt(StreamHandler.getResponse());
        programs = new Program[cntPrograms];
        for (int i = 0; i < cntPrograms; i++) {
            int w;
            String name;
            name = StreamHandler.getResponse();
            w = Integer.parseInt(StreamHandler.getResponse());
            programs[i] = new Program(w, name);
        }
        subServerShutDownHook = new SubServerShutDownHook();
        Runtime.getRuntime().addShutdownHook(subServerShutDownHook);
        new Thread(new MasterServerSocketListener()).start();
    }

    synchronized static void sendAnswerToMaster(SubProcess subProcess, int ans) {
        StreamHandler.sendMessage("done_" + subProcess.programId+ "/" + subProcess.input + "/" + ans);
        subProcesses.remove(subProcess);
    }

    static void runProcess(SubProcess subProcess) {

        subProcesses.add(subProcess);
        Program program = programs[subProcess.programId];
        int input = subProcess.input;
        CurWeight += program.weight;
        try {
            List<String> args = new ArrayList<>(comArgs);
            args.add(program.name);
            Process process = processBuilder.command(args).start();
            activeProcess.add(process);
            executorService.execute(()-> {
                try {
                    PrintStream printStream1 = new PrintStream((process.getOutputStream()));
                    printStream1.print(input);
                    printStream1.flush();
                    printStream1.close();
                    Scanner scanner1 = new Scanner(process.getInputStream());
                    int output = scanner1.nextInt();
                    sendAnswerToMaster(subProcess, output);
                    scanner1.close();
                    process.waitFor();
                    activeProcess.remove(process);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
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
    }

    private static class StreamHandler {
        public static void sendMessage( String message) {
            printStream.println(message);
        }

        public static String getResponse() {
            return scanner.nextLine();
        }
    }
    static class SubServerShutDownHook extends Thread {


        public void run() {
            executorService.shutdown();
            for (Process process : activeProcess)
                process.destroyForcibly();
            StreamHandler.sendMessage("shutdown");
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    static class MasterServerSocketListener implements Runnable{
        public void run() {
            while (true) {
                String response = StreamHandler.getResponse();
                String []tmp = response.split("_");
                String status = tmp[0];
                if (status.equals("run")) {
                    int programInd = Integer.parseInt(tmp[1].split("/")[0]);
                    int input = Integer.parseInt(tmp[1].split("/")[1]);
                    runProcess(new SubProcess(input, programInd));
                }

            }
        }
    }

    private static class Program {
        private int weight;
        private String name;

        public Program(int weight, String name) {
            this.weight = weight;
            this.name = name;
        }

        public int getWeight() {
            return weight;
        }

        public void setWeight(int weight) {
            this.weight = weight;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
