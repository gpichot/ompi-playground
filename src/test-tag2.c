
#include <mpi.h>
#include <stdio.h>
#include <stdlib.h>
#include <omp.h>
#define SIZE 24

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
  //initialisation des tableaux de nombres envoyés et de nombres reçus
  int size2=SIZE/2;
  int numbersent[size2];
  int numberreceived[size2];

  for(int i=0;i<size2;i++){
    numbersent[i]=-1;
    numberreceived[i]=-1;
  }

  #pragma omp parallel
  {
    int k;
    #pragma omp for private(k)
    for(k=0;k<SIZE;k++){
      int thread = omp_get_thread_num();
      printf("le nombre de thread vaut %d\n",thread);
      int i = k / 2;
      printf("%d \n",i);
      if(k%2==0){
        printf("new thread Sender %d \n", i);

        if (world_rank == 0) {
          numbersent[i] = i+1;
          MPI_Send(&numbersent[i], 1, MPI_INT, 1, i, MPI_COMM_WORLD);
          printf("Process 0 sent number %d to process 1 with tag %d \n", numbersent[i], i);
        }

      }else if(k%2==1){
        printf("new thread Receiver %d \n", i);
        if (world_rank == 1){
          MPI_Recv(&numberreceived[i], 1, MPI_INT, 0, i, MPI_COMM_WORLD, MPI_STATUS_IGNORE);
          printf("Process 1 received number %d from process 0 with tag %d \n", numberreceived[i], i);
        }
      }
    }

    #pragma omp barrier
  }

  MPI_Finalize();
if (world_rank == 0) {
  printf("On a envoyé les nombres suivants (les tags valaient le nombre)\n");
  for(int i=0;i<size2;i++){
    printf(" %d ", numbersent[i]);
  }
  printf("\n");
}

if (world_rank == 1) {

  printf("On a reçu les nombres suivants (les tags valaient le nombre)\n");
  for(int i=0;i<size2;i++){
    printf(" %d ", numberreceived[i]);
  }
  printf("\n");
}
int thread = omp_get_thread_num();
printf("%d",thread);


}
