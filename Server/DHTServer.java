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
                return DeregisterUser(tokenizedCommand[1]);
                
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

