
#include <mpi.h>
#include <stdio.h>
#include <stdlib.h>
#include <omp.h>

int main(int argc, char** argv) {
  // Initialize the MPI environment


  #pragma omp parallel{

    int number;

    #pragma omp sections{
      #pragma omp section{
        if (true) {
          for(int i=0;i<12;i++){
            number = i;
            printf(number);

          }
        }
      }
        #pragma omp section{
          if (true){

          for(int i=0;i<12;i+=2){
            //int[] resulttab;
            printf("coucou");
          }
        }
      }
    }
  }
}
