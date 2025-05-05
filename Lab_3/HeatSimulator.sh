#!/bin/bash
#SBATCH -p main # queue name
#SBATCH -N1  # number of computers (1 computer for OpenMP or thread processing)
#SBATCH -c16 # number of cores for 1 computer
javac HeatSimulator.java #compile
java HeatSimulator --threads 16 --matrixSize 1000 --epsilon 0.01 