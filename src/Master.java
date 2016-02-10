import mpi.*;

import java.nio.CharBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Random;


public class Master {

    public static final int GLOBAL_MASTER = 0;
    public static final int DEFAULT_TAG = 0;

    /**
     * Local random number generator.
     */
    private Random _gen = new Random();


    /**
     * Stores a FMU Address (Host/Master Id + FMU Global Id)
     *
     */
    class FMUAddress {

        /**
         * Master ID, unique identifier for the master (in MPI: rank).
         */
        public int masterId;

        /**
         * Global ID for the FMU (unique for the whole simulation).
         */
        public int fmuGlobalId;

        public FMUAddress(int master_id, int fmu_global_id) {
            masterId = master_id;
            fmuGlobalId = fmu_global_id;
        }

    }

    /**
     * Id for the master (unique, rank for MPI).
     */
    private int _id;

    /**
     * Communicator (MPI).
     */
    private Intracomm _comm;

    /**
     * FMUs locals to the master (as strings).
     */
    private ArrayList<String> _localFMUs = new ArrayList<String>();


    /**
     * Map of all the FMUs with their address (Master Id + FMU Id).
     */
    private HashMap<String, FMUAddress> _fmuMap = new HashMap<String, FMUAddress>();


    /**
     * Internal counter used to attribute a unique identifier to each FMU.
     */
    private int _counter = 0;

    /**
     * Default constructor.
     */
    public Master(int id, Intracomm comm) {

        _id = id;
        _comm = comm;

        _localFMUs.add("FMU_" + (_gen.nextInt() % 1000 + 1000));
        _localFMUs.add("FMU_" + (_gen.nextInt() % 1000 + 1000));
        _localFMUs.add("FMU_" + (_gen.nextInt() % 1000 + 1000));

    }

    /**
     * Function that introduces itself to all other masters.
     *
     * Each local master advertizes its FMU names to the global master.
     * For each FMU name received by the global masterm an unique identifier is
     * attributed to the FMU. Once every local master have advertized their FMUs
     * the global master broadcasts a message that contains the entire mapping
     * to all local masters.
     */
    public void introduce() throws MPIException {

        System.out.println(
                String.format("Introduce process %d to everyone.", _id)
                );

        int nb_masters = _comm.getSize();

        if(_id == GLOBAL_MASTER) {

            for(int i = 1; i < nb_masters; ++i) {

                // Retrieves the size of the message and its source
                Status status = _comm.probe(MPI.ANY_SOURCE, DEFAULT_TAG);
                int source = status.getSource();
                int length = status.getCount(MPI.CHAR);

                // Receives the message
                CharBuffer message = CharBuffer.allocate(length);
                _comm.recv(message, length, MPI.CHAR, source, DEFAULT_TAG);
                System.out.println("Message received from " + source + " : " + message.toString());

                // For each FMU attribute a new identifier
                addToFmuMap(source, message);

            }

            // Broadcasts the message to all local masters
            CharBuffer message = CharBuffer.wrap(getEntireMapping().toCharArray());    
            System.out.println("Global message sent from global_master : " + message.toString());

            // First broadcasts the size of the message...
            IntBuffer sizeMessage = IntBuffer.allocate(1);
            sizeMessage.put(message.length());
            _comm.bcast(sizeMessage, 1, MPI.INT, GLOBAL_MASTER);

            // ... then sends the message
            _comm.bcast(message, message.length(), MPI.CHAR, GLOBAL_MASTER);

        } else {

            // Sends the local FMU names to the Global Master
            CharBuffer message = CharBuffer.wrap(buildUnitMessage().toCharArray());
            System.out.println(message);
            _comm.send(message, message.length(), MPI.CHAR, GLOBAL_MASTER, DEFAULT_TAG);

            // Waits for the global master to send the size of the mapping message 
            IntBuffer sizeMessage = IntBuffer.allocate(1);
            _comm.bcast(sizeMessage, 1, MPI.INT, GLOBAL_MASTER);
            int length = sizeMessage.get(0);
            System.out.println("Message global received from global_master : " + sizeMessage.get(0));

            // Then receives the whole mapping
            message = CharBuffer.allocate(length);
            _comm.bcast(message, length, MPI.CHAR, GLOBAL_MASTER);
            System.out.println("Message global received from global_master : " + message.toString());

        }

    }

    /**
     *
     */
    public String buildUnitMessage() {
        String message = "";
        for(String fmuName: _localFMUs) {
            message += " " + fmuName;
        }
        return message;

    }

    /**
     *
     */
    public void addToFmuMap(int rank, CharBuffer fmuNames) {
        for(String name: fmuNames.toString().split(" ")) {
            if(name.equals("")) {
                continue;
            }
            if(_fmuMap.containsKey(name)) {
                System.out.println("FMU " + name + " already declared.");
                System.exit(-1);
            } else {
                _counter++;
                _fmuMap.put(name, new FMUAddress(rank, _counter));
            }
        }
    }

    /**
     *
     */
    public String getEntireMapping() {
        String message = "";
        for(Entry<String, FMUAddress> entry: _fmuMap.entrySet()) {
            FMUAddress address = entry.getValue();
            message += " " + entry.getKey() + "@" + address.masterId + ":" + address.fmuGlobalId;
        }
        return message;
    }

}
