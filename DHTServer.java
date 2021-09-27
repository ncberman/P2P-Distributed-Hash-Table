import java.util.List;
import java.net.*;
import java.io.*;

public class DHTServer 
{
    private ServerSocket serverSocket;
    private int serverPort = 38500; // Socket Group 75 is allowed to use ports 38500 - 38999

    private List<UserData> UserList;

    // Constantly blocks and listens to our socket on our port for incoming messages, when a message is received we create a new listener class to read the message.
    public void StartServer() throws IOException
    {
        serverSocket = new ServerSocket(serverPort);
        while(true)
        {
            new ClientListener(serverSocket.accept()).Listen();
        }
    }

        private class ClientListener extends Thread
        {
            private Socket clientSocket;
            private PrintWriter outMessage;
            private BufferedReader inMessage;
        
            public ClientListener(Socket socket)
            {
                this.clientSocket = socket;
            }
        
            public void Listen()
            {
                // Try to listen to our message from a client
                try
                {
                    outMessage = new PrintWriter(clientSocket.getOutputStream(), true);
                    inMessage = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    
                    // Here we will figure out how to parse the message expected given formats
                    String returnMsg = ClientMessageParser(inMessage.readLine());
                    outMessage.println(returnMsg);
            
                    // We have finished reading and responding to the client's request / message
                    inMessage.close();
                    outMessage.close();
                    clientSocket.close();
                }
                catch(IOException e){ System.out.println(e.toString()); }
            }

            private String ClientMessageParser(String msg)
            {
                // The format of our client's messages is in command:input:input:... where ':' is our tokenizer / delimiter 
                // This also means that ':' must be a disallowed character for inputs as to not disrupt our server's parser
                String[] tokenizedMsg = msg.split(":");

                // This state represents parsing a message with the case of registering a new User
                if(tokenizedMsg[0].equals("RegisterUser"))
                {
                    // Save our message's username string
                    String username = tokenizedMsg[1];
                    // Save our message's port number
                    int port = Integer.parseInt(tokenizedMsg[3]);
                    // Convert a string in the form of an IPv4 address to the byte array of said address
                    String[] tokenizedIPAddress = tokenizedMsg[2].split(".");
                    byte[] ipAddr = new byte[4];
                    for(int i = 0; i < 4; i++)
                    {
                        ipAddr[i] = Byte.parseByte(tokenizedIPAddress[i]);
                    }

                    UserData newUser = new UserData(username, ipAddr, port);
                    return RegisterUser(newUser);
                }
                // This state represents 
                if(tokenizedMsg[0].equals("DeregisterUser"))
                {
                    // Save our message's username string
                    String username = tokenizedMsg[1];
                    int port = 0;
                    byte[] ipAddr = new byte[] { 0, 0, 0, 0};

                    UserData newUser = new UserData(username, ipAddr, port);
                    return DeregisterUser(newUser);
                }
                if(tokenizedMsg[0].equals("SetupDHT"))
                {

                }
                if(tokenizedMsg[0].equals("QueryDHT"))
                {

                }

                return "default message";
            }
        }

    // Registers a new user to our list of users so that other users can find them using the server
    public String RegisterUser(UserData newUser)
    {
        boolean doesUserExist = false;
        for(UserData registeredUser : UserList)
        {
            if(registeredUser.username.equalsIgnoreCase(newUser.username))
            {
                doesUserExist = true;
                break;
            }
        }
        if(!doesUserExist)
        {
            UserList.add(newUser);
            return "SUCCESS";
        }
        return "FAILURE";
    }

    // Removes the user from the list of registered users so that they may no longer be used in DHT creation
    public String DeregisterUser(UserData existingUser)
    {
        boolean doesUserExist = false;
        for(UserData registeredUser : UserList)
        {
            if(registeredUser.username.equals(existingUser.username) && registeredUser.GetState().equals("FREE"))
            {
                doesUserExist = true;
                existingUser = registeredUser;
                break;
            }
        }
        if(doesUserExist)
        {
            UserList.remove(existingUser);
            return "SUCCESS";
        }
        return "FAILURE";
    }
}

