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
    static String userHome = System.getProperty("user.home");
    static String text = "";


public Process runProcess(String text){
        try 
        {           
            String cmd[] = {
            "text-scroller", "-f",
            userHome + "/dev/bitmap-fonts/bitmap/cherry/cherry-10-b.bdf",
            "--led-cols=64",
            "--led-rows=64",
            "--led-slowdown-gpio=4",
            "--led-no-hardware-pulse",text};

            // objecte global Runtime
            Runtime rt = java.lang.Runtime.getRuntime();
            
            // executar comanda en subprocess
            System.out.println("Executing");
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

    return p;

    }
}

