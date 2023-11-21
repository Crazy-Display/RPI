package com.project;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

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

    static jcmd obj_jcmd = new jcmd();
    
    //Users
    static ArrayList<WebSocket> flutterCon =  new ArrayList<>();
    static ArrayList<WebSocket> appCon =  new ArrayList<>();

    //int numConnFlutter = 0;
    //int numConnApp = 0;

    //String textConnections = "Flutter connections: " + numConnFlutter + "\n" + "App connection: " + numConnApp ;

    public RPIServer (int port) {
        super(new InetSocketAddress(port));
    }

    @Override
    public void onStart() {
        // Quan el servidor s'inicia
        String host = getAddress().getAddress().getHostAddress();
        int port = getAddress().getPort();
        System.out.println("WebSockets server running at: ws://" + host + ":" + port);
        System.out.println("Type 'exit' to stop and exit server.");

        //Execute commands in RPI
        p = obj_jcmd.runProcess(ip);
        setConnectionLostTimeout(0);
        setConnectionLostTimeout(100);

    }   

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        clientId = getConnectionId(conn);
        
        // Li enviem Connected
        conn.send("Connected"); 



        if(p.isAlive())
        {
            p.destroy();
        }   

    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        
        for (WebSocket c : flutterCon){
            if(c == conn){
                flutterCon.remove(conn);
            }
        }

        for (WebSocket ca : appCon){
            if(ca == conn){
                appCon.remove(conn);
            }
        }

        System.out.println("Adios");
        
    }

    @Override
    public void onMessage(WebSocket conn, String message) {

        String clientId = getConnectionId(conn);  
        String outputPath = "image.jpg";      
        try {
            //Lee el texo y lo muestra en el display 
            //System.out.println("Mensaje: " + message);

            if (message.equalsIgnoreCase("Fluutter")||message.equalsIgnoreCase("App")){

                if (message.equalsIgnoreCase("Fluutter")){
                    System.out.println("Flutter user " + clientId + " connected");
                    flutterCon.add(conn);

                }
                else if (message.equalsIgnoreCase("App")){
                    System.out.println("App user " + clientId + " connected");
                    appCon.add(conn);
                }
                
                //Te dice los Usuarios Connectados 
                p = obj_jcmd.runProcess("Usuarios Flutter " + flutterCon.size() + "\n" + " Usuarios App " + appCon.size());

                TimeUnit.SECONDS.sleep(5);
                // el matem si encara no ha acabat
                if( p.isAlive() ) p.destroy();
                p.waitFor();
                
                setConnectionLostTimeout(0);
                setConnectionLostTimeout(100);
            }
            else
            {
                //Display img
                JSONObject obj_json = new JSONObject(message);
                String type = obj_json.getString("type");
                System.out.println(type);


                if (obj_json.getString("type").equals("image")){
                    String base64String = obj_json.getString("image");
                    byte[] imageBytes = Base64.getDecoder().decode(base64String.split(",")[1]);

                    try (FileOutputStream fos = new FileOutputStream(outputPath)) {
                        fos.write(imageBytes);
                        System.out.println("Imagen guardada exitosamente en " + outputPath);
                    } catch (IOException e) {
                        System.err.println("Error al escribir el archivo: " + e.getMessage());
                    }

                    System.out.println("Hola");
                    p = obj_jcmd.runProcessImg("aaaa");
                    TimeUnit.SECONDS.sleep(5);
                    // el matem si encara no ha acabat
                    if( p.isAlive() ) p.destroy();
                    p.waitFor();
                }
                
        

                //Display Message
                if (obj_json.getString("type").equals("texto")){
                    String texto = obj_json.getString("texto");
                    p = obj_jcmd.runProcess(texto);
                    TimeUnit.SECONDS.sleep(5);
                    // el matem si encara no ha acabat
                    if( p.isAlive() ) p.destroy();
                    p.waitFor();
                }

                

            }}catch (Exception e) {
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
                    if(p.isAlive() && p != null)
                    {
                        p.destroy();
                    }   
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
