package Client;
import java.io.*;
import java.net.*;
import java.util.Hashtable;

public class DHTClient
{
    private Hashtable<String, String> localTable;
    public boolean isDHT = false;

    private static String serverIP = "127.0.0.1";
    private static int serverPort = 38500;
    private static DHTClientData clientData;
    private static DHTClientListener listener;

    public static void main(int argc, String[] args) throws UnknownHostException, IOException
    {
        if(argc < 2|| Integer.parseInt(args[1]) > 38999 || Integer.parseInt(args[1]) < 38501){ System.out.println("Port must be between 38500 and 39000"); return; }
        clientData  = new DHTClientData();
        listener = new DHTClientListener(clientData, Integer.parseInt(args[1]));
        listener.start();
        
        // Connect our client to our server
        Socket serverSocket = new Socket(InetAddress.getByName(serverIP), serverPort);
        PrintWriter out = new PrintWriter(serverSocket.getOutputStream(), true);
        BufferedReader in = new BufferedReader( new InputStreamReader(serverSocket.getInputStream()));
        

        try
        {
            BufferedReader commandLine = new BufferedReader(new InputStreamReader(System.in));
            String command = commandLine.readLine();
            while(!command.equalsIgnoreCase("exit"))
            {
                String[] tokenizedCommand = command.split(" ");
                String msg = "";
                /*
                    Our first command will be the 'register' command, this command requires 3 inputs variables being
                        1. Username
                        2. IP Address
                        3. Port Number
                    The command will construct a #-Separated string with a header using a default register command header 'RegisterUser'

                    The purpose of this command is to tell the watch-server to register a new user with the given specifications. The server will
                    ping the given user to confirm the registration and availability of newly added users.
                */
                if(tokenizedCommand[0].equalsIgnoreCase("register"))
                {
                    String username = tokenizedCommand[1];
                    String ipv4 = tokenizedCommand[2];
                    String port = tokenizedCommand[3];

                    msg = "RegisterUser#" + username + "#" + ipv4 + "#" + port;
                    out.write(msg);
                }
                /*
                    Our second command will be the 'setup-dht' command, takes 2 inputs variables being
                        1. DHT-Size >= 2
                        2. Username of DHT Leader
                    This command will construct a #-Separated with the header using the default setup-dht header 'SetupDHT'

                    The purpose of this command is to tell the watch-server to setup a distributed hash-table using the specified user
                    as a leader and grabbing n-1 other users to construct the table framework.
                */
                else if(tokenizedCommand[0].equalsIgnoreCase("setup-dht"))
                {
                    String size = tokenizedCommand[1];
                    String username = tokenizedCommand[2];

                    msg = "SetupDHT#" + size + "#" + username;
                    out.write(msg);
                }
                /*
                    Our third command will be the 'dht-complete' command, takes 1 input variable being
                        1. Username of DHT Leader
                    This command will construct a #-Separated with the header using the default dht-complete header 'CompleteDHT'

                    The purpose of this command is to see if the given user is the leader of the current DHT. The server will respond to this 
                    message with either SUCCESS or FAILURE depending on if the state of the given username is 'LeaderDHT' or not. In relation
                    a server will only recognize and set the state of a registered user to leader / worker if and only if the DHT has completed
                    its setup.
                */
                else if(tokenizedCommand[0].equalsIgnoreCase("dht-complete"))
                {
                    String username = tokenizedCommand[1];

                    msg = "CompleteDHT#" + username;
                    out.write(msg);
                }
                /*
                    Our fourth command will be the 'query-dht' command, takes 1 input variable being
                        1. Querying username
                    This command will construct a #-Separated with the header using the default query-dht header 'QueryDHT'

                    The purpose of this command is to see return the username, ip address, and port number of one of the various members of
                    the DHT. If the username specified is a member of the DHT or is not registered with the server then the query fails.
                */
                else if(tokenizedCommand[0].equalsIgnoreCase("query-dht"))
                {

                }
                /*
                    Our fifth command will be the 'leave-dht' command, takes 1 input variable being
                        1. Username to be removed
                    This command will construct a #-Separated with the header using the default leave-dht header 'LeaveDHT'

                    The purpose of this command is to begin the process of removing a specified user from the DHT. After the DHT has been
                    modified by the successful execution of this command the server will only accept the command 'dht-rebuilt'.
                */
                else if(tokenizedCommand[0].equalsIgnoreCase("leave-dht"))
                {

                }
                /*
                    Our sixth command will be the 'dht-rebuilt' command, takes 2 input variables being
                        1. Username that was removed
                        2. Username of new DHT leader
                    This command will construct a #-Separated with the header using the default dht-rebuilt header 'RebuildDHT'

                    The purpose of this command is to be used in conjunction with 'leave-dht' command, this command is the only command that
                    can be accepted by the server after the leave-dht has executed succesfully. Rebuilds the DHT after the given username 
                    has been removed and set to free.
                */
                else if(tokenizedCommand[0].equalsIgnoreCase("dht-rebuilt"))
                {

                }
                /*
                    Our seventh command will be the 'deregister' command, takes 1 input variable being
                        1. Username that wants to be deregistered
                    This command will construct a #-Separated with the header using the default deregister header 'DeregisterUser'

                    The purpose of this command is to tell the watch-server that we would no longer like to register the specified user
                    with the DHT service.
                */
                else if(tokenizedCommand[0].equalsIgnoreCase("deregister"))
                {

                }
                /*
                    Our eighth command will be the 'teardown-dht' command, takes 1 input variable being
                        1. Username of the leader of the DHT
                    This command will construct a #-Separated with the header using the default teardown-dht header 'TeardownDHT'

                    The purpose of this command is to tell the leader of the current DHT to begin DHT deletion. The username inputted by
                    this command must be the username of the current DHT leader.
                */
                else if(tokenizedCommand[0].equalsIgnoreCase("teardown-dht"))
                {

                }
                /*
                    Our last command will be the 'teardown-complete' command, takes 1 input variable being
                        1. Username of the leader of the old DHT
                    This command will construct a #-Separated with the header using the default teardown-complete header 'TeardownCompleteDHT'

                    The purpose of this command is to tell the leader of the recently deleted DHT to finalize DHT deletion. This means
                    that the server should set all the users that were a part of the DHT to be set to the free state.
                */
                else if(tokenizedCommand[0].equalsIgnoreCase("teardown-complete"))
                {

                }
                else
                {
                    System.out.println("Unrecognized command...");
                }

                if(!msg.equals(""))
                {
                    SendMessage(msg, serverIP, serverPort);
                }

                // We handle the server response here.
                String serverResponse = in.readLine();
                String[] tokenizedServerResponse = serverResponse.split("#");
                for(String token : tokenizedServerResponse)
                {
                    System.out.println(token);
                }
                command = commandLine.readLine();
            }

            System.out.println("Exiting Program");
            try 
            {
                Thread.sleep(1000);
            } 
            catch (InterruptedException e) { e.printStackTrace(); }
        }
        catch(IOException e){ System.out.println(e); }
    }

    // Our Method to send messages from
    public static void SendMessage(String msg, String ipaddr, int port)
    {
        InetAddress address;
        try
        {
            address = InetAddress.getByName(ipaddr);

            try
            {
                Socket socket = new Socket(address, port);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                out.write(msg);
                out.close();
                socket.close();
            } 
            catch (IOException e) 
            {
                e.printStackTrace();
            }
        }
        catch (UnknownHostException e) 
        {
            e.printStackTrace();
        }
        
    }
}