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
public class CSVList {
    
    private ArrayList<String> keyNames, variableNames;
    
    public CSVList() {
        keyNames = new ArrayList<>();
        variableNames = new ArrayList<>();
    }
    
    public void readCSVList(String path) throws IOException {
        //InputStream inputStream = this.getClass().getResourceAsStream("DefaultMapList.txt");
        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        
        String line;
        String[] slice;
        
        while(true) {
            line = bufferedReader.readLine();
            if(line == null || line.isEmpty())
                break;
            slice = line.split(":");
            variableNames.add(slice[0].trim());
            keyNames.add(slice[1].trim());
        }
        
        if(keyNames.size() < 1)
            throw new IOException("no rows readed from: " + path);
    }
    
    public String getVariableName(int selection) {
        return variableNames.get(selection);
    }
    
    public String[] getMapNames() {
        String[] maps = new String[variableNames.size()];
        return keyNames.toArray(maps);
    }
    
}
