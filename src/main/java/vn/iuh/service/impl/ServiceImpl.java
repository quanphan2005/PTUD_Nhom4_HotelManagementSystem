package vn.iuh.service.impl;

import vn.iuh.constraint.ActionType;
import vn.iuh.constraint.EntityIDSymbol;
import vn.iuh.dao.*;
import vn.iuh.dto.response.ServicePriceHistoryResponse;
import vn.iuh.dto.response.ServiceResponse;
import vn.iuh.entity.LichSuThaoTac;
import vn.iuh.entity.LoaiDichVu;
import vn.iuh.gui.base.Main;
import vn.iuh.service.ServiceService;
import vn.iuh.util.DatabaseUtil;
import vn.iuh.util.EntityUtil;

import javax.xml.crypto.Data;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class ServiceImpl implements ServiceService {

    private final DichVuDAO dichVuDAO;
    private final LoaiDichVuDAO loaiDichVuDAO;
    private final GiaDichVuDAO giaDichVuDAO;
    private final DonGoiDichVuDao donGoiDichVuDao;

    public ServiceImpl() {
        this.dichVuDAO = new DichVuDAO();
        this.loaiDichVuDAO = new LoaiDichVuDAO();
        this.giaDichVuDAO = new GiaDichVuDAO();
        this.donGoiDichVuDao = new DonGoiDichVuDao();
    }

    public ServiceImpl(DichVuDAO dichVuDAO, LoaiDichVuDAO loaiDichVuDAO, GiaDichVuDAO giaDichVuDAO) {
        this.dichVuDAO = dichVuDAO;
        this.loaiDichVuDAO = loaiDichVuDAO;
        this.giaDichVuDAO = giaDichVuDAO;
        this.donGoiDichVuDao = new DonGoiDichVuDao();
    }

    @Override
    public List<ServiceResponse> layTatCaDichVuCungGia() {
        return dichVuDAO.timTatCaDichVuVoiGia();
    }

    @Override
    public Map<String, String> layMapMaThanhTenLoaiDichVu() {
        return loaiDichVuDAO.layMapMaThanhTenLoaiDichVu();
    }

    @Override
    public List<LoaiDichVu> layTatCaLoaiDichVu() {
        return loaiDichVuDAO.layDanhSachLoaiDichVu();
    }

    @Override
    public List<ServicePriceHistoryResponse> layLichSuGiaDichVu(String maDichVu) {
        return giaDichVuDAO.layLichSuGiaTheoMaDichVu(maDichVu);
    }

    @Override
    public ServiceResponse themDichVuMoi(String tenDichVu, int tonKho, boolean coTheTang, String maLoaiDichVu, double giaMoi) {
        if (tenDichVu == null || tenDichVu.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên dịch vụ không được rỗng");
        }

        // mở connection mới và dùng transaction
        try {
            DatabaseUtil.khoiTaoGiaoTac();
            DichVuDAO dichVuDAO = new DichVuDAO();
            GiaDichVuDAO giaDao = new GiaDichVuDAO();
            LichSuThaoTacDAO lichSuDao = new LichSuThaoTacDAO();

            // 1) kiểm tra trùng tên
            if (dichVuDAO.existsByTenDichVu(tenDichVu)) {
                DatabaseUtil.hoanTacGiaoTac();
                return null; // caller GUI sẽ hiển thị thông báo trùng tên
            }

            // 2) sinh mã dịch vụ mới dựa trên ma mới nhất
            String lastMa = dichVuDAO.timMaDichVuMoiNhatRaw(); // có thể null
            String maMoi = EntityUtil.increaseEntityID(lastMa, "DV", 8);

            // 3) chèn dich vu
            boolean ok = dichVuDAO.insertNewDichVu(maMoi, tenDichVu, tonKho, coTheTang, maLoaiDichVu);
            if (!ok) {
                DatabaseUtil.hoanTacGiaoTac();
                return null;
            }

            // 4) chèn gia moi vao GiaDichVu
            String lastMaGia = giaDao.timMaGiaDichVuMoiNhatRaw();
            String maGiaMoi = EntityUtil.increaseEntityID(lastMaGia, "GDV", 8);
            // giaCu ta để null (lần đầu)
            boolean okGia = giaDao.insertGiaDichVu(maGiaMoi, null, giaMoi, Main.getCurrentLoginSession(), maMoi);
            if (!okGia) {
                DatabaseUtil.hoanTacGiaoTac();
                return null;
            }

            // 5) ghi lịch sử thao tác
            LichSuThaoTac lichSuThaoTac = lichSuDao.timLichSuThaoTacMoiNhat();
            String lastMaLichSu = (lichSuThaoTac == null) ? null : trimToNull(lichSuThaoTac.getMaLichSuThaoTac());            String maLichSu = EntityUtil.increaseEntityID(lastMaLichSu, "LT", 8);
            String moTa = "Thêm dịch vụ mới: " + maMoi + " - " + tenDichVu + " (giá: " + giaMoi + ")";
            LichSuThaoTac lsmoi = new LichSuThaoTac();
            lsmoi.setMaLichSuThaoTac(maLichSu);
            lsmoi.setMoTa(moTa);
            lsmoi.setTenThaoTac(ActionType.CREATE_SERVICE.getActionName());
            lsmoi.setMaPhienDangNhap(Main.getCurrentLoginSession());
            lichSuDao.themLichSuThaoTac(lsmoi);
//            if (!okLs) {
//                conn.rollback();
//                return null;
//            }

            // commit
           DatabaseUtil.thucHienGiaoTac();

            // tạo ServiceResponse để trả về (bạn có thể thêm constructor / setter tương ứng)
            ServiceResponse created = new ServiceResponse();
            created.setMaDichVu(maMoi);
            created.setTenDichVu(tenDichVu);
            created.setTonKho(tonKho);
            created.setMaLoaiDichVu(maLoaiDichVu);
            created.setGiaHienTai(giaMoi);

            return created;

        } catch (Exception ex) {
            DatabaseUtil.hoanTacGiaoTac();
            throw new RuntimeException("Lỗi khi thêm dịch vụ mới: " + ex.getMessage(), ex);
        }
    }

    @Override
    public ServiceResponse capNhatDichVu(String maDichVu, String tenDichVu, int tonKho, String maLoaiDichVu, double giaMoi) {
        if (maDichVu == null || maDichVu.trim().isEmpty()) throw new IllegalArgumentException("Mã dịch vụ không hợp lệ");
        if (tenDichVu == null || tenDichVu.trim().isEmpty()) throw new IllegalArgumentException("Tên dịch vụ không được rỗng");

        try {
            DatabaseUtil.khoiTaoGiaoTac();

            // dùng DAO với connection chung để transaction
            DichVuDAO dichVuDAO = new DichVuDAO();
            GiaDichVuDAO giaDao = new GiaDichVuDAO();
            LichSuThaoTacDAO lichSuDao = new LichSuThaoTacDAO();

            // Lấy thông tin hiện tại của dịch vụ (để so sánh tên / giá hiện tại)
            ServiceResponse existing = dichVuDAO.timDichVuV2(maDichVu); // nếu bạn chưa có hàm timDichVu(conn) -> timDichVu ở DichVuDAO hiện có dùng connection trường
            if (existing == null) {
                DatabaseUtil.hoanTacGiaoTac();
                return null;
            }

            // 1) nếu tên đổi (khác với existing.getTenDichVu()), kiểm tra trùng tên (ngoại trừ chính bản thân)
            if (!existing.getTenDichVu().equalsIgnoreCase(tenDichVu)) {
                if (dichVuDAO.existsByTenDichVuExceptId(tenDichVu, maDichVu)) {
                    DatabaseUtil.hoanTacGiaoTac();
                    return null; // trùng tên
                }
            }

            // 2) cập nhật thông tin DichVu (không chỉnh co_the_tang theo yêu cầu - giữ nguyên từ existing)
            boolean coTheTang = false;
            boolean okUpd = dichVuDAO.capNhatDichVu(maDichVu, tenDichVu, tonKho, coTheTang, maLoaiDichVu);
            if (!okUpd) {
                DatabaseUtil.hoanTacGiaoTac();
                return null;
            }

            // 3) nếu giá mới khác (hoặc luôn ghi 1 bản giá mới) -> thêm bản ghi GiaDichVu
            // Lưu ý: để đơn giản và theo yêu cầu, luôn thêm 1 bản GiaDichVu mới (để lưu lịch sử)
            String lastMaGia = giaDao.timMaGiaDichVuMoiNhatRaw();
            String maGiaMoi = EntityUtil.increaseEntityID(lastMaGia, "GDV", 8);
            Double giaCu = existing.getGiaHienTai() != null ? existing.getGiaHienTai() : null;
            boolean okGia = giaDao.insertGiaDichVu(maGiaMoi, giaCu, giaMoi, Main.getCurrentLoginSession(), maDichVu);
            if (!okGia) {
                DatabaseUtil.hoanTacGiaoTac();
                return null;
            }

            // 4) ghi lịch sử thao tác
            // tạo mã lịch sử mới
            String lastMaLichSu = null;
            try {
                // nếu có method timLichSuThaoTacMoiNhat() trả entity
                var last = new LichSuThaoTacDAO().timLichSuThaoTacMoiNhat();
                lastMaLichSu = last != null ? last.getMaLichSuThaoTac() : null;
            } catch (Exception ex) {
                // fallback: lấy raw mã
                LichSuThaoTac lichSuThaoTac = lichSuDao.timLichSuThaoTacMoiNhat();
                lastMaLichSu = lichSuThaoTac.getMaLichSuThaoTac();
            }
            String maLichSu = EntityUtil.increaseEntityID(lastMaLichSu, "LT", 8);
            LichSuThaoTac lichSu = new LichSuThaoTac();
            lichSu.setMaLichSuThaoTac(maLichSu);
            lichSu.setTenThaoTac(ActionType.UPDATE_SERVICE.getActionName());
            String moTa = String.format("Cập nhật dịch vụ %s: tên từ '%s' -> '%s', giá: %s -> %s", maDichVu,
                    existing.getTenDichVu(), tenDichVu,
                    giaCu != null ? giaCu : "NULL", giaMoi);
            lichSu.setMoTa(moTa);
            lichSu.setMaPhienDangNhap(Main.getCurrentLoginSession());
            // dùng DAO để insert
            new LichSuThaoTacDAO().themLichSuThaoTac(lichSu);

            // commit
            DatabaseUtil.thucHienGiaoTac();

            // trả về ServiceResponse mới (cập nhật)
            ServiceResponse updated = new ServiceResponse();
            updated.setMaDichVu(maDichVu);
            updated.setTenDichVu(tenDichVu);
            updated.setTonKho(tonKho);
            updated.setMaLoaiDichVu(maLoaiDichVu);
            updated.setGiaHienTai(giaMoi);
            updated.setCoTheTang(coTheTang);

            return updated;

        } catch (Exception ex) {
            DatabaseUtil.hoanTacGiaoTac();
            throw new RuntimeException("Lỗi khi cập nhật dịch vụ: " + ex.getMessage(), ex);
        }
    }

    @Override
    public boolean isServiceCurrentlyUsed(String maDichVu) {
        try {
            // chuyển tiếp đến DAO; DAO sử dụng cùng connection nội bộ của nó
            return donGoiDichVuDao.isServiceCurrentlyUsed(maDichVu);
        } catch (Exception ex) {
            // nếu lỗi DB, log ra và trả về false (hoặc bạn có thể ném exception tuỳ ý)
            ex.printStackTrace();
            throw new RuntimeException("Lỗi khi kiểm tra trạng thái sử dụng dịch vụ: " + ex.getMessage(), ex);
        }
    }

    @Override
    public boolean xoaDichVu(String maDichVu) {
        if (maDichVu == null || maDichVu.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã dịch vụ không hợp lệ");
        }

        try {
            // 1) open connection & transaction
            DatabaseUtil.khoiTaoGiaoTac();

            // DAO dùng connection chung
            DonGoiDichVuDao donGoiDichVuDaoTx = new DonGoiDichVuDao();
            // double-check: nếu đang dùng thì chặn
            if (donGoiDichVuDaoTx.isServiceCurrentlyUsed(maDichVu)) {
                DatabaseUtil.hoanTacGiaoTac();
                throw new IllegalStateException("Không thể xóa dịch vụ do dịch vụ này đang được sử dụng");
            }

            DichVuDAO dichVuDaoTx = new DichVuDAO();
            GiaDichVuDAO giaDaoTx = new GiaDichVuDAO();
            LichSuThaoTacDAO lichSuDaoTx = new LichSuThaoTacDAO();

            // 2) logical delete giá dịch vụ
            boolean okGia = giaDaoTx.deleteServicePrice(maDichVu);
            if (!okGia) {
                // vẫn có thể continue hoặc rollback tùy bạn — ở đây rollback
                DatabaseUtil.hoanTacGiaoTac();
                return false;
            }

            // 3) logical delete dịch vụ (DichVu.da_xoa = 1)
            boolean okDv = dichVuDaoTx.markAsDeleted(maDichVu);
            if (!okDv) {
                DatabaseUtil.hoanTacGiaoTac();
                return false;
            }

            // 4) ghi lịch sử thao tác
            // tạo mã lịch sử mới (giống pattern bạn đang dùng)
            LichSuThaoTac last = lichSuDaoTx.timLichSuThaoTacMoiNhat();
            String lastMa = last != null ? last.getMaLichSuThaoTac() : null;
            String maLichSu = EntityUtil.increaseEntityID(lastMa, EntityIDSymbol.WORKING_HISTORY_PREFIX.getPrefix(), EntityIDSymbol.WORKING_HISTORY_PREFIX.getLength());

            LichSuThaoTac lichSu = new LichSuThaoTac();
            lichSu.setMaLichSuThaoTac(maLichSu);
            // nếu bạn muốn có 1 action cụ thể, thêm enum ActionType.DELETE_SERVICE (mình gợi ý phía dưới)
            lichSu.setTenThaoTac(ActionType.DELETE_SERVICE.getActionName()); // hoặc ActionType.DELETE_SERVICE nếu bạn thêm
            String moTa = "Xóa dịch vụ: " + maDichVu;
            lichSu.setMoTa(moTa);
            lichSu.setMaPhienDangNhap(Main.getCurrentLoginSession());
            lichSuDaoTx.themLichSuThaoTac(lichSu);

            // commit
           DatabaseUtil.thucHienGiaoTac();
            return true;

        } catch (Exception ex) {
            DatabaseUtil.hoanTacGiaoTac();
            throw new RuntimeException("Lỗi khi xóa dịch vụ: " + ex.getMessage(), ex);
        }
    }

    // Xóa khoảng trắng
    private String trimToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
