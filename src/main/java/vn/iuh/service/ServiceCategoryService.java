package vn.iuh.service;

import vn.iuh.dto.response.ServiceCategoryResponse;
import vn.iuh.entity.LoaiDichVu;

import java.util.List;

public interface ServiceCategoryService {
    LoaiDichVu getServiceCategoryByID(String id);
    List<LoaiDichVu> getAllServiceCategories();
    LoaiDichVu createServiceCategory(LoaiDichVu loaiDichVu);
    LoaiDichVu updateServiceCategory(LoaiDichVu loaiDichVu);
    boolean deleteServiceCategoryByID(String id);

    List<ServiceCategoryResponse> getAllServiceCategoriesWithCount();
    LoaiDichVu createServiceCategoryV2(LoaiDichVu loaiDichVu);
    LoaiDichVu updateServiceCategoryV2(LoaiDichVu loaiDichVu);
    boolean capNhatTenLoaiDichVu(String maLoai, String tenMoi);
    boolean deleteServiceCategoryV2(String maLoai);
}
