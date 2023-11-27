package com.project;
import java.lang.Runtime;
import java.net.SocketException;
import java.net.UnknownHostException;
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

    public Process runProcessImg(){
        Process proceso = null;
        try 
        {           
            String command = "cd ~/dev/rpi-rgb-led-matrix/utils &&  ./led-image-viewer -C --led-cols=64 --led-rows=64 --led-slowdown-gpio=4 --led-no-hardware-pulse ~/bin/RPI/RPI/image.jpg";

            ProcessBuilder pB = new ProcessBuilder("bash", "-c", command);
            proceso = pB.start();

                
        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }     

        return proceso;
    }
}

