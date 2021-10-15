package Client;

import java.io.*;
import java.net.*;

public class DHTClientThread extends Thread
{
    private DHTClientData clientData;
    private Socket socket;

    /* 
        Constructor to handle the creation of a new connection thread, handles our current socket connection 
        as well as our static server object we might wish to modify
    */
    public DHTClientThread(Socket socket, DHTClientData clientData)
    {
        this.clientData = clientData;
        this.socket = socket;
    }

    /*
        The part of the thread that actually handles the streams from our socket
    */
    public void run()
    {
        try
        {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in  = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String inputString;
            while((inputString = in.readLine()) != null)
            {
                clientData.Command(inputString);
            }

            socket.close();
            out.close();
            in.close();
        } 
        catch (IOException e)
        {
            e.printStackTrace();
        }
        
    }
}
