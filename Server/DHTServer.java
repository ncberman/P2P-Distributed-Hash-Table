package Server;
import java.util.List;

import java.net.*;
import java.io.*;

public class DHTServer
{
    private int serverPort = 38500; // Socket Group 75 is allowed to use ports 38500 - 38999

    private List<DHTUserData> UserList;
    private boolean completeDHT = false;

    public synchronized String Command(String command)
    {
        String[] tokenizedCommand = command.split("#");

        switch(tokenizedCommand[0])
        {
            case "RegisterUser":
                if(completeDHT){ return "FAILURE"; }
                DHTUserData newUser = new DHTUserData(tokenizedCommand[1], tokenizedCommand[2], Integer.parseInt(tokenizedCommand[3]));
                return RegisterUser(newUser);

            case "SetupDHT":
                if(completeDHT){ return "FAILURE"; }
                return SetupDHT(Integer.parseInt(tokenizedCommand[1]), tokenizedCommand[2]);

            case "CompleteDHT":
                break;
                
            case "QueryDHT":
                if(completeDHT){ return "FAILURE"; }
                return QueryDHT(tokenizedCommand[1]);
            
            case "LeaveDHT":
                if(completeDHT){ return "FAILURE"; }
                break;
                
            case "RebuildDHT":
                if(completeDHT){ return "FAILURE"; }
                break;
            
            case "DeregisterUser":
                if(completeDHT){ return "FAILURE"; }
                DHTUserData existingUser = new DHTUserData(tokenizedCommand[1], tokenizedCommand[2], Integer.parseInt(tokenizedCommand[3]));
                return DeregisterUser(existingUser);
                
            case "TeardownDHT":
                if(completeDHT){ return "FAILURE"; }
                break;
            
            case "TeardownCompleteDHT":
                if(completeDHT){ return "FAILURE"; }
                break;
                
            default:
                return "FAILURE";
        }
        return "FAILURE";
    }

    // Registers a new user to our list of users so that other users can find them using the server
    public String RegisterUser(DHTUserData newUser)
    {
        boolean doesUserExist = false;
        for(DHTUserData registeredUser : UserList)
        {
            if(registeredUser.username.equalsIgnoreCase(newUser.username))
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

    // Removes the user from the list of registered users so that they may no longer be used in DHT creation
    public String DeregisterUser(DHTUserData existingUser)
    {
        boolean doesUserExist = false;
        for(DHTUserData registeredUser : UserList)
        {
            if(registeredUser.username.equals(existingUser.username) && registeredUser.GetState().equals("FREE"))
            {
                doesUserExist = true;
                existingUser = registeredUser;
                break;
            }
        }
        if(doesUserExist)
        {
            UserList.remove(existingUser);
            return "SUCCESS";
        }
        return "FAILURE";
    }

    // Sets up a distributed hash table with specified size and leader
    public String SetupDHT(int size, String leader)
    {
        boolean doesUserExist = false;
        DHTUserData leaderData;
        for(DHTUserData registeredUser : UserList)
        {
            if(registeredUser.username.equals(leader) && registeredUser.GetState().equals("FREE"))
            {
                doesUserExist = true;
                leaderData = registeredUser;
                break;
            }
        }
        if(!doesUserExist){ return "FAILURE"; }
        if(size < 2 || UserList.size() < size){ return "FAILURE"; }

        return "SUCCESS";
    }

    public String QueryDHT(String queriedUser)
    {
        return "FAILURE";
    }
}

