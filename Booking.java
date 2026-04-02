package com.example;

public class Booking {

    private final String name;
    private final String room;
    private final String roomNumber;
    private final int days;
    private final int total;
    private final String checkIn;
    private final String checkOut;
    private final String phone;
    private final String email;

    public Booking(String name, String room, String roomNumber, int days, int total,
                   String checkIn, String checkOut, String phone, String email) {
        this.name       = name;
        this.room       = room;
        this.roomNumber = roomNumber;
        this.days       = days;
        this.total      = total;
        this.checkIn    = checkIn;
        this.checkOut   = checkOut;
        this.phone      = phone;
        this.email      = email;
    }

    public String getName()       { return name;       }
    public String getRoom()       { return room;       }
    public String getRoomNumber() { return roomNumber; }
    public int    getDays()       { return days;       }
    public int    getTotal()      { return total;      }
    public String getCheckIn()    { return checkIn;    }
    public String getCheckOut()   { return checkOut;   }
    public String getPhone()      { return phone;      }
    public String getEmail()      { return email;      }

    @Override
    public String toString() {
        return String.format("Booking{name='%s', room='%s', roomNumber='%s', days=%d, total=%d}",
                name, room, roomNumber, days, total);
    }
}