/*
AUTHOR: SIMONAS JAUNIUS URBUTIS (INITIAL TEMPLATE FROM PROFESSOR)
PROJECT: SOLVE LAPLACE'S HEAT DISTRIBUTION HEAT EQUATION WITH JACOBI ITERATION IN A MULTITHREADED MANNER USING THE MPI INTERFACE TO ESTABLISH A MASTER-SLAVE RELATION BETWEEN THE THREADS
DATE: 2025/05
*/

#include <mpi.h>
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <math.h>

#define WORKTAG    1
#define DIETAG     2
#define SEED 42
float TOLERANCE = 0.01;
int MAX_ITER = 10;
int N = 10;
int DEBUG = 0;

void master();
void slave();
void initialize_matrix(double A[N][N]);
void print_matrix(double A[N][N]);
void assign_worker_row_ranges(int num_workers, int start_rows[], int end_rows[]);
double compute_max_difference(int N, double A[N][N], double B[N][N]);


int main(int argc, char *argv[])
{
	int myrank;

	for (int i = 1; i < argc; i++) {
		if (strcmp(argv[i], "--iterations") == 0 && i + 1 < argc) {
			MAX_ITER = atoi(argv[++i]);
		} else if (strcmp(argv[i], "--size") == 0 && i + 1 < argc) {
			N = atoi(argv[++i]);
		} else if (strcmp(argv[i], "--tolerance") == 0 && i + 1 < argc) {
			TOLERANCE = atof(argv[++i]);
		} else if (strcmp(argv[i], "--debug") == 0) {
			DEBUG = 1;
		}
	}
		
	MPI_Init(&argc, &argv);   		/* initialize MPI */
	MPI_Comm_rank(MPI_COMM_WORLD,   /* always use this */
					&myrank);      	/* process rank, 0 thru N-1 */

	if (myrank == 0) {
		master();
	} else {
		slave();
	}
	MPI_Finalize();       /* cleanup MPI */
}

void master() {
	double start_time = MPI_Wtime();
    double currentMatrix[N][N], nextMatrix[N][N];
    double total_comm_time = 0.0;
	int converged = 0;
    
	printf("***CONFIGURATION OF THE PROGRAM***\n");
	printf("ITERATION CAP: %d \n", MAX_ITER);
	printf("MATRIX SIZE: %d \n", N);
	printf("TOLERANCE: %f \n", TOLERANCE);
	printf("DEBUG MODE: %d \n", DEBUG);
	printf("********************************* \n");

    initialize_matrix(currentMatrix);
    initialize_matrix(nextMatrix);
    double global_max_diff = 0.0;
	
	if (DEBUG) printf("Starter matrix: \n");
	if (DEBUG) print_matrix(currentMatrix);

    int	ntasks, rank, work=0;
    MPI_Comm_size(MPI_COMM_WORLD, &ntasks);
    int num_workers = ntasks - 1;
    printf("TOTAL WORKERS/RANKS %d\n", ntasks);

    // TERMINATE IF THERE ARE MORE WORKERS THAN WORKABLE ROWS!
    int total_work_rows = N - 2;
    if (num_workers > total_work_rows) {
        printf("ERROR: Too many worker processes (%d) for only %d work rows.\n", num_workers, total_work_rows);
        printf("Use at most %d MPI processes (1 master + %d workers).\n", total_work_rows + 1, total_work_rows);
        MPI_Abort(MPI_COMM_WORLD, 1);
        return;
    }

    int start_rows[num_workers], end_rows[num_workers];
    assign_worker_row_ranges(num_workers, start_rows, end_rows);

	int iteration_count = 0;

    for (iteration_count = 0; iteration_count < MAX_ITER; iteration_count++) {
        double comm_start = MPI_Wtime();
        global_max_diff = 0.0;
        // Seed the slaves
        for (int rank = 1; rank < ntasks; rank++) {
            int start = start_rows[rank - 1];
            int end = end_rows[rank - 1];
            int rows_to_send = end - start + 3;

            MPI_Send(&start, 1, MPI_INT, rank, WORKTAG, MPI_COMM_WORLD);
			if (DEBUG) printf("*Master(1): [1] Procesui %d issiustas duomuo %d\n", rank, start);
            MPI_Send(&end, 1, MPI_INT, rank, WORKTAG, MPI_COMM_WORLD);
			if (DEBUG) printf("*Master(1): [2] Procesui %d issiustas duomuo %d\n", rank, end);
            MPI_Send(&currentMatrix[start - 1][0], rows_to_send * N, MPI_DOUBLE, rank, WORKTAG, MPI_COMM_WORLD);
			if (DEBUG) printf("*Master(1): [3] Procesui %d issiusta matricos dalis su %d eiluciu ir %d stulpeliu, pirma reikšmė - %f \n", rank, rows_to_send, N, currentMatrix[start - 1][0]);
        }

        // Receive updated rows from each worker
        for (int rank = 1; rank < ntasks; rank++) {
            int start = start_rows[rank - 1];
            int end = end_rows[rank - 1];
            int count = end - start + 1;
            MPI_Recv(&nextMatrix[start][0], count * N, MPI_DOUBLE, rank, MPI_ANY_TAG, MPI_COMM_WORLD, MPI_STATUS_IGNORE);
        }

		// Receive local max differences
		for (int rank = 1; rank < ntasks; rank++) {
			double local_diff;
			MPI_Recv(&local_diff, 1, MPI_DOUBLE, rank, 0, MPI_COMM_WORLD, MPI_STATUS_IGNORE);
			if (local_diff > global_max_diff) {
				global_max_diff = local_diff;
			}
		}

        double comm_end = MPI_Wtime();
        total_comm_time += (comm_end - comm_start);

		// check convergence
		if (global_max_diff < TOLERANCE) {
            printf("Converged after %d iterations with max diff = %.20f\n", iteration_count, global_max_diff);
			converged = 1;
			break;
		}   
        memcpy(currentMatrix, nextMatrix, sizeof(currentMatrix));
    }

    // Tell all the slaves to exit.
    for (int rank = 1; rank < ntasks; rank++) {
        MPI_Send(0, 0, MPI_INT, rank, DIETAG, MPI_COMM_WORLD);
		if (DEBUG) printf("*Master(5): sustabdyti procesa %d\n", rank);
    }

	if (!converged){
		printf("Did not converge and stopped at max iterations - %d with max diff = %.20f\n", MAX_ITER, global_max_diff);
	}

	printf("***RESULTS***\n");
	double end_time = MPI_Wtime();
	printf("Iteration count %d \n", iteration_count + 1);
    printf("***TIME***\n");
    printf("Total Execution time: %.6f seconds\n", end_time - start_time);
    printf("Total communication time: %.6f seconds\n", total_comm_time);
    printf("Calculations time: %.6f\n", ((end_time - start_time) - total_comm_time));
    printf("Average communication time per iteration: %.6f seconds\n", total_comm_time / (iteration_count + 1));

    
	if (DEBUG) printf("Final matrix after %d iterations:\n", MAX_ITER);
    if (DEBUG) print_matrix(currentMatrix);
}

