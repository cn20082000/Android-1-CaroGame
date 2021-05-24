package com.example.carogame.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Checkable;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.carogame.Adapters.ChatListAdapter;
import com.example.carogame.Adapters.RoomListAdapter;
import com.example.carogame.Models.Chat;
import com.example.carogame.R;
import com.example.carogame.Utilities.CaroSocket;
import com.google.android.material.button.MaterialButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class PlayActivity extends AppCompatActivity {

    private Toolbar tb;
    private TextView tvPlayer1, tvPlayer2, tvClock, tvPlayer1Turn, tvPlayer2Turn, tvResult;
    private TableLayout tl;
    private MaterialButton btnReady, btnContinue, btnChat, btnSendChat;
    private ImageView imgNewChat;
    private View vCover, vCoverTl, vChat;
    private RecyclerView rvChat;
    private EditText edtChat;

    private String player1, player2;
    private boolean turn;
    private CountDownTimer timer, timerTemp;
    private ChatListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        Intent intent = getIntent();

        tb = findViewById(R.id.tb_play);
        tvPlayer1 = findViewById(R.id.tv_player_play);
        tvPlayer2 = findViewById(R.id.tv_competitor_play);
        tvClock = findViewById(R.id.tv_clock_play);
        tvPlayer1Turn = findViewById(R.id.tv_player_turn);
        tvPlayer2Turn = findViewById(R.id.tv_competitor_turn);
        tvResult = findViewById(R.id.tv_result_play);
        tl = findViewById(R.id.tl_play);
        btnReady = findViewById(R.id.btn_ready_play);
        btnContinue = findViewById(R.id.btn_continue_play);
        btnChat = findViewById(R.id.btn_chat);
        btnSendChat = findViewById(R.id.btn_send_chat);
        imgNewChat = findViewById(R.id.img_new_chat);
        vCover = findViewById(R.id.v_cover_play);
        vCoverTl = findViewById(R.id.v_cover_tl);
        vChat = findViewById(R.id.v_chat);
        rvChat = findViewById(R.id.rv_chat);
        edtChat = findViewById(R.id.edt_chat);

        // toolbar
        setSupportActionBar(tb);
        tb.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // button
        btnReady.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONObject obj = new JSONObject();
                try {
                    obj.put("name", player1);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                CaroSocket.socket.emit("ready", obj);
                btnReady.setEnabled(false);
            }
        });
        btnReady.setEnabled(false);

        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vCover.setVisibility(View.GONE);

                btnReady.setVisibility(View.VISIBLE);
                btnReady.setEnabled(true);

                for (int i = 0; i < 20; ++i) {
                    TableRow tr = (TableRow) tl.getChildAt(i);
                    for (int j = 0; j < 20; ++j) {
                        Square s = (Square) tr.getChildAt(j);
                        s.setPlayed(0);
                        s.setImageResource(R.drawable.ic_empty);
                        s.setEnabled(true);
                    }
                }
            }
        });

        btnChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vChat.setVisibility(View.VISIBLE);
                imgNewChat.setVisibility(View.INVISIBLE);
            }
        });

        btnSendChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!edtChat.getText().toString().equals("")) {
                    JSONObject obj = new JSONObject();
                    try {
                        obj.put("name", player1);
                        obj.put("msg", edtChat.getText().toString());
                        CaroSocket.socket.emit("client-chat", obj);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    edtChat.setText("");
                }
            }
        });

        // name
        String message = intent.getStringExtra("message");
        if (message.equals("create")) {
            tb.setTitle("Phòng của bạn");
            player1 = intent.getStringExtra("name");
            tvPlayer1.setText(player1);
            turn = true;
        } else {
            tb.setTitle("Phòng " + intent.getStringExtra("room"));
            player1 = intent.getStringExtra("name");
            tvPlayer1.setText(player1);
            player2 = intent.getStringExtra("competitor");
            tvPlayer2.setText(player2);
            btnReady.setEnabled(true);
            turn = false;
        }

        // turn
        tvPlayer1Turn.setVisibility(View.INVISIBLE);
        tvPlayer2Turn.setVisibility(View.INVISIBLE);

        // clock
        tvClock.setVisibility(View.INVISIBLE);

        timer = new CountDownTimer(20000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                tvClock.setText(String.valueOf(millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                if (turn) {
                    JSONObject obj = new JSONObject();
                    try {
                        obj.put("name", player1);
                        obj.put("x", -1);
                        obj.put("y", -1);
                        Log.e("play", obj.toString());
                        CaroSocket.socket.emit("move", obj);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        // cover
        vCover.setVisibility(View.GONE);
        vCoverTl.setVisibility(View.VISIBLE);

        // table
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;
        int size = width - 32;

        for (int i = 0; i < 20; ++i) {
            TableRow tableRow = new TableRow(this);
            tableRow.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT));

            for (int j = 0; j < 20; ++j) {
                Square im = new Square(this, j, i);
                im.setImageResource(R.drawable.ic_empty);
                im.setLayoutParams(new TableRow.LayoutParams(size / 20, size / 20));
                im.setClickable(true);
                im.setEnabled(true);

                im.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (turn) {
                            if (im.getPlayed() == 0) {
                                im.setEnabled(false);
                                im.setChecked(true);

                                timerTemp.cancel();
                                JSONObject obj = new JSONObject();
                                try {
                                    obj.put("name", player1);
                                    obj.put("x", im.getPosX());
                                    obj.put("y", im.getPosY());
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                Log.e("play", obj.toString());
                                CaroSocket.socket.emit("move", obj);
                                timerTemp = timer;
                                timerTemp.start();

                                vCoverTl.setVisibility(View.VISIBLE);
                                changeTurn();

                                turn = !turn;
                            }
                        }
                    }
                });

                tableRow.addView(im);
            }

            tl.addView(tableRow, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.WRAP_CONTENT));
        }
        tl.setEnabled(false);

        // recyclerview
        LinearLayoutManager layoutManager = new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false);
        layoutManager.setReverseLayout(true);
        adapter = new ChatListAdapter(player1, player2, this);
        rvChat.setLayoutManager(layoutManager);
        rvChat.setHasFixedSize(true);
        rvChat.setAdapter(adapter);

        CaroSocket.socket.on("new-player", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject obj = (JSONObject) args[0];
                try {
                    String player2 = obj.getString("name");
                    tvPlayer2.setText(player2);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            PlayActivity.this.btnReady.setEnabled(true);
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        CaroSocket.socket.on("game-start", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject obj = (JSONObject) args[0];

                try {
                    String name = obj.getString("name");
                    turn = name.equals(player1);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        btnReady.setVisibility(View.GONE);
                        vCoverTl.setVisibility(View.GONE);
                        tvClock.setVisibility(View.VISIBLE);
                        if (turn) {
                            tvPlayer1Turn.setVisibility(View.VISIBLE);
                        } else {
                            tvPlayer2Turn.setVisibility(View.VISIBLE);
                        }
                    }
                });
                timerTemp = timer;
                timerTemp.start();
            }
        });

        CaroSocket.socket.on("new-move", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject obj = (JSONObject) args[0];
                try {
                    int x = obj.getInt("x");
                    int y = obj.getInt("y");
                    TableRow tr = (TableRow) tl.getChildAt(y);
                    Square s = (Square) tr.getChildAt(x);
                    if (s.getPlayed() == 0) {
                        s.setEnabled(false);
                        s.setChecked(false);

                        timerTemp.cancel();
                        timerTemp = timer;
                        timerTemp.start();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                vCoverTl.setVisibility(View.GONE);
                                changeTurn();
                            }
                        });

                        turn = !turn;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        CaroSocket.socket.on("game-finished", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject obj = (JSONObject) args[0];
                Log.e("game-finished", obj.toString());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        gameFinished();
                    }
                });

                try {
                    String winer = obj.getString("name");

                    if (winer.equals(player1)) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                vCover.setVisibility(View.VISIBLE);
                                tvResult.setText("Chiến thắng");
                                tvResult.setTextColor(0xFF4CAF50);
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                vCover.setVisibility(View.VISIBLE);
                                tvResult.setText("Thua cuộc");
                                tvResult.setTextColor(0xFFF44336);
                            }
                        });
                    }

                    JSONArray line = obj.getJSONArray("line");
                    for (int i = 0; i < line.length(); ++i) {
                        JSONObject o = line.getJSONObject(i);
                        int x = o.getInt("x");
                        int y = o.getInt("y");
                        TableRow tr = (TableRow) tl.getChildAt(y);
                        Square s = (Square) tr.getChildAt(x);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (s.isChecked()) {
                                    s.setImageResource(R.drawable.ic_o_win);
                                } else {
                                    s.setImageResource(R.drawable.ic_x_win);
                                }
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        CaroSocket.socket.on("friend-disconnect", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tb.setTitle("Phòng của bạn");
                        tvPlayer2.setText("Đang chờ đối thủ...");
                    }
                });
                turn = true;
            }
        });

        CaroSocket.socket.on("new-chat", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject obj = (JSONObject) args[0];
                Log.e("chat", obj.toString());

                rvChat.smoothScrollToPosition(0);

                try {
                    String name = obj.getString("name");
                    String msg = obj.getString("msg");
                    Chat chat = new Chat(name, msg);
                    adapter.newChat(chat);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (vChat.getVisibility() != View.VISIBLE) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            imgNewChat.setVisibility(View.VISIBLE);
                        }
                    });
                }
            }
        });

        CaroSocket.socket.open();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CaroSocket.disconnect();
    }

    @Override
    public void onBackPressed() {
        if (vChat.getVisibility() == View.VISIBLE) {
            hideKeyboard();
            vChat.setVisibility(View.GONE);
        } else {
            super.onBackPressed();
        }
    }

    private void changeTurn() {
        if (tvPlayer1Turn.getVisibility() == View.VISIBLE) {
            tvPlayer1Turn.setVisibility(View.INVISIBLE);
            tvPlayer2Turn.setVisibility(View.VISIBLE);
            return;
        }
        if (tvPlayer2Turn.getVisibility() == View.VISIBLE) {
            tvPlayer2Turn.setVisibility(View.INVISIBLE);
            tvPlayer1Turn.setVisibility(View.VISIBLE);
        }
    }

    private void gameFinished() {
        timerTemp.cancel();
        tvClock.setVisibility(View.INVISIBLE);
        tvPlayer1Turn.setVisibility(View.INVISIBLE);
        tvPlayer2Turn.setVisibility(View.INVISIBLE);
        vCoverTl.setVisibility(View.VISIBLE);
    }

    private void hideKeyboard() {
//        View view = this.getCurrentFocus();
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
//        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        imm.hideSoftInputFromWindow(getWindow().getDecorView().getRootView().getWindowToken(), 0);
    }

    class Square extends androidx.appcompat.widget.AppCompatImageView implements Checkable {

        private int posX, posY;
        private int played;

        public Square(Context context) {
            super(context);
        }

        public Square(Context context, int posX, int posY) {
            super((context));
            this.posX = posX;
            this.posY = posY;
            played = 0;
        }

        public int getPosX() {
            return posX;
        }

        public int getPosY() {
            return posY;
        }

        public int getPlayed() {
            return played;
        }

        public void setPosX(int posX) {
            this.posX = posX;
        }

        public void setPosY(int posY) {
            this.posY = posY;
        }

        public void setPlayed(int played) {
            this.played = played;
        }

        @Override
        public void setChecked(boolean checked) {
            if (checked) {
                played = 1;
                this.setImageResource(R.drawable.ic_o);
            } else {
                played = -1;
                this.setImageResource(R.drawable.ic_x);
            }
        }

        @Override
        public boolean isChecked() {
            return played == 1;
        }

        @Override
        public void toggle() {

        }
    }
}