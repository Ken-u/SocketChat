/**
 * Created by ken on 16/6/1.
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class MutiServer {
    private static final int PORT = 6666;
    private List<Socket> mList = new ArrayList<Socket>();
    private ServerSocket server = null;
    private ExecutorService mExecutorService = null; //thread pool
    private ArrayList<UserData> userList = new ArrayList<UserData>();


    public static void main(String[] args) {
        new MutiServer();
    }
    public MutiServer() {
        try {
            server = new ServerSocket(PORT);
            mExecutorService = Executors.newCachedThreadPool();  //create a thread pool
            System.out.println("服务器已启动...");

            System.out.println("本机信息:");
            System.out.print(getAddress().getHostAddress().toString());
            System.out.println("/"+getAddress().getHostName().toString()+"/"+PORT);
            System.out.println("开启聊天服务");

            Socket client = null;
            while(true) {
                client = server.accept();
                //把客户端放入客户端集合中
                mList.add(client);
                addUserIpWithNoName(client.getInetAddress().toString());
                mExecutorService.execute(new Service(client)); //start a new thread to handle the connection
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    //获取本机网络信息
    private static InetAddress getAddress(){
        InetAddress ip = null;
        try{
            ip= InetAddress.getLocalHost();
        } catch(UnknownHostException e) {
            System.out.println("木找着");
        }
        return ip;
    }

    class Service implements Runnable {
        private Socket socket;
        private BufferedReader in = null;
        private String msg = "";

        public Service(Socket socket) {
            this.socket = socket;
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                //客户端只要一连到服务器，便向客户端发送下面的信息。
                msg = this.socket.getInetAddress() + ",Welcome! 当前用户数:"
                        +mList.size();
                //保存用户数据到列表
                //saveUserInfo();
                this.sendmsg();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void run() {
            try {
                while(true) {
                    if((msg = in.readLine())!= null) {
                        //当客户端发送的信息为：exit时，关闭连接
                        if(msg.equals("exit")) {
                            System.out.println("ssssssss");
                            mList.remove(socket);
                            in.close();
                            msg = "user:" + socket.getInetAddress()
                                    + "exit total:" + mList.size();
                            socket.close();
                            this.sendmsg();
                            break;
                            //接收客户端发过来的信息msg，然后发送给客户端。
                        } else {

                            String getString = msg;

                            int type = getString.charAt(0);
                            System.out.println(type);
                            if (type == 48){
                                System.out.println("消息类");
                                msg = getString.substring(1,getString.length());
                                msg = getNameFromIp(socket.getInetAddress().toString()) + ":" + msg;
                                this.sendmsg();
                            }else {
                                String setting_type = getString.substring(2,9);
                                String left = getString.substring(10,getString.length());
                                switch (setting_type){
                                    case "setName":
                                        System.out.println("设置用户名:"+left);
                                        setUserName(socket.getInetAddress().toString(),left);
                                        break;
                                    default:
                                        System.out.println("未定义的设置");
                                        break;
                                }
                            }


                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        /**
         * 循环遍历客户端集合，给每个客户端都发送信息。
         */
        public void sendmsg() {
            System.out.println(msg);
            int num =mList.size();
            for (int index = 0; index < num; index ++) {
                Socket mSocket = mList.get(index);
                PrintWriter pout = null;
                try {
                    pout = new PrintWriter(new BufferedWriter(
                            new OutputStreamWriter(mSocket.getOutputStream())),true);
                    pout.println(msg);
                }catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


    }

    //从IP获取用户名
    public String getNameFromIp(String ip){
        String result = ip;
        for (int index = 0;index<userList.size();index++){
            if (ip.equals(userList.get(index).getUser_ip())){
                result = userList.get(index).getUser_name();
            }
        }
        return result;
    }

    public void setUserName(String ip,String name){

        System.out.println("给"+ip+"设置用户名:"+name);
        for (int index = 0;index<userList.size();index++){
            if (ip.equals(userList.get(index).getUser_ip())){
                userList.get(index).setUser_name(name);
            }
        }
        System.out.println("当前用户:");
        for (int index = 0;index<userList.size();index++){
            System.out.println("ip:"+userList.get(index).getUser_ip()+" name:"+userList.get(index).getUser_name());
        }

    }

    public void addUserIpWithNoName(String ip){
        UserData userData = new UserData();
        userData.setUser_ip(ip);
        userData.setUser_name(ip);

        System.out.println("增加用户"+ip);
        userList.add(userData);
    }
}