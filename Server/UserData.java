package Server;

public class UserData
{
    public String username;
    private byte[] ipAddr;
    private int port;
    private String state;

    public UserData(String usr, byte[] ip, int prt)
    {
        ipAddr = new byte[4];
        ipAddr[0] = ip[0];
        ipAddr[1] = ip[1];
        ipAddr[2] = ip[2];
        ipAddr[3] = ip[3];

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

    public void SetIP(byte[] newIP)
    {
        ipAddr[0] = newIP[0];
        ipAddr[1] = newIP[1];
        ipAddr[2] = newIP[2];
        ipAddr[3] = newIP[3];
    }

    public byte[] GetIP()
    {
        return ipAddr;
    }
}