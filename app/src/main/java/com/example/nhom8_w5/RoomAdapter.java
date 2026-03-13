package com.example.nhom8_w5;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;
import java.util.List;

public class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.RoomViewHolder> {

    private List<Room> roomList;
    private OnItemClickListener listener;
    private DecimalFormat decimalFormat = new DecimalFormat("###,###,### VNĐ");

    public interface OnItemClickListener {
        void onItemClick(Room room, int position);
        void onItemLongClick(Room room, int position);
    }

    public RoomAdapter(List<Room> roomList, OnItemClickListener listener) {
        this.roomList = roomList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_room, parent, false);
        return new RoomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RoomViewHolder holder, int position) {
        Room room = roomList.get(position);
        Context context = holder.itemView.getContext();
        
        holder.tvRoomName.setText(room.getName());
        holder.tvRoomPrice.setText(decimalFormat.format(room.getPrice()));
        
        GradientDrawable badgeBackground = (GradientDrawable) holder.tvStatusLabel.getBackground();
        
        if (room.isRented()) {
            holder.tvStatusLabel.setText("Đã thuê");
            holder.tvStatusLabel.setTextColor(ContextCompat.getColor(context, R.color.text_status_rented));
            badgeBackground.setColor(ContextCompat.getColor(context, R.color.bg_status_rented));
            
            holder.llRenterInfo.setVisibility(View.VISIBLE);
            holder.tvRenterName.setText(room.getRenterName());
            holder.tvPhone.setText(room.getPhoneNumber());
        } else {
            holder.tvStatusLabel.setText("Còn trống");
            holder.tvStatusLabel.setTextColor(ContextCompat.getColor(context, R.color.text_status_available));
            badgeBackground.setColor(ContextCompat.getColor(context, R.color.bg_status_available));
            
            holder.llRenterInfo.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> listener.onItemClick(room, position));
        holder.itemView.setOnLongClickListener(v -> {
            listener.onItemLongClick(room, position);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return roomList.size();
    }

    static class RoomViewHolder extends RecyclerView.ViewHolder {
        TextView tvRoomName, tvRoomPrice, tvStatusLabel, tvRenterName, tvPhone;
        LinearLayout llRenterInfo;

        public RoomViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRoomName = itemView.findViewById(R.id.tvRoomName);
            tvRoomPrice = itemView.findViewById(R.id.tvRoomPrice);
            tvStatusLabel = itemView.findViewById(R.id.tvStatusLabel);
            tvRenterName = itemView.findViewById(R.id.tvRenterName);
            tvPhone = itemView.findViewById(R.id.tvPhone);
            llRenterInfo = itemView.findViewById(R.id.llRenterInfo);
        }
    }
}
