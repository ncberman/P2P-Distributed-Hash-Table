package Client;

import java.net.*;
import java.util.Dictionary;
import java.util.Hashtable;
import java.io.*;

public class DHTClientData 
{
    private int myID = -1;
    private int ringSize = -1;
    private String neighborIP;
    private int neighborPort;
    private Hashtable<Integer, String[]> localTable;

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

        localTable = new Hashtable<Integer, String[]>(353);

        // Build our message to send to the next client
        String msg = "";
        for(int i = 1; i < tokenizedCommandSet.length; i++)
        {
            msg += tokenizedCommandSet[i];
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
            return;
        }

        ringSize++;
        String msg = "GetRingSize#" + ringSize;
        SendMessage(msg, neighborIP, neighborPort);
    }

    private void Store(String[] tokenizedCommandSet, String[] tokenizedCommand)
    {
        if(myID == -1){ return; }

        int asciiValue = 0;
        for(int i = 0; i < tokenizedCommand[4].length(); i++)
        {
            asciiValue += tokenizedCommand[4].charAt(i);
        }
        int hashKey = asciiValue % 353;
        if((hashKey % ringSize) == myID)
        {
            String[] tableEntry = { tokenizedCommand[1],
                                    tokenizedCommand[2],
                                    tokenizedCommand[3],
                                    tokenizedCommand[4],
                                    tokenizedCommand[5],
                                    tokenizedCommand[6],
                                    tokenizedCommand[7],
                                    tokenizedCommand[8],
                                    tokenizedCommand[9] };
            localTable.put(hashKey, tableEntry);
        }
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
