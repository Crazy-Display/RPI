package com.project;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class RPIServer extends WebSocketServer {

    static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
    static Process p;
    static Process p2;
    static String ip = get_IP();
    static String clientId = "";
    static String user = "";
    static String password = "";

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

        String filePath =  System.getProperty("user.dir") + "/data/UsersLogIn.json";
        System.out.println(filePath);
        

        try (FileReader reader = new FileReader(filePath)) {
            // Utilizar JsonParser para obtener un JsonElement directamente

            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(reader, JsonObject.class);
            System.out.println(jsonObject);
            System.out.println(jsonObject.get("username"));
            /* 
            // Verificar si es un objeto JSON antes de convertirlo
            if (jsonElement.isJsonObject()) {
                // Convertir el JsonElement a un JsonObject
                JsonObject jsonObject = jsonElement.getAsJsonObject();

                System.out.println(jsonObject);
                // Acceder a los datos en el objeto JsonObject según la estructura de tu JSON
                 user = jsonObject.get("username").getAsString();
                 password = jsonObject.get("password").getAsString();

            
            } else {
                System.out.println("El JSON no es un objeto.");
            }*/
        } catch (IOException e) {
            e.printStackTrace();
        }

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
            //Lee el texo y lo muestra en el display 
            //System.out.println("Mensaje: " + message);

        JSONObject objMessage = new JSONObject(message);
        
        String type = objMessage.getString("type");

        System.out.println("user: " + user + " password: " + password);
        System.out.println(String.valueOf(objMessage));


        try {
            // El matem si encara no ha acabat
            if( p != null && p.isAlive() ) p.destroy();
            p.waitFor();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


        boolean verification = false;
        if (objMessage.getString("type").equals("verify")){
            if (user.equals(objMessage.getString("username")) && password.equals(objMessage.getString("password"))){
                verification = true;
                conn.send("OK");
            }
            else {
                verification = false;
                conn.send("NOTOK");
            }
        }

        if (verification)
        {
            if (objMessage.getString("from").equalsIgnoreCase("Fluutter")||objMessage.getString("from").equalsIgnoreCase("App")){

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
                                    
                setConnectionLostTimeout(0);
                setConnectionLostTimeout(100);
            }
        

            if (objMessage.getString("type").equals("image")){

                String base64String = objMessage.getString("image");
                byte[] imageBytes = Base64.getDecoder().decode(base64String.split(",")[1]);

                try (FileOutputStream fos = new FileOutputStream(outputPath)) {
                    fos.write(imageBytes);
                    System.out.println("Imagen guardada exitosamente en " + outputPath);
                } catch (IOException e) {
                    System.err.println("Error al escribir el archivo: " + e.getMessage());
                }

                System.out.println("Hola");
                p = obj_jcmd.runProcessImg("aaaa");
            }
                
            //Display Message
            if (objMessage.getString("type").equals("texto")){
                String texto = objMessage.getString("texto");
                p = obj_jcmd.runProcess(texto);
            }
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
        // IP from Main
        try {        
            return  Main.getLocalIPAddress(); 

        } catch (Exception e) {
        }
        return "";
    }

    public String getConnectionId (WebSocket connection) {
        String name = connection.toString();
        return name.replaceAll("org.java_websocket.WebSocketImpl@", "").substring(0, 3);
    }
}
