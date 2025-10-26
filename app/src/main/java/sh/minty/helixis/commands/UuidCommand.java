package sh.minty.helixis.commands;

import java.security.SecureRandom;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.function.BiFunction;
import java.util.logging.Level;
import java.util.logging.Logger;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "uuid", mixinStandardHelpOptions = true, description = "Generate an UUID")
public class UuidCommand implements Callable<Integer> {
    private static final BiFunction<UUID, String, UUID> UUID_V3_FN = UuidFactory::uuidV3;
    private static final BiFunction<UUID, String, UUID> UUID_V5_FN = UuidFactory::uuidV5;

    private static final Logger LOGGER = Logger.getLogger(UuidCommand.class.getName());
    private Random random = new Random();

    @Option(names = {"-n", "--number"}, description = "generate x uuids (default: 1)")
    private int amount = 1;

    @Option(names = {"-V", "--uuid-version"}, description = "uuid version (default: 4)")
    private int version = 4;

    @Option(names = {"-s", "--secure"}, description = "use secure random (default: false)")
    private boolean useSecureRandom = false;

    @ArgGroup(exclusive = false, multiplicity = "0..1")
    Version2Options version2Options;

    static class Version2Options {
        @Option(names = {"-l", "--local"}, description = "local identifier for UUIDv2 (default: none)", required = true)
        int localIdentifier;

        @Option(names = {"-d",
                "--domain"}, description = "local domain for UUIDv2 (0-255). Typical DCE domains: 0=user,1=group,2=org", required = true)
        int localDomain;
    }

    @ArgGroup(exclusive = false, multiplicity = "0..1")
    Version3Or5Options version3Or5Options;

    static class Version3Or5Options {
        @Option(names = {"-ns", "--namespace"}, description = "namespace for UUIDv3/v5", required = true)
        UUID namespace;

        @Option(names = {"-N", "--name"}, description = "name for UUIDv3/v5", required = true)
        String name;
    }

    @Override
    public Integer call() {
        UUID uuid;

        if (useSecureRandom) {
            random = new SecureRandom();
        }

        var data = new byte[16];
        switch (version) {
            case 1 -> uuid = UuidFactory.uuidV1();
            case 2 -> {
                if (version2Options == null) {
                    LOGGER.log(Level.SEVERE, "For UUIDv2, --local and --domain are required.");
                    return 1;
                }
                uuid = UuidFactory.uuidV2(version2Options.localIdentifier, version2Options.localDomain);
            }
            case 3, 5 -> {
                if (version3Or5Options == null || version3Or5Options.namespace == null
                        || version3Or5Options.name == null) {
                    LOGGER.log(Level.SEVERE, "For UUIDv" + version + ", --namespace and --name are required.");
                    return 1;
                }
                // choose function by mapping: compute index 0 for v3, 1 for v5 without
                // branching
                // version 3 -> 0, version 5 -> 1
                int idx = (version - 3) / 2;
                var fn = (idx == 0) ? UUID_V3_FN : UUID_V5_FN;
                uuid = fn.apply(version3Or5Options.namespace, version3Or5Options.name);
            }
            case 4 -> {
                random.nextBytes(data);

                // set version 4 bits
                data[6] &= 0x0f; // clear version
                data[6] |= 0x40; // set version 4
                data[8] &= 0x3f; // clear variant
                data[8] |= 0x80; // set IETF variant

                long mostSigBits = 0;
                for (int i = 0; i < 8; i++) {
                    mostSigBits = (mostSigBits << 8) | (data[i] & 0xff);
                }

                long leastSigBits = 0;
                for (int i = 8; i < 16; i++) {
                    leastSigBits = (leastSigBits << 8) | (data[i] & 0xff);
                }

                uuid = new UUID(mostSigBits, leastSigBits);
            }
            default -> {
                LOGGER.log(Level.SEVERE, "Invalid UUID version: " + version);
                return 1;
            }
        }

        for (int i = 0; i < amount; i++) {
            LOGGER.log(Level.INFO, uuid.toString());
        }
        return 0;
    }
}
