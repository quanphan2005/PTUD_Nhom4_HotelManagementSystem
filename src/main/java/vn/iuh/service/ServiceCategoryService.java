package vn.iuh.service;

import vn.iuh.entity.LoaiDichVu;

import java.util.List;

public interface ServiceCategoryService {
    LoaiDichVu getServiceCategoryByID(String id);
    List<LoaiDichVu> getAllServiceCategories();
    LoaiDichVu createServiceCategory(LoaiDichVu loaiDichVu);
    LoaiDichVu updateServiceCategory(LoaiDichVu loaiDichVu);
    boolean deleteServiceCategoryByID(String id);
}
