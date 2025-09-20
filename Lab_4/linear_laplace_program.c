#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include <string.h>
#include <time.h>

#define SEED 42
float TOLERANCE = 0.01;
int MAX_ITER = 1000;
int N = 10;
int DEBUG = 0;

void initialize_matrix(double A[N][N]);
void print_matrix(double A[N][N]);
double compute_max_difference(int N, double A[N][N], double B[N][N]);

int main(int argc, char *argv[]) {
    // Parse arguments
	for (int i = 1; i < argc; i++) {
		if (strcmp(argv[i], "--iterations") == 0 && i + 1 < argc) {
			MAX_ITER = atoi(argv[++i]); // ++i advances directly
		} else if (strcmp(argv[i], "--size") == 0 && i + 1 < argc) {
			N = atoi(argv[++i]);
		} else if (strcmp(argv[i], "--tolerance") == 0 && i + 1 < argc) {
			TOLERANCE = atof(argv[++i]);
		} else if (strcmp(argv[i], "--debug") == 0) {
			DEBUG = 1;
		}
	}

    printf("***CONFIGURATION OF THE PROGRAM***\n");
	printf("ITERATION CAP: %d \n", MAX_ITER);
	printf("MATRIX SIZE: %d \n", N);
	printf("TOLERANCE: %f \n", TOLERANCE);
	printf("DEBUG MODE: %d \n", DEBUG);
	printf("********************************* \n");

    double currentMatrix[N][N], nextMatrix[N][N];
    initialize_matrix(currentMatrix);
    initialize_matrix(nextMatrix);
    double diff = 0.0;

    if (DEBUG) {
        printf("*** Initial Matrix ***\n");
        print_matrix(currentMatrix);
    }

    clock_t start = clock();

    int iteration_count = 0;
    int converged = 0;

    for (iteration_count = 0; iteration_count < MAX_ITER; iteration_count++) {
        // Jacobi update
        for (int i = 1; i < N - 1; i++) {
            // Boundary columns preserved
            nextMatrix[i][0] = currentMatrix[i][0];
            nextMatrix[i][N - 1] = currentMatrix[i][N - 1];

            for (int j = 1; j < N - 1; j++) {
                nextMatrix[i][j] = 0.25 * (
                    currentMatrix[i - 1][j] + currentMatrix[i + 1][j] +
                    currentMatrix[i][j - 1] + currentMatrix[i][j + 1]
                );
            }
        }

        diff = compute_max_difference(N, currentMatrix, nextMatrix);
        if (diff < TOLERANCE) {
            converged = 1;
            printf("Converged after %d iterations with max diff = %.20f\n", iteration_count, diff);
            break;
        }

        memcpy(currentMatrix, nextMatrix, sizeof(currentMatrix));
    }

    clock_t end = clock();
    double elapsed = (double)(end - start) / CLOCKS_PER_SEC;

    printf("*** RESULTS ***\n");
    if (!converged)
		printf("Did not converge and stopped at max iterations - %d with max diff = %.20f\n", MAX_ITER, diff);

    printf("Final iteration count: %d\n", iteration_count + 1);
    printf("Execution time: %.6f seconds\n", elapsed);

    if (DEBUG) {
        printf("Final matrix:\n");
        print_matrix(currentMatrix);
    }

    return 0;
}

void initialize_matrix(double A[N][N]) {
    srand(SEED);
    for (int i = 0; i < N; i++) {
        for (int j = 0; j < N; j++) {
            if (i == 0) {
                A[i][j] = 100.0;
            } else if (i == N - 1) {
                A[i][j] = 0.0; 
            } else if (j == 0 || j == N - 1) {
                A[i][j] = 50.0;
            } else {
                A[i][j] = (double)(rand() % 100);
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
