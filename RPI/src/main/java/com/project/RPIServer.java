package com.project;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;


import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class RPIServer extends WebSocketServer {

    static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
    static Process p;
    static Process p2;
    static String ip = get_IP();
    static String clientId = "";
    static String user = "";
    static String password = "";
    static HashMap<WebSocket, String> connectedUsers = new HashMap<>();
    static ArrayList<String> users = new ArrayList<>();
    static ArrayList<String> passwords = new ArrayList<>();
    static boolean verification = false;
    static String usersfilePath =  System.getProperty("user.dir") + "/data/UsersLogIn.json"; //--

    static jcmd obj_jcmd = new jcmd();
    
    //CONNECTIONS 
    static ArrayList<WebSocket> flutterCon =  new ArrayList<>();
    static ArrayList<WebSocket> appCon =  new ArrayList<>();


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

        //SAVE AUTHORTISED USERS 

        try (FileReader reader = new FileReader(usersfilePath)) {
            
            //JSONObject CLIENT 
            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(reader, JsonObject.class);

            JsonElement jsonElementUsers = jsonObject.get("username");
            JsonElement jsonElementPasswords = jsonObject.get("password");

            JsonArray jsonArrayUsers = jsonElementUsers.getAsJsonArray();
            JsonArray jsonArrayPaswords = jsonElementPasswords.getAsJsonArray();

            System.out.println(usersfilePath);

            // SAVE USERS IN LIST 
            for (JsonElement element : jsonArrayUsers) {
                users.add(element.getAsString());
                
            }
            // SAVE PASSWORDS IN LIST
            for (JsonElement element : jsonArrayPaswords) {
                passwords.add(element.getAsString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }   

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        clientId = getConnectionId(conn);     
        // Li enviem Connected
        conn.send("Connected");
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        WebSocket deleteConn = null;

        for (WebSocket c : flutterCon){
            if(c == conn){
                 deleteConn = c;
            }
        }
        if (deleteConn != null) flutterCon.remove(conn);
        

        for (WebSocket ca : appCon){
            if(ca == conn){
                deleteConn = ca;
            }
        }
        if (appCon != null) appCon.remove(conn);

        String deleteUser = connectedUsers.get(conn);

        JSONObject disUser = new JSONObject();
        disUser.put("type","disconnected");
        disUser.put("name", deleteUser);
        broadcast(disUser.toString());
        connectedUsers.remove(conn);
        System.out.println("Adios");

        try {
            // El matem si encara no ha acabat
            if( p != null && p.isAlive() ) p.destroy();     // Para matar la img comanda kill led-image-viewer
                p.waitFor();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void onMessage(WebSocket conn, String message) {

        //MESSAGE SYSTEM
        
        String outputPath = "image.jpg";      
        System.out.println(message);
        JSONObject objMessage = new JSONObject(message);
        System.out.println(objMessage);
         
        try {
            // El matem si encara no ha acabat
            if( p != null && p.isAlive() ) p.destroy();     // Para matar la img comanda kill led-image-viewer
                p.waitFor();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        

        if (objMessage.getString("type").equals("verify")){

            user = objMessage.getString("username");
            password = objMessage.getString("password");

            

            for (int i = 0; i< users.size(); i++ )
            {
                System.out.println("user: " + user + " password: " + password);
                System.out.println("user: " + users.get(i) + " password: " + passwords.get(i));

                if (user.equals(users.get(i)) && password.equals(passwords.get(i))){
                    verification = true;
                    connectedUsers.put(conn, user );

                    
                    // ADD CONNECTIONS TO ARRAYS
                    if (objMessage.getString("from").equalsIgnoreCase("Flutter"))
                    {
                        System.out.println("Flutter user " + user + " connected");
                        flutterCon.add(conn);
                    }
                    if (objMessage.getString("from").equalsIgnoreCase("App"))
                    {
                        System.out.println("App user " + user + " connected");
                        appCon.add(conn);
                    }
                    conn.send("OK");

                    JSONObject connUser = new JSONObject();
                    connUser.put("type","connected");
                    connUser.put("name", user);
                    broadcast(connUser.toString());

                    //Te dice los Usuarios Connectados 
                    p = obj_jcmd.runProcess("Usuarios Flutter " + flutterCon.size() + " Usuarios App " + appCon.size());
                                        
                    setConnectionLostTimeout(0);
                    setConnectionLostTimeout(100);
                    break;
                }

                
                else {
                    verification = false;
                }
            }
        }
        if (!verification) conn.send("NOTOK"); 

        System.out.println("verification : " + verification);
        System.out.println(connectedUsers);
        if (verification)
        {
            String type = objMessage.getString("type");
            System.out.println(type);

            if (type.equals("image")){

                String base64String = objMessage.getString("image");
                byte[] imageBytes = Base64.getDecoder().decode(base64String);

                try (FileOutputStream fos = new FileOutputStream(outputPath)) {
                    fos.write(imageBytes);
                    System.out.println("Imagen guardada exitosamente en " + outputPath);
                } catch (IOException e) {
                    System.err.println("Error al escribir el archivo: " + e.getMessage());
                }

                 p = obj_jcmd.runProcessImg();
            }
                
            //Display Message
            if (type.equals("texto")){

                JSONObject messageSend = new JSONObject();
                messageSend.put("type", "message");
                messageSend.put("user", connectedUsers.get(conn));
                broadcast(messageSend.toString());

                String texto = objMessage.getString("texto");
                System.out.println("Text: " + texto);
                System.out.println(user);
                 p = obj_jcmd.runProcess(texto);
            }

            if(type.equals("users")){
                
                /* Set<WebSocket> connUsers = connectedUsers.keySet();
                ArrayList<WebSocket> connList = new ArrayList<>(connUsers); */

                ArrayList<String> flutterUsers = new ArrayList<>();
                ArrayList<String> appUsers = new ArrayList<>();

                for (WebSocket c : flutterCon){
                    if (connectedUsers.get(c) != null){
                        flutterUsers.add(connectedUsers.get(c));
                    }
                }
                for (WebSocket c : appCon){
                    if (connectedUsers.get(c) != null){
                        appUsers.add(connectedUsers.get(c));
                    }
                }
                
                JSONObject requestedUsers = new JSONObject();
                requestedUsers.put("type", "users");
                requestedUsers.put("usersFlutter", flutterUsers);
                requestedUsers.put("usersApp", appUsers);

                conn.send(requestedUsers.toString());
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
                    if(p != null && p.isAlive())
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
