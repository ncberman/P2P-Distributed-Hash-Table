package Server;

import java.io.IOException;
import java.net.ServerSocket;

public class DHTServerListener 
{
    private static DHTServer server = new DHTServer();
    private boolean listening = false;

    public DHTServerListener(int port)
    {
        try
        {
            ServerSocket mySocket = new ServerSocket(port);
            while(listening)
            {
                new DHTServerThread(mySocket.accept(), server).start();
            }
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }
}
