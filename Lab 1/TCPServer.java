import java.io.*;
import java.net.*;

class TCPServer{
  public static void main(String[] args) throws Exception{
    String clientSentence;
    String capitalizedSentence;
    int port = 6789;

    ServerSocket welcomeSocket = new ServerSocket(port);

    System.out.print("Getting IP..."); //remove later
    String localhost = InetAddress.getLocalHost().getHostAddress().trim();  //grabs IP to use for Client
    System.out.println(localhost);
    
    while(true){
      Socket connectionSocket = welcomeSocket.accept();
      BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
      DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());

      clientSentence = inFromClient.readLine();

      capitalizedSentence = clientSentence.toUpperCase() + '\n';

      outToClient.writeBytes(capitalizedSentence);
    }
  }
}
