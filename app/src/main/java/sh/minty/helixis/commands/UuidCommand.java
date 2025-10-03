package sh.minty.helixis.commands;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.security.SecureRandom;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.function.BiFunction;
import java.util.logging.Level;
import java.util.logging.Logger;

@Command(name = "uuid", mixinStandardHelpOptions = true, description = "Generate an UUID")
public class UuidCommand implements Callable<Integer> {
    private static final BiFunction<UUID, String, UUID> UUID_V3_FN = UuidFactory::uuidV3;
    private static final BiFunction<UUID, String, UUID> UUID_V5_FN = UuidFactory::uuidV5;

    private static final Logger LOGGER = Logger.getLogger(UuidCommand.class.getName());
    private Random random = new Random();

    @Option(names = {"-n", "--number"}, description = "generate x uuids (default: 1)")
    private int amount = 1;

    @Option(names = {"--version"}, description = "uuid version (default: 4)")
    private int version = 4;

    @Option(names = {"-s", "--secure"}, description = "use secure random (default: false)")
    private boolean useSecureRandom = false;

    // TODO: make these two options depend on version == 2
    @Option(names = {"-l", "--local"}, description = "local identifier for UUIDv2 (default: none)")
    private int localIdentifier = random.nextInt();

    @Option(names = {"-d", "--domain"}, description = "local domain for UUIDv2 (default: none)")
    private int localDomain = random.nextInt(0, 256);

    // TODO: make these two options depend on version == 3/5
    @Option(names = {"-n", "--namespace"}, description = "namespace for UUIDv3/v5")
    private UUID namespace;

    @Option(names = {"-n", "--name"}, description = "name for UUIDv3/v5")
    private String name;

    @Override
    public Integer call() {
        UUID uuid;

        if (useSecureRandom) {
            random = new SecureRandom();
        }

        var data = new byte[16];
        switch (version) {
            case 1 -> uuid = UuidFactory.uuidV1();
            case 2 -> uuid = UuidFactory.uuidV2(localIdentifier, localDomain);
            case 3, 5 -> {
                // choose function by mapping: compute index 0 for v3, 1 for v5 without branching
                // version 3 -> 0, version 5 -> 1
                int idx = (version - 3) / 2;
                var fn = (idx == 0) ? UUID_V3_FN : UUID_V5_FN;
                uuid = fn.apply(namespace, name);
            }
            case 4 -> {
                random.nextBytes(data);
                uuid = UUID.nameUUIDFromBytes(data);
            }
            default -> uuid = UUID.randomUUID();
        }

        for (int i = 0; i < amount; i++) {
            LOGGER.log(Level.INFO, uuid.toString());
        }
        return 0;
    }
}
