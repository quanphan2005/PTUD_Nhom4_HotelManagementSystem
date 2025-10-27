package vn.iuh.service.impl;

import vn.iuh.dao.LoaiDichVuDAO;
import vn.iuh.entity.LoaiDichVu;
import vn.iuh.service.ServiceCategoryService;

import java.util.List;

public class ServiceCategoryServiceImpl implements ServiceCategoryService {
    private LoaiDichVuDAO loaiDichVuDAO;

    public ServiceCategoryServiceImpl(LoaiDichVuDAO loaiDichVuDAO) {
        this.loaiDichVuDAO = loaiDichVuDAO;
    }

    public ServiceCategoryServiceImpl() {
        this.loaiDichVuDAO = new LoaiDichVuDAO();
    }

    @Override
    public LoaiDichVu getServiceCategoryByID(String id) {
        return null;
    }

    @Override
    public List<LoaiDichVu> getAllServiceCategories() {
        return loaiDichVuDAO.layDanhSachLoaiDichVu();
    }

    @Override
    public LoaiDichVu createServiceCategory(LoaiDichVu loaiDichVu) {
        return null;
    }

    @Override
    public LoaiDichVu updateServiceCategory(LoaiDichVu loaiDichVu) {
        return null;
    }

    @Override
    public boolean deleteServiceCategoryByID(String id) {
        return false;
    }
}
