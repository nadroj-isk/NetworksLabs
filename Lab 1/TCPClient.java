import java.io.*;
import java.net.*;

class TCPClient{
  public static void main(String[] args) throws Exception {
    String sentence;
    String modifiedSentence;
    String hostname = "hostname";
    int port = 6789;

    //Create input stream
    BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));

    //Create client socket, connect to server
    Socket clientSocket = new Socket(hostname, port);

    //Create output stream attached to socket
    DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());

    //create input stream attached to socket
    BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

    System.out.print("Please type in message to be converted: ");
    sentence = inFromUser.readLine();

    System.out.println("Sending Packet..."); //remove later
    outToServer.writeBytes(sentence + '\n');  //send line to server

    System.out.println("Receiving Packet..."); //remove later
    modifiedSentence = inFromServer.readLine(); //read line from server

    System.out.println("FROM SERVER: " + modifiedSentence);

    clientSocket.close();

  }
}
