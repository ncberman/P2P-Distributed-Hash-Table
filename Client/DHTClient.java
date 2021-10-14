package Client;
import java.io.*;
import java.util.Hashtable;

public class DHTClient
{
    private Hashtable<String, String> localTable;
    public boolean isDHT = false;

    private int serverIP = 0;

    public static void main(String[] args)
    {
        System.out.println("'Start' - To start the DHT server.\n'Stop' - To stop the DHT server.\n'Exit' - To close the program\n");

        try
        {
            BufferedReader commandLine = new BufferedReader(new InputStreamReader(System.in));
            String command = commandLine.readLine();
            while(!command.equalsIgnoreCase("exit"))
            {
                String[] tokenizedCommand = command.split(" ");
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

                    String registerMsg = "RegisterUser#" + username + "#" + ipv4 + "#" + port;
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
                else if(tokenizedCommand[0].equalsIgnoreCase("leave-dht"))
                {

                }
                else if(tokenizedCommand[0].equalsIgnoreCase("dht-rebuilt"))
                {

                }
                else if(tokenizedCommand[0].equalsIgnoreCase("deregister"))
                {

                }
                else if(tokenizedCommand[0].equalsIgnoreCase("teardown-dht"))
                {

                }
                else if(tokenizedCommand[0].equalsIgnoreCase("teardown-complete"))
                {

                }
                else
                {
                    System.out.println("Unrecognized command...");
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
}