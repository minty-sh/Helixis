package sh.minty.helixis.commands;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.Random;
import java.util.concurrent.Callable;

@Command(name = "color", mixinStandardHelpOptions = true, description = "Converts RGB values to hexadecimal values and finds similar colors.")
public class ColorCommand implements Callable<Integer> {
    private Random random = new Random();

    @Parameters(index = "0", description = "Red value (0-255)")
    private int red;

    @Parameters(index = "1", description = "Green value (0-255)")
    private int green;

    @Parameters(index = "2", description = "Blue value (0-255)")
    private int blue;

    @Option(names = "-s", paramLabel = "NUMBER", description = "Get similar colors")
    private int similarColors;

    @Override
    public Integer call() {
        if (red > 255 || green > 255 || blue > 255 || red < 0 || green < 0 || blue < 0) {
            System.err.println("ERROR: One or more RGB values are outside the valid range (0-255).");
            return 1;
        }

        String hexValue = rgbToHex(red, green, blue);
        System.out.printf("RGB values (%d, %d, %d) are equivalent to hex value: %s%n", red, green, blue, hexValue);

        if (similarColors != 0) {
            var colors = getSimilarColors(red, green, blue, similarColors);
            for (var color : colors) {
                System.out.println(color);
            }   
        }

        return 0;
    }

    private String rgbToHex(int r, int g, int b) {
        return String.format("#%02x%02x%02x", r, g, b);
    }

    /**
     * Generates {@code amount} colours that are similar to the supplied RGB colour.
     *
     * @param r      red component (0‑255)
     * @param g      green component (0‑255)
     * @param b      blue component (0‑255)
     * @param amount how many similar colours to generate
     * @return an array containing the hex strings of the generated colours
     */
    private String[] getSimilarColors(int r, int g, int b, int amount) {
        var similarColors = new String[amount];
        // Base color as an array so we can loop over the three channels.
        final int[] base = { r, g, b };

        // standard deviation for the Gaussian noise
        final double sigma = 30.0;
        final int MIN = 0, MAX = 255;
    
        for (int i = 0; i < amount; i++) {
            var newRgb = new int[3];
    
            // Apply Gaussian noise to each channel and clamp to [0,255].
            for (int c = 0; c < 3; c++) {
                double noisy = base[c] + random.nextGaussian() * sigma;
                newRgb[c] = (int) Math.min(MAX, Math.max(MIN, noisy));
            }
    
            similarColors[i] = rgbToHex(newRgb[0], newRgb[1], newRgb[2]);
        }
    
        return similarColors;
    }
}
