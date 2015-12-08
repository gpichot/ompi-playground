import mpi.*;
import java.nio.*;
import java.util.Arrays;


public class BSendAndRecv {

    // TODO Factorize with a parent class or a util class
    public static String bufferToString(DoubleBuffer buffer, int size) {
        double[] items = new double[size];
        for(int i = 0; i < size; i++) {
            items[i] = buffer.get(i);   
        }
        return Arrays.toString(items);
    }

    public static void main(String[] args) throws MPIException {

        int NB_ITEMS = 5;
        MPI.Init(args);

        Intracomm comm = MPI.COMM_WORLD;
        int me = comm.getRank();
        int nb = comm.getSize();

        DoubleBuffer buffer = MPI.newDoubleBuffer(NB_ITEMS);

        //Initialisation
        for (int i = 0; i < NB_ITEMS; i++) {
            buffer.put(i, me);
        }

        // Print
        System.out.println(me + " " + bufferToString(buffer, NB_ITEMS) + ".");
        MPI.COMM_WORLD.barrier();

        if(me % 2 == 0) {
            // Nombre pair d'éléments
            if(me + 1 < nb) {
                comm.bSend(buffer, NB_ITEMS, MPI.DOUBLE, me + 1, 1);
            }
        } else {
            Status status = comm.recv(buffer, NB_ITEMS, MPI.DOUBLE, me - 1, 1);
            int count = status.getCount(MPI.DOUBLE);
            int src = status.getSource();
            System.out.println(src + " -> " + me + ": received "+ count +" values");
        }
        MPI.COMM_WORLD.barrier();
        System.out.println(me + " " + bufferToString(buffer, NB_ITEMS) + ".");

        MPI.COMM_WORLD.barrier();

        MPI.Finalize();


    }

}
