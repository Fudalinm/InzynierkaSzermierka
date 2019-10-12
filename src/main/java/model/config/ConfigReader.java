package model.config;
import util.HumanReadableFatalError;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class ConfigReader {
    private class TagVariables
    {
        private HashMap<String,String> vars = new HashMap<>();

        public boolean getBoolean(String varName)
        {
            String var=vars.get(varName);
            if (var!=null)
            {
                if (var.equals("Y"))
                {
                    return true;
                }
                else if (var.equals("N")) {
                    return false;
                }
                throw new IllegalStateException("Value was not boolean");
            }
            throw new IllegalStateException("Value was not found");
        }

        public int getInt(String varName)
        {
            int out;
            String var=vars.get(varName);
            if (var!= null)
            {
                try
                {
                    return Integer.parseInt(var);
                }
                catch(NumberFormatException  ex)
                {
                    throw new IllegalStateException("Value was not int");
                }
            }
            throw new IllegalStateException("Value was not found");
        }

        public void set(String name, String value)
        {
            vars.put(name,value);
        }

        public void setIfNotSet(String name, String value)
        {
            if (vars.containsKey(name))
                return;
            vars.put(name,value);
        }

        public void debugStringBuilder(StringBuilder sbuilder)
        {
            Set<String> tags_names = vars.keySet();
            Iterator<String> key_it= tags_names.iterator();
            while (key_it.hasNext())
            {
                String var_name=key_it.next();
                sbuilder.append("\t");
                sbuilder.append(var_name);
                sbuilder.append(" ");
                sbuilder.append(vars.get(var_name));
                sbuilder.append("\n");
            }
        }
    }

    private static ConfigReader instance=null;
    private static int MAX_IO_RETRIES = 5;
    private static int IO_COOLDOWN_TIME_MS = 200; // if we have to retry how long to wait ?

    private HashMap<String,TagVariables> tags = new HashMap<>();

    private ConfigReader() { throw new IllegalStateException(); } // DO NOT CALL THIS
    // in better language we would use ConfigReader() = delete;

    private ConfigReader(String defaultConfigFilePath, String overrideConfigFilePath) throws FileNotFoundException,IOException,IllegalArgumentException
    {
        if(defaultConfigFilePath==null && overrideConfigFilePath==null)
            throw new IllegalArgumentException(" At least one config file must be specified");
        try{
            if(defaultConfigFilePath!=null)
            {
                fillWeak(defaultConfigFilePath);
            }
        }
        catch (IOException ex)
        {
            for(int i=0; i< MAX_IO_RETRIES; i++)
            {
                try{
                    Thread.sleep(IO_COOLDOWN_TIME_MS);
                    fillWeak(defaultConfigFilePath);
                    break; // if successfull break
                }
                catch (FileNotFoundException fileNotFoundEx)
                {
                    throw fileNotFoundEx;
                }
                catch (IOException|InterruptedException exInner)
                {
                    ;// ignore
                }
            }
        }
        try{
            if(overrideConfigFilePath!=null)
            {
                fillOverride(overrideConfigFilePath);
            }
        }
        catch (IOException ex)
        {
            for(int i=0; i< MAX_IO_RETRIES; i++)
            {
                try{
                    Thread.sleep(IO_COOLDOWN_TIME_MS);
                    fillOverride(overrideConfigFilePath);
                    break; // if successfull break
                }
                catch (FileNotFoundException fileNotFoundEx)
                {
                    throw fileNotFoundEx;
                }
                catch (IOException|InterruptedException exInner)
                {
                    ;// ignore
                }
            }
        }

    }


    private void fillWeak(String filePath) throws FileNotFoundException,IOException
    {
        BufferedReader reader  = new BufferedReader(new FileReader(
                filePath));
        String currentTag=null;
        TagVariables currentTagVars=new TagVariables();
        String line = reader.readLine();
        while(line != null)
        {
            line=line.trim();
            if (line.startsWith("["))
            {
                if(currentTag!= null)
                {
                    tags.put(currentTag,currentTagVars);
                }
                currentTag=line.substring(1,line.length()-1);
                currentTagVars=new TagVariables();
            }
            else
            {
                String[] splitedLine= line.split(" ");
                try {
                    currentTagVars.set(splitedLine[0], splitedLine[1]);
                }
                catch (ArrayIndexOutOfBoundsException ex)
                {
                    throw new IllegalStateException("invalid file format, probably variable without value");
                }
            }
            line=reader.readLine();
        }
        if (currentTag != null)
            if(!tags.containsKey(currentTag))
                tags.put(currentTag,currentTagVars);
    }

    private void fillOverride(String filePath) throws FileNotFoundException,IOException
    {
        BufferedReader reader  = new BufferedReader(new FileReader(
                filePath));
        String currentTag=null;
        TagVariables currentTagVars=new TagVariables();
        String line = reader.readLine();
        while(line != null)
        {
            line=line.trim();
            if (line.startsWith("["))
            {
                if(currentTag!= null)
                {
                    tags.put(currentTag,currentTagVars);
                }
                currentTag=line.substring(1,line.length()-1);
                currentTagVars=new TagVariables();
            }
            else
            {
                String[] splitedLine= line.split(" ");
                try {
                    currentTagVars.set(splitedLine[0], splitedLine[1]);
                }
                catch (ArrayIndexOutOfBoundsException ex)
                {
                    throw new IllegalStateException("invalid file format, probably variable without value");
                }
            }
            line=reader.readLine();
        }
        if (currentTag != null)
            tags.put(currentTag,currentTagVars);
    }



    public static ConfigReader getInstance()
    {
        if (instance != null) {
            return instance;
        }
        throw new IllegalStateException("Not initilized");
    }

    public static void init(String defaultConfigFilePath,String overrideConfigFilePath) throws HumanReadableFatalError
    {
        if(instance!=null)
            throw new IllegalStateException("multiple initializations");
        try {
            instance = new ConfigReader(defaultConfigFilePath, overrideConfigFilePath);
        }
        catch (FileNotFoundException ex)
        {
            throw new HumanReadableFatalError("File not found",ex);
        }
        catch (IOException ex)
        {
            throw new HumanReadableFatalError("Input Output Error",ex);
        }
        catch (IllegalArgumentException ex)
        {
            throw new HumanReadableFatalError("Invalid Argument",ex);
        }
    }

    public boolean getBooleanValue(String tag, String name)
    {
        if( !tags.containsKey(tag))
            throw new IllegalStateException("tag was not found");
        return tags.get(tag).getBoolean(name);
    }
    public boolean getBooleanValue(String tag, String name,boolean defaultValue)
    {
        boolean out;
        try
        {
            out=getBooleanValue(tag,name);
        }
        catch (IllegalStateException ex)
        {
            return defaultValue;
        }
        return out;
    }

    public int getIntValue(String tag, String name)
    {
        if( !tags.containsKey(tag))
            throw new IllegalStateException("tag was not found");
        return tags.get(tag).getInt(name);
    }
    public int getIntValue(String tag, String name,int defaultValue)
    {
        int out;
        try
        {
            out=getIntValue(tag,name);
        }
        catch (IllegalStateException ex)
        {
            return defaultValue;
        }
        return out;
    }

    public String debugDump()
    {
        StringBuilder sbuilder= new StringBuilder();
        Set<String> tags_names = tags.keySet();
        Iterator<String> key_it= tags_names.iterator();
        while (key_it.hasNext())
        {
            String tag_name=key_it.next();
            sbuilder.append(tag_name);
            sbuilder.append("\n");
            tags.get(tag_name).debugStringBuilder(sbuilder);
        }
        return sbuilder.toString();
    }
}