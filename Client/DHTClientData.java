package Client;

import java.net.*;
import java.util.Hashtable;
import java.util.List;
import java.util.Scanner;

import java.io.*;

public class DHTClientData 
{
    private String myIP;
    private int myPort;
    private String username;
    private String serverIP;
    private int serverPort = 0;
    private int myID = -1;
    private int ringSize = 0;
    private String neighborIP;
    private int neighborPort;
    private Hashtable<Integer, List<String[]>> localTable;

    public DHTClientData(String myIP, int myPort, String serverIP, int serverPort)
    {
        this.myIP = myIP;
        this.myPort = myPort;
        this.serverIP = serverIP;
        this.serverPort = serverPort;
    }

    public void Command(String input)
    {
        /*
            Client Data inputs many times contains information meant for separate clients. To differentiate which data is meant for which clients 
            my string tokenizer symbols have become '$' to separate commands for different clients and '#' to separate individual pieces of information
            within a single clients command.        
        */
        String[] tokenizedCommandSet = input.split("$");
        String[] tokenizedCommand = tokenizedCommandSet[0].split("#");

        switch(tokenizedCommand[0])
        {
            case "SetupDHT":
                SetupDHT(tokenizedCommandSet, tokenizedCommand);
                break;
            
            case "GetRingSize":
                GetRingSize(tokenizedCommand);
                break;

            case "Store":
                Store(tokenizedCommandSet, tokenizedCommand);
                break;

            case "Username":
                username = tokenizedCommand[1];
                break;

            case "QueryUser":
                BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                System.out.println("Enter what country you'd like to query: ");
                String query;
                try 
                {
                    query = reader.readLine();
                    String msg = "QueryDHT#" + query + "#" + myIP + "#" + myPort;
                    SendMessage(msg, tokenizedCommand[2], Integer.parseInt(tokenizedCommand[3]));
                } 
                catch (IOException e) 
                {
                    e.printStackTrace();
                }
                break;

            case "QueryDHT":
                QueryDHT(tokenizedCommandSet, tokenizedCommand);
                break;

            case "QueryResponse":
                if(tokenizedCommand[1].equals("ERROR")){ System.out.println("The queried name does not exist."); }
                System.out.println("Country Code: " + tokenizedCommand[1]);
                System.out.println("Short Name: " + tokenizedCommand[2]);
                System.out.println("Table Name: " + tokenizedCommand[3]);
                System.out.println("Long Name: " + tokenizedCommand[4]);
                System.out.println("2-Alpha Code: " + tokenizedCommand[5]);
                System.out.println("Currency Unit: " + tokenizedCommand[6]);
                System.out.println("Region: " + tokenizedCommand[7]);
                System.out.println("WB-2 Code: " + tokenizedCommand[8]);
                System.out.println("Latest Population Census: " + tokenizedCommand[9]);
                break;

            case "TeardownDHT":
                TeardownDHT();
                break;

            case "LeaveDHT":
                LeaveDHT();
                break;

            case "NewLeader":
                RebuildDHT();
                break;

            case "NewNeighbor":
                NewNeighbor(tokenizedCommandSet, tokenizedCommand);
                break;

            default:
                
        }
    }

    /*
        This command sets up this particular client to be a part of the DHT by giving it an ID as well as its neighborsIP and port like a linked list
        intakes command set as 'SetupDHT # myID # neighborIP # neighborPort' excluding whitespace.
    */
    private void SetupDHT(String[] tokenizedCommandSet, String[] tokenizedCommand)
    {
        // Set local variables based on incoming information
        myID = Integer.parseInt(tokenizedCommand[1]);
        neighborIP = tokenizedCommand[2];
        neighborPort = Integer.parseInt(tokenizedCommand[3]);

        localTable = new Hashtable<Integer, List<String[]>>(353);

        // Build our message to send to the next client
        String msg = tokenizedCommandSet[1];
        if(tokenizedCommandSet.length > 1)
        {
            for(int i = 1; i < tokenizedCommandSet.length; i++)
            {
                msg += ("$" + tokenizedCommandSet[i]);
            }
        }
        
        SendMessage(msg, neighborIP, neighborPort);
    }

    /*
        This command takes in the GetRingSize input which tells the clients to add 1 to the count and then pass to the next client in the ring until
        it has gone around the entire ring
    */
    private void GetRingSize(String[] tokenizedCommand)
    {
        int count = Integer.parseInt(tokenizedCommand[1]);

        // If we have made a pass and this client is the leader of the DHT we return
        if(count != 0 && myID == 0)
        {
            ringSize = count;
            DistributeData();
            return;
        }

        myID = ringSize;
        ringSize++;
        String msg = "GetRingSize#" + ringSize;
        SendMessage(msg, neighborIP, neighborPort);
    }

    /*
        Distribute data reads all of the individual lines of the CSV we wish to read and converts it to our command-readable format. It gives us a separate
        command for each line of data.
    */
    private void DistributeData()
    {
        File csvFile = new File("StatsCountry.csv");
        try 
        {
            Scanner fileReader = new Scanner(csvFile);
            String line = fileReader.nextLine(); // Skip the first line of the file
            while(fileReader.hasNextLine())
            {
                line = fileReader.nextLine();
                String[] csvSeparate = line.split(","); // Split 'A,B,C' to { A, B, C }
                String command = "Store"; // Begin building command to send to self 'Store'

                for (String str : csvSeparate) // build the full store command, 'Store#A#B#C'
                {
                    command += ("#" + str);
                }

                Command(command);
            }
            fileReader.close();
            /*
                At this point our DHT should be completely set up and we will send a message back to the server letting it know that the DHT is complete.
            */
            String msg = "CompleteDHT#" + username;
            SendMessage(msg, serverIP, serverPort);
        } 
        catch (FileNotFoundException e) 
        {
                e.printStackTrace();
        }
    }

