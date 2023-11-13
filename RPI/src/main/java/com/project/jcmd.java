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
        static Display d;
        public static void main(String args[])
        {
            Main main_class = new Main();
            try {
                String ip = main_class.getLocalIPAddress();
                System.out.println("Iniciant comanda...");            
                String userHome = System.getProperty("user.home");
                String cmd[] = {
                "text-scroller", "-f",
                userHome + "/dev/bitmap-fonts/bitmap/cherry/cherry-10-b.bdf",
                "--led-cols=64",
                "--led-rows=64",
                "--led-slowdown-gpio=4",
                "--led-no-hardware-pulse", ip
                };

                String filepath = userHome + "/dev/text.txt";
                try {
                    String line = Files.readString(Paths.get(filepath), StandardCharsets.UTF_8);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }            

                try {
                    // objecte global Runtime
                    Runtime rt = java.lang.Runtime.getRuntime();
        
                    // executar comanda en subprocess
                    Process p = rt.exec(cmd);
                    d = new Display(p);
                    
                    // donem un temps d'execució
                    // el matem si encara no ha acabat
                    //TimeUnit.SECONDS.sleep(15);
                    //if( p.isAlive() ) p.destroy();
                    //p.waitFor();
                    /* 
                    // comprovem el resultat de l'execució
                    System.out.println("Comanda 1 exit code="+p.exitValue());
                    */

            } catch (Exception e) {
                e.printStackTrace();
            } 
            // finish
            System.out.println("Comandes finalitzades.");
            } catch (SocketException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (UnknownHostException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }}

          
        public Display getDisplay(){
            return d;
        }

    }

