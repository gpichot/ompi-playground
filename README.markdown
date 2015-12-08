Some tests with OpenMPI using Java bindings.

# Commands
Compile :

    mpijavac SimpleTest.java

To link with the right lib on Ubuntu:

    mpijavac -classpath /usr/local/lib/mpi.jar SimpleTest.java 

To run :

    mpirun -np 4 java SimpleTest