    /*
        The Store command looks at the given command string and calculates if the data that we want to store should be stored in this client's hashtable
        or if it should be passed on to its neighbor.
    */
    private void Store(String[] tokenizedCommandSet, String[] tokenizedCommand)
    {
        // Check to see if this client is actually a part of a DHT
        if(myID == -1){ return; }

        // tokenizedCommand[4] represents the 'long name' of our table entry, our hash function uses the sum of the ascii values of the long name so we
        // calculate that here.
        int asciiValue = 0;
        for(int i = 0; i < tokenizedCommand[4].length(); i++)
        {
            asciiValue += tokenizedCommand[4].charAt(i);
        }
        // Our hash function is the sum of the long name mod 353
        int hashKey = asciiValue % 353;

        // If this message should be stored on this client then we put it in the hash table at the appropriate position
        if((hashKey % ringSize) == myID)
        {
            
            String[] tableEntry = { tokenizedCommand[1],    // Country Code
                                    tokenizedCommand[2],    // Short Name
                                    tokenizedCommand[3],    // Table Name
                                    tokenizedCommand[4],    // Long Name
                                    tokenizedCommand[5],    // 2-Alpha Code
                                    tokenizedCommand[6],    // Currency
                                    tokenizedCommand[7],    // Region
                                    tokenizedCommand[8],    // WB-2 Code
                                    tokenizedCommand[9] };  // Latest Census
            localTable.get(hashKey).add(tableEntry);
        }
        // Otherwise we pass the store message to the next client in the ring
        else
        {
            String msg = tokenizedCommandSet[0];
            SendMessage(msg, neighborIP, neighborPort);
        }
    }

    /*
        intakes command QueryDHT LongName QuerierIP QuerierPort and searches its local hash table for the entry
    */
    private void QueryDHT(String[] tokenizedCommandSet, String[] tokenizedCommand)
    {
        // Check to see if this client is actually a part of a DHT
        if(myID == -1){ return; }

        // tokenizedCommand[4] represents the 'long name' of our table entry, our hash function uses the sum of the ascii values of the long name so we
        // calculate that here.
        int asciiValue = 0;
        for(int i = 0; i < tokenizedCommand[2].length(); i++)
        {
            asciiValue += tokenizedCommand[2].charAt(i);
        }
        // Our hash function is the sum of the long name mod 353
        int hashKey = asciiValue % 353;
        // If the hashkey matchess this client's id
        if((hashKey % ringSize) == myID)
        {
            for(String[] entry : localTable.get(hashKey))
            {
                if(entry[3].equals(tokenizedCommand[1]))
                {
                    String msg = "QueryResponse";
                    for(String str : entry)
                    {
                        msg += ("#" + str);
                    }
                    SendMessage(msg, tokenizedCommand[2], Integer.parseInt(tokenizedCommand[3])); // This response returns the entry's information
                    return;
                }
            }
            String msg = "QueryResponse#ERROR";
            SendMessage(msg, tokenizedCommand[2], Integer.parseInt(tokenizedCommand[3])); // This is the response we give if the entry should be in this table but isn't
            return;
        }
        SendMessage(tokenizedCommandSet[0], neighborIP, neighborPort); // If we don't expect the queried entry to be in this hash table then we pass it on to the next client in the DHT.
    }

    /*
        Teardown DHT passes the teardown command around the ring telling all the DHT participants to delete their neighbor info and their hashtable
        after the message has been passed around the whole ring the leader deletes its information and tells the server.
    */
    private void TeardownDHT()
    {
        if(myID != -1)
        {
            SendMessage("TeardownDHT", neighborIP, neighborPort);
        }
        if(myID != 0)
        {
            neighborIP = "";
            neighborPort = -1;
            localTable.clear();
        }
        if(myID == -1)
        {
            SendMessage("TeardownCompleteDHT", serverIP, serverPort);
        }
        myID = -1;
    }

    private void LeaveDHT()
    {
        String msg = "NewNeighbor#" + (myID-1) + "#" + neighborIP + "#" + neighborPort;
        SendMessage(msg, neighborIP, neighborPort);
    }

    private void RebuildDHT()
    {
        myID = 0;
        String[] str = { "GetRingSize", "0" };
        GetRingSize(str);
    }

    private void NewNeighbor(String[] tokenizedCommandSet, String[] tokenizedCommand)
    {
        // If this client matches the id of the client we want to change then change neighbor info
        if(Integer.parseInt(tokenizedCommand[1]) == myID)
        {
            neighborIP = tokenizedCommand[2];
            neighborPort = Integer.parseInt(tokenizedCommand[3]);
        }
        // Otherwise pass on the message to the next client
        else
        {
            String msg = tokenizedCommandSet[0];
            SendMessage(msg, neighborIP, neighborPort);
        }
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
