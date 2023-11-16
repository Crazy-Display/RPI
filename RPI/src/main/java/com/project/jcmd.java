package com.project;

    import java.util.concurrent.TimeUnit;

import org.java_websocket.util.Base64.OutputStream;

import java.lang.Runtime;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.Process;
     
class jcmd
{
    static Process p;
    static String cmd[];
    
    jcmd(String cmd[] ){
        this.cmd = cmd;
    }
    public static void main(String args[])
    {
        try 
        {           
            // Command line

            // objecte global Runtime
            Runtime rt = java.lang.Runtime.getRuntime();

            // executar comanda en subprocess
            p = rt.exec(cmd);
            
          
                
        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }   
}
public Process getProcess(){
        return p;
    }
}

