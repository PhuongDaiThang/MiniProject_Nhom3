package com.example.nhom8_w5;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;
import java.util.List;

/**
 * View (MVC): Adapter hiển thị danh sách phòng lên RecyclerView.
 * Hỗ trợ click để sửa, long click để xóa, và bấm nút gọi điện.
 */
public class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.RoomViewHolder> {

    private List<Room> roomList;
    private OnItemClickListener listener;
    private final DecimalFormat decimalFormat = new DecimalFormat("###,###,### VNĐ");

    // -------------------- Interface --------------------
    public interface OnItemClickListener {
        void onItemClick(Room room, int position);
        void onItemLongClick(Room room, int position);
        void onCallClick(String phoneNumber);
    }

    // -------------------- Constructor --------------------
    public RoomAdapter(List<Room> roomList, OnItemClickListener listener) {
        this.roomList = roomList;
        this.listener = listener;
    }

    // -------------------- RecyclerView Methods --------------------
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

        holder.tvRoomId.setText("Mã: " + room.getId());
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

            // Hiện nút gọi điện
            holder.btnCall.setVisibility(View.VISIBLE);
            holder.btnCall.setOnClickListener(v -> {
                if (listener != null) listener.onCallClick(room.getPhoneNumber());
            });
        } else {
            holder.tvStatusLabel.setText("Còn trống");
            holder.tvStatusLabel.setTextColor(ContextCompat.getColor(context, R.color.text_status_available));
            badgeBackground.setColor(ContextCompat.getColor(context, R.color.bg_status_available));

            holder.llRenterInfo.setVisibility(View.GONE);
            holder.btnCall.setVisibility(View.GONE);
        }

        // Click để sửa
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(room, holder.getAdapterPosition());
        });

        // Long click để xóa
        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) listener.onItemLongClick(room, holder.getAdapterPosition());
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return roomList.size();
    }

    // -------------------- ViewHolder --------------------
    static class RoomViewHolder extends RecyclerView.ViewHolder {
        TextView tvRoomId, tvRoomName, tvRoomPrice, tvStatusLabel, tvRenterName, tvPhone;
        LinearLayout llRenterInfo;
        ImageButton btnCall;

        public RoomViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRoomId       = itemView.findViewById(R.id.tvRoomId);
            tvRoomName     = itemView.findViewById(R.id.tvRoomName);
            tvRoomPrice    = itemView.findViewById(R.id.tvRoomPrice);
            tvStatusLabel  = itemView.findViewById(R.id.tvStatusLabel);
            tvRenterName   = itemView.findViewById(R.id.tvRenterName);
            tvPhone        = itemView.findViewById(R.id.tvPhone);
            llRenterInfo   = itemView.findViewById(R.id.llRenterInfo);
            btnCall        = itemView.findViewById(R.id.btnCall);
        }
    }
}
