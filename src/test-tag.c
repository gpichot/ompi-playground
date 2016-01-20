
#include <mpi.h>
#include <stdio.h>
#include <stdlib.h>
#include <omp.h>

int main(int argc, char** argv) {
  // Initialize the MPI environment
  int provided;
  MPI_Init_thread(NULL, NULL, MPI_THREAD_MULTIPLE, &provided);
  // Find out rank, size
  int world_rank;
  MPI_Comm_rank(MPI_COMM_WORLD, &world_rank);
  int world_size;
  MPI_Comm_size(MPI_COMM_WORLD, &world_size);


  if (world_size < 2) {
    fprintf(stderr, "World size must be greater than 1 for %s\n", argv[0]);
    MPI_Abort(MPI_COMM_WORLD, 1);
  }

  #pragma omp parallel
  {

    int number;

    #pragma omp sections
    {
      #pragma omp section
      {
        if (world_rank == 0) {
          for(int i=0;i<12;i++){
            number = i;
            MPI_Send(&number, 1, MPI_INT, 1, i, MPI_COMM_WORLD);
            printf("Process 0 sent number %d to process 1 with tag %d \n", number, i);

          }
        }
      }
        #pragma omp section
        {
          if (world_rank == 1){
          for(int i=0;i<12;i+=2){
            MPI_Recv(&number, 1, MPI_INT, 0, i, MPI_COMM_WORLD, MPI_STATUS_IGNORE);
            //int[] resulttab;
            printf("Process 1 received number %d from process 0 with tag %d \n", number, i);
          }
        }
      }
    }
  }
  MPI_Finalize();

}
