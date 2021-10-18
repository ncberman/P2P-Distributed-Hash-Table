//package Server;

import java.io.IOException;
import java.net.ServerSocket;

public class DHTServerListener extends Thread
{
    public boolean listening = false;
    private DHTServer server;
    private int port;

    public DHTServerListener(DHTServer server, int port)
    {
        this.server = server;
        this.port = port;
    }

    public void run()
    {
        listening = true;
        try
        {
            ServerSocket mySocket = new ServerSocket(port);
            while(listening)
            {
                System.out.println("Waiting for connections . . .");
                new DHTServerThread(mySocket.accept(), server).start();
            }
            mySocket.close();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }
}
