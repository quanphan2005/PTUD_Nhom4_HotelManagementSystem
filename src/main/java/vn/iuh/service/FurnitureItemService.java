package vn.iuh.service;

import vn.iuh.entity.NoiThat;

public interface FurnitureItemService {
    NoiThat getFurnitureItemByID(String id);
    NoiThat createFurnitureItem(NoiThat noiThat);
    NoiThat updateFurnitureItem(NoiThat noiThat);
    boolean deleteFurnitureItemByID(String id);
}
