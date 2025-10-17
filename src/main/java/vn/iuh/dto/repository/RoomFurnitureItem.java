package vn.iuh.dto.repository;

public class RoomFurnitureItem {
    private String maNoiThat;
    private String name;
    private int quantity;

    public RoomFurnitureItem() {
    }

    public RoomFurnitureItem(String maNoiThat, String name, int quantity) {
        this.maNoiThat = maNoiThat;
        this.name = name;
        this.quantity = quantity;
    }

    public String getMaNoiThat() {
        return maNoiThat;
    }

    public String getName() {
        return name;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setMaNoiThat(String maNoiThat) {
        this.maNoiThat = maNoiThat;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
