package vn.iuh.service.impl;

import com.github.lgooddatepicker.zinternaltools.Pair;
import vn.iuh.dao.LoaiPhongDAO;
import vn.iuh.entity.LoaiPhong;
import vn.iuh.service.LoaiPhongService;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;

public class LoaiPhongServiceImpl implements LoaiPhongService {
    private final LoaiPhongDAO loaiPhongDao;

    public LoaiPhongServiceImpl() {
        this.loaiPhongDao = new LoaiPhongDAO();
    }

    @Override
    public LoaiPhong getRoomCategoryByID(String id) {
        return null;
    }

    @Override
    public LoaiPhong createRoomCategory(LoaiPhong loaiPhong) {
        return null;
    }

    @Override
    public LoaiPhong updateRoomCategory(LoaiPhong loaiPhong) {
        return null;
    }

    @Override
    public boolean deleteRoomCategoryByID(String id) {
        return false;
    }

    @Override
    public BigDecimal layGiaTheoLoaiPhong(String maLoaiPhong, boolean isGiaNgay) {
        if(maLoaiPhong != null){
            Map<String, Double> listPrice = loaiPhongDao.layGiaLoaiPhongTheoId(maLoaiPhong);
            if(Objects.isNull(listPrice) || listPrice.isEmpty()){
                throw new RuntimeException("Không tìm thấy giá của mã phòng");
            }
            else {
                return isGiaNgay ? BigDecimal.valueOf(listPrice.get("gia_ngay")) : BigDecimal.valueOf(listPrice.get("gia_gio"));
            }
        }
        throw new RuntimeException("Mã loại phòng rỗng ko tìm thấy ");
    }
}
