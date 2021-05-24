package com.example.carogame.Models;

public class Room {
    private String room;
    private String name;

    public Room() {
        this.room = "";
        this.name = "";
    }

    public Room(String room, String name) {
        this.name = name;
        this.room = room;
    }

    public String getRoom() {
        return room;
    }

    public String getName() {
        return name;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean equal(Room room) {
        if (!this.name.equals(room.getName())) {
            return false;
        }
        return this.room.equals(room.getRoom());
    }
}
