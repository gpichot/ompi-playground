Some tests with OpenMPI using Java bindings.

# Commands

## OS X
Compile :

    mpijavac SimpleTest.java

To run :

    mpirun -np 4 java SimpleTest

## Linux (debian)
Add to your ~/.zshrc or ~/.bashrc (if not already set)

    export PATH=/usr/local/bin/:$PATH

To link with the right lib on Ubuntu:

    mpijavac -classpath /usr/local/lib/mpi.jar SimpleTest.java 

Run :

    mpirun -np 8 java -classpath .:/usr/local/lib/mpi.jar -Djava.library.path=/usr/local/lib/ SimpleTest
