import mpi.*;
import java.nio.*;
import java.util.Arrays;


public class Simulation {

    public static String bufferToString(DoubleBuffer buffer, int size) {
        double[] items = new double[size];
        for(int i = 0; i < size; i++) {
            items[i] = buffer.get(i);   
        }
        return Arrays.toString(items);
    }

    private static Master _master;

    public static void main(String[] args) {

        try {
            MPI.Init(args);

        Intracomm comm = MPI.COMM_WORLD;
        int me = comm.getRank();
        int nb = comm.getSize();

        _master = new Master(me, comm);

        comm.barrier();

        _master.introduce();

        comm.barrier();

        MPI.Finalize();
        } catch(MPIException e) {
            e.printStackTrace();
        }


    }

}
