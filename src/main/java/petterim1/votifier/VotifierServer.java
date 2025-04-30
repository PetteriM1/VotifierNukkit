package petterim1.votifier;

import cn.nukkit.Nukkit;

import javax.crypto.BadPaddingException;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class VotifierServer extends Thread {

    private volatile boolean running = true;

    private ServerSocket server;

    VotifierServer(String address, int port) {
        setName("Votifier");
        setPriority(Thread.MIN_PRIORITY);

        Main.instance.getLogger().info("Starting Votifier server on " + address + ":" + port);

        try {
            server = new ServerSocket();
            server.bind(new InetSocketAddress(address, port));

            start();
        } catch (Exception ex) {
            Main.instance.getLogger().error("Error while starting Votifier server, please check address/port in config", ex);
        }
    }

    void close() {
        running = false;

        try {
            server.close();
        } catch (Exception ex) {
            Main.instance.getLogger().error("Error while stopping Votifier server", ex);
        }
    }

    @Override
    public void run() {
        while (running) {
            try (Socket socket = server.accept()) {
                if (Nukkit.DEBUG > 1) {
                    Main.instance.getLogger().debug("Connection from " + socket.getRemoteSocketAddress());
                }

                socket.setSoTimeout(5000);

                byte[] block;

                try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {
                    writer.write("VOTIFIER 1.9");
                    writer.newLine();
                    writer.flush();

                    try (InputStream in = socket.getInputStream()) {
                        block = new byte[256];

                        in.read(block, 0, block.length);
                    }
                }

                block = RSA.decrypt(block, Main.instance.keyPair.getPrivate());

                int position = 0;

                String opcode = readString(block, position);
                position += opcode.length() + 1;

                if (!opcode.equals("VOTE")) {
                    throw new Exception("Unable to decode RSA");
                }

                String serviceName = readString(block, position);
                position += serviceName.length() + 1;
                String username = readString(block, position);
                position += username.length() + 1;
                String address = readString(block, position);
                position += address.length() + 1;
                String timestamp = readString(block, position);

                Main.instance.onVoteReceived(new Vote(serviceName, username, address, timestamp, System.currentTimeMillis()));
            } catch (BadPaddingException ex) {
                Main.instance.getLogger().error("Couldn't decrypt vote, make sure public keys match", ex);
            } catch (Exception ex) {
                if (running) {
                    Main.instance.getLogger().error("Couldn't receive vote", ex);
                }
            }
        }
    }

    private static String readString(byte[] data, int offset) {
        StringBuilder builder = new StringBuilder();

        for (int i = offset; i < data.length; i++) {
            if (data[i] == '\n') {
                break;
            }

            builder.append((char) data[i]);
        }

        return builder.toString();
    }
}
