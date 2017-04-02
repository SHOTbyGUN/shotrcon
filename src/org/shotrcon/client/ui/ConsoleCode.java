package org.shotrcon.client.ui;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import net.kronos.rkon.core.Rcon;
import org.shotrcon.client.CSVList;
import org.shotrcon.client.TimedTask;

/**
 *
 * @author shotbygun
 */
public class ConsoleCode extends ConsoleUI implements Runnable {
    
    public static final int BLUEFOR = 0;
    public static final int REDFOR = 1;
    public static final String SIDECHANGESTRING = " PlayerAlliance ";
    public static final String[] BANLENGTHOPTIONS = new String[] {"Hour","Day","Week","Month","Permanent"};
    
    private final LinkedList<String> lastCommands = new LinkedList<>();
    private Iterator lastCommandsIterator;
    //private boolean connected = false;
    private Rcon rcon;
    
    private final CSVList mapList;
    private final TimedTask timedTask;
    
    public ConsoleCode() {
        super();
        mapList = new CSVList();
        timedTask = new TimedTask(this);
        try {
            mapList.readCSVList("org/shotrcon/client/data/DefaultMapList.csv");
            setMapList(mapList.getMapNames());
        } catch (Exception ex) {
            printError(ex.toString());
        }
    }
    

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

            // Add input to lastCommands
            lastCommands.add(inputText);

            // Reset lastCommandsIterator
            lastCommandsIterator = null;

            sendCommand(inputText);
            
            
            
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
            
            // stop timer
            timedTask.stop();
        } catch (Exception ex) {
            printError(ex.toString());
        }
    }
    
    public void connect() {
        try {
            // try to connect
            rcon = new Rcon(getIpAddress(), getPort(), getPassword().getBytes());
            // Set keepalive
            rcon.getSocket().setKeepAlive(true);
            // initial refresh playerlist
            onRefreshPlayers();
            
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

    @Override
    protected void onRefreshPlayers() {
        
        final String clientListRequest = "display_all_clients";
        final String clientListResponseStartsWith = "Client List :";
        
        try {
            String response = sendCommand(clientListRequest);
            if(response.startsWith(clientListResponseStartsWith)) {

                String clientListString = response.replaceFirst(clientListResponseStartsWith, "").trim();
                String[] clientsArray = clientListString.split("\n");

                setPlayerList(clientsArray);
            } else {
                setPlayerList(new String[] {"unknown"});
            }
        } catch (Exception ex) {
            printError(ex.toString());
        }
        
    }
    
    public synchronized String sendCommand(String commandLine) throws IOException {
        // Test if we are connected
        if (!connected()) {
            printError("not connected");
            return null;
        }

        // print command we are about to send
        printInput(commandLine);

        // Send rcon command
        String response = rcon.command(commandLine);

        // Print response
        printResponse(response);
        
        // Reset timer
        timedTask.resetTimer();

        // return response
        return response;
    }

    @Override
    protected void onChangeMap() {
        try {
            
            sendCommand("setsvar Map " + mapList.getVariableName(getSelectedMap()));
            
        } catch (Exception ex) {
            printError(ex.toString());
        }
    }
    
    public String getClientID(String playerName) {
        return playerName.split(" ")[0];
    }

    @Override
    protected void onKickPlayer() {
        try {
            sendCommand("kick " + getClientID(getSelectedPlayer()));
        } catch (Exception ex) {
            printError(ex.toString());
        }
    }

    @Override
    protected void onBanPlayer() {
        
        // This is quick and dirty way to do this
        // Feel free to improve
        
        final String selectedPlayer = getSelectedPlayer();
        final String clientID = getClientID(selectedPlayer);
        
        // Show options hour / day / week etc...
        JFrame frame = new JFrame("Ban player");
        String selectedOption = (String) JOptionPane.showInputDialog(frame, 
            selectedPlayer,
            "Select length for ban",
            JOptionPane.QUESTION_MESSAGE, 
            null, 
            BANLENGTHOPTIONS, 
            BANLENGTHOPTIONS[0]);
        
        int banLength;
        
        // Parse return string to ban length
        if(selectedOption.equals(BANLENGTHOPTIONS[0]))
            banLength = 1;
        else if(selectedOption.equals(BANLENGTHOPTIONS[1]))
            banLength = 24;
        else if(selectedOption.equals(BANLENGTHOPTIONS[2]))
            banLength = 24*7;
        else if(selectedOption.equals(BANLENGTHOPTIONS[3]))
            banLength = 24*7*4;
        else if(selectedOption.equals(BANLENGTHOPTIONS[4]))
            banLength = 0;
        else
            return;
        
        try {
            sendCommand("ban " + clientID + " " + banLength);
        } catch (Exception ex) {
            printError(ex.toString());
        }
            
    }

    @Override
    protected void onChangeTeamBlue() {
        try {
            sendCommand("setpvar " + getClientID(getSelectedPlayer()) + SIDECHANGESTRING + BLUEFOR);
        } catch (Exception ex) {
            printError(ex.toString());
        }
    }

    @Override
    protected void onChangeTeamRed() {
        try {
            sendCommand("setpvar " + getClientID(getSelectedPlayer()) + SIDECHANGESTRING + REDFOR);
        } catch (Exception ex) {
            printError(ex.toString());
        }
    }

    /*
        This will be run if no command sent for 60 seconds
        There will be unnotified connection timeout if we don't keep line open
    */
    @Override
    public void run() {
        onRefreshPlayers();
    }
    
    
}
