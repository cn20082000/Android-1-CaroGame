package com.example.carogame.Adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.carogame.Activities.PlayActivity;
import com.example.carogame.Models.Chat;
import com.example.carogame.Models.Room;
import com.example.carogame.R;
import com.example.carogame.Utilities.CaroSocket;
import com.google.android.material.button.MaterialButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.DataViewHolder> {

    private final String player1;
    private final String player2;
    private final List<Chat> list = new ArrayList<>();
    private final Context context;

    public ChatListAdapter(String player1, String player2, Context context) {
        this.player1 = player1;
        this.player2 = player2;
        this.context = context;
    }

    public void newChat(Chat chat) {
        list.add(0, chat);
        notifyItemInserted(0);
    }

    @NonNull
    @Override
    public ChatListAdapter.DataViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView;

        switch (viewType) {
            case 1:
                itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_player1,
                        parent, false);
                break;
            default:
                itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_player2,
                        parent, false);
                break;
        }
        return new ChatListAdapter.DataViewHolder(itemView);
    }

    @Override
    public int getItemViewType(int position) {
        if (list.get(position).getName().equals(player1)) {
            return 1;
        }
        return 2;
    }

    @Override
    public void onBindViewHolder(@NonNull ChatListAdapter.DataViewHolder holder, int position) {
        holder.tvMsg.setText(list.get(position).getMsg());
        holder.tvName.setText(list.get(position).getName());
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    public class DataViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvMsg;
        private final TextView tvName;

        public DataViewHolder(@NonNull View itemView) {
            super(itemView);

            tvName = itemView.findViewById(R.id.tv_chat_name);
            tvMsg = itemView.findViewById(R.id.tv_msg);
        }
    }
}