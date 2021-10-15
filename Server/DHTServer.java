package Server;
import java.util.List;

import java.net.*;
import java.io.*;

public class DHTServer
{
    private int serverPort = 38500; // Socket Group 75 is allowed to use ports 38500 - 38999

    private List<UserData> UserList;

    // Registers a new user to our list of users so that other users can find them using the server
    public String RegisterUser(UserData newUser)
    {
        boolean doesUserExist = false;
        for(UserData registeredUser : UserList)
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
    public String DeregisterUser(UserData existingUser)
    {
        boolean doesUserExist = false;
        for(UserData registeredUser : UserList)
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
        UserData leaderData;
        for(UserData registeredUser : UserList)
        {
            if(registeredUser.username.equals(leader) && registeredUser.GetState().equals("FREE"))
            {
                doesUserExist = true;
                leaderData = registeredUser;
                break;
            }
        }
        if(!doesUserExist){ return "FAILURE"; }
        if(size < 2){ return "FAILURE"; }

        return "SUCCESS";
    }

    public String QueryDHT(String queriedUser)
    {
        return "FAILURE";
    }
}

