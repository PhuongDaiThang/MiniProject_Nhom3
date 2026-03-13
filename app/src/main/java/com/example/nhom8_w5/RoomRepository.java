package com.example.nhom8_w5;

import java.util.ArrayList;
import java.util.List;

/**
 * Model Controller / Repository for managing Room data.
 * This class handles the business logic for the room list.
 */
public class RoomRepository {
    private List<Room> roomList;

    public RoomRepository() {
        this.roomList = new ArrayList<>();
        initDefaultData();
    }

    public RoomRepository(List<Room> roomList) {
        this.roomList = roomList != null ? roomList : new ArrayList<>();
    }

    private void initDefaultData() {
        roomList.add(new Room("P101", "Phòng 101 - Lầu 1", 1500000, false, "", ""));
        roomList.add(new Room("P102", "Phòng 102 - Lầu 1", 2000000, true, "Nguyễn Văn An", "0987654321"));
        roomList.add(new Room("P201", "Phòng 201 - Lầu 2", 1800000, false, "", ""));
    }

    public List<Room> getRoomList() {
        return roomList;
    }

    public void addRoom(Room room) {
        roomList.add(room);
    }

    public void updateRoom(int position, Room room) {
        if (position >= 0 && position < roomList.size()) {
            roomList.set(position, room);
        }
    }

    public void deleteRoom(int position) {
        if (position >= 0 && position < roomList.size()) {
            roomList.remove(position);
        }
    }

    public boolean isIdExists(String id) {
        for (Room r : roomList) {
            if (r.getId().equalsIgnoreCase(id)) return true;
        }
        return false;
    }
}
