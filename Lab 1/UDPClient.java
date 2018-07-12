import java.io.*;
import java.net.*;
import java.util.*;

public class UDPClient {

	public static void main(String args[]) throws Exception {
		int[] ports = {10028, 10029, 10030, 10031}; //Group Assigned Port Numbers
		int port = ports[0];  //must be the same as port in server file
	  //use tux050 tux065


		BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in)); 	//sets up input reader
		DatagramSocket clientSocket = new DatagramSocket();		//creates socket for user
		String localhost = InetAddress.getLocalHost().getHostAddress().trim();	//gets user IP
		InetAddress IPAddress = InetAddress.getByName("131.204.14.50");	//gets IP address of host

		byte[] sendData = new byte[256];	//creates packet to be sent
		byte[] receiveData = new byte[256]; //cretes packet to be received

		String TestFile = "GET TestFile.html HTTP/1.0";
		sendData = TestFile.getBytes();

		DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
		//sending the packet
		clientSocket.send(sendPacket);
		//notify user of sending
		System.out.println("Sending request packet....");

		DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

		clientSocket.receive(receivePacket);

		String modifiedTestFile = new String(receivePacket.getData());

		System.out.println("FROM SERVER" + modifiedTestFile);
		clientSocket.close();

	}

	 private static void Gremlin(String probOfDamage, Packet receivedPacket)
	    {
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
	    	if (dmgRand <= damagedProbability)
	    	{
					for (int i = 0; i <= bytesToChange; i++){
	    			byte[] data = receivedPacket.getData();
	    			int byteToCorrupt = random.nextInt(receivedPacket.getDataSize()); // pick a random byte
	    			data[byteToCorrupt] = (byte) (~data[byteToCorrupt] | 0xFF); // flip the bits in that byte
	    	}

		}
	}

	    private static boolean ErrorDetection(ArrayList<Packet> PacketList)
	    {
	    	for (int i = 0; i < PacketList.size(); i++)
	    	{
	    		String strReceivedCheckSum = PacketList.get(i).getHeaderValue(Packet.HEADER_ELEMENTS.CHECKSUM);
	    		Short receivedCheckSum = Short.parseShort(strReceivedCheckSum);

	    		byte[] data = PacketList.get(i).getData();
	    		int calcCheckSum = Packet.checkSum(data);
	    		System.out.println("Post-Gremlin checksum: " + String.valueOf(calcCheckSum));
	    		if (!receivedCheckSum.equals(calcCheckSum))
	    			return true;
	    	}

	    	return false;
	    }



		private static byte[] ReassemblePacket(ArrayList<Packet> PacketList)
	    {
	    	int totalSize = 0;
	    	for (int i = 0; i < PacketList.size(); i++)
	    		totalSize += PacketList.get(i).getDataSize();

	    	byte[] returnPacket = new byte[totalSize];
	    	int retCounter = 0;
	    	for (int i = 0; i < PacketList.size(); i++)
	    	{
		    	//Do a boring linear search on the list for each packet number
		    	for (int j = 0; j < PacketList.size(); j++)
		    	{
		    		Packet check = PacketList.get(j);
		    		String segNum = check.getHeaderValue(
										Packet.HEADER_ELEMENTS.SEGMENT_NUMBER);
		    		if (Integer.parseInt(segNum) == i)
		    		{
		    			for (int k = 0; k < check.getDataSize(); k++)
		    				returnPacket[retCounter + k] = check.getData(k);
		    			retCounter += check.getDataSize();
		    			break;
		    		}
		    	}
	    	}

	    	return returnPacket;
	    }

}
