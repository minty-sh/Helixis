package sh.minty.helixis.commands;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Command(
    name = "portscan",
    mixinStandardHelpOptions = true,
    description = "Scans for open ports on a given host."
)
public class PortScanCommand implements Callable<Integer> {

    @Parameters(index = "0", description = "The target host (IP address or hostname).")
    private String host;

    @Parameters(index = "1", description = "The port range to scan (e.g., '1-1024').")
    private String portRange;

    @Option(names = {"-t", "--timeout"}, description = "Connection timeout in milliseconds. Defaults to 200.")
    private int timeout = 200;

    @Option(names = {"-w", "--workers"}, description = "Number of concurrent workers. Defaults to 100.")
    private int workers = 100;

    @Override
    public Integer call() {
        int startPort;
        int endPort;

        try {
            String[] ports = portRange.split("-");
            if (ports.length == 2) {
                // If two ports are specified, use them as the start and end ports
                startPort = Integer.parseInt(ports[0]);
                endPort = Integer.parseInt(ports[1]);
            } else if (ports.length == 1) {
                // If only one port is specified, assume it's the start port
                startPort = Integer.parseInt(ports[0]);
                endPort = startPort;
            } else {
                System.err.println("Error: Invalid port range format. Use 'start-end' or 'port'.");
                return 1;
            }

            if (startPort < 1 || endPort > 65535 || startPort > endPort) {
                System.err.println("Error: Invalid port range. Ports must be between 1 and 65535.");
                return 1;
            }
        } catch (NumberFormatException e) {
            System.err.println("Error: Invalid port number in range. " + e.getMessage());
            return 1;
        }

        System.out.println("Scanning " + host + " for open ports in range " + portRange + "...");

        var executor = Executors.newFixedThreadPool(workers);

        for (int port = startPort; port <= endPort; port++) {
            final int currentPort = port;
            executor.submit(() -> {
                try {
                    var socket = new Socket();
                    socket.connect(new InetSocketAddress(host, currentPort), timeout);
                    socket.close();
                    System.out.println("Port " + currentPort + " is open.");
                } catch (Exception e) {
                    // Port is closed or unreachable
                }
            });
        }

        executor.shutdown();
        try {
            executor.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            System.err.println("Port scan interrupted.");
            Thread.currentThread().interrupt();
            return 1;
        }

        System.out.println("Scan complete.");
        return 0;
    }
}
