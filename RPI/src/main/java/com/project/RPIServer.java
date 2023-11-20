package com.project;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.json.JSONObject;

public class RPIServer extends WebSocketServer {

    static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
    static Process p;
    static Process p2;
    static String ip = get_IP();
    static String clientId = "";
    String userHome = System.getProperty("user.home");
    String cmd[] = {
            "text-scroller", "-f",
            userHome + "/dev/bitmap-fonts/bitmap/cherry/cherry-10-b.bdf",
            "--led-cols=64",
            "--led-rows=64",
            "--led-slowdown-gpio=4",
            "--led-no-hardware-pulse", ip};
    int numConnFlutter = 0;
    int numConnApp = 0;
    String textConnections = "Flutter connections: " + numConnFlutter + "\n" + "App connection: " + numConnApp ;

    public RPIServer (int port) {
        super(new InetSocketAddress(port));
    }

    @Override
    public void onStart() {
        // CMD
        

        // Quan el servidor s'inicia
        String host = getAddress().getAddress().getHostAddress();
        int port = getAddress().getPort();
        System.out.println("WebSockets server running at: ws://" + host + ":" + port);
        System.out.println("Type 'exit' to stop and exit server.");

        //Execute commands in RPI
        jcmd obj_jcmd = new jcmd(cmd);
        obj_jcmd.main(null);        
        p = obj_jcmd.getProcess();
        setConnectionLostTimeout(0);
        setConnectionLostTimeout(100);

    }   

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        clientId = getConnectionId(conn);
        
        // Li enviem el seu identificador
        JSONObject objId = new JSONObject("{}");
        objId.put("type", "id");
        objId.put("from", "server");
        objId.put("value", clientId);
        conn.send(objId.toString()); 



        if(p.isAlive())
        {
            p.destroy();
   
        }   

    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        
        System.out.println("Adios");
    }

    @Override
    public void onMessage(WebSocket conn, String message) {

        String clientId = getConnectionId(conn);        
        try {
            //Lee el texo y lo muestra en el display 
            //System.out.println("Mensaje: " + message);

            if (message.equalsIgnoreCase("Fluutter")){
                System.out.println("Flutter user " + clientId + " connected");
                numConnFlutter = numConnFlutter + 1;

            }
            else if (message.equalsIgnoreCase("App")){
                System.out.println("App user " + clientId + " connected");
                numConnApp = numConnApp + 1;
            }
            
            if (message.equalsIgnoreCase("Fluutter") || message.equalsIgnoreCase("App") ){
                
            }

            


            setConnectionLostTimeout(0);
            setConnectionLostTimeout(100);

        } catch (Exception e) {
            // TODO: handle exception
        }
        
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        // Quan hi ha un error
        ex.printStackTrace();
    }


    public void runServerBucle () {
        boolean running = true;
        try {
            System.out.println("Starting server");
            start();
            while (running) {
                String line;
                line = in.readLine();
                if (line.equals("exit")) {
                    p.destroy();
                    running = false;
                }
            } 
            System.out.println("Stopping server");
            stop(1000);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }  
    }

    public static String get_IP(){
        Main main_class = new Main();
        // IP from Main
        try {        
            return  main_class.getLocalIPAddress(); 

        } catch (Exception e) {
        }
        return "";
    }

    public String getConnectionId (WebSocket connection) {
        String name = connection.toString();
        return name.replaceAll("org.java_websocket.WebSocketImpl@", "").substring(0, 3);
    }
}