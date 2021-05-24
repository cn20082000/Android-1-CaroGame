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
import com.example.carogame.Interfaces.OnSelectRoomListener;
import com.example.carogame.Models.Room;
import com.example.carogame.R;
import com.example.carogame.Utilities.CaroSocket;
import com.google.android.material.button.MaterialButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class RoomListAdapter extends RecyclerView.Adapter<RoomListAdapter.DataViewHolder> {

    private final String name;
    private final List<Room> list;
    private final Context context;
    private final OnSelectRoomListener onSelectRoomListener;

    public RoomListAdapter(String name, Context context, List<Room> list,
                           OnSelectRoomListener onSelectRoomListener) {
        this.name = name;
        this.context = context;
        this.list = list;
        this.onSelectRoomListener = onSelectRoomListener;
    }

    @NonNull
    @Override
    public RoomListAdapter.DataViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_room_list,
                parent, false);
        return new DataViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RoomListAdapter.DataViewHolder holder, int position) {
        holder.tvRoomName.setText("Phòng số " + list.get(position).getRoom());
        holder.tvRoomStatus.setText("Người chơi " + list.get(position).getName() + " đang chờ...");
        holder.btnJoinRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSelectRoomListener.onSelectRoom(list.get(position));
            }
        });
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    public class DataViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvRoomName;
        private final TextView tvRoomStatus;
        private final MaterialButton btnJoinRoom;

        public DataViewHolder(@NonNull View itemView) {
            super(itemView);

            tvRoomName = itemView.findViewById(R.id.tv_room_name);
            tvRoomStatus = itemView.findViewById(R.id.tv_room_status);
            btnJoinRoom = itemView.findViewById(R.id.btn_join_room);
        }
    }
}
