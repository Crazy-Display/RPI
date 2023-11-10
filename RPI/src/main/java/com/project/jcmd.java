package com.project;

    import java.util.concurrent.TimeUnit;

import org.java_websocket.util.Base64.OutputStream;

import java.lang.Runtime;
    import java.lang.Process;
     
    class jcmd
    {
        public static void main(String args[])
        {
            System.out.println("Iniciant comanda...");
            
            String userHome = System.getProperty("user.home");
            String cmd[] = {
                userHome + "/dev/rpi-rgb-led-matrix/examples-api-use/text-example",
                "-x", "5",
                "-y", "18",
                "-f", userHome + "/dev/bitmap-fonts/bitmap/cherry/cherry-10-b.bdf",
                "--led-cols=64",
                "--led-rows=64",
                "--led-slowdown-gpio=4",
                "--led-no-hardware-pulse"
            };
            try {
                // objecte global Runtime
                Runtime rt = java.lang.Runtime.getRuntime();
     
                // executar comanda en subprocess
                Process p = rt.exec(cmd);
                // Obtener el stream de entrada del proceso (para escribir en la consola del proceso)
                java.io.OutputStream processInput =  p.getOutputStream();

                // Escribir datos en la entrada del proceso (puedes adaptar esto según tus necesidades)
                String inputData = "Funciona";
                processInput.write(inputData.getBytes());
                processInput.flush();
                
                // donem un temps d'execució
                TimeUnit.SECONDS.sleep(5);
                // el matem si encara no ha acabat
                if( p.isAlive() ) p.destroy();
                p.waitFor();
                // comprovem el resultat de l'execució
                System.out.println("Comanda 1 exit code="+p.exitValue());
                System.out.println(p.getErrorStream());
                processInput.close();  

     
            } catch (Exception e) {
                e.printStackTrace();
            }
     
            // finish
            System.out.println("Comandes finalitzades.");
        }
    }

