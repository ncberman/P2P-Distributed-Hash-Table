package Server;
import java.io.*;

public class DHTServerClient
{
    public static void main()
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
                        listener.StopListener();
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

            listener.StopListener();
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