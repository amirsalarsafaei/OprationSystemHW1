package os.hw1.master;

import java.util.Scanner;

public class tmp {
    public static void main(String[] args) {
        System.out.println("Attaching to 7b654d4a-4e19-44bc-8bb1-3d0e99411b87_java-test_1\n[INFO] Scanning for projects...\n[INFO] \n[INFO] --------------------------< ir.samssh:judge >---------------------------\n[INFO] Building judge 0.1.0-SNAPSHOT\n[INFO] --------------------------------[ jar ]---------------------------------\n[INFO] \n[INFO] --- maven-resources-plugin:2.6:resources (default-resources) @ judge ---\n[INFO] Using 'UTF-8' encoding to copy filtered resources.\n[INFO] skip non existing resourceDirectory /software/src/main/resources\n[INFO] \n[INFO] --- maven-compiler-plugin:3.8.0:compile (default-compile) @ judge ---\n[INFO] Changes detected - recompiling the module!\n[INFO] Compiling 8 source files to /software/target/classes\n[INFO] \n[INFO] --- maven-resources-plugin:2.6:testResources (default-testResources) @ judge ---\n[INFO] Using 'UTF-8' encoding to copy filtered resources.\n[INFO] skip non existing resourceDirectory /software/src/test/resources\n[INFO] \n[INFO] --- maven-compiler-plugin:3.8.0:testCompile (default-testCompile) @ judge ---\n[INFO] Nothing to compile - all classes are up to date\n[INFO] \n[INFO] --- maven-surefire-plugin:2.19.1:test (default-test) @ judge ---\n\n-------------------------------------------------------\n T E S T S\n-------------------------------------------------------\nRunning os.hw1.testers.Cache3Test\nmaster start 75 16543\nmaster\nworker 0 start 93 8080\nworker\nworker 1 start 112 8081\nworker\n-1\nTests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 2.894 sec - in os.hw1.testers.Cache3Test\nRunning os.hw1.testers.WorkerWeights3Test\nmaster start 202 16543\nmaster\nworker 0 start 219 8080\nworker\n-148\n-286\nTests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 10.157 sec - in os.hw1.testers.WorkerWeights3Test\nRunning os.hw1.testers.Cache1Test\nmaster start 413 16543\nmaster\nworker 0 start 430 8080\nworker\nworker 1 start 449 8081\nworker\n1\n77\n77\n125\nTests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 3.465 sec - in os.hw1.testers.Cache1Test\nRunning os.hw1.testers.WorkerKillTest\nmaster start 563 16543\nmaster\nworker 0 start 580 8080\nworker\nworker 1 start 600 8081\nworker\ncache start 619 8082\nworker 0 stop 580 8080\nworker 0 start 703 8083\nTests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 4.062 sec - in os.hw1.testers.WorkerKillTest\nRunning os.hw1.testers.CheckPIDTest\nmaster start 770 16543\nmaster\nworker 0 start 787 8080\nworker\nworker 1 start 807 8081\nworker\ncache start 826 8082\ncache\n79\n124\nTests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 2.969 sec - in os.hw1.testers.CheckPIDTest\nRunning os.hw1.testers.MultiTimeKillTest\nmaster start 934 16543\nmaster\nworker 0 start 951 8080\nworker\ncache start 970 8081\nworker 0 stop 951 8080\nworker 0 start 1013 8082\nworker 0 stop 1013 8082\nworker 0 start 1054 8083\nworker 0 stop 1054 8083\nworker 0 start 1095 8084\nTests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 3.928 sec - in os.hw1.testers.MultiTimeKillTest\nRunning os.hw1.testers.Cache2Test\nmaster start 1160 16543\nmaster\nworker 0 start 1177 8080\nworker\nworker 1 start 1197 8081\nworker\n-99\n-34\n43\nTests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 4.693 sec - in os.hw1.testers.Cache2Test\nRunning os.hw1.testers.WorkerWeights2Test\nmaster start 1349 16543\nmaster\nworker 0 start 1366 8080\nworker\nworker 1 start 1385 8081\nworker\n52\n41\n-9\nTests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 4.203 sec - in os.hw1.testers.WorkerWeights2Test\nRunning os.hw1.testers.CheckPID2Test\nmaster start 1516 16543\nmaster\nworker 0 start 1533 8080\nworker\nworker 1 start 1553 8081\nworker\n95\n26\nTests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 3.451 sec - in os.hw1.testers.CheckPID2Test\nRunning os.hw1.testers.WorkerWeightsTest\nmaster start 1679 16543\nmaster\nworker 0 start 1696 8080\nworker\nworker 1 start 1715 8081\nworker\n18\n138\nTests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 6.376 sec - in os.hw1.testers.WorkerWeightsTest\nRunning os.hw1.testers.WorkerKillAndCacheTest\nmaster start 1909 16543\nmaster\nworker 0 start 1926 8080\nworker\ncache start 1946 8081\nworker 0 stop 1926 8080\nworker 0 start 1990 8082\n-346\n-837\nTests run: 1, Failures: 1, Errors: 0, Skipped: 0, Time elapsed: 6.734 sec <<< FAILURE! - in os.hw1.testers.WorkerKillAndCacheTest\nworkerKillAndCacheTest(os.hw1.testers.WorkerKillAndCacheTest)  Time elapsed: 6.734 sec  <<< FAILURE!\njava.lang.AssertionError: 837\n\tat os.hw1.testers.WorkerKillAndCacheTest.workerKillAndCacheTest(WorkerKillAndCacheTest.java:32)\n\nRunning os.hw1.testers.WorkerWeights4Test\nmaster start 2099 16543\nmaster\nworker 0 start 2116 8080\nworker\nworker 1 start 2136 8081\nworker\nworker 2 start 2155 8082\nworker\n312\n304\n6\nTests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 6.658 sec - in os.hw1.testers.WorkerWeights4Test\n\nResults :\n\nFailed tests: \n  WorkerKillAndCacheTest.workerKillAndCacheTest:32->BaseTester.assertTime:91 837\n\nTests run: 12, Failures: 1, Errors: 0, Skipped: 0\n\n[INFO] ------------------------------------------------------------------------\n[INFO] BUILD FAILURE\n[INFO] ------------------------------------------------------------------------\n[INFO] Total time:  01:02 min\n[INFO] Finished at: 2022-04-08T19:30:42Z\n[INFO] ------------------------------------------------------------------------\n[ERROR] Failed to execute goal org.apache.maven.plugins:maven-surefire-plugin:2.19.1:test (default-test) on project judge: There are test failures.\n[ERROR] \n[ERROR] Please refer to /software/target/surefire-reports for the individual test results.\n[ERROR] -> [Help 1]\n[ERROR] \n[ERROR] To see the full stack trace of the errors, re-run Maven with the -e switch.\n[ERROR] Re-run Maven using the -X switch to enable full debug logging.\n[ERROR] \n[ERROR] For more information about the errors and possible solutions, please read the following articles:\n[ERROR] [Help 1] http://cwiki.apache.org/confluence/display/MAVEN/MojoFailureException\n\u001b[36m7b654d4a-4e19-44bc-8bb1-3d0e99411b87_java-test_1 exited with code 1\n\u001b[0m");
    }
}