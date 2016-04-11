package com.dataguasu.javalab;

import java.net.*;
import java.io.*;
import java.util.ArrayList;

public class IRC_Challenge {

    private Socket s = null;
    private String[] params = new String[6];
    private Writer out;

    public void IRC_Challenge()
    {

    }

    public void OpenReadFile()
    {

        //args[0] localhost:port
        //args[1] Nickname
        //args[2] Username
        //args[3] Real Name

        try {

            String filePath = new File("").getAbsolutePath();

            FileReader fr = new FileReader(filePath+"/irc-input.txt");
            BufferedReader in = new BufferedReader(fr);
            String s = new String();
            int i = 0;
            while ((s=in.readLine())!=null)
            {
                params[i] = s;
                i+=1;
            }

            if (params.length == 0)
                System.out.println("Please enter parameters in the input file");

        }
        catch (FileNotFoundException ex)
        {
            System.err.println("File not found");
        }
        catch (IOException ex)
        {
            System.err.println("Issue while reading file");
        }
    }

    public void Connect()
    {
        String host = this.params[0].split(":")[0];
        int port = Integer.parseInt(this.params[0].split(":")[1]);

        try {
            this.s = new Socket(host,port);
            //System.out.println(host + ":" + port);
            this.out = new OutputStreamWriter(s.getOutputStream());
        }
        catch (UnknownHostException ex){
            System.err.println(ex);
        }
        catch (IOException ex){
            System.err.println(ex);
        }
    }

    public void ProcessMessages(){

        String Nickname = this.params[1];
        String Username = this.params[2];
        String RealName = this.params[3];
        String Channel = this.params[4];
        String Message = this.params[5];

        try {
            out.write("NICK " + Nickname + "\r\n");  // Send Nickname
            out.write("USER " + Username + " 0 * :" + RealName + "\r\n");  // Send Username and Realname
            out.flush();

            InputStream raw = s.getInputStream();     // Create InputSteam from socket
            BufferedInputStream buffer = new BufferedInputStream(raw); // Create BufferedInputStream from InputStream
            InputStreamReader in = new InputStreamReader(buffer); // Create InputStreamReader from buffer

            int c;
            int i=0;
            int sum=0;
            String sender;
            String operation;
            StringBuffer msg = new StringBuffer();

            while ((c=in.read())!=-1) {
                System.out.write(c);
                msg.append((char) c);

                if (c=='\n')

                {
                    // PING messages
                    if (msg.toString().split(" ")[0].startsWith("PING"))
                    {
                        System.out.println(">> PONG :" + msg.toString().split(":")[1]);
                        out.write("PONG :" + msg.toString().split(":")[1] + "\n");
                        out.flush();
                    }
                    // JOIN messages
                    else if (msg.toString().split(" ")[1].startsWith("376"))
                    {
                        System.out.println(">> JOIN "  + Channel.trim());
                        out.write("JOIN " + Channel.trim() + "\n");
                        out.flush();
                    }
                    // JOIN - PRIVMSG
                    else if (msg.toString().split(" ")[1].startsWith("JOIN") && msg.toString().split(" ")[2].startsWith("#"))
                    {
                        System.out.println(">> PRIVMSG " + msg.toString().split(" ")[2].trim() + " :" + Message);
                        out.write("PRIVMSG " + msg.toString().split(" ")[2].trim() + " :" + Message + "\n");
                        out.flush();
                    }
                    else if (msg.toString().split(" ")[1].startsWith("PRIVMSG")) {

                        // :uiolep!b578bc10@gateway/web/freenode/ip.181.120.188.16 PRIVMSG #rdp :javalabot sum 1 2 4
                        if (msg.toString().split(" ")[3].startsWith(":" + Nickname))

                        {
                            sender = msg.toString().split(" ")[0].split("!")[0];
                            operation =  msg.toString().split(" ")[4];

                            if (operation.equals("add"))
                            {
                                for (int j=5;j<msg.toString().split(" ").length; j++)
                                    sum += Integer.parseInt(msg.toString().split(" ")[j].trim());

                                out.write("PRIVMSG " + msg.toString().split(" ")[2].trim() + " " + sender + " :Sum is " + sum + "\n");
                                out.flush();
                            }
                            else
                            {
                                System.out.println(">> PRIVMSG " + msg.toString().split(" ")[2].trim() + " " + sender + " :<add> x y \n");
                                out.write("PRIVMSG " + msg.toString().split(" ")[2].trim() + " " + sender + " :<add> x y \n");
                                out.flush();
                            }
                        }

                        // :uiolep!b578bc10@gateway/web/freenode/ip.181.120.188.16 PRIVMSG javalabot :hello
                        else if (msg.toString().split(" ")[2].startsWith(Nickname))

                        {
                            sender = msg.toString().split(" ")[0].split("!")[0].split(":")[1];
                            operation =  msg.toString().split(" ")[3];

                            if (operation.equals(":add"))
                            {
                                for (int j=4;j<msg.toString().split(" ").length; j++)
                                    sum += Integer.parseInt(msg.toString().split(" ")[j].trim());

                                out.write("PRIVMSG " + sender + " :Sum is " + sum + "\n");
                                out.flush();
                            }
                            else
                            {
                                out.write("PRIVMSG " + sender + " :<add> x y\n");
                                out.flush();
                            }

                        }

                    }
                    sum = 0;
                    msg = new StringBuffer("");
                }
            }

        }
        catch (IOException ex){
            System.err.println(ex);
        }
        finally {
            try {
                if (s!=null) s.close();
            }
            catch(IOException ex){

            }
        }
    }

    public static void main(String[] args) {

        String host;
        int port;

        IRC_Challenge irc = new IRC_Challenge();

        irc.OpenReadFile();
        irc.Connect();
        irc.ProcessMessages();

    }
}
