import java.io.File;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Scanner;


public class UDPServer {

    public static void main(String args[]) throws Exception {

        int[] ports = {10028, 10029, 10030, 10031}; //list of port numbers assigned to our group to use
        int port = ports[0];

        System.out.print("Getting IP Address..."); //remove later
        String localhost = InetAddress.getLocalHost().getHostAddress().trim();  //grabs IP to use for Client
        System.out.println("\nConnected to: " + localhost); //prints out the Server IP

        DatagramSocket serverSocket = new DatagramSocket(port);


        byte[] receiveData = new byte[256]; //create bytes for sending/receiving data

        String fileDataContents = "";   //variable for the file data contents
        Scanner readFileIn;  //Create an instance of the Scanner class so files can be read in

        while (true) {

            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length); //Creates a new datagram
            serverSocket.receive(receivePacket);

            System.out.println("Receiving the request packet.");

            //Gets the IPAddress and Port number of Host
            InetAddress IPAddress = receivePacket.getAddress();
            int portReceive = receivePacket.getPort();


            String dataFromClient = new String(receivePacket.getData()); //Gets the data for the packet from host

            ///File Data read in/////////
            //gets the data that needs to be read which is the packet data from above
            //The next few lines will check the filename for white space make sure file name is correct
            readFileIn = new Scanner(dataFromClient);

            readFileIn.next(); //skips the GET command of the HTTP request message to just read in the TestFile
            String fileName = readFileIn.next(); //checks for Null space in filename and if there is then file closes TODO
            readFileIn.close(); //closes the file


            readFileIn = new Scanner(new File(fileName));  //File requested by host
            while (readFileIn.hasNext()) {  //get the contents of the file line by line
                fileDataContents += readFileIn.nextLine();
            }
            System.out.println("File: " + fileDataContents); //print the file contents received
            readFileIn.close(); //close the file


            //Header Form given by Lab document
            String HTTP_HeaderForm = "HTTP/1.0 200 Document Follows\r\n"
                    + "Content-Type: text/plain\r\n"
                    + "Content-Length: " + fileDataContents.length() + "\r\n"
                    + "\r\n";

            HTTP_HeaderForm += fileDataContents;
            //////////////////////////////////////////////////////////////////////////////////////
            ArrayList<Packet> PacketList = Packet.Segmentation(HTTP_HeaderForm.getBytes()); //segments file into packets
            int packetNumber = 0;
            for (Packet packet : PacketList) {  //iterates through packets and sends them to host
                DatagramPacket sendPacket = packet.getDatagramPacket(IPAddress, portReceive);
                serverSocket.send(sendPacket);
                packetNumber++;
                System.out.println("Sending Packet " + packetNumber + " of " + PacketList.size());
            }
            //Sends Null Packet to let host know transfer is over
            //TODO check and see if this is correct implementation
            String nullByte = "\0";
            ArrayList<Packet> nullPacket = Packet.Segmentation(nullByte.getBytes());    //null packet changed to full 256 bit size, but only contains 1 bit of info
            DatagramPacket nullDatagram = nullPacket.get(0).getDatagramPacket(IPAddress, portReceive);
            serverSocket.send(nullDatagram);
            System.out.println("Sending Final Packet");
        }
    }

}
