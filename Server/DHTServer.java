package Server;
import java.util.List;

import java.net.*;
import java.io.*;

public class DHTServer
{
    private int serverPort = 38500; // Socket Group 75 is allowed to use ports 38500 - 38999

    private List<DHTUserData> UserList;
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

            case "inProcessDHT":
                break;
                
            case "QueryDHT":
                if(inProcessDHT){ return "FAILURE"; }
                return QueryDHT(tokenizedCommand[1]);
            
            case "LeaveDHT":
                if(inProcessDHT){ return "FAILURE"; }
                break;
                
            case "RebuildDHT":
                if(inProcessDHT){ return "FAILURE"; }
                break;
            
            case "DeregisterUser":
                if(inProcessDHT){ return "FAILURE"; }
                return DeregisterUser(tokenizedCommand[1]);
                
            case "TeardownDHT":
                if(inProcessDHT){ return "FAILURE"; }
                break;
            
            case "TeardowninProcessDHT":
                if(inProcessDHT){ return "FAILURE"; }
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
                break;
            }
            // see if ip/port combination already exists
            if(registeredUser.GetIP().equals(newUser.GetIP()) && registeredUser.GetPort() == newUser.GetPort())
            {
                doesUserExist = true;
                break;
            }
        }
        if(!doesUserExist)
        {
            UserList.add(newUser);
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
                return "SUCCESS";
            }
        }
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
                // Begin DHT setup here
                inProcessDHT = true;
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
    public String QueryDHT(String queriedUser)
    {
        return "FAILURE";
    }
}

