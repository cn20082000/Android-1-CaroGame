package com.example.carogame.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.carogame.Adapters.RoomListAdapter;
import com.example.carogame.Interfaces.OnSelectRoomListener;
import com.example.carogame.Models.Room;
import com.example.carogame.R;
import com.example.carogame.Utilities.CaroSocket;
import com.example.carogame.Utilities.Constants;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class RoomActivity extends AppCompatActivity implements OnSelectRoomListener {

    private Toolbar tb;
    private TextView tvName;
    private MaterialButton btnCreateRoom;
    private RecyclerView rvRoomList;
    private View vMain;

    private String name;

    private RoomListAdapter adapter;
    private List<Room> roomList = new ArrayList<>();
    private Room selectedRoom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);

        CaroSocket.init();

        tb = findViewById(R.id.tb_room);
        tvName = findViewById(R.id.tv_player_name);
        btnCreateRoom = findViewById(R.id.btn_create_room);
        rvRoomList = findViewById(R.id.rv_room_list);
        vMain = findViewById(R.id.v_room);

        // toolbar
        setSupportActionBar(tb);
        tb.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // name
        Intent intent = this.getIntent();
        name = intent.getStringExtra("name");
        tvName.setText(name);

        // create room
        btnCreateRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONObject obj = new JSONObject();
                try {
                    obj.put("name", name);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                CaroSocket.socket.emit("create-room", obj);

                CaroSocket.socket.off();

                Intent intent = new Intent(RoomActivity.this, PlayActivity.class);
                intent.putExtra("message", "create");
                intent.putExtra("name", name);
                startActivity(intent);

                finish();
            }
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false);
        adapter = new RoomListAdapter(name, this, roomList, this);
        rvRoomList.setLayoutManager(layoutManager);
        rvRoomList.setHasFixedSize(true);
        rvRoomList.setAdapter(adapter);

        // room list
        CaroSocket.socket.on("room-list", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject obj = (JSONObject) args[0];
                List<Room> data = new ArrayList<>();
                try {
                    JSONArray arr = obj.getJSONArray("rooms");
                    for (int i = 0; i < arr.length(); ++i) {
                        JSONObject o = arr.getJSONObject(i);
                        data.add(new Room(o.getString("room"), o.getString("player")));
                    }
                    boolean isUpdate = false;
                    for (int i = 0; i < data.size() && i < roomList.size(); ++i) {
                        if (!data.get(i).equal(roomList.get(i))) {
                            isUpdate = true;
                            break;
                        }
                    }
                    Log.e("list-room", data.toString());
                    if (isUpdate || data.size() != roomList.size()) {
                        roomList = data;
                        RoomActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                RoomActivity.this.notifyUpdate();
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        CaroSocket.socket.on("room-full", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject obj = (JSONObject) args[0];

                try {
                    boolean isFull = obj.getBoolean("full");
                    if (isFull) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Snackbar snackbar = Snackbar
                                        .make(vMain, "Phòng đã đầy", Snackbar.LENGTH_SHORT);
                                snackbar.show();
                            }
                        });
                    } else {
                        Intent intent = new Intent(RoomActivity.this, PlayActivity.class);
                        intent.putExtra("message", "join");
                        intent.putExtra("name", name);
                        intent.putExtra("competitor", selectedRoom.getName());
                        intent.putExtra("room", selectedRoom.getRoom());
                        RoomActivity.this.startActivity(intent);

                        finish();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        CaroSocket.socket.open();
    }

    @Override
    protected void onResume() {
        super.onResume();

        reloadRoom();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_room, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.btn_reload_room: {
                reloadRoom();
                break;
            }
            default:
        }

        return super.onOptionsItemSelected(item);
    }

    private void reloadRoom() {
        CaroSocket.socket.emit("get-room-list");
        Log.e("Room", "reload");
    }

    private void notifyUpdate() {
        adapter = new RoomListAdapter(name, RoomActivity.this, roomList, this);
        rvRoomList.setAdapter(adapter);

    }

    @Override
    public void onSelectRoom(Room room) {
        JSONObject obj = new JSONObject();

        try {
            obj.put("name", name);
            obj.put("room", Integer.parseInt(room.getRoom()));
            Log.e("adaper", obj.toString());

            CaroSocket.socket.emit("join-room", obj);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        selectedRoom = room;

//        Intent intent = new Intent(RoomActivity.this, PlayActivity.class);
//        intent.putExtra("message", "join");
//        intent.putExtra("name", name);
//        intent.putExtra("competitor", selectedRoom.getName());
//        intent.putExtra("room", selectedRoom.getRoom());
//        RoomActivity.this.startActivity(intent);
    }
}