package org.shotrcon.client.ui;

import java.util.Iterator;
import java.util.LinkedList;
import net.kronos.rkon.core.Rcon;

/**
 *
 * @author shotbygun
 */
public class ConsoleCode extends ConsoleUI {
    
    private final LinkedList<String> lastCommands = new LinkedList<>();
    private Iterator lastCommandsIterator;
    //private boolean connected = false;
    private Rcon rcon;

    @Override
    protected void onSend() {

        try {

            // Get input 
            String inputText = getInputText();
            
            // Ignore empty string
            if(inputText.length() < 1)
                return;
            
            // print input & clear inputField
            setInputText("");
            printInput(inputText);

            // Add input to lastCommands
            lastCommands.add(inputText);

            // Reset lastCommandsIterator
            lastCommandsIterator = null;

            // Test if we are connected
            if (!connected()) {
                printError("not connected");
                return;
            }
            
            // send rcon message
            String response = rcon.command(inputText);
            
            // print response
            printResponse(response);
            
        } catch (Exception ex) {
            printError(ex.toString());
        }
    }

    @Override
    protected void onToggleConnect() {
        try {
            
            if(connected())
                disconnect();
            else
                connect();
            
            
            if(connected())
                setToggleConnectButtonText("Disconnect");
            else
                setToggleConnectButtonText("Connect");
                
        } catch (Exception ex) {
            printError(ex.toString());
        }
    }

    @Override
    protected void onUpArrow() {
        if(lastCommandsIterator == null || !lastCommandsIterator.hasNext())
            lastCommandsIterator = lastCommands.descendingIterator();
        
        setInputText((String)lastCommandsIterator.next());
    }

    @Override
    protected void onDownArrow() {
        setInputText("");
        lastCommandsIterator = null;
    }
    
    public void disconnect() {
        try {
            if(rcon != null)
                rcon.disconnect();
            rcon = null;
            printInput("disconnected");
        } catch (Exception ex) {
            printError(ex.toString());
        }
    }
    
    public void connect() {
        try {
            rcon = new Rcon(getIpAddress(), getPort(), getPassword().getBytes());
        } catch (Exception ex) {
            printError(ex.toString());
        } finally {
            if(connected())
                printInput("connected");
            else
                printInput("not connected");
        }
    }
    
    public boolean connected() {
        if(rcon == null || rcon.getSocket() == null || rcon.getSocket().isConnected() == false)
            return false;
        else
            return true;
    }
    
    
}
