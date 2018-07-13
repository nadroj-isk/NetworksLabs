import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Random;

public class UDPClient {

    public static void main(String args[]) throws Exception {
        int[] ports = {10028, 10029, 10030, 10031}; //Group Assigned Port Numbers
        int port = ports[0];  //must be the same as port in server file
        //use tux050 tux065


        //BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in)); 	//not used anymore because we use TestFile
        DatagramSocket clientSocket = new DatagramSocket();        //creates socket for user
        String localhost = InetAddress.getLocalHost().getHostAddress().trim();    //gets user IP
        InetAddress IPAddress = InetAddress.getByName("172.19.145.232");    //gets IP address of host

        byte[] sendData = new byte[256];    //creates packet to be sent
        byte[] receiveData = new byte[256]; //creates packet to be received
        String GremlinProbability = "0.0";
        boolean DataDoneSending = false;
        int packetNumber = 0;

        // ********** SENDING DATA **********
        String TestFile = "GET TestFile.html HTTP/1.0";
        sendData = TestFile.getBytes();

        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
        clientSocket.send(sendPacket);

        System.out.println("Sending request packet...."); //notify user of sending

        // ********** RECEIVING PACKETS **********

        System.out.println("Receiving packets...");

        DatagramPacket receivePacket; //Declares the Datagram packet for receive packet
        ArrayList<Packet> receivedPackets = new ArrayList<Packet>(); //create a new array of packets received
        while (DataDoneSending == false) { //check to see if the packet is done sending//check to see if the packet is done sending

            receivePacket = new DatagramPacket(receiveData, receiveData.length); //creates a null datagram packet
            clientSocket.receive(receivePacket); //receives the actual packet from the server


            //create a packet from the data received
            Packet createReceivedPacket = Packet.CreatePacket(receivePacket);
            packetNumber++;

            System.out.println("Packet: " + packetNumber);
            //checks to see if the packet data is null
            //if it is then that means the data is done sending and it will break out of the loop
            if (createReceivedPacket.GETPacketData()[0] == '\0') {
                DataDoneSending = true;
                break;
            }

            //received packets are added to the packet array
            receivedPackets.add(createReceivedPacket);
        }

        //using command line arguments to detect Gremlin probability
        //checks for no arguments and if there are none then notify user and
        //set the DataDoneSending to true
        System.out.println("Gremlin...");
        if (args.length == 0) {
            System.out.println("There are no arguments detected for Gremlin Probability");
            DataDoneSending = true;
        } else {
            //if there is arguments then set the Gremlin Probability to these
            GremlinProbability = args[0];
        }
        //send each of the packets with arguments through the Gremlin function to
        //determine whether to change some of the packet bit or pass the packet as it is to the receiving function
        for (Packet packets : receivedPackets) {
            Gremlin(GremlinProbability, packets);
        }

        //Check for error detection in the received packets
        if (ErrorDetection(receivedPackets)) {
            System.out.println("There were errors detected in packet received.");
        }
        //Reassembles Packets that were received
        byte[] ReassemblePacketFile = Packet.ReassemblePacket(receivedPackets);
        String modifiedPacketData = new String(ReassemblePacketFile);
        System.out.println("Packet Data Received from UDPServer:" + modifiedPacketData);
        clientSocket.close();

        //Display packets using a Web browser
        int index = modifiedPacketData.lastIndexOf("\r\n", 100);
        modifiedPacketData = modifiedPacketData.substring(index);
        //gets the operating system name
        System.out.println(System.getProperty("os.name"));
        //if using windows then need to change "MAC" to Windows----- from Stack Overflow
        if (System.getProperty("os.name").startsWith("MAC")) {
            //creates a temporary TestFile
            File TestFileTemp = File.createTempFile("TestFile", ".html");
            FileWriter writer = new FileWriter(TestFileTemp);
            writer.write(modifiedPacketData);
            writer.close();
            //opens the test file on the desktop
            Desktop desk = Desktop.getDesktop();
            desk.open(TestFileTemp);
        }


    }

    //Gremlin function
    private static void Gremlin(String probOfDamage, Packet receivedPacket) {
        Random random = new Random();
        //pick a random number
        int dmgRand = random.nextInt(100) + 1;
        int howManyRand = random.nextInt(100) + 1;
        int bytesToChange;
        if (howManyRand <= 50) {
            bytesToChange = 1;
        } else if (50 < howManyRand && howManyRand <= 80) {
            bytesToChange = 2;
        } else bytesToChange = 3;
        //pick a
        double damagedProbability = Double.parseDouble(probOfDamage) * 100;
        if (dmgRand <= damagedProbability) {
            for (int i = 0; i <= bytesToChange; i++) {
                byte[] data = receivedPacket.GETPacketData();
                int byteToCorrupt = random.nextInt(receivedPacket.getPacketDataSize()); // pick a random byte
                data[byteToCorrupt] = (byte) (~data[byteToCorrupt] | 0xFF); // flip the bits in that byte
            }

        }
    }

    //ErrorDetection
    private static boolean ErrorDetection(ArrayList<Packet> PacketList) {
        for (int i = 0; i < PacketList.size(); i++) {
            String strReceivedCheckSum = PacketList.get(i).getHeaderValue(Packet.HEADER_ELEMENTS.CHECKSUM);
            Short receivedCheckSum = Short.parseShort(strReceivedCheckSum);

            byte[] data = PacketList.get(i).GETPacketData();
            int calcCheckSum = Packet.CheckSum(data);
            System.out.println("Post-Gremlin checksum: " + String.valueOf(calcCheckSum));
            if (!receivedCheckSum.equals(calcCheckSum))
                return true;
        }

        return false;
    }

}
