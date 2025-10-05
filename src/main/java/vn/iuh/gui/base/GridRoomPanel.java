package vn.iuh.gui.base;

import vn.iuh.dto.response.BookingResponse;

import javax.swing.*;
import java.awt.*;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class GridRoomPanel extends JPanel implements Serializable {
    private List<RoomItem> roomItems;
    private Map<String, RoomItem> roomItemMap;
    private void init(List<RoomItem> rooms){
        setLayout(new GridLayout(0,3, 10,10));
        roomItems = rooms;
        roomItemMap = new HashMap<>();
        for(RoomItem room : rooms){
            if(!Objects.isNull(room.getRoomId())){
                roomItemMap.put(room.getRoomId(), room);
                add(room);
            }
        }
    }

    public void updateRoomItemStatus(List<BookingResponse> updatedRoomItems) {
        for (BookingResponse res : updatedRoomItems) {
            RoomItem roomItem = roomItemMap.get(res.getRoomId());
            System.out.println("Cập nhật ui cho phòng " + roomItem.getRoomId() + " từ " + roomItem.getBookingResponse().getRoomStatus() + " thành " + res.getRoomStatus());
            RoomItem newItem = new RoomItem(res);
            int index = getComponentZOrder(roomItem);
            remove(roomItem);
            add(newItem, index);
        }
        revalidate();
        repaint();
    }

    public void updateSingleRoomItem(String roomId, BookingResponse updatedRoom) {
        RoomItem roomItem = roomItemMap.get(roomId);
        if(roomItem != null){
            RoomItem newItem = new RoomItem(updatedRoom);
            int index = getComponentZOrder(roomItem);
            remove(roomItem);
            add(newItem, index);
            roomItemMap.put(roomId, newItem);
            revalidate();
            repaint();
        }
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
        this.roomItemMap.clear();
        for(RoomItem room : filteredRooms){
            roomItemMap.put(room.getRoomId(), room);
            add(room);
        }
        revalidate();
        repaint();
    }


}
