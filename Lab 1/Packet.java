
import java.net.*;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;


public class Packet {

	//Constant Variables
	private static int PackageData_SIZE = 256;
	public static enum HEADER_ELEMENTS
	{
		SEGMENT_NUMBER,
		CHECKSUM
	}

		//package data
		private byte[] PackageData;

		//Header information
		//Header is a map from strings to strings
		private Map<String, String> packet_header;

		//Private constant to map segment number and checksum
		private static final String HEADER_SEGMENT_NUM = "segmentNum";
		private static final String HEADER_CHECKSUM = "checkSum";

//General Methods ---------------------------------------------------------------------
	//Constructor for a Packet that initializes members
	public Packet()
	{
		//Initialize data array
		PackageData = new byte[PackageData_SIZE];

		//Initialize Map (using HashMap because it's fun)
		packet_header = new HashMap<String, String>();
	}

	//Function simply displays all of the key-value pairs in the map
	//as well as all of the bytes of data (as numerical values from
	//0 - 255).
	public void display()
	{
		System.out.println("PACKET HEADER------------------------------");
		Iterator<Entry<String, String>> it = packet_header.entrySet().iterator();
		while (it.hasNext())
		{
			Entry<String,String> n = it.next();
			System.out.println((n.getKey()) + " : " + (n.getValue()));
		}
		System.out.println("PACKET DATA--------------------------------");
		for (int i = 0; i < PackageData.length; i++)
			System.out.print(PackageData[i] + " ");
		System.out.println("");
	}

	//Increases data of the array by adding the default data size
	//to what is already allocated.
	private void IncreasePacketData()
	{
		byte[] temp = PackageData;
		PackageData = new byte[PackageData.length + PackageData_SIZE];
		for (int i = 0; i < temp.length; i++)
			PackageData[i] = temp[i];
	}

//Header Methods --------------------------------------------------------------
	//Sets a key-value pair in the header of this packet. The keys are
	//governed by our enumeration, HEADER_ELEMENTS, such that the
	//packets all have the exact same headers.
	public void setHeaderValue(HEADER_ELEMENTS ele, String value)
	{
		switch (ele)
		{
			case SEGMENT_NUMBER:
				packet_header.put(HEADER_SEGMENT_NUM, value);
				break;
			case CHECKSUM:
				packet_header.put(HEADER_CHECKSUM, value);
				break;
			default:
				throw new IllegalArgumentException("Something is broken... bad broken");
		}
	}

	//Gets one of the values given the key governed by our
	//enumeration, HEADER_ELEMENTS.
	public String getHeaderValue(HEADER_ELEMENTS ele)
	{
		switch (ele)
		{
			case SEGMENT_NUMBER:
				return packet_header.get(HEADER_SEGMENT_NUM);
			case CHECKSUM:
				return packet_header.get(HEADER_CHECKSUM);
			default:
				throw new IllegalArgumentException("HSomething is broken... bad broken");
		}
	}

//Data Methods ----------------------------------------------------------------

	//Resets the data array to default size and values
	public void resetData()
	{
		PackageData = new byte[PackageData_SIZE];
	}

	//Allows user to set a singular element in the data
	//array, given that the index is within bounds.
	//Throws IndexOutOfBoundsException if not.
	public void setPacketData(int index, byte val)
	{
		if (index >= 0)
		{
			while (index > PackageData.length)
				IncreasePacketData();
			PackageData[index] = val;
		}
		else
			throw new IndexOutOfBoundsException(
				"PACKET -- SET DATA; index = " + index);
	}

	//Takes an array of bytes to be set as the data segment.
	//If the Packet contains data already, the data is overwritten.
	//Throws IllegalArgumentException if the size of toSet does not
	//conform with the size of the data segment in the packet.
	public void setPacketData(byte[] toSet) throws IllegalArgumentException
	{
		int argSize = toSet.length;
		if (argSize > 0)
		{
			PackageData = new byte[argSize];
			for (int i = 0; i < PackageData.length; i++)
				PackageData[i] = toSet[i];
		}
		else
			throw new IllegalArgumentException(
				"PACKET -- SET DATA; toSet.length = " + toSet.length);
	}

	public byte getData(int index)
	{
		if (index >= 0 && index < PackageData.length)
			return PackageData[index];
		throw new IndexOutOfBoundsException(
				"PACKET -- GET DATA; index = " + index);
	}

	public byte[] getData()
	{
		return PackageData;
	}

	public int getDataSize()
	{
		return PackageData.length;
	}

	/**
	 * Returns the contents of this packet as a DatagramPacket
	 * @param i the IP Address to give to the DatagramPacket
	 * @param port the port number to give to the DatagramPacket
	 * @return returns the DatagramPacket fully ready to be sent
	 */
	public DatagramPacket getDatagramPacket(InetAddress i, int port)
	{
		byte[] setData = ByteBuffer.allocate(256)
				.putShort(Short.parseShort(packet_header.get(HEADER_SEGMENT_NUM)))
				.putShort(Short.parseShort(packet_header.get(HEADER_CHECKSUM)))
				.put(PackageData)
				.array();

		return new DatagramPacket(setData, setData.length, i, port);
	}

	public static Packet createPacket(DatagramPacket in)
	{
		Packet newPacket = new Packet();
		ByteBuffer bb = ByteBuffer.wrap(in.getData());
		newPacket.setHeaderValue(HEADER_ELEMENTS.SEGMENT_NUMBER, bb.getShort()+"");
		newPacket.setHeaderValue(HEADER_ELEMENTS.CHECKSUM, bb.getShort()+"");

		byte[] inData = in.getData();
		byte[] remaining = new byte[inData.length - bb.position()];
		for (int i = 0; i < remaining.length; i++)
			remaining[i] = inData[i+bb.position()];
		newPacket.setPacketData(remaining);
		return newPacket;
	}
	  //Check sum function that return the 16 bit checkSum value for a packet
	  public static short checkSum(byte[] packetBytes) {
		  short sum = 0;
		  int packetByteLength = packetBytes.length;

		  int count = 0;
		  while (count > 1) {
			  sum += ((packetBytes[count]) << 8 & 0xFF00) | ((packetBytes[count + 1]) & 0x00FF);
			  if ((sum & 0xFFFF0000) > 0) {
				  sum = (short) ((sum & 0xFF) + 1);
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
