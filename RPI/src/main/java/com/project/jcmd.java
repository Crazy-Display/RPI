package com.project;

    import java.util.concurrent.TimeUnit;
    import java.lang.Runtime;
    import java.lang.Process;
     
    class jcmd
    {
        public static void main(String args[])
        {
            System.out.println("Iniciant comanda...");
            
            String cmd[] = {"tail","-f","/var/log/syslog"};
     
            try {
                // objecte global Runtime
                Runtime rt = java.lang.Runtime.getRuntime();
     
                // executar comanda en subprocess
                Process p = rt.exec(cmd);
                // donem un temps d'execució
                TimeUnit.SECONDS.sleep(5);
                // el matem si encara no ha acabat
                if( p.isAlive() ) p.destroy();
                p.waitFor();
                // comprovem el resultat de l'execució
                System.out.println("Comanda 1 exit code="+p.exitValue());
     
            } catch (Exception e) {
                e.printStackTrace();
            }
     
            // finish
            System.out.println("Comandes finalitzades.");
        }
    }

