package vn.iuh.servcie;

import vn.iuh.entity.LoaiDichVu;

public interface ServiceCategoryService {
    LoaiDichVu getServiceCategoryByID(String id);
    LoaiDichVu createServiceCategory(LoaiDichVu loaiDichVu);
    LoaiDichVu updateServiceCategory(LoaiDichVu loaiDichVu);
    boolean deleteServiceCategoryByID(String id);
}
