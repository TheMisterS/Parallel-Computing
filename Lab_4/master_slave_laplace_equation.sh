#!/bin/bash
#SBATCH -p main # queue name
#SBATCH -n4 # number of processes allocated - up tp 256
module load openmpi
mpicc -o master_slave_laplace_equation master_slave_laplace_equation.c #compile
mpirun -np 4 ./master_slave_laplace_equation --iterations 100000 --size 300 --tolerance 0.1