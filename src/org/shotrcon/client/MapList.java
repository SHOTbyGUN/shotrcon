/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.shotrcon.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 *
 * @author shotbygun
 */
public class MapList {
    
    private ArrayList<String> mapNames, variableNames;
    
    public MapList() {
        mapNames = new ArrayList<>();
        variableNames = new ArrayList<>();
    }
    
    public void readMapList() throws IOException {
        InputStream inputStream = this.getClass().getResourceAsStream("DefaultMapList.txt"); 
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        
        String line;
        String[] slice;
        
        while(true) {
            line = bufferedReader.readLine();
            if(line == null || line.isEmpty())
                break;
            slice = line.split(":");
            variableNames.add(slice[0].trim());
            mapNames.add(slice[1].trim());
        }
    }
    
    public String getVariableName(int selection) {
        return variableNames.get(selection);
    }
    
    public String[] getMapNames() {
        String[] maps = new String[variableNames.size()];
        return mapNames.toArray(maps);
    }
    
}
