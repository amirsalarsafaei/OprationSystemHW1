package os.hw1.master;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class MasterMain {
    //variables
    private static int maxWeight;
    private static List<String> comArgs = new ArrayList<>();
    private static Program []programs;

    private static SortedSet<SubServerInfo> subServerInfos = new TreeSet<>();
    private static int portCnt = 8080;
    private static int masterPort;
    private static int cachePort;
    private static long masterPid;
    private static Scanner scanner;
    private static ServerSocket masterSocket, cacheServerSocket;
    private static Socket cacheSocket;
    private static StreamHandler cacheIO;
    private static Map<SubProcess, ArrayList<AssignedProcess> > waitingForAnswer;
    private static Map<SubProcess, Boolean> isProcessed, isBeingProcessed;
    private static final SortedSet<AssignedProcess> queue = new TreeSet<>();
    private static Process cacheProcess;
    private static ProcessBuilder processBuilder = new ProcessBuilder();
    static final ExecutorService executorService = Executors.newFixedThreadPool(100);
    //preprocess and starting main server

    private synchronized static int nextPort() {
        try (ServerSocket ignored = new ServerSocket(portCnt)) {
            ignored.close();
            return portCnt++;
        } catch (IOException ignored) {
            portCnt++;
            return nextPort();
        }
    }

    private static SubServerInfo makeSubServer(int id) throws IOException {
        int port = nextPort();

        ServerSocket serverSocket = null;
        StreamHandler tmp = null;
        Process process = null;
        Socket socket = null;

        serverSocket = new ServerSocket(port);
        List<String> args = new ArrayList<>(comArgs);
        args.add("os.hw1.master.SubServer");
        args.add(String.valueOf(port));
        args.add(String.valueOf(id));
        process = processBuilder.command(args).start();
        socket = serverSocket.accept();
        System.out.println("worker " + id + " " + "start " + process.pid() + " " + port);
        tmp = new StreamHandler(socket);
        tmp.sendMessage(String.valueOf(comArgs.size()));
        for (String s : comArgs)
            tmp.sendMessage(s);
        tmp.sendMessage(String.valueOf(programs.length));
        for (Program program : programs) {
            tmp.sendMessage(program.getName());
            tmp.sendMessage(String.valueOf(program.getWeight()));
        }
        SubServerInfo subServerInfo = new SubServerInfo(0, process.pid(), port, tmp, socket, serverSocket, id, process);
        executorService.execute(new SubServerListener(subServerInfo));
        Socket finalSocket = socket;
        return subServerInfo;
    }

    private static void printLines(InputStream ins) throws Exception {
        String line = null;
        BufferedReader in = new BufferedReader(
                new InputStreamReader(ins));
        while ((line = in.readLine()) != null) {
            System.out.println( line);
        }
    }

    private static void startSubServers(int subServerCnt) throws IOException {
        for (int i = 0; i < subServerCnt; i++) {
            subServerInfos.add(makeSubServer(i));
        }
    }


    private static void startCacheServer() throws IOException {

        cachePort = nextPort();
        cacheServerSocket = new ServerSocket(cachePort);
        List<String> args = new ArrayList<>(comArgs);
        args.add("os.hw1.master.CacheServer");
        args.add(String.valueOf(cachePort));
        cacheProcess = processBuilder.command(args).start();
        cacheSocket = cacheServerSocket.accept();
        cacheIO = new StreamHandler(cacheSocket);
        System.out.println("cache start " + cacheProcess.pid() + " " + cachePort);
    }

    private static void preprocess() {
        scanner = new Scanner(System.in);
        //MasterPort
        masterPort = Integer.parseInt(scanner.nextLine());
        masterPid = ProcessHandle.current().pid();

        Runtime.getRuntime().addShutdownHook(new MasterMainShutDownHook());
        try {
            masterSocket = new ServerSocket(masterPort);

        } catch (IOException e) {
            System.out.println("Couldn't Start Master Socket");
            e.printStackTrace();
            System.exit(-1);
        }
        isBeingProcessed = new HashMap<>();
        isProcessed = new HashMap<>();
        waitingForAnswer = new HashMap<>();


        //Arguments
        int subServerCnt = Integer.parseInt(scanner.nextLine());
        maxWeight = Integer.parseInt(scanner.nextLine());
        int cntArgs = Integer.parseInt(scanner.nextLine());
        for (int i = 0; i < cntArgs; i++) {
            comArgs.add(scanner.nextLine());
        }
        //Programs
        int cntPrograms = Integer.parseInt(scanner.nextLine());
        programs = new Program[cntPrograms];
        for (int i = 0; i < cntPrograms; i++) {
            String className;
            int w;
            String line = scanner.nextLine();
            String[] tmp = line.split(" ");
            className = tmp[0];
            w = Integer.parseInt(tmp[1]);
            programs[i] = new Program(w, className);
        }
        System.out.println("master start " + masterPid + " " + masterPort);
        //SubServers
        try {
            startSubServers(subServerCnt);
        } catch (IOException e) {
            System.out.println("Couldn't Start Sub Servers");
            e.printStackTrace();
            System.exit(-1);
        }
        //CacheServer
        try {
            startCacheServer();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static synchronized int getAnsFromCache(SubProcess subProcess) {
        cacheIO.sendMessage("get_" + subProcess);
        int resp = Integer.parseInt(cacheIO.getResponse());
        return resp;
    }
    static synchronized void storeAnsInCache(SubProcess subProcess, int ans) {
        cacheIO.sendMessage("store_" + subProcess + "/" + ans);
        cacheIO.getResponse();
    }



    static void QueueChanged() {

        synchronized (queue) {
           while (queue.size() > 0 && maxWeight - subServerInfos.first().getWeight() >= programs[queue.first().processChain.peek()].getWeight()) {
               SubProcess subProcess = queue.first().getSubProcess();
               isBeingProcessed.put(subProcess, true);
               ArrayList<AssignedProcess> assignedProcesses = new ArrayList<>();
               for (AssignedProcess assignedProcess : queue)
                   if(assignedProcess.getSubProcess().equals(subProcess)) {
                       assignedProcesses.add(assignedProcess);
                   }
               for (AssignedProcess assignedProcess : assignedProcesses) {
                   queue.remove(assignedProcess);
               }
               waitingForAnswer.put(subProcess, assignedProcesses);
               SubServerInfo subServerInfo = subServerInfos.first();
               subServerInfos.remove(subServerInfo);
               subServerInfo.getSubProcesses().add(subProcess);
               subServerInfo.setWeight(subServerInfo.getWeight() + programs[subProcess.getProgramId()].getWeight());
               subServerInfos.add(subServerInfo);
               subServerInfo.io.sendMessage("run_" + subProcess);
           }
        }

    }



    static void sendAnswerBack(AssignedProcess assignedProcess) {
        assignedProcess.io.sendMessage(String.valueOf(assignedProcess.input));
        assignedProcess.io.sendMessage("\n");
        try {
            assignedProcess.socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void addToQueue(AssignedProcess assignedProcess) {
        synchronized (queue) {
            while(assignedProcess.getProcessChain().size() != 0 && isProcessed.getOrDefault(assignedProcess.getSubProcess(), false)) {
                int ans = getAnsFromCache(assignedProcess.getSubProcess());
                assignedProcess.getProcessChain().pop();
                assignedProcess.setInput(ans);
            }
            if (assignedProcess.getProcessChain().size() == 0) {
                sendAnswerBack(assignedProcess);
            }
            else if (isBeingProcessed.getOrDefault(assignedProcess.getSubProcess(), false)) {
                waitingForAnswer.get(assignedProcess.getSubProcess()).add(assignedProcess);
            }
            else  {
                queue.add(assignedProcess);
                if (queue.first() == assignedProcess && subServerInfos.size() > 0)
                    QueueChanged();
            }
        }
    }




    public static void main(String []args) {
        preprocess();
        while(true) {
            try {

                Socket socket = masterSocket.accept();

                    StreamHandler io = null;
                    try {
                        io = new StreamHandler(socket);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    String work = io.getResponse();
                    AssignedProcess assignedProcess = new AssignedProcess(work.split(" ")[0],
                            Integer.parseInt(work.split(" ")[1]), System.nanoTime(), socket, io);
                    addToQueue(assignedProcess);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



    // inner classes
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
        public String toString() {
            return programId + "/" + input;
        }

        @Override
        public int hashCode() {
            return input;
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

    private static class SubServerInfo implements Comparable<SubServerInfo>{
        private int weight, port;
        private long pid;
        private int id;
        private StreamHandler io;
        private ArrayList<SubProcess> subProcesses;
        private Socket socket;
        private ServerSocket serverSocket;
        private Process process;

        public Process getProcess() {
            return process;
        }

        public void setProcess(Process process) {
            this.process = process;
        }

        public ServerSocket getServerSocket() {
            return serverSocket;
        }

        public void setServerSocket(ServerSocket serverSocket) {
            this.serverSocket = serverSocket;
        }

        public Socket getSocket() {
            return socket;
        }

        public void setSocket(Socket socket) {
            this.socket = socket;
        }

        public StreamHandler getIo() {
            return io;
        }

        public void setIo(StreamHandler io) {
            this.io = io;
        }

        public ArrayList<SubProcess> getSubProcesses() {
            return subProcesses;
        }

        public void setSubProcesses(ArrayList<SubProcess> subProcesses) {
            this.subProcesses = subProcesses;
        }

        public SubServerInfo(int weight, long pid, int port, StreamHandler io, Socket socket, ServerSocket serverSocket,
                             int id, Process process) {
            this.weight = weight;
            this.pid = pid;
            this.port = port;
            this.io = io;
            subProcesses = new ArrayList<>();
            this.socket = socket;
            this.serverSocket = serverSocket;
            this.id = id;
            this.process = process;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public long getPid() {
            return pid;
        }

        public void setPid(long pid) {
            this.pid = pid;
        }

        public int getWeight() {
            return weight;
        }

        public void setWeight(int weight) {
            this.weight = weight;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }


        @Override
        public int compareTo(SubServerInfo o) {

            if (weight == o.weight) {
                if (pid != o.pid)
                    return Long.compare(pid, o.pid);
                return Integer.compare(port, o.port);
            }
            return Integer.compare(weight, o.weight);
        }
    }

    private static class AssignedProcess implements Comparable<AssignedProcess>{
        private final Stack<Integer> processChain;
        private int input;
        private long epochTime;
        private Socket socket;
        private StreamHandler io;
        public long getEpochTime() {
            return epochTime;
        }

        public void setEpochTime(long epochTime) {
            this.epochTime = epochTime;
        }

        public AssignedProcess(String chain, int input, long time, Socket socket, StreamHandler io) {
            epochTime = time;
            this.input = input;
            this.processChain = new Stack<>();
            this.socket = socket;
            this.io = io;
            for (String s : chain.split("\\|")) {
                processChain.add(Integer.parseInt(s) - 1);
            }
        }

        public Socket getSocket() {
            return socket;
        }

        public void setSocket(Socket socket) {
            this.socket = socket;
        }

        public StreamHandler getIo() {
            return io;
        }

        public void setIo(StreamHandler io) {
            this.io = io;
        }

        public int getInput() {
            return input;
        }

        public void setInput(int input) {
            this.input = input;
        }

        public Stack<Integer> getProcessChain() {
            return processChain;
        }

        @Override
        public int compareTo(AssignedProcess o) {
            if (epochTime != o.epochTime)
                return Long.compare(epochTime, o.epochTime);
            return Integer.compare(socket.getPort(), o.getSocket().getPort());
        }

        public SubProcess getSubProcess() {
            return new SubProcess(input, processChain.peek());
        }

        @Override
        public boolean equals(Object o) {
            return o.getClass() == this.getClass() && ((AssignedProcess)o).epochTime == epochTime;
        }
    }


    private static class StreamHandler {
        Scanner scanner;
        PrintStream printStream;
        StreamHandler(Socket socket) throws IOException {
            scanner = new Scanner(socket.getInputStream());
            printStream = new PrintStream(socket.getOutputStream());
        }
        public void sendMessage( String message) {
                printStream.println(message);
                printStream.flush();
        }

        public String getResponse() {
            return scanner.nextLine();
        }
    }

    static class SubServerListener implements Runnable {
        SubServerInfo subServer;
        public SubServerListener (SubServerInfo subServer) {
            this.subServer = subServer;
        }

        public void run() {
            while(true) {
                String response = subServer.io.getResponse();
                String stat = response.split("_")[0];

                if (stat.equals("shutdown")) {
                    synchronized (queue) {
                        subServerInfos.remove(subServer);
                        try {
                            subServer.getSocket().close();
                            subServer.getServerSocket().close();
                            System.out.println("worker " + subServer.getId() + " stop " + subServer.getPid() + " " + subServer.getPort());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        for (SubProcess subProcess : subServer.getSubProcesses()) {
                            ArrayList<AssignedProcess> assignedProcesses = waitingForAnswer.get(subProcess);
                            waitingForAnswer.put(subProcess, null);
                            isBeingProcessed.put(subProcess, false);
                            for (AssignedProcess process : assignedProcesses ) {
                                addToQueue(process);
                            }
                        }

                        try {
                            subServer.getSocket().close();
                            subServer.getServerSocket().close();

                            subServerInfos.add(makeSubServer(subServer.getId()));
                            QueueChanged();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                }
                else if (stat.equals("done")) {
                    String message = response.split("_")[1];
                    executorService.execute(()-> {
                        String []tmp = message.split("/");
                        int programId = Integer.parseInt(tmp[0]);
                        int input = Integer.parseInt(tmp[1]);
                        int ans = Integer.parseInt(tmp[2]);
                        SubProcess subProcess = new SubProcess(input, programId);
                        storeAnsInCache(subProcess, ans);

                        synchronized (queue) {
                            isProcessed.put(subProcess, true);
                            isBeingProcessed.put(subProcess, false);
                            subServerInfos.remove(subServer);
                            subServer.setWeight(subServer.getWeight() - programs[programId].getWeight());
                            subServer.getSubProcesses().remove(subProcess);
                            subServerInfos.add(subServer);
                        }
                        for (AssignedProcess assignedProcess : waitingForAnswer.get(subProcess))
                            executorService.execute(()->addToQueue(assignedProcess));
                        executorService.execute(MasterMain::QueueChanged);

                    });
                }

            }
        }

    }
    static class MasterMainShutDownHook extends Thread {
        public void run() {
            for (AssignedProcess assignedProcess : queue) {
                assignedProcess.io.sendMessage("\n");
                try {
                    assignedProcess.socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            queue.clear();
            System.out.println("master stop " + masterPid + " " + masterPort);
            executorService.shutdown();
            for (SubServerInfo subServer : subServerInfos) {
                subServer.getProcess().destroy();
                try {
                    subServer.getProcess().waitFor();
                } catch (InterruptedException e) {
                    subServer.getProcess().destroyForcibly();
                }
                System.out.println("worker " + subServer.getId() + " stop " + subServer.getPid() + " " + subServer.getPort());
            }
            if (cacheProcess != null) {
                cacheProcess.destroy();
                try {
                    cacheProcess.waitFor();
                } catch (InterruptedException e) {
                    cacheProcess.destroyForcibly();
                }

                System.out.println("cache stop " + cacheProcess.pid() + " " + cachePort);
            }
            if (masterSocket != null) {
                try {
                    masterSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            System.out.flush();

        }
    }
}
