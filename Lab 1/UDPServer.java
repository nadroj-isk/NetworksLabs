import java.io.*;
import java.net.*;

class UDPServer{
  public static void main(String[] args) throws Exception {
      int[] ports = {10028, 10029, 10030, 10031};
      DatagramSocket serverSocket = new DatagramSocket(10029);

      byte[] receiveData = new byte[1024];
      byte[] sendData = new byte[1024];

      System.out.print("Getting IP..."); //remove later
      String localhost = InetAddress.getLocalHost().getHostAddress().trim();
      System.out.println(localhost);
      while(true){
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        serverSocket.receive(receivePacket);
        String sentence = new String(receivePacket.getData());

        InetAddress IPAddress = receivePacket.getAddress();
        int port = receivePacket.getPort();

        String capitalizedSentence = sentence.toUpperCase();

        sendData = capitalizedSentence.getBytes();

        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);

        serverSocket.send(sendPacket);
      }
  }
}
