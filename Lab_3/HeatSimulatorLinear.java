/* AUTHOR: SIMONAS JAUNIUS URBUTIS
 * UNIVERSITY: VILNIUS UNIVERSITY
 * PROGRAM: INFORMATICS 3rd Year
 * PROGRAM: Linear program that implements the Laplace equation using the Jacobi method to be used in the HPC enviroment
 */
public class HeatSimulatorLinear {

    static int matrixSize = 100;
    static double epsilon = 0.01;
    static boolean debug = false;

    static double[][] currentHeatMatrix;
    static double[][] nextHeatMatrix;

    public static void main(String[] args) {
        parseArgs(args);
        currentHeatMatrix = new double[matrixSize][matrixSize];
        nextHeatMatrix = new double[matrixSize][matrixSize];
        System.out.println("Linear algorithm started with these parameters:");
        System.out.println("matrixSize: " + matrixSize);
        System.out.println("Epsilon maxDelta boundary: " + epsilon);
        System.out.println("Debug mode: " + debug);
        initializeMatrix();

        int iterationCount = 0;
        double maxDelta;

        long start = System.nanoTime();

        do {
            maxDelta = 0;

            for (int i = 1; i < matrixSize - 1; i++) {
                for (int j = 1; j < matrixSize - 1; j++) {
                    nextHeatMatrix[i][j] = (
                        currentHeatMatrix[i + 1][j] +
                        currentHeatMatrix[i - 1][j] +
                        currentHeatMatrix[i][j + 1] +
                        currentHeatMatrix[i][j - 1]
                    ) / 4.0;

                    double delta = Math.abs(nextHeatMatrix[i][j] - currentHeatMatrix[i][j]);
                    if (delta > maxDelta) maxDelta = delta;
                }
            }

            iterationCount++;
            if (debug && (iterationCount % 100 == 0 || iterationCount == 1)) {
                System.out.printf("Iteration %d: Max delta = %.6f\n", iterationCount, maxDelta);
            }

            // Swap matrices
            double[][] temp = currentHeatMatrix;
            currentHeatMatrix = nextHeatMatrix;
            nextHeatMatrix = temp;

        } while (maxDelta >= epsilon);

        long end = System.nanoTime();
        System.out.printf("Converged at iteration %d: Max delta = %.6f\n", iterationCount, maxDelta);
        System.out.printf("Completed in %.3f s\n", (end - start) / 1e9);

        if (debug) {
            printMatrix();
        }
    }

    static void parseArgs(String[] args) {
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--matrixSize":
                    matrixSize = Integer.parseInt(args[++i]);
                    break;
                case "--epsilon":
                    epsilon = Double.parseDouble(args[++i]);
                    break;
                case "--debug":
                    debug = true;
                    break;
            }
        }
    }

    static void initializeMatrix() {
        java.util.Random rand = new java.util.Random(42);

        for (int i = 0; i < matrixSize; i++) {
            for (int j = 0; j < matrixSize; j++) {
                if (i == 0 || i == matrixSize - 1 || j == 0 || j == matrixSize - 1) {
                    currentHeatMatrix[i][j] = rand.nextDouble() * 100;
                    nextHeatMatrix[i][j] = currentHeatMatrix[i][j];
                } else {
                    currentHeatMatrix[i][j] = rand.nextDouble() * 100;
                }
            }
        }
    }

    static void printMatrix() {
        for (int i = 0; i < matrixSize; i++) {
            for (int j = 0; j < matrixSize; j++) {
                System.out.printf("%6.1f ", currentHeatMatrix[i][j]);
            }
            System.out.println();
        }
    }
}
