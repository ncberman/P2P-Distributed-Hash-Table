//package Server;
import java.io.*;
import java.net.*;


public class DHTServerClient
{
    public static void main(String[] args)
    {
        DHTServer server = new DHTServer();
        DHTServerListener listener = new DHTServerListener(server, 38500);
        System.out.println("'Start' - To start the DHT server.\n'Stop' - To stop the DHT server.\n'Exit' - To close the program\n");

        try
        {
            BufferedReader commandLine = new BufferedReader(new InputStreamReader(System.in));
            String command = commandLine.readLine();
            while(!command.equalsIgnoreCase("exit"))
            {
                if(command.equalsIgnoreCase("restart"))
                {
                    server.ResetServer();
                    listener.listening = false;
                    SendMessage("Exit", "127.0.0.1", 38500);
                    listener.start();                    
                }
                if(command.equalsIgnoreCase("table"))
                {
                    server.PrintTable();
                }
                if(command.equalsIgnoreCase("start"))
                {
                    if(!listener.isAlive())
                    {
                        listener.start();
                        System.out.println("Server successfully started!");
                    }
                    else{ System.out.println("Server is already running."); }
                }
                else if(command.equalsIgnoreCase("stop"))
                {
                    if(listener.isAlive())
                    {
                        listener.listening = false;
                        SendMessage("Exit", "127.0.0.1", 38500);
                        System.out.println("Server successfully stopped!");
                    }
                    else{ System.out.println("Server is not currently running."); }
                }
                else
                {
                    System.out.println("Unrecognized command...\n'Start' - To start the DHT server.\n'Stop' - To stop the DHT server.\n'Exit' - To close the program\n");
                }

                command = commandLine.readLine();
            }

            System.out.println("Exiting Program");
            listener.listening = false;
            SendMessage("Exit", "127.0.0.1", 38500);
        }
        catch(IOException e){ System.out.println(e); }
    }
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
                //System.out.println("Message Sent: " + msg);
                out.println(msg);
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