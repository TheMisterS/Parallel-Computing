/* AUTHOR: SIMONAS JAUNIUS URBUTIS
 * UNIVERSITY: VILNIUS UNIVERSITY
 * PROGRAM: INFORMATICS 3rd Year
 * PROGRAM: Multi-threaded program that implements the Laplace equation using the Jacobi method to be used in the HPC enviroment
 */
public class HeatSimulator {

    //Set the default parameters
    static int matrixSize = 100;
    static int threadCount = 12;
    static double epsilon = 0.01;
    static boolean debug = false;

    static double[][] currentHeatMatrix;
    static double[][] nextHeatMatrix;
    static volatile boolean converged = false;

    static Worker[] workers;
    static final Object iterationLock = new Object();
    static volatile boolean proceedToNextIteration = false;




    public static void main(String[] args) {
        parseArgs(args);
        currentHeatMatrix = new double[matrixSize][matrixSize];
        nextHeatMatrix = new double[matrixSize][matrixSize];
        
        //Matrix size - 2, has to be bigger than the thread count for correct distribution accross threads
        if ((matrixSize - 2) < threadCount) {
            System.err.println("Error: Too many threads for matrix size.");
            System.exit(1);
        }
        System.out.println("--------------------------------------------------");
        System.out.println("Multithreaded algorithm started with these parameters:");
        System.out.println("matrixSize: " + matrixSize);
        System.out.println("threadCount: " + threadCount);
        System.out.println("Epsilon maxDelta boundary: " + epsilon);
        System.out.println("Debug mode: " + debug);
        System.out.println("--------------------------------------------------");


        initializeMatrix();

        // if(debug){
        //     System.out.println("Starting matrix: ");
        //     printMatrix();
        // }

        workers = new Worker[threadCount];
        Thread[] threads = new Thread[threadCount];

        //Dynamic calculation of the thread grain(rows to be calculated) = size of the heat matrix / thread count
        int rowsPerThread = (matrixSize - 2) / threadCount; 
        if(debug){
            System.out.println("Calculated grain size: " + rowsPerThread + " rows");
        }

        long start = System.nanoTime();

        // Initialize and start threads
        for (int i = 0; i < threadCount; i++) {
            //remove first row as it is static
            int startRow = 1 + i * rowsPerThread;

            //last thread picks up any remaining rows
            int endRow;
            if (i == threadCount - 1) {
                endRow = matrixSize - 2;
            } else {
                endRow = startRow + rowsPerThread - 1;
            }
            workers[i] = new Worker(startRow, endRow);
            threads[i] = new Thread(workers[i]);
            threads[i].start();
        }
        int iterationCount = 0;
        while (!converged) {
            iterationCount++;

            //Wait for all workers to finish their work and reset next iteration flag
            waitForWorkers();
            proceedToNextIteration = false;


            //Compute max delta  of each iteration by comparing global max Delta to local delta's    
            double maxDelta = 0;
            for (Worker w : workers) {
                if (w.localMaxDelta > maxDelta){
                     maxDelta = w.localMaxDelta;
                }
            }

            if ((debug && iterationCount % 100 == 0) || (debug && iterationCount == 1)) {
                System.out.printf("Iteration %d: Max delta = %.6f\n", iterationCount, maxDelta);
            }

            //Check if convergence was reached
            if (maxDelta < epsilon) {
                converged = true;
                System.out.printf("Converged at iteration %d: Max delta = %.6f\n", iterationCount, maxDelta);
                synchronized (iterationLock) {
                    iterationLock.notifyAll(); // Wake up all remaining workers
                }
            }

            //Swap arrays
            double[][] temp = currentHeatMatrix;
            currentHeatMatrix = nextHeatMatrix;
            nextHeatMatrix = temp;

            //Reset workers for next iteration
            for (Worker w : workers) {
                w.reset();
            }
            
            proceedToNextIteration = true;
            synchronized (iterationLock) {
                iterationLock.notifyAll();
            }
            
        }

        //Cleanup
        for (Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException ignored) {}
        }

        long end = System.nanoTime();
        if (!debug) {
            System.out.printf("Completed in %.3f s\n", (end - start) / 1e9);
        } else {
            // printMatrix();
            System.out.printf("Completed in %.3f s\n", (end - start) / 1e9);
        }
    }

    static void waitForWorkers() {
        for (Worker w : workers) {
            synchronized (w.lock) {
                while (!w.done) {
                    try {
                        w.lock.wait();
                    } catch (InterruptedException ignored) {}
                }
            }
        }
    }

    static void parseArgs(String[] args) {
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--threads":
                    threadCount = Integer.parseInt(args[++i]);
                    break;
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
                //Outside-cells get value 100
                if (i == 0 || i == matrixSize - 1 || j == 0 || j == matrixSize - 1) {
                    currentHeatMatrix[i][j] = rand.nextDouble() * 100;
                    nextHeatMatrix[i][j] = currentHeatMatrix[i][j];
                } else {
                    // Inside-cells get random values between 0 and 100
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

    static class Worker implements Runnable {
        int startRow, endRow;
        double localMaxDelta = 0;
        boolean done = false;
        final Object lock = new Object();

        Worker(int startRow, int endRow) {
            this.startRow = startRow;
            this.endRow = endRow;
        }

        void reset() {
            done = false;
        }
        
        public void run() {
            while (!converged) {
                double localMax = 0;
                for (int i = startRow; i <= endRow; i++) {
                    for (int j = 1; j < matrixSize - 1; j++) {
                        nextHeatMatrix[i][j] = (currentHeatMatrix[i + 1][j] + currentHeatMatrix[i - 1][j] +
                                      currentHeatMatrix[i][j + 1] + currentHeatMatrix[i][j - 1]) / 4.0;
                        double delta = Math.abs(nextHeatMatrix[i][j] - currentHeatMatrix[i][j]);
                        if (delta > localMax) localMax = delta;
                    }
                }
                localMaxDelta = localMax;
                
                //Notify main thread that this Worker thread finished
                synchronized (lock) {
                    done = true;
                    lock.notify();
                }
                
                //Wait for main thread to reset
                synchronized (iterationLock) {
                    while (!proceedToNextIteration && !converged) {
                        try {
                            iterationLock.wait();
                        } catch (InterruptedException ignored) {}
                    }
                }
            }
        }
    }
}
