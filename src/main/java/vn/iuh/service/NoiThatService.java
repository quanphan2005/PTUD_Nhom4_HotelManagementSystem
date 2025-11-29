package vn.iuh.service;

import vn.iuh.dto.repository.NoiThatAssignment;
import vn.iuh.entity.NoiThat;

import java.util.List;

public interface NoiThatService {
    List<NoiThat> getAllNoiThat();
    List<NoiThat> getNoiThatByLoaiPhong(String maLoaiPhong);
    boolean assignNoiThatToLoaiPhong(String maLoaiPhong, List<NoiThatAssignment> itemsWithQty);
}
