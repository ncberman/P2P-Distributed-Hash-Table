//package Client;

import java.io.IOException;
import java.net.ServerSocket;

public class DHTClientListener extends Thread
{
    public boolean listening = false;
    private DHTClientData clientData;
    private int port;

    public DHTClientListener(DHTClientData clientData, int port)
    {
        this.clientData = clientData;
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
                new DHTClientThread(mySocket.accept(), clientData).start();
            }
            mySocket.close();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    public void StopListener()
    {
        listening = false;
    }
}