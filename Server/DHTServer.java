//package Server;
import java.util.*;
import java.net.*;
import java.io.*;

public class DHTServer
{
    private List<DHTUserData> UserList = new LinkedList<DHTUserData>();
    private List<DHTUserData> ringList = new LinkedList<DHTUserData>();
    private boolean inProcessDHT = false;
    private boolean DHTExists = false;

    public synchronized String Command(String command)
    {
        String[] tokenizedCommand = command.split("#");

        switch(tokenizedCommand[0])
        {
            case "RegisterUser":
                if(inProcessDHT){ return "FAILURE"; }
                DHTUserData newUser = new DHTUserData(tokenizedCommand[1], tokenizedCommand[2], Integer.parseInt(tokenizedCommand[3]));
                return RegisterUser(newUser);

            case "SetupDHT":
                if(inProcessDHT){ return "FAILURE"; }
                return SetupDHT(Integer.parseInt(tokenizedCommand[1]), tokenizedCommand[2]);

            case "CompleteDHT":
                if(!inProcessDHT){ return "FAILURE"; }
                CompleteDHT(tokenizedCommand[1]);
                break;
                
            case "QueryDHT":
                if(inProcessDHT){ return "FAILURE"; }
                return QueryDHT(tokenizedCommand[1], tokenizedCommand[2]);
            
            case "LeaveDHT":
                if(inProcessDHT){ return "FAILURE"; }
                LeaveDHT(tokenizedCommand[1]);
                break;

            case "JoinDHT":
                if(inProcessDHT){ return "FAILURE"; }
                JoinDHT(tokenizedCommand[1]);
                break;
                
            case "RebuildDHT":
                if(!inProcessDHT){ return "FAILURE"; }
                RebuildDHT(tokenizedCommand[1], tokenizedCommand[2], tokenizedCommand[3], tokenizedCommand[4]);
                break;
            
            case "DeregisterUser":
                if(inProcessDHT){ return "FAILURE"; }
                return DeregisterUser(tokenizedCommand[1]);
                
            case "TeardownDHT":
                if(inProcessDHT){ return "FAILURE"; }
                TeardownDHT(tokenizedCommand[1]);
                break;
            
            case "TeardownCompleteDHT":
                if(!inProcessDHT){ return "FAILURE"; }
                TeardownCompleteDHT(tokenizedCommand[1]);
                break;

            case "ChangeState":
                
                break;
                
            default:
                return "FAILURE";
        }
        return "FAILURE";
    }

    /*
        RegisterUser function
            This function takes in a data structure 'DHTUserData' and attempts to register the given data with the user-state-table.
            To properly complete execution a few things must be true,
                1. Username is a unique username
                2. IP + Port combination must be unique
    */
    public String RegisterUser(DHTUserData newUser)
    {
        boolean doesUserExist = false;

        // Check to see if the username or ip:port already exists
        for(DHTUserData registeredUser : UserList)
        {
            // See if username already exists
            if(registeredUser.username.equalsIgnoreCase(newUser.username)) 
            {
                doesUserExist = true;
                System.out.println("Attempted to register user: " + newUser.username + " but username is already in use");
                break;
            }
            // see if ip/port combination already exists
            if(registeredUser.GetIP().equals(newUser.GetIP()) && registeredUser.GetPort() == newUser.GetPort())
            {
                doesUserExist = true;
                System.out.println("Attempted to register user: " + newUser.username + " but IP/Port combination is already in use");
                break;
            }
        }
        if(!doesUserExist)
        {
            UserList.add(newUser);
            SendMessage("Username#" + newUser.username, newUser.GetIP(), newUser.GetPort());
            System.out.println(newUser.username + " has been successfully registered");
            PrintTable();
            return "SUCCESS";
        }
        return "FAILURE";
    }

    /*
        DeregisterUser function
            This function removes the first user (which should also be the only user) with a name matching the name received.
    */
    public String DeregisterUser(String existingUser)
    {
        // Check to see if a user with the corresponding username exists
        for(DHTUserData registeredUser : UserList)
        {
            // Remove the user if the username matches and existing user and that user is also in the FREE state
            if(registeredUser.username.equals(existingUser) && registeredUser.GetState().equals("FREE"))
            {
                UserList.remove(registeredUser);
                System.out.println(existingUser + " has been deregistered");
                PrintTable();
                return "SUCCESS";
            }
        }
        System.out.println("Attempted to deregister user: " + existingUser + " but no user was found");
        return "FAILURE";
    }

    /*
        SetupDHT function
            This function starts the process to create a distributed hashtable based on the given parameters given that the parameters 
            meet our requirements.
                These requirements must be met
                1. Size of the DHT is less than or equal to the amount of registered users
                2. A DHT does not already exist
                3. The requested leader of the table exists
                4. size is greater than or equal to 2
            If all requirements are met the server will send a message to the leader containing instructions on who should be in the DHT
            and the size.
    */
    public String SetupDHT(int size, String leader)
    {
        // First lets check the simplest requirements, size requirements
        if(size < 2){ return "FAILURE"; }
        if(size > UserList.size()){ return "FAILURE"; }

        // Check if the DHT already exists
        if(DHTExists){ return "FAILURE"; }

        // Lastly lets check to see if the leader exists
        for(DHTUserData registereduser : UserList)
        {
            if(registereduser.username.equals(leader))
            {
                // Create our list of random users we want to put into the DHT
                ringList.clear();
                Random rnd = new Random();
                ringList.add(registereduser);
                registereduser.SetState("LEADER");
                int numUsers = 1;
                while(numUsers < size)
                {
                    int randomIndex = rnd.nextInt(size);
                    DHTUserData randomUser = UserList.get(randomIndex);
                    if(randomUser.GetState().equals("FREE"))
                    {
                        ringList.add(randomUser);
                        UserList.get(randomIndex).SetState("INDHT");
                        numUsers++;
                    }
                }

                // Begin DHT setup here
                inProcessDHT = true;
                String msg = "";
                for(int i = 0; i < size; i++)
                {
                    int neighborIndex = ((i+1)%size);
                    msg += "SetupDHT";                              // SetupDHT
                    msg += "#";                                     // SetupDHT#
                    msg += i;                                       // SetupDHT#0
                    msg += "#";                                     // SetupDHT#0#
                    msg += ringList.get(neighborIndex).GetIP();        // SetupDHT#0#0.0.0.0
                    msg += "#";                                     // SetupDHT#0#0.0.0.0#
                    msg += ringList.get(neighborIndex).GetPort();      // SetupDHT#0#0.0.0.0#0000
                    msg += "%";                                     // SetupDHT#0#0.0.0.0#0000$...SetupDHT#N#255.255.255.255#9999$
                }
                msg += "GetRingSize#0";                             // SetupDHT#0#0.0.0.0#0000$...SetupDHT#N#255.255.255.255#9999$GetRingSize#0
                SendMessage(msg, ringList.get(0).GetIP(), ringList.get(0).GetPort()); // ringList[0] should be the leader of the DHT
                return "SUCCESS";
            }
        }

        return "FAILURE";
    }

    /*
        CompleteDHT function
            This function allows the client to send a message to finish DHT setup, this function can only execute if the DHT is currently 
            'inProcessDHT' of being setup AND the given username is the leader of the DHT.
    */
    public String CompleteDHT(String leader)
    {
        // First let's check to see that our server is currently in the process of setting up a DHT
        if(!inProcessDHT){ return "FAILURE"; }

        // Now let's check to see if our given username is the leader of the current DHT.
        for(DHTUserData registeredUser : UserList)
        {
            if(registeredUser.username.equals(leader) && registeredUser.GetState().equals("LEADER"))
            {
                // Completes our setup process
                inProcessDHT = false;
                DHTExists = true;
                PrintTable();
                return "SUCCESS";
            }
        }

        return "FAILURE";
    }

    /*
        QueryDHT function
            This function is used to query a random user that is mantaining the DHT. The taken username must not be a member of the DHT and a 
            DHT must exist for this function to execute properly.
    */
    public String QueryDHT(String queriedUser, String countryCode)
    {
        if(!DHTExists){ System.out.println("Query attempted on DHT but DHT does not exist"); return "FAILURE"; }
        for(DHTUserData usr : UserList)
        {
            if(usr.username.equals(queriedUser) && usr.GetState().equals("FREE") && DHTExists)
            {
                Random rand = new Random();
                DHTUserData randomUsr = ringList.get(rand.nextInt(ringList.size()));
                String msg = "QueryDHT#" + countryCode + "#" + usr.GetIP() + "#" + usr.GetPort();
                System.out.println("Query on DHT has been initiated by " + queriedUser);
                SendMessage(msg, randomUsr.GetIP(), randomUsr.GetPort());
                return "SUCCESS";
            }
        }

        return "FAILURE";
    }

    /*
        LeaveDHT function
            This function initiates the removal of a DHT member leaving the DHT
    */
    public String LeaveDHT(String userToLeave)
    {
        if(!DHTExists){ System.out.println("User to attempted to leave DHT but DHT does not exist"); return "FAILURE"; }
        if(ringList.size() < 2){ System.out.println("LeaveDHT command received but DHT is too small"); return "FAILURE"; }
        for(DHTUserData usr : ringList)
        {
            if(userToLeave.equals(usr.username))
            {
                if(usr.GetState().equals("INDHT") || usr.GetState().equals("LEADER"))
                {
                    String msg = "LeaveDHT";
                    SendMessage(msg, usr.GetIP(), usr.GetPort());
                    inProcessDHT = true;
                    System.out.println(usr.username + " has begun the process of leaving the DHT");
                    PrintTable();
                    return "SUCCESS";
                }
                else
                {
                    System.out.println(usr.username + " tried to leave DHT but is not part of the DHT");
                    return "FAILURE";
                }
            }
        }
        System.out.println(userToLeave + " tried to leave the DHT but does not exist");
        return "FAILURE";
    }

    public String JoinDHT(String userToJoin)
    {
        if(!DHTExists){ System.out.println("User to attempted to leave DHT but DHT does not exist"); return "FAILURE"; }
        for(DHTUserData usr : UserList)
        {
            if(userToJoin.equals(usr.username))
            {
                if(usr.GetState().equals("FREE"))
                {
                    String msg = "JoinDHT#" + ringList.size() + "#" + ringList.get(0).GetIP() + "#" + ringList.get(0).GetPort();
                    SendMessage(msg, usr.GetIP(), usr.GetPort());
                    inProcessDHT = true;
                    System.out.println(usr.username + " has begun the process of joining the DHT");
                    PrintTable();
                    return "SUCCESS";
                }
            }
        }
        System.out.println(userToJoin + " tried to join the DHT but does not exist");
        return "FAILURE";
    }

    /*
        RebuildDHT function
            This function executes after the server has been told that the DHT has been succesfully reconfigured.
    */
    public String RebuildDHT(String userLeft, String leaderIP, String leaderPort, String polarity)
    {
        if(polarity.equals("-1"))
        {
            for(DHTUserData usr : ringList)
            {
                if(userLeft.equals(usr.username))
                {
                    ringList.remove(usr);
                }
            }

            for(DHTUserData usr : UserList)
            {
                if(userLeft.equals(usr.username))
                {
                    usr.SetState("FREE");
                    System.out.println(usr.username + " has successfully left the DHT");
                }
                if((leaderIP+leaderPort).equals(usr.GetIP()+usr.GetPort()))
                {
                    usr.SetState("LEADER");
                    System.out.println("New DHT leader is: " + usr.username);
                }
                else if(usr.GetState().equals("LEADER"))
                {
                    usr.SetState("INDHT");
                }
            }
        }
        else
        {
            for(DHTUserData usr : UserList)
            {
                if(userLeft.equals(usr.username))
                {
                    usr.SetState("INDHT");
                    System.out.println(usr.username + " has successfully joined the DHT");
                    ringList.add(usr);
                }
            }
        }
        

        inProcessDHT = false;
        PrintTable();
        return "SUCCESS";
    }

    /*
        TeardownDHT function
            this function tells the expected leader of the current DHT to teardown the current DHT
    */
    public String TeardownDHT(String leader)
    {
        if(!DHTExists){System.out.println("Teardown attempted on DHT but DHT does not exist"); return "FAILURE"; }
        for(DHTUserData usr : UserList)
        {
            if(usr.username.equals(leader) && usr.GetState().equals("LEADER"))
            {
                String msg = "TeardownDHT";
                SendMessage(msg, usr.GetIP(), usr.GetPort());
                inProcessDHT = true;
                System.out.println("Teardown process for DHT initiated successfully");
                return "SUCCESS";
            }
        }
        System.out.println(leader + " is not the current leader of the DHT");
        return "FAILURE";
    }

    /*
        TeardownCompleteDHT function
            this function is only activated when the leader of the current DHT has completed a total DHT deletion with its peers
    */
    public String TeardownCompleteDHT(String leader)
    {
        boolean leaderIsLeader = false;
        for(DHTUserData usr : UserList)
        {
            if(usr.username.equals(leader) && usr.GetState().equals("LEADER"))
            {
                leaderIsLeader = true;
                break;
            }
        }

        if(leaderIsLeader)
        {
            ringList.clear();
            for(DHTUserData usr : UserList)
            {
                if(usr.GetState().equals("LEADER") || usr.GetState().equals("INDHT"))
                {
                    usr.SetState("FREE");
                }
            }
            DHTExists = false;
            inProcessDHT = false;
            System.out.println("Teardown process for DHT completed successfully");
            PrintTable();
            return "SUCCESS";
        }
        System.out.println(leader + " cannot verify teardown");
        return "FAILURE";
    }

    public void PrintTable()
    {
        System.out.println("Username\tState\t\t\tIPv4\t\tPort");
        for(DHTUserData usr : UserList)
        {
            System.out.println(usr.username + "\t\t" + usr.GetState() + "\t\t" + usr.GetIP() + "\t\t" + usr.GetPort());
        }
    }

    public void ResetServer()
    {
        ringList.clear();
        UserList.clear();
        inProcessDHT = false;
        DHTExists = false;
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
                System.out.println("Message Sent: " + msg);
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

