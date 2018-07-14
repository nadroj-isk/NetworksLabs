import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


//This class contains the methods that are related to packets including Segmentation, Reassembly, and CheckSum
class Packet {

    ///////Package Header///////
    //Constant Variables
    //Private constant to map segment number and checksum
    private static final String HEADER_SEGMENT_NUMBER = "SegmentNumber";
    private static final String HEADER_CHECKSUM = "CheckSum";
    //package data
    private static int PACKET_SIZE = 256;  //Size of the packets to be sent
    private static final int HEADER_LINES = 4;  //Number of header lines that go before the objects to be sent given in the lab assignment
    private static final int PACKET_DATA_SIZE = PACKET_SIZE - HEADER_LINES; //Size of the data that is transmitted in the packet
    private byte[] PackageData;
    //Map data dictionary that maps the header string to strings
    private Map<String, String> PacketHeader;

    //Constructor
    private Packet() {
        //Initialize data array
        PackageData = new byte[PACKET_SIZE];

        //Initialize Map using HashMap
        PacketHeader = new HashMap<>();
    }

    //Reassemble Packet function called by the UDPClient
    static byte[] ReassemblePacket(ArrayList<Packet> PacketList) {
        int totalSize = 0;
        for (Packet aPacketList : PacketList) totalSize += aPacketList.getPacketDataSize();

        byte[] returnPacket = new byte[totalSize];
        int returnCounter = 0;
        for (int i = 0; i < PacketList.size(); i++) {
            //Search the packetList for each packet
            for (Packet FindPacket : PacketList) {
                String segmentNumber = FindPacket.getHeaderValue(HEADER_ELEMENTS.SEGMENT_NUMBER);
                if (Integer.parseInt(segmentNumber) == i) {
                    for (int k = 0; k < FindPacket.getPacketDataSize(); k++)
                        returnPacket[returnCounter + k] = FindPacket.GETPacketData(k);
                    returnCounter += FindPacket.getPacketDataSize();
                    break;
                }
            }
        }

        return returnPacket;
    }

    //Segmentation is called by the UDPServer to break the packets into segments
    static ArrayList<Packet> Segmentation(byte[] fileBytes) {
        ArrayList<Packet> returnPacket = new ArrayList<>();
        int fileLength = fileBytes.length;
        if (fileLength == 0) {
            throw new IllegalArgumentException("File Empty");
        }

        int byteCounter = 0;
        int segmentNumber = 0;
        while (byteCounter < fileLength) {
            Packet nextPacket = new Packet();
            byte[] nextPacketData = new byte[PACKET_DATA_SIZE];
            //read in amount of data size
            int readInDataSize = PACKET_DATA_SIZE; //only allows 252 bytes since the other 4 are for the header

            if (fileLength - byteCounter < PACKET_DATA_SIZE) {
                readInDataSize = fileLength - byteCounter;
            }

            //copy the file data
            int j = byteCounter;
            for (int i = 0; i < readInDataSize; i++) {
                nextPacketData[i] = fileBytes[j];
                j++;
            }

            //set the packet data for the next packet
            nextPacket.setPacketData(nextPacketData);

            //set the header for the next packet
            nextPacket.setHeaderValue(HEADER_ELEMENTS.SEGMENT_NUMBER, segmentNumber + "");

            //CheckSum (errors)
            String CheckSumPacket = String.valueOf(Packet.CheckSum(nextPacketData));
            nextPacket.setHeaderValue(HEADER_ELEMENTS.CHECKSUM, CheckSumPacket);
            returnPacket.add(nextPacket);

            //increase the segment number
            segmentNumber++;

            //increase the counter by the amount read in
            byteCounter = byteCounter + readInDataSize;
        }

        return returnPacket;
    }

