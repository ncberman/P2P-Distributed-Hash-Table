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
                if(tokenizedCommand[0].equalsIgnoreCase("register"))
                {
                    String username = tokenizedCommand[1];
                    String ipv4 = tokenizedCommand[2];
                    String port = tokenizedCommand[3];

                    String registerMsg = "RegisterUser#" + username + "#" + ipv4 + "#" + port;
                }
                else if(tokenizedCommand[0].equalsIgnoreCase("setup-dht"))
                {

                }
                else if(tokenizedCommand[0].equalsIgnoreCase("dht-complete"))
                {

                }
                else if(tokenizedCommand[0].equalsIgnoreCase("query-dht"))
                {

                }
                else if(tokenizedCommand[0].equalsIgnoreCase("deregister"))
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