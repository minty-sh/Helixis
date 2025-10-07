package sh.minty.helixis.commands;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Utilities for RFC 4122 UUID versions 1, 2, 3 and 5.
 *
 * Note on v2: Java doesn't provide a cross-platform UID/GID API. This implementation
 * requires you to provide the 32-bit local identifier and an 8-bit domain (0-255).
 */
public final class UuidFactory {
    private static final long UUID_EPOCH_OFFSET = 0x01B21DD213814000L; // 122192928000000000L
    private static final SecureRandom secureRandom = new SecureRandom();

    // State for v1/v2 generation:
    private static long lastTimestamp;
    private static int clockSequence; // 14-bit
    private static final Object lock = new Object();
    private static final long nodeIdentifier = createNodeIdentifier();

    static {
        // initialize clockSequence randomly 14-bit
        clockSequence = secureRandom.nextInt() & 0x3FFF;
    }

    private UuidFactory() {}

    /**
     * Generate a version-1 (time-based) UUID.

     * @return java.util.UUID the generated UUID from the current time
     */
    public static UUID uuidV1() {
        return generateV1(null, -1, false);
    }

    /**
     * Generate a version-2 (DCE security) UUID.

     * @param localIdentifier 32-bit local identifier (e.g. UID or GID)
     * @param localDomain 8-bit local domain (0..255). Typical DCE domains: 0=user,1=group,2=org
     * @return java.util.UUID
     */
    public static UUID uuidV2(int localIdentifier, int localDomain) {
        if (localDomain < 0 || localDomain > 0xFF) {
            throw new IllegalArgumentException("localDomain must be 0..255");
        }
        return generateV1(localIdentifier, localDomain, true);
    }

    /**
     * Generate a version-3 (name-based, MD5) UUID.

     * @param namespace namespace UUID
     * @param name name string (UTF-8)
     */
    public static UUID uuidV3(UUID namespace, String name) {
        return nameBased(namespace, name, "MD5", 3);
    }

    /**
     * Generate a version-5 (name-based, SHA-1) UUID.

     * @param namespace namespace UUID
     * @param name name string (UTF-8)
     */
    public static UUID uuidV5(UUID namespace, String name) {
        return nameBased(namespace, name, "SHA-1", 5);
    }

    private static UUID generateV1(Integer maybeLocalIdentifier, int localDomain, boolean isV2) {
        long timestamp100ns;
        int clkSeq;

        synchronized (lock) {
            long nowMillis = System.currentTimeMillis();
            // try to add nanosecond precision using nanoTime, not perfect but helps uniqueness within same ms
            long nanosPart = (System.nanoTime() % 10000L); // remainder in nanoseconds - best-effort
            timestamp100ns = nowMillis * 10000L + nanosPart + UUID_EPOCH_OFFSET;

            if (timestamp100ns <= lastTimestamp) {
                // clock moved backwards or same timestamp - increment clockSequence
                clockSequence = (clockSequence + 1) & 0x3FFF; // keep 14 bits
            }
            lastTimestamp = timestamp100ns;
            clkSeq = clockSequence;
        }

        // split timestamp into fields
        long timeLow = timestamp100ns & 0xFFFFFFFFL;
        long timeMid = (timestamp100ns >>> 32) & 0xFFFFL;
        long timeHi = (timestamp100ns >>> 48) & 0x0FFFL; // 12 bits for time_hi
        // apply version (1)
        timeHi = timeHi | (1 << 12); // version 1 (or we'll overwrite for v2 later if needed)

        // If v2: replace time_low with localIdentifier (32-bit) and set version to 2
        if (isV2 && maybeLocalIdentifier != null) {
            timeLow = ((long) maybeLocalIdentifier) & 0xFFFFFFFFL;
            timeHi = (timeHi & 0x0FFF) | (2 << 12); // set version to 2
        }

        // clock seq: 14 bits. For v2, clock_seq_low low byte is replaced by domain (8-bit)
        int clockSeqLow = clkSeq & 0xFF;
        int clockSeqHi = (clkSeq >>> 8) & 0x3F; // 6 bits (top 2 bits used for variant later)

        if (isV2) {
            // replace low byte with domain
            clockSeqLow = localDomain & 0xFF;
        }

        // Build MSB and LSB
        long msb = 0L;
        msb |= (timeLow & 0xFFFFFFFFL) << 32;
        msb |= (timeMid & 0xFFFFL) << 16;
        msb |= (timeHi & 0xFFFFL);

        long lsb = 0L;
        // clock_seq_hi_and_reserved: top two bits are variant (10), next 6 are clockSeqHi
        int clockSeqHiAndReserved = (clockSeqHi & 0x3F) | 0x80; // set variant bits 10xxxxxx (0x80)
        lsb |= ((long) clockSeqHiAndReserved & 0xFFL) << 56;
        lsb |= ((long) clockSeqLow & 0xFFL) << 48;
        lsb |= (nodeIdentifier & 0xFFFFFFFFFFFFL);

        return new UUID(msb, lsb);
    }

