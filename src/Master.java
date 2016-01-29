

import mpi.*;

import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;


public class Master {

    private Random _gen = new Random();

    class UnitAddress {
        public int Rank;
        public int Tag;

        public UnitAddress(int rank, int tag) {
            Rank = rank;
            Tag = tag;
        }
    }

    private int _rank;

    private Intracomm _comm;

    private ArrayList<String> _localUnits = new ArrayList<String>();

    private HashMap<String, UnitAddress> _units = new HashMap<String, UnitAddress>();

    private int _counter = 0;

    public Master(int rank, Intracomm comm) {

        _rank = rank;
        _comm = comm;

        _localUnits.add("Unit_" + _gen.nextInt());
        _localUnits.add("Unit_" + _gen.nextInt());
        _localUnits.add("Unit_" + _gen.nextInt());

    }

    public void introduce() throws MPIException {

        System.out.println(
                String.format("Introduce process %d to everyone.", _rank)
                );

        int nb_masters = _comm.getSize();

        if(_rank == 0) {
            for(int i = 1; i < nb_masters; ++i) {
                Status status = _comm.probe(MPI.ANY_SOURCE, 0);
                int source = status.getSource();

                int length = status.getCount(MPI.CHAR);
                CharBuffer message = CharBuffer.allocate(length);
                _comm.recv(message, length, MPI.CHAR, source, 0);
                System.out.println("Message received from " + source + " : " + message.toString());
                
                message = CharBuffer.wrap(getMapping(i, message).toCharArray());
                _comm.send(message, message.length(), MPI.CHAR, source, 0);

            }
        } else {
            CharBuffer message = CharBuffer.wrap(buildUnitMessage().toCharArray());
            System.out.println(message);
            _comm.send(message, message.length(), MPI.CHAR, 0, 0);
            
            Status status = _comm.probe(0, 0);
            int length = status.getCount(MPI.CHAR);
            message = CharBuffer.allocate(length);
            _comm.recv(message, length, MPI.CHAR, 0, 0);
            System.out.println("Message received from global_master : " + message.toString());

        }

    }

    public String buildUnitMessage() {
        String message = "";
        for(String unitName: _localUnits) {
            message += " " + unitName;
        }
        return message;

    }
    
    public String getMapping(int rank, CharBuffer unitNames) {
        String message = "";
        for(String unit: unitNames.toString().split(" ")) {
            if(unit.equals("")) {
                continue;
            }
            if(_units.containsKey(unit)) {
                System.out.println("Unit " + unit + " already declared.");
                System.exit(-1);
            } else {
                _counter++;
                _units.put(unit, new UnitAddress(rank, _counter));
                message += " " + unit + ":" + _counter;
            }
        }
        return message;
    }

}
