package Server;

// Class used to create objects representing our Users to help with data organization
public class DHTUserData
{
    public String username;
    private String ipAddr;
    private int port;
    private String state;

    // Constructor to store our given variables
    public DHTUserData(String usr, String ip, int prt)
    {
        ipAddr = ip;
        username = usr;
        port = prt;
        state = "FREE";
    }

    public void SetState(String newState)
    {
        state = newState;
    }

    public String GetState()
    {
        return state;
    }

    public void SetPort(int newPort)
    {
        port = newPort;
    }

    public int GetPort()
    {
        return port;
    }

    public void SetIP(String newIP)
    {
        ipAddr = newIP;
    }

    public String GetIP()
    {
        return ipAddr;
    }
}