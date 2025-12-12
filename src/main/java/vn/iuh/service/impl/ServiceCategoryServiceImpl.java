package vn.iuh.service.impl;

import vn.iuh.constraint.ActionType;
import vn.iuh.constraint.EntityIDSymbol;
import vn.iuh.dao.LichSuThaoTacDAO;
import vn.iuh.dao.LoaiDichVuDAO;
import vn.iuh.dto.response.ServiceCategoryResponse;
import vn.iuh.dto.response.ServiceResponse;
import vn.iuh.entity.LichSuThaoTac;
import vn.iuh.entity.LoaiDichVu;
import vn.iuh.gui.base.Main;
import vn.iuh.service.ServiceCategoryService;
import vn.iuh.service.ServiceService;
import vn.iuh.util.DatabaseUtil;
import vn.iuh.util.EntityUtil;

import java.sql.Connection;
import java.sql.SQLException;
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

    @Override
    public List<ServiceCategoryResponse> getAllServiceCategoriesWithCount() {
        return loaiDichVuDAO.layTatCaLoaiDichVuVaSoLuongDichVu();
    }

    @Override
    public LoaiDichVu createServiceCategoryV2(LoaiDichVu loaiDichVu) {
        if (loaiDichVu == null || loaiDichVu.getTenDichVu() == null || loaiDichVu.getTenDichVu().trim().isEmpty())
            throw new IllegalArgumentException("Tên loại dịch vụ không được rỗng");

        Connection conn = null;
        try {
            conn = DatabaseUtil.getConnect();
            conn.setAutoCommit(false);

            LoaiDichVuDAO daoTx = new LoaiDichVuDAO(conn);
            // 1) kiểm tra trùng tên
            if (daoTx.existsByTenLoaiDichVu(loaiDichVu.getTenDichVu().trim())) {
                conn.rollback();
                return null; // caller sẽ hiển thị thông báo trùng tên
            }

            // 2) sinh mã mới
            String lastMa = daoTx.timMaLoaiDichVuMoiNhatRaw(); // có thể null
            String maMoi = EntityUtil.increaseEntityID(lastMa, EntityIDSymbol.SERVICE_CATEGORY.getPrefix(), EntityIDSymbol.SERVICE_CATEGORY.getLength());

            // 3) chèn loại dịch vụ
            boolean ok = daoTx.insertLoaiDichVu(maMoi, loaiDichVu.getTenDichVu().trim());
            if (!ok) {
                conn.rollback();
                return null;
            }

            // 4) Ghi lịch sử thao tác
            LichSuThaoTacDAO lichSuDao = new LichSuThaoTacDAO(conn);
            LichSuThaoTac last = lichSuDao.timLichSuThaoTacMoiNhat();
            String lastMaLs = last != null ? last.getMaLichSuThaoTac() : null;
            String maLichSu = EntityUtil.increaseEntityID(lastMaLs, EntityIDSymbol.WORKING_HISTORY_PREFIX.getPrefix(), EntityIDSymbol.WORKING_HISTORY_PREFIX.getLength());

            LichSuThaoTac ls = new LichSuThaoTac();
            ls.setMaLichSuThaoTac(maLichSu);
            ls.setTenThaoTac(ActionType.CREATE_SERVICE_CATEGORY.getActionName());
            ls.setMoTa("Thêm loại dịch vụ mới: " + maMoi + " - " + loaiDichVu.getTenDichVu().trim());
            ls.setMaPhienDangNhap(Main.getCurrentLoginSession());
            lichSuDao.themLichSuThaoTac(ls);

            conn.commit();

            // trả về entity mới (lấy lại từ DB cho đầy đủ)
            return daoTx.timLoaiDichVu(maMoi);

        } catch (Exception ex) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (Exception ignored) {
                }
            }
            throw new RuntimeException("Lỗi khi tạo loại dịch vụ: " + ex.getMessage(), ex);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (Exception ignored) {
                }
            }
        }
    }

    @Override
    public LoaiDichVu updateServiceCategoryV2(LoaiDichVu loaiDichVu) {
        if (loaiDichVu == null || loaiDichVu.getMaLoaiDichVu() == null || loaiDichVu.getMaLoaiDichVu().trim().isEmpty())
            throw new IllegalArgumentException("Mã loại dịch vụ không hợp lệ");

        if (loaiDichVu.getTenDichVu() == null || loaiDichVu.getTenDichVu().trim().isEmpty())
            throw new IllegalArgumentException("Tên loại dịch vụ không được rỗng");

        Connection conn = null;
        try {
            conn = DatabaseUtil.getConnect();
            conn.setAutoCommit(false);

            LoaiDichVuDAO daoTx = new LoaiDichVuDAO(conn);

            // 1) Lấy bản ghi hiện tại từ DB
            LoaiDichVu current = daoTx.timLoaiDichVu(loaiDichVu.getMaLoaiDichVu());
            if (current == null) {
                // không tồn tại -> rollback và trả về null
                conn.rollback();
                return null;
            }

            String newName = loaiDichVu.getTenDichVu().trim();
            String currentName = current.getTenDichVu() != null ? current.getTenDichVu().trim() : "";

            // 2) Nếu tên không thay đổi -> trả về current (không cần cập nhật)
            if (currentName.equals(newName)) {
                conn.rollback(); // không có thay đổi, chỉ trả về current (rollback để không giữ transaction open)
                return current;
            }

            // 3) Kiểm tra trùng tên (không cho phép nếu tồn tại một loại khác đã dùng tên này)
            boolean existsByName = daoTx.existsByTenLoaiDichVu(newName);
            // nếu tồn tại theo tên nhưng đó chính là bản ghi hiện tại (không thể due to name changed check),
            // else nếu tồn tại và không phải bản ghi hiện tại -> duplicate
            if (existsByName) {
                // để chính xác: nếu tồn tại tên và tên khác với currentName -> duplicate
                conn.rollback();
                return null;
            }

            // 4) Thực hiện cập nhật
            LoaiDichVu toUpdate = new LoaiDichVu();
            toUpdate.setMaLoaiDichVu(loaiDichVu.getMaLoaiDichVu());
            toUpdate.setTenDichVu(newName);

            LoaiDichVu updated = daoTx.capNhatLoaiDichVuDAO(toUpdate);
            if (updated == null) {
                conn.rollback();
                return null;
            }

            // 5) Ghi lịch sử thao tác
            LichSuThaoTacDAO lichSuDao = new LichSuThaoTacDAO(conn);
            var last = lichSuDao.timLichSuThaoTacMoiNhat();
            String lastMaLs = last != null ? last.getMaLichSuThaoTac() : null;
            String maLichSu = EntityUtil.increaseEntityID(lastMaLs, EntityIDSymbol.WORKING_HISTORY_PREFIX.getPrefix(), EntityIDSymbol.WORKING_HISTORY_PREFIX.getLength());

            LichSuThaoTac ls = new LichSuThaoTac();
            ls.setMaLichSuThaoTac(maLichSu);
            // sử dụng ActionType (nếu có) — thay bằng chuỗi nếu enum khác
            ls.setTenThaoTac(ActionType.UPDATE_SERVICE_CATEGORY.getActionName());
            ls.setMoTa("Sửa loại dịch vụ: " + updated.getMaLoaiDichVu() + " - " + currentName + " -> " + newName);
            ls.setMaPhienDangNhap(Main.getCurrentLoginSession());
            lichSuDao.themLichSuThaoTac(ls);

            conn.commit();

            // trả về bản ghi mới (lấy lại đầy đủ)
            return daoTx.timLoaiDichVu(updated.getMaLoaiDichVu());

        } catch (Exception ex) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            throw new RuntimeException("Lỗi khi cập nhật loại dịch vụ: " + ex.getMessage(), ex);
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
            }
        }
    }

    @Override
    public boolean capNhatTenLoaiDichVu(String maLoai, String tenMoi) {
        if (maLoai == null || maLoai.trim().isEmpty()) throw new IllegalArgumentException("Mã loại không hợp lệ");
        if (tenMoi == null || tenMoi.trim().isEmpty()) throw new IllegalArgumentException("Tên mới không hợp lệ");

        LoaiDichVu existing = loaiDichVuDAO.timLoaiDichVu(maLoai);
        if (existing == null) return false;

        existing.setTenDichVu(tenMoi.trim());
        LoaiDichVu updated = updateServiceCategoryV2(existing);
        return updated != null;
    }

    @Override
    public boolean deleteServiceCategoryV2(String maLoai) {
        if (maLoai == null || maLoai.trim().isEmpty())
            throw new IllegalArgumentException("Mã loại dịch vụ không hợp lệ");

        // 0) Đầu tiên: lấy toàn bộ dịch vụ và lọc theo maLoai (dùng ServiceImpl để tận dụng DAO hiện có)
        ServiceService service = new ServiceImpl();
        List<ServiceResponse> all = service.layTatCaDichVuCungGia();
        List<ServiceResponse> inCategory = new java.util.ArrayList<>();
        if (all != null) {
            for (ServiceResponse sr : all) {
                if (maLoai.equals(sr.getMaLoaiDichVu())) inCategory.add(sr);
            }
        }

        // 1) Kiểm tra tất cả dịch vụ thuộc loại này có đang được dùng hay không.
        //    Nếu có 1 dịch vụ đang dùng => không cho xóa, ném IllegalStateException (caller sẽ hiển thị thông báo).
        for (ServiceResponse s : inCategory) {
            String maDv = s.getMaDichVu();
            if (maDv == null) continue;
            boolean inUse;
            try {
                inUse = service.isServiceCurrentlyUsed(maDv);
            } catch (Exception ex) {
                // lỗi khi kiểm tra DB -> ném tiếp để caller biết
                throw new RuntimeException("Lỗi khi kiểm tra trạng thái sử dụng dịch vụ " + maDv + ": " + ex.getMessage(), ex);
            }
            if (inUse) {
                // không thể xóa do có dịch vụ đang được sử dụng
                throw new IllegalStateException("Không thể xóa loại dịch vụ vì dịch vụ " + (s.getTenDichVu() != null ? s.getTenDichVu() : maDv) + " đang được sử dụng");
            }
        }

        // 2) Nếu không có dịch vụ nào đang dùng -> thực hiện xóa loại (logical delete) trong transaction,
        //    đồng thời ghi lịch sử thao tác.
        Connection conn = null;
        try {
            conn = DatabaseUtil.getConnect();
            conn.setAutoCommit(false);

            LoaiDichVuDAO daoTx = new LoaiDichVuDAO(conn);
            // Sử dụng method xóa đã có trong DAO (logical delete)
            boolean ok = daoTx.xoaLoaiDichVu(maLoai);
            if (!ok) {
                conn.rollback();
                return false;
            }

            // Ghi lịch sử thao tác
            LichSuThaoTacDAO lichSuDao = new LichSuThaoTacDAO(conn);
            var last = lichSuDao.timLichSuThaoTacMoiNhat();
            String lastMa = last != null ? last.getMaLichSuThaoTac() : null;
            String maLichSu = EntityUtil.increaseEntityID(lastMa, EntityIDSymbol.WORKING_HISTORY_PREFIX.getPrefix(), EntityIDSymbol.WORKING_HISTORY_PREFIX.getLength());

            LichSuThaoTac ls = new LichSuThaoTac();
            ls.setMaLichSuThaoTac(maLichSu);
            ls.setTenThaoTac(ActionType.DELETE_SERVICE_CATEGORY.getActionName()); // hoặc dùng chuỗi tương ứng nếu enum khác
            ls.setMoTa("Xóa loại dịch vụ: " + maLoai);
            ls.setMaPhienDangNhap(Main.getCurrentLoginSession());
            lichSuDao.themLichSuThaoTac(ls);

            conn.commit();
            return true;

        } catch (IllegalStateException ise) {
            // truyền thẳng IllegalStateException để caller biết nguyên nhân (dịch vụ đang dùng)
            if (conn != null) {
                try { conn.rollback(); } catch (Exception ignored) {}
            }
            throw ise;
        } catch (Exception ex) {
            if (conn != null) {
                try { conn.rollback(); } catch (Exception ignored) {}
            }
            throw new RuntimeException("Lỗi khi xóa loại dịch vụ: " + ex.getMessage(), ex);
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (Exception ignored) {}
            }
        }
    }

}
