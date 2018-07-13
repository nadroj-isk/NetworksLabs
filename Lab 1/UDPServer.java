import java.io.File;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Scanner;


public class UDPServer {

    //*****Constant Variables*****//
    //Number of header lines that go before the objects to be sent given in the lab assignment
    public static final int HEADER_LINES = 4;

    //Size of the packets to be sent
    public static final int PACKET_SIZE = 256;

    //Size of the data that is transmitted in the packet
    public static final int PACKET_DATA_SIZE = PACKET_SIZE - HEADER_LINES;


    public static void main(String args[]) throws Exception {

        //list of port numbers assigned to our group to use
        int[] ports = {10028, 10029, 10030, 10031};
        int port = ports[0];
        //Gets the IP address ***Remove Later****
        System.out.print("Getting IP Address..."); //remove later
        String localhost = InetAddress.getLocalHost().getHostAddress().trim();  //grabs IP to use for Client
        System.out.println("\nConnected to: " + localhost); //prints out the host

        //changed the port number b/c the original port number provided an error saying it was already in use
        DatagramSocket serverSocket = new DatagramSocket(port);

        //create bytes for sending/receiving data
        byte[] receiveData = new byte[256];
        byte[] sendData = new byte[256];

        ///////file variables//////////
        //variable for the file data contents
        String fileDataContents = "";
        //Create an instance of the Scanner class so files can be read in
        Scanner readFileIn;

        while (true) {
            //Gets a new requested packet
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            serverSocket.receive(receivePacket);
            //add in line to say when packet is being received
            System.out.println("Receiving the request packet.");

            //Gets the IPAddress and Port number
            InetAddress IPAddress = receivePacket.getAddress();
            int portRecieve = receivePacket.getPort();

            //Gets the data for the packet
            String dataFromClient = new String(receivePacket.getData());

            ///File Data read in/////////
            //gets the data that needs to be read which is the packet data from above
            //The next few lines will check the filename for white space make sure file name is correct
            readFileIn = new Scanner(dataFromClient);
            //skips the GET part of the HTTP request message to just read in the TestFile
            readFileIn.next();
            //skips delimiter patterns and scans the data for the next complete token
            String fileName = "C:\\Users\\jsosn\\IdeaProjects\\lab1\\src\\" + readFileIn.next(); //checks for Null space in filename and if there is then file closes
            //closes the file
            readFileIn.close();

            //Once file name is correct then a new file is initiated
            readFileIn = new Scanner(new File(fileName));
            //get the contents of the file line by line
            while (readFileIn.hasNext()) {
                fileDataContents += readFileIn.nextLine();
            }
            //print the file contents received
            System.out.println("File: " + fileDataContents);
            //close the file
            readFileIn.close();


            //Header Form given by Lab document
            String HTTP_HeaderForm = "HTTP/1.0 200 Document Follows\r\n"
                    + "Content-Type: text/plain\r\n"
                    + "Content-Length: " + fileDataContents.length() + "\r\n"
                    + "\r\n";

            HTTP_HeaderForm += fileDataContents;
            //////////////////////////////////////////////////////////////////////////////////////
            ArrayList<Packet> PacketList = Packet.Segmentation(HTTP_HeaderForm.getBytes());
            DatagramPacket sendPacket;
            int packetNumber = 0;
            for (Packet packet : PacketList) {
                sendPacket = packet.getDatagramPacket(IPAddress, portRecieve);
                serverSocket.send(sendPacket);
                packetNumber++;
                System.out.println("Sending Packet " + packetNumber + " of " + PacketList.size());
            }
            //checks to see if the final packet being sent is null and then send the final packet
            String nullByte = "\0";
            //byte[] nullData = nullByte.getBytes();
            ArrayList<Packet> nullpacket = Packet.Segmentation(nullByte.getBytes());
            DatagramPacket nullDatagram = nullpacket.get(0).getDatagramPacket(IPAddress, portRecieve);
            serverSocket.send(nullDatagram);
            System.out.println("Sending Final Packet");
        }
    }

}