void slave() {
    int start, end;
    double buffer[N][N];       // receive working rows + 2 extra
    double updated_rows[N][N]; // only inner rows updated

    MPI_Status status;

    while (1) {
        MPI_Recv(&start, 1, MPI_INT, 0, MPI_ANY_TAG, MPI_COMM_WORLD, &status);
		if (DEBUG) printf("Slave(2.1): atsiustas duomuo %d\n", end);
        if (status.MPI_TAG == DIETAG) {
			if (DEBUG) printf("Slave(1): atsiusta baigmes zyme\n");
            break;
        }

        MPI_Recv(&end, 1, MPI_INT, 0, WORKTAG, MPI_COMM_WORLD, MPI_STATUS_IGNORE);
		if (DEBUG) printf("Slave(2.2): atsiustas duomuo %d\n", end);


        int rows_received = end - start + 3;
        MPI_Recv(&buffer[0][0], rows_received * N, MPI_DOUBLE, 0, WORKTAG, MPI_COMM_WORLD, MPI_STATUS_IGNORE);
		if (DEBUG) printf("Slave(2.3): atsiustas duomuo %d\n", end);
		
		double local_max_diff = 0.0;

        // Perform Jacobi update for assigned rows
        for (int i = 1; i <= end - start + 1; i++) {
            updated_rows[i - 1][0] = buffer[i][0];
            updated_rows[i - 1][N - 1] = buffer[i][N - 1];

            for (int j = 1; j < N - 1; j++) {
                double new_val = 0.25 * (
                    buffer[i - 1][j] + buffer[i + 1][j] +
                    buffer[i][j - 1] + buffer[i][j + 1]
                );
                double diff = fabs(new_val - buffer[i][j]);
                if (diff > local_max_diff) {
                    local_max_diff = diff;
                }
                updated_rows[i - 1][j] = new_val;
            }
        }

        int count = end - start + 1;
        MPI_Send(&updated_rows[0][0], count * N, MPI_DOUBLE, 0, 0, MPI_COMM_WORLD);
		if (DEBUG) printf("Slave(3.1): issiustas rezultatas - matricos dalis su %d eiluciu ir %d stulpeliu, pirma reikšmė - %f \n", count, N, updated_rows[0][0]);

		MPI_Send(&local_max_diff, 1, MPI_DOUBLE, 0, 0, MPI_COMM_WORLD);
	
		if (DEBUG) printf("Slave(3.2): issiustas rezultatas, maksimalus skirtumas - %f \n", local_max_diff);

	}
}

void initialize_matrix(double A[N][N]) {
    srand(SEED); // fixed seed for reproducibility

    for (int i = 0; i < N; i++) {
        for (int j = 0; j < N; j++) {
            if (i == 0) {
                A[i][j] = 100.0;       // Top boundary -> "Hot Ceiling"
            } else if (i == N - 1) {
                A[i][j] = 0.0;         // Bottom boundary -> "Cold floor"
            } else if (j == 0 || j == N - 1) {
                A[i][j] = 50.0;        // Left & right boundaries -> "Semi-hot air"
            } else {
                A[i][j] = (double)(rand() % 100); // Inside temperatures random 0–99
            }
        }
    }
}

void print_matrix(double A[N][N]) {
    for (int i = 0; i < N; i++) {
        for (int j = 0; j < N; j++) {
            printf("%6.1f ", A[i][j]);
        }
        printf("\n");
    }
}

void assign_worker_row_ranges(int num_workers, int start_rows[], int end_rows[]) {
    int total_work_rows = N - 2; // exclude rows 0 N-1
    int rows_per_worker = total_work_rows / num_workers;

    int current_row = 1; // start from row 1 (skip top boundary)

    for (int i = 0; i < num_workers; i++) {
        start_rows[i] = current_row;
        // If last worker, go to N-2 (bottom boundary excluded)
        if (i == num_workers - 1) {
            end_rows[i] = N - 2;
        } else {
            end_rows[i] = current_row + rows_per_worker - 1;
        }
        current_row = end_rows[i] + 1;
    }
}
double compute_max_difference(int N, double A[N][N], double B[N][N]) {
    double max_diff = 0.0;
    for (int i = 1; i < N - 1; i++) {
        for (int j = 1; j < N - 1; j++) {
            double diff = fabs(A[i][j] - B[i][j]);
            if (diff > max_diff) {
                max_diff = diff;
            }
        }
    }
    return max_diff;
}
