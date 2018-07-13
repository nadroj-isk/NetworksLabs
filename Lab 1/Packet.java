
import java.net.*;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

//This class contains the methods that are related to packets including Segmentation, Reassembly, and CheckSum
public class Packet {
	
	//package data
	private static int PackageData_SIZE = 256;
    private byte[] PackageData;

		///////Package Header///////
	//Constant Variables
	//Private constant to map segment number and checksum
	private static final String HEADER_SEGMENT_NUMBER = "SegmentNumber";
	private static final String HEADER_CHECKSUM = "CheckSum";
	
	//Map data dictionary that maps the header string to strings
	private Map<String, String> PacketHeader;

	//Constructor
	public Packet()
	{
		//Initialize data array
		PackageData = new byte[PackageData_SIZE];

		//Initialize Map using HashMap
		PacketHeader = new HashMap<String, String>();
	}
	
	//Reassemble Packet function called by the UDPClient 
	public static byte[] ReassemblePacket(ArrayList<Packet> PacketList)
    {
    	int totalSize = 0;
    	for (int i = 0; i < PacketList.size(); i++)
    		totalSize += PacketList.get(i).getPacketDataSize();

    	byte[] returnPacket = new byte[totalSize];
    	int returnCounter = 0;
    	for (int i = 0; i < PacketList.size(); i++)
    	{
	    	//Search the packetList for each packet
	    	for (int j = 0; j < PacketList.size(); j++)
	    	{
	    		Packet FindPacket = PacketList.get(j);
	    		String segmentNumber = FindPacket.getHeaderValue(HEADER_ELEMENTS.SEGMENT_NUMBER);
	    		if (Integer.parseInt(segmentNumber) == i)
	    		{
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
	public static ArrayList<Packet> Segmentation(byte[] fileBytes) {
		  ArrayList<Packet> returnPacket = new ArrayList<Packet>();
		  int fileLength = fileBytes.length;
		  if (fileLength == 0) {
			  throw new IllegalArgumentException("File Empty");
		  }

		  int byteCounter = 0;
		  int segmentNumber = 0;
		  while (byteCounter < fileLength) {
			  Packet nextPacket = new Packet();
			  byte[] nextPacketData = new byte[PackageData_SIZE];
			  //read in amount of data size
			  int readInDataSize = PackageData_SIZE;

			  if(fileLength - byteCounter < PackageData_SIZE) {
				  readInDataSize = fileLength - byteCounter;
			  }

			  //copy the file data
			  for (int i = 0; i < readInDataSize; i++) {
				  nextPacketData[i] = fileBytes[byteCounter + 1];
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

	//displays the key value pairs
	public void display()
	{
		System.out.println("---------------------------PACKET HEADER------------------------------");
		Iterator<Entry<String, String>> header = PacketHeader.entrySet().iterator();
		while (header.hasNext())
		{
			Entry<String,String> n = header.next();
			System.out.println((n.getKey()) + " : " + (n.getValue()));
		}
		System.out.println("------------------------------PACKET DATA--------------------------------");
		for (int i = 0; i < PackageData.length; i++)
			System.out.print(PackageData[i] + " ");
		System.out.println("");
	}
	
	/////////////////////////PACKAGE HEADER METHODS////////////////////////////////// 

	//declaring enum Header_Elements for key/value pairs
	public static enum HEADER_ELEMENTS
	{
		SEGMENT_NUMBER,
		CHECKSUM
	}

	//Get Header Element Values
	public String getHeaderValue(HEADER_ELEMENTS HeaderElements)
	{
		switch (HeaderElements)
		{
			case SEGMENT_NUMBER:
				return PacketHeader.get(HEADER_SEGMENT_NUMBER);
			case CHECKSUM:
				return PacketHeader.get(HEADER_CHECKSUM);
			default:
				throw new IllegalArgumentException("HSomething is broken... bad broken");
		}
	}

	//SET header key/value pairs
	public void setHeaderValue(HEADER_ELEMENTS HeaderElements, String HeaderValue)
	{
		switch (HeaderElements)
		{
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
	
	
	
	//////////////////////////////PACKAGE DATA METHODS/////////////////////////////
	
	//increases array data by adding default data size to what has already been allocated
	private void IncreasePacketData()
	{
		byte[] temp = PackageData;
		PackageData = new byte[PackageData.length + PackageData_SIZE];
		for (int i = 0; i < temp.length; i++)
			PackageData[i] = temp[i];
	}

	//Resets the data array to default size and values
	public void ResetPacketData()
	{
		PackageData = new byte[PackageData_SIZE];
	}
	
	public byte GETPacketData(int index)
	{
		if (index >= 0 && index < PackageData.length)
			return PackageData[index];
		throw new IndexOutOfBoundsException(
				"GET PACKET DATA INDEX OUT OF BOUNDS EXCEPTION: index = " + index);
	}
	//get packet data
	public byte[] GETPacketData()
	{
		return PackageData;
	}
	//get packet data size
	public int getPacketDataSize()
	{
		return PackageData.length;
	}

	//Allows user to set a singular element in the data array as long as it is in bounds
	public void setPacketData(int index, byte value)
	{
		if (index >= 0)
		{
			while (index > PackageData.length)
				IncreasePacketData();
			PackageData[index] = value;
		}
		else
			throw new IndexOutOfBoundsException(
				"SET PACKET DATA INDEX OUT OF BOUND EXCEPTION: index = " + index);
	}

	//Takes an array of bytes to be set as the data segment.
	//If the Packet contains data already, the data is overwritten.
	//Throws IllegalArgumentException if the size of toSet does not
	//conform with the size of the data segment in the packet.
	public void setPacketData(byte[] toSet) throws IllegalArgumentException
	{
		int argumentSize = toSet.length;
		if (argumentSize > 0)
		{
			PackageData = new byte[argumentSize];
			for (int i = 0; i < PackageData.length; i++)
				PackageData[i] = toSet[i];
		}
		else
			throw new IllegalArgumentException(
				"ILLEGAL ARGUEMENT EXCEPTION-SET PACKET DATA: toSet.length = " + toSet.length);
	}


	//returns packet as a datagram packet
	public DatagramPacket getDatagramPacket(InetAddress i, int port)
	{
		byte[] setData = ByteBuffer.allocate(256)
				.putShort(Short.parseShort(PacketHeader.get(HEADER_SEGMENT_NUMBER)))
				.putShort(Short.parseShort(PacketHeader.get(HEADER_CHECKSUM)))
				.put(PackageData)
				.array();

		return new DatagramPacket(setData, setData.length, i, port);
	}

	//Creates a new packet
	public static Packet CreatePacket(DatagramPacket packet)
	{
		Packet newPacket = new Packet();
		ByteBuffer bytebuffer = ByteBuffer.wrap(packet.getData());
		newPacket.setHeaderValue(HEADER_ELEMENTS.SEGMENT_NUMBER, bytebuffer.getShort()+"");
		newPacket.setHeaderValue(HEADER_ELEMENTS.CHECKSUM, bytebuffer.getShort()+"");

		byte[] PacketData = packet.getData();
		byte[] remaining = new byte[PacketData.length - bytebuffer.position()];
		for (int i = 0; i < remaining.length; i++)
			remaining[i] = PacketData[i+bytebuffer.position()];
		newPacket.setPacketData(remaining);
		return newPacket;
	}
	
	  //Check sum function that return the 16 bit checkSum value for a packet
	  public static short CheckSum(byte[] packetBytes) {
		  short sum = 0;
		  int packetByteLength = packetBytes.length;

		  int count = 0;
		  while (count > 1) {
			  sum += ((packetBytes[count]) << 8 & 0xFF00) | ((packetBytes[count + 1]) & 0x00FF);
			  if ((sum & 0xFFFF0000) > 0) {
				  sum = (short) ((sum & 0xFFFF) + 1);
			  }
			  count += 2;
			  packetByteLength -=2;
		  }

		  if(packetByteLength > 0) {
			  sum += (packetBytes[count] << 8 & 0xFF00);
			  if ((sum & 0xFFFF0000) > 0) {
				  sum = (short) ((sum & 0xFFFF) + 1);
			  }
		  }
		  return (short) (~sum & 0xFFFF);
	  }

}
