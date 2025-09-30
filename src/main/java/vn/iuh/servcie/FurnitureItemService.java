package vn.iuh.servcie;

import vn.iuh.entity.FurnitureItem;

public interface FurnitureItemService {
    FurnitureItem getFurnitureItemByID(String id);
    FurnitureItem createFurnitureItem(FurnitureItem furnitureItem);
    FurnitureItem updateFurnitureItem(FurnitureItem furnitureItem);
    boolean deleteFurnitureItemByID(String id);
}
