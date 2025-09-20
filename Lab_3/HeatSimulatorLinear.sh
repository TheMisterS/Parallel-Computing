#!/bin/bash
#SBATCH -p
#SBATCH -N1
#SBATCH -c1
javac HeatSimulatorLinear.java #compile
java HeatSimulatorLinear --matrixSize 100 --epsilon 0.01