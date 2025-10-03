package vn.iuh.gui.base;

import vn.iuh.dto.response.BookingResponse;

import javax.swing.*;
import java.awt.*;
import java.io.Serializable;
import java.util.List;

public class GridRoomPanel extends JPanel implements Serializable {
    private List<RoomItem> roomItems;

    private void init(List<RoomItem> rooms){
        setLayout(new GridLayout(0,3, 10,10));

        this.roomItems = rooms;
        for(RoomItem room : rooms){
//            room.setPreferredSize(new Dimension(200, 120));
//            room.setMinimumSize(new Dimension(200, 120));
//            room.setMaximumSize(new Dimension(200, 120));
            add(room);
        }
    }

    public void updateRoomItemStatus(List<String> roomIds, String newStatus) {
        for (int i = 0; i < roomItems.size(); i++) {
            RoomItem roomItem = roomItems.get(i);

            if (roomIds.contains(roomItem.getRoomId())) {
                BookingResponse data = roomItem.getBookingResponse();
                data.setRoomStatus(newStatus);

                roomItems.set(i, new RoomItem(data));
            }
        }

        // Remove all components and re-add updated RoomItems
        removeAll();
        for (RoomItem room : roomItems) {
            add(room);
        }
        revalidate();
        repaint();
    }

    public GridRoomPanel(List<RoomItem> rooms) {
        init(rooms);
    }

    public List<RoomItem> getRoomItems() {
        return roomItems;
    }

    public void setRoomItems(List<RoomItem> filteredRooms) {
        this.roomItems = filteredRooms;
        removeAll();
        for(RoomItem room : filteredRooms){
            add(room);
        }
        revalidate();
        repaint();
    }
}
