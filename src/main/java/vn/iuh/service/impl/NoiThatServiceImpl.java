// file: vn/iuh/service/impl/NoiThatServiceImpl.java
package vn.iuh.service.impl;

import vn.iuh.dao.NoiThatDAO;
import vn.iuh.dao.NoiThatTrongLoaiPhongDAO;
import vn.iuh.dto.repository.NoiThatAssignment;
import vn.iuh.entity.NoiThat;
import vn.iuh.service.NoiThatService;

import java.util.List;

public class NoiThatServiceImpl implements NoiThatService {

    private final NoiThatDAO noiThatDAO;
    private final NoiThatTrongLoaiPhongDAO mappingDAO;

    public NoiThatServiceImpl() {
        this.noiThatDAO = new NoiThatDAO();
        this.mappingDAO = new NoiThatTrongLoaiPhongDAO();
    }

    @Override
    public List<NoiThat> getAllNoiThat() {
        return noiThatDAO.layTatCaNoiThat();
    }

    @Override
    public List<NoiThat> getNoiThatByLoaiPhong(String maLoaiPhong) {
        if (maLoaiPhong == null || maLoaiPhong.isEmpty()) return java.util.Collections.emptyList();
        return mappingDAO.findByLoaiPhong(maLoaiPhong);
    }

    @Override
    public boolean assignNoiThatToLoaiPhong(String maLoaiPhong, List<NoiThatAssignment> itemsWithQty) {
        if (maLoaiPhong == null) return false;
        if (itemsWithQty == null) itemsWithQty = java.util.Collections.emptyList();
        // convert to DAO-level operation
        return mappingDAO.replaceMappingsWithQuantities(maLoaiPhong, itemsWithQty);
    }

}