    private static UUID nameBased(UUID namespace, String name, String algorithm, int version) {
        try {
            var md = MessageDigest.getInstance(algorithm);
            // namespace as bytes (network byte order)
            md.update(uuidToBytes(namespace));
            byte[] nameBytes = name.getBytes(StandardCharsets.UTF_8);
            md.update(nameBytes);
            byte[] hash = md.digest();

            byte[] out = new byte[16];
            System.arraycopy(hash, 0, out, 0, 16); // for SHA-1 (20 bytes) take first 16 bytes

            // set version nibble (byte 6)
            out[6] = (byte) ((out[6] & 0x0F) | (version << 4));
            // set variant (byte 8): 10xxxxxx
            out[8] = (byte) ((out[8] & 0x3F) | 0x80);

            long msb = bytesToLong(out, 0);
            long lsb = bytesToLong(out, 8);
            return new UUID(msb, lsb);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Hash algorithm not available: " + algorithm, e);
        }
    }

    // convert UUID to 16-byte array (big-endian, network order)
    private static byte[] uuidToBytes(UUID uuid) {
        byte[] bytes = new byte[16];
        long msb = uuid.getMostSignificantBits();
        long lsb = uuid.getLeastSignificantBits();
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        bb.order(ByteOrder.BIG_ENDIAN);
        bb.putLong(msb);
        bb.putLong(lsb);
        return bytes;
    }

    private static long bytesToLong(byte[] b, int offset) {
        long value = 0;
        for (int i = 0; i < 8; i++) {
            value = (value << 8) | (b[offset + i] & 0xFFL);
        }
        return value;
    }

    private static boolean isValidNetworkInterface(NetworkInterface ni) throws SocketException {
        return ni.isUp() && !ni.isLoopback() && !ni.isVirtual();
    }

    private static long getMacAddressAsLong(NetworkInterface ni) throws SocketException {
        byte[] mac = ni.getHardwareAddress();
        if (mac != null && mac.length == 6) {
            long node = 0;
            for (int i = 0; i < 6; i++) {
                node = (node << 8) | (mac[i] & 0xFF);
            }
            return node;
        }
        return -1; // Indicate that no valid MAC address was found
    }

    // Try to obtain a MAC address; if none, generate a random 48-bit number and set multicast bit.
    private static long createNodeIdentifier() {
        try {
            var ifs = NetworkInterface.getNetworkInterfaces();
            while (ifs.hasMoreElements()) {
                NetworkInterface ni = ifs.nextElement();
                try {
                    if (isValidNetworkInterface(ni)) {
                        long node = getMacAddressAsLong(ni);
                        if (node != -1) { // -1 indicates an invalid or unavailable MAC address
                            return node;
                        }
                    }
                } catch (SocketException e) { }
            }
        } catch (SocketException e) { }

        // fallback: random 48-bit with multicast bit set (per RFC)
        long node = ThreadLocalRandom.current().nextLong() & 0x0000FFFFFFFFFFFFL;
        node = node | 0x010000000000L; // set multicast bit to indicate random node id
        return node;
    }
}
