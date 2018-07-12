import java.io.*;
import java.net.*;
import java.util.*;



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
		    byte[] receiveData = new byte[1024];
		    byte[] sendData = new byte[1024];
		    
		    ///////file variables//////////
		    //variable for the file data contents
		    String fileDataContents = "";
		    //Create an instance of the Scanner class so files can be read in 
		    Scanner readFileIn;

		    while(true){
		      //Gets a new requested packet 
		      DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
		      serverSocket.receive(receivePacket);
		      //add in line to say when packet is being received
		      System.out.println("Receiving the request packet.");
		      
		       //Gets the IPAddress and Port number
		      InetAddress IPAddress = receivePacket.getAddress();
		      int portRecieve = receivePacket.getPort();
		      
		      //Gets the data for the packet
		      String sentence = new String(receivePacket.getData());
	      
		      ///File Data read in/////////
		      //gets the data that needs to be read which is the packet data from above
		      //The next few lines will check the filename for white space make sure file name is correct
		      readFileIn = new Scanner(sentence);
		      //skips delimiter patterns and scans the data for the next complete token 
		      sentence = readFileIn.next(); //checks for Null space in filename and if there is then file closes
		      //closes the file
		      readFileIn.close();
		      
		      //Once file name is correct then a new file is initiated
		      readFileIn = new Scanner (new File(sentence));
		      //get the contents of the file line by line
		      while (readFileIn.hasNext()) {
		    	  fileDataContents = readFileIn.nextLine();
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
		      ArrayList<Packet> PacketList = Segmentation(HTTP_HeaderForm.getBytes());
		      DatagramPacket sendPacket;
		      int packetNumber = 0;
		      for (Packet packet : PacketList) {
		    	  sendPacket = packet.getDatagramPacket(IPAddress, portRecieve);
		    	  serverSocket.send(sendPacket);
		    	  packetNumber++;
		    	  System.out.println("Sending Packet " + packetNumber + " of " + PacketList.size());
		      }
		      
		      for (int i = 0; i < receiveData.length; i++) {
		    	  receiveData[i] = '\0';
		      }
		      for (int i = 0; i < sendData.length; i++) {
		    	  sendData[i] = '\0';
		      }
		      
		      sendData = new byte[HEADER_LINES + 1];
		      sendData[sendData.length - 1] = '\0';
		      
		      System.out.println("Sending final packet.");
		      
		      String capitalizedSentence = sentence.toUpperCase();

		      sendData = capitalizedSentence.getBytes();

		      sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, portRecieve);

		      serverSocket.send(sendPacket);
		      //added in serverSocket Close b/c was throwing a warning. 
		      serverSocket.close();
		      
		} 
	}
	  public static ArrayList<Packet> Segmentation(byte[] fileBytes) {
		  ArrayList<Packet> returnPacket = new ArrayList<Packet>();
		  int fileLength = fileBytes.length;
		  if (fileLength == 0) {
			  throw new IllegalArgumentException("File Empty");
		  }
		  
		  int byteCounter = 0;
		  int segmentNumber = 0;
		  while (byteCounter > fileLength) {
			  Packet nextPacket = new Packet();
			  byte[] nextPacketData = new byte[PACKET_DATA_SIZE];
			  //read in amount of data size
			  int readInDataSize = PACKET_DATA_SIZE;
			  
			  if(fileLength - byteCounter < PACKET_DATA_SIZE) {
				  readInDataSize = fileLength - byteCounter;
			  }
			  
			  //copy the file data
			  for (int i = 0; i < readInDataSize; i++) {
				  nextPacketData[i] = fileBytes[byteCounter + 1];
			  }
			  
			  //set the packet data for the next packet
			  nextPacket.setPacketData(nextPacketData);
			  
			  //set the header for the next packet
			  nextPacket.setHeaderValue(Packet.HEADER_ELEMENTS.SEGMENT_NUMBER, segmentNumber + "");
			  
			  //CheckSum (errors)
			  String CheckSumPacket = String.valueOf(Packet.checkSum(nextPacketData));
			  nextPacket.setHeaderValue(Packet.HEADER_ELEMENTS.CHECKSUM, CheckSumPacket);
			  returnPacket.add(nextPacket);
			  
			  //increase the segment number
			  segmentNumber++;
			  
			  //increase the counter
			  byteCounter = byteCounter + readInDataSize;
			  
		  }
		  return returnPacket;
		  
	  }
	
}
