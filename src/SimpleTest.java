import mpi.*;



public class SimpleTest {

	public static void main(String[] args) throws MPIException {
		// TODO Auto-generated method stub
	
		        MPI.Init(args);

		        int me = MPI.COMM_WORLD.getRank();
		        int nb = MPI.COMM_WORLD.getSize();

		        System.out.println("Hello from " + me + ".");
		        
		        MPI.COMM_WORLD.barrier();

		        MPI.Finalize();


	}

}