    //Creates a new packet
    static Packet CreatePacket(DatagramPacket packet) {
        Packet newPacket = new Packet();
        ByteBuffer bytebuffer = ByteBuffer.wrap(packet.getData());
        newPacket.setHeaderValue(HEADER_ELEMENTS.SEGMENT_NUMBER, bytebuffer.getShort() + "");
        newPacket.setHeaderValue(HEADER_ELEMENTS.CHECKSUM, bytebuffer.getShort() + "");

        byte[] PacketData = packet.getData();
        byte[] remaining = new byte[PacketData.length - bytebuffer.position()];
        System.arraycopy(PacketData, bytebuffer.position(), remaining, 0, remaining.length);
        newPacket.setPacketData(remaining);
        return newPacket;
    }

    /////////////////////////PACKAGE HEADER METHODS//////////////////////////////////

    //Check sum function that return the 16 bit checkSum value for a packet
    static short CheckSum(byte[] packetBytes) {
        short sum = 0;
        int packetByteLength = packetBytes.length;

        int count = 0;
        while (count > 1) {
            sum += ((packetBytes[count]) << 8 & 0xFF00) | ((packetBytes[count + 1]) & 0x00FF);
            if ((sum & 0xFFFF0000) > 0) {
                sum = (short) ((sum & 0xFFFF) + 1);
            }
            count += 2;
            packetByteLength -= 2;
        }

        if (packetByteLength > 0) {
            sum += (packetBytes[count] << 8 & 0xFF00);
            if ((sum & 0xFFFF0000) > 0) {
                sum = (short) ((sum & 0xFFFF) + 1);
            }
        }
        return (short) (~sum & 0xFFFF);
    }


    //Get Header Element Values
    String getHeaderValue(HEADER_ELEMENTS HeaderElements) {
        switch (HeaderElements) {
            case SEGMENT_NUMBER:
                return PacketHeader.get(HEADER_SEGMENT_NUMBER);
            case CHECKSUM:
                return PacketHeader.get(HEADER_CHECKSUM);
            default:
                throw new IllegalArgumentException("HSomething is broken... bad broken");
        }
    }


    //////////////////////////////PACKAGE DATA METHODS/////////////////////////////

    //SET header key/value pairs
    private void setHeaderValue(HEADER_ELEMENTS HeaderElements, String HeaderValue) {
        switch (HeaderElements) {
            case SEGMENT_NUMBER:
                PacketHeader.put(HEADER_SEGMENT_NUMBER, HeaderValue);
                break;
            case CHECKSUM:
                PacketHeader.put(HEADER_CHECKSUM, HeaderValue);
                break;
            default:
                throw new IllegalArgumentException("Something is broken... bad broken");
        }
    }


    private byte GETPacketData(int index) {
        if (index >= 0 && index < PackageData.length)
            return PackageData[index];
        throw new IndexOutOfBoundsException(
                "GET PACKET DATA INDEX OUT OF BOUNDS EXCEPTION: index = " + index);
    }

    //get packet data
    byte[] GETPacketData() {
        return PackageData;
    }

    //get packet data size
    int getPacketDataSize() {
        return PackageData.length;
    }


    //Takes an array of bytes to be set as the data segment.
    //If the Packet contains data already, the data is overwritten.
    //Throws IllegalArgumentException if the size of toSet does not
    //conform with the size of the data segment in the packet.
    private void setPacketData(byte[] toSet) throws IllegalArgumentException {
        int argumentSize = toSet.length;
        if (argumentSize > 0) {
            PackageData = new byte[argumentSize];
            System.arraycopy(toSet, 0, PackageData, 0, PackageData.length);
        } else
            throw new IllegalArgumentException(
                    "ILLEGAL ARGUEMENT EXCEPTION-SET PACKET DATA: toSet.length = " + toSet.length);
    }

    //returns packet as a datagram packet
    DatagramPacket getDatagramPacket(InetAddress i, int port) {
        byte[] setData = ByteBuffer.allocate(256)
                .putShort(Short.parseShort(PacketHeader.get(HEADER_SEGMENT_NUMBER)))
                .putShort(Short.parseShort(PacketHeader.get(HEADER_CHECKSUM)))
                .put(PackageData)
                .array();

        return new DatagramPacket(setData, setData.length, i, port);
    }

    //declaring enum Header_Elements for key/value pairs
    public enum HEADER_ELEMENTS {
        SEGMENT_NUMBER,
        CHECKSUM
    }

}
