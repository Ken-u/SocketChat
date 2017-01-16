package com.example.ken.socketchatwithandroid_v2.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ken.socketchatwithandroid_v2.MessageAdapter;
import com.example.ken.socketchatwithandroid_v2.MessageData;
import com.example.ken.socketchatwithandroid_v2.R;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, Runnable {

    private EditText editor;
    private TextView userName;

    private ListView chatListView;
    private ArrayList<MessageData> itemsList;
    private MessageData messageData;
    private MessageAdapter mAdapter;

    private String content = "";

    private static int PORT = 7777;
    private static String HOST = "192.168.1.100";

    private BufferedReader in = null;
    private PrintWriter out = null;
    private Socket socket = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        editor = (EditText) findViewById(R.id.edit_input);
        userName = (TextView)findViewById(R.id.user_name);

        chatListView = (ListView) findViewById(R.id.message_list);

        itemsList = new ArrayList<MessageData>();

        initData();

        mAdapter = new MessageAdapter(MainActivity.this, itemsList, R.layout.item_message);

        chatListView.setAdapter(mAdapter);

        refresh();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
                //发送放这
                //发送
                String msg = editor.getText().toString();

                if (msg.equals("")) {
                    Toast.makeText(MainActivity.this, "不能发送空数据！", Toast.LENGTH_SHORT).show();
                } else {
                    sendMessage(0,msg);
                    editor.setText("");
                    messageData = new MessageData();
                    messageData.setName("我");
                    messageData.setMsg(msg);
                    messageData.setImgId(R.drawable.phone);

                    itemsList.add(messageData);
                    refresh();
                }
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void initData() {
        messageData = new MessageData();
        messageData.setName("我");
        messageData.setMsg("请点击上方连接服务器");
        messageData.setImgId(R.drawable.phone);

        itemsList.add(messageData);

    }

    private void sendMessage(int type,String msg){
        //type为类型，用于其他操作，默认0是发聊天消息，1是设置相关
        //发送时，将type直接加到开头，即第一位代表类型，服务器根据此解析
        if (socket == null) {
            Toast.makeText(MainActivity.this, "请先点击上方连接服务器！", Toast.LENGTH_SHORT).show();
        }else if (socket.isConnected()) {
            if (!socket.isOutputShutdown()) {


                out.println(String.valueOf(type)+msg);
            }
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {

            new Thread() {
                @Override
                public void run() {
                    //打开服务
                    try {
                        SharedPreferences sp = getSharedPreferences("system_config",MODE_PRIVATE);
                        int port = Integer.parseInt(sp.getString("port","7777"));
                        String host = sp.getString("ip","192.168.1.100");
                        socket = new Socket(host, port);
                        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);

                        Log.d("建立连接", "成功");

                        sendMessage(1,"_setName_"+sp.getString("name","Socket"));

                        new Thread(MainActivity.this).start();

                    } catch (IOException e) {
                        Log.d("建立连接", "失败");
                        e.printStackTrace();
                    }
                }
            }.start();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //刷新listView
    private Handler refreshHandler = new Handler() {
        public void handleMessage(Message msg) {

            mAdapter.notifyDataSetChanged();
            //SharedPreferences sp = getSharedPreferences("system_config",MODE_PRIVATE);

            //userName.setText(sp.getString("name","Socket Test"));
            super.handleMessage(msg);
        }
    };

    private void refresh() {

        Message message = new Message();
        message.what = 1;
        refreshHandler.sendMessage(message);
    }

    public void run() {
        try {
            while (true) {
                if (!socket.isClosed()) {
                    if (!socket.isInputShutdown()) {
                        if ((content = in.readLine()) != null) {

                            String name = content.substring(0,content.indexOf(":"));
                            SharedPreferences sp = getSharedPreferences("system_config",MODE_PRIVATE);
                            System.out.println(name);
                            if (!name.equals(sp.getString("name",""))){
                                //自己发的不显示
                                messageData = new MessageData();
                                messageData.setName("服务器");
                                messageData.setMsg(content);
                                messageData.setImgId(R.drawable.computer);

                                itemsList.add(messageData);
                                refresh();
                            }
                        }
                    }
                }
                Log.e("socket", "到循环了");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("socket", "直接挂了");
        }
    }

    //获取本机ip地址的函数


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
            //设置IP地址
            showSetDialog("ip");
        } else if (id == R.id.nav_gallery) {
            //设置端口

            showSetDialog("port");

        } else if (id == R.id.nav_slideshow) {
            //更改用昵称
            showSetDialog("name");


        } else if (id == R.id.nav_manage) {
            //清除列表
            for (int index = 0;index<itemsList.size();index++){
                itemsList.clear();
            }
            refresh();
            Toast.makeText(MainActivity.this,"清除列表成功！",Toast.LENGTH_SHORT).show();

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    private void showSetDialog(final String type){

        LayoutInflater inflater = LayoutInflater.from(this);
        final View textEntryView = inflater.inflate(R.layout.dialog_setting,null);
        final EditText editText = (EditText)textEntryView.findViewById(R.id.edtInput);

        SharedPreferences sp = getSharedPreferences("system_config",MODE_PRIVATE);
        final SharedPreferences.Editor editor = sp.edit();

        final AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("设置"+type);
        builder.setView(textEntryView);
        editText.setHint(sp.getString(type,"default"));
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //确定
                //保存到本地

                editor.putString(type,editText.getText().toString());
                editor.commit();

                if (type.equals("name")){
                    sendMessage(1,"_setName_"+editText.getText().toString());
                }else {
                    Toast.makeText(MainActivity.this,"请重新连接服务器以使配置生效",Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        //取消
                    }
                });
        builder.show();



    }
}
