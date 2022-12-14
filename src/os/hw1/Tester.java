package os.hw1;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.Objects;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class Tester {
    public static final int WAIT_P1 = 1000;
    public static final int WAIT_P2 = 2000;
    static final long SAFA_MARGIN = 500;
    static final int port = 16054;
    static final int workerCount = 2;
    static final int w = 5;
    static final String[] commonArgs = {
            "C:\\Users\\user\\.jdks\\corretto-17\\bin\\java", // replace with your java path with version 1.8
            "-classpath",
            "out/production/OS-final"
    };
    static final String[] programs = {
            "os.hw1.programs.Program1 2",
            "os.hw1.programs.Program2 3",
    };

    static final ExecutorService executorService = Executors.newFixedThreadPool(50);

    static Process runProcess() throws Exception {
        Process process = new ProcessBuilder(
                commonArgs[0], commonArgs[1], commonArgs[2], "os.hw1.master.MasterMain"
        ).start();
        PrintStream printStream = new PrintStream(process.getOutputStream());
        printStream.println(port);
        printStream.println(workerCount);
        printStream.println(w);
        printStream.println(commonArgs.length);
        Arrays.stream(commonArgs).forEach(printStream::println);
        printStream.println(programs.length);
        Arrays.stream(programs).forEach(printStream::println);
        printStream.flush();
        new Thread(()-> {
            try {
                printLines(process.getInputStream());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
        new Thread(()-> {
            try {
                printLines(process.getErrorStream());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
        Thread.sleep(1000);
        // wait for first line of output to ensure that master initialization is completed
        return process;
    }
    private static void printLines(InputStream ins) throws Exception {
        String line = null;
        BufferedReader in = new BufferedReader(
                new InputStreamReader(ins));
        while ((line = in.readLine()) != null) {
            System.out.println(line);
        }
    }
    static void transfer(OutputStream outputStream, InputStream inputStream) {
        Scanner scanner = new Scanner(inputStream);
        PrintStream printStream = new PrintStream(outputStream);
        while (scanner.hasNextLine()) {
            printStream.println(scanner.nextLine());
            printStream.flush();
        }
    }

    static class Response {
        private final long time;
        private final int output;

        Response(long time, int output) {
            this.time = time;
            this.output = output;
        }
    }

    private static Response sendRequest(int input, int... programs) {
        String request = Arrays.stream(programs).mapToObj(String::valueOf).collect(Collectors.joining("|")) + " " + input;
        try {
            long start = System.currentTimeMillis();
            Socket socket = new Socket(InetAddress.getLocalHost(), port);
            Scanner scanner = new Scanner(socket.getInputStream());

            PrintStream printStream = new PrintStream(socket.getOutputStream());
            printStream.println(request);
            printStream.flush();
            int response = scanner.nextInt();
            System.out.println(response);
            socket.close();
            return new Response(System.currentTimeMillis() - start, response);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    static void assertTime(long actual, long expected) throws Exception {
        System.out.println("diff time" + (actual - expected));
        if (expected - SAFA_MARGIN > actual || actual > expected + SAFA_MARGIN) {
            throw new Exception("boom");
        }
    }

    static void assertInt(int actual, int expected) throws Exception {
        if (actual != expected) {
            throw new Exception("boom");
        }
    }

    static void assertString(String actual, String expected) throws Exception {
        if (!Objects.equals(actual, expected)) {
            throw new Exception("boom");
        }
    }


    public static void main(String[] args) throws Exception {
        Process process = runProcess();
        int a = 5;
        int[] programs = new int[a];
        Arrays.fill(programs, 1);
        Future<Response> r1 = executorService.submit(() -> sendRequest(50, programs));
        Thread.sleep(100);
        Future<Response> r2 = executorService.submit(() -> sendRequest(50, programs));
        Thread.sleep(100);
        Future<Response> r3 = executorService.submit(() -> sendRequest(50, programs));
        Response result1 = r1.get();
        assertTime(result1.time, a * WAIT_P1);
        assertInt(result1.output, 50 - a);
        Response result2 = r2.get();

        assertTime(result2.time, a * WAIT_P1 - 100);
        assertInt(result2.output, 50 - a);
        Response result3 = r3.get();
        assertTime(result3.time, a * WAIT_P1 - 200);
        assertInt(result3.output, 50 - a);
        System.out.println("pass phase 1");

        Future<Response> ra1 = executorService.submit(() -> sendRequest(10, 2));
        Thread.sleep(50);
        Future<Response> ra2 = executorService.submit(() -> sendRequest(13, 2));
        Thread.sleep(50);
        Future<Response> ra3 = executorService.submit(() -> sendRequest(17, 2));
        Response resulta1 = ra1.get();
        assertTime(resulta1.time, WAIT_P2);
        assertInt(resulta1.output, 0);
        Response resulta2 = ra2.get();
        assertTime(resulta2.time, WAIT_P2);
        assertInt(resulta2.output, 1);
        Response resulta3 = ra3.get();
        assertTime(resulta3.time, 2 * WAIT_P2);
        assertInt(resulta3.output, 3);
        System.out.println("phase 2 done");
        process.destroy();
        process.waitFor();
        executorService.shutdown();
        Thread.sleep(1000);
    }

}
