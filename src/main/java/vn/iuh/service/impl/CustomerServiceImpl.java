package vn.iuh.service.impl;

import vn.iuh.constraint.EntityIDSymbol;
import vn.iuh.dao.ChiTietDatPhongDAO;
import vn.iuh.dao.KhachHangDAO;
import vn.iuh.dao.LichSuThaoTacDAO;
import vn.iuh.dto.response.BookingResponse;
import vn.iuh.dto.response.BookingResponseV2;
import vn.iuh.entity.KhachHang;
import vn.iuh.entity.LichSuThaoTac;
import vn.iuh.service.CustomerService;
import vn.iuh.util.DatabaseUtil;
import vn.iuh.util.EntityUtil;

import java.util.List;

public class CustomerServiceImpl implements CustomerService {
    private final KhachHangDAO khachHangDAO;

    public CustomerServiceImpl() {
        this.khachHangDAO = new KhachHangDAO();
    }

    public CustomerServiceImpl(KhachHangDAO khachHangDAO) {
        this.khachHangDAO = khachHangDAO;
    }

    @Override
    public KhachHang getCustomerByID(String id) {
        if (id == null) return null;
        return khachHangDAO.timKhachHang(id);
    }

    @Override
    public KhachHang getCustomerByCCCD(String cccd) {
        return khachHangDAO.timKhachHangBangCCCD(cccd);
    }

    @Override
    public KhachHang createCustomer(KhachHang khachHang) {
        return null;
    }

    @Override
    public KhachHang updateCustomer(KhachHang khachHang) {
        return null;
    }

    @Override
    public boolean deleteCustomerByID(String id) {
        return false;
    }

    @Override
    public List<KhachHang> layTatCaKhachHang() {
        return khachHangDAO.layTatCaKhachHang();
    }

    @Override
    public List<BookingResponseV2> layDonDatPhongCuaKhach(String maKhachHang) {
        return khachHangDAO.layDonDatPhongTheoMaKhachHang(maKhachHang);
    }

    @Override
    public KhachHang createCustomerV2(KhachHang khachHang) {
        if (khachHang == null) throw new IllegalArgumentException("Khách hàng không hợp lệ");
        String ten = khachHang.getTenKhachHang();
        if (ten == null || ten.trim().isEmpty()) throw new IllegalArgumentException("Tên khách hàng không được rỗng");

        try {
            // khởi tạo transaction (DatabaseUtil có các method giống bạn dùng ở ServiceImpl)
            DatabaseUtil.khoiTaoGiaoTac();

            KhachHangDAO khDao = new KhachHangDAO();
            LichSuThaoTacDAO lichSuDao = new LichSuThaoTacDAO();

            // 1) kiểm tra trùng tên
            if (khDao.existsByTenKhachHang(ten)) {
                DatabaseUtil.hoanTacGiaoTac();
                return null; // caller xử lý thông báo trùng tên
            }

            // 2) sinh mã khách hàng mới
            String lastMa = khDao.timMaKhachHangMoiNhatRaw(); // có thể null
            String maMoi = EntityUtil.increaseEntityID(lastMa, EntityIDSymbol.CUSTOMER_PREFIX.getPrefix(), EntityIDSymbol.CUSTOMER_PREFIX.getLength());

            // 3) chèn khách hàng
            boolean ok = khDao.insertKhachHang(maMoi, khachHang.getCCCD(), ten.trim(), khachHang.getSoDienThoai());
            if (!ok) {
                DatabaseUtil.hoanTacGiaoTac();
                return null;
            }

            // 4) ghi lịch sử thao tác
            vn.iuh.entity.LichSuThaoTac last = lichSuDao.timLichSuThaoTacMoiNhat();
            String lastMaLs = last != null ? last.getMaLichSuThaoTac() : null;
            String maLs = EntityUtil.increaseEntityID(lastMaLs, EntityIDSymbol.WORKING_HISTORY_PREFIX.getPrefix(), EntityIDSymbol.WORKING_HISTORY_PREFIX.getLength());

            vn.iuh.entity.LichSuThaoTac lsmoi = new vn.iuh.entity.LichSuThaoTac();
            lsmoi.setMaLichSuThaoTac(maLs);
            lsmoi.setTenThaoTac(vn.iuh.constraint.ActionType.CREATE_CUSTOMER.getActionName()); // bạn có thể thêm ActionType.CREATE_CUSTOMER
            lsmoi.setMoTa("Thêm khách hàng mới: " + maMoi + " - " + ten.trim());
            lsmoi.setMaPhienDangNhap(vn.iuh.gui.base.Main.getCurrentLoginSession());
            lichSuDao.themLichSuThaoTac(lsmoi);

            // 5) commit
            DatabaseUtil.thucHienGiaoTac();

            // trả về đối tượng vừa chèn (lấy lại từ DB để đầy đủ)
            return khDao.timKhachHang(maMoi);

        } catch (Exception ex) {
            DatabaseUtil.hoanTacGiaoTac();
            throw new RuntimeException("Lỗi khi tạo khách hàng: " + ex.getMessage(), ex);
        }
    }

    @Override
    public KhachHang updateCustomerV2(KhachHang khachHang) {
        if (khachHang == null || khachHang.getMaKhachHang() == null || khachHang.getMaKhachHang().trim().isEmpty())
            throw new IllegalArgumentException("Mã khách hàng không hợp lệ");

        String ma = khachHang.getMaKhachHang();
        try {
            DatabaseUtil.khoiTaoGiaoTac();

            KhachHangDAO khDao = new KhachHangDAO();
            LichSuThaoTacDAO lichSuDao = new LichSuThaoTacDAO();

            // 0) Nếu khách hàng đang có đơn đặt phòng hiện tại/tương lai -> không cho sửa
            if (khDao.hasCurrentOrFutureBookings(ma)) {
                DatabaseUtil.hoanTacGiaoTac();
                throw new IllegalStateException("Không thể sửa: khách hàng đang có đơn đặt phòng hiện tại hoặc trong tương lai.");
            }

            // 1) lấy thông tin hiện tại
            KhachHang existing = khDao.timKhachHang(ma);
            if (existing == null) {
                DatabaseUtil.hoanTacGiaoTac();
                return null;
            }

            // 2) kiểm tra trùng (ngoại trừ id)
            StringBuilder dup = new StringBuilder();
            String newTen = khachHang.getTenKhachHang() != null ? khachHang.getTenKhachHang().trim() : null;
            String newCCCD = khachHang.getCCCD() != null ? khachHang.getCCCD().trim() : null;
            String newPhone = khachHang.getSoDienThoai() != null ? khachHang.getSoDienThoai().trim() : null;

            // tên
            if (newTen != null && !newTen.equalsIgnoreCase(existing.getTenKhachHang())) {
                if (khDao.existsByTenKhachHangExceptId(newTen, ma)) {
                    if (dup.length() > 0) dup.append(", ");
                    dup.append("tên");
                }
            }
            // cccd
            if (newCCCD != null && !newCCCD.equals(existing.getCCCD())) {
                if (khDao.existsByCCCDExceptId(newCCCD, ma)) {
                    if (dup.length() > 0) dup.append(", ");
                    dup.append("CCCD");
                }
            }
            // phone
            if (newPhone != null && !newPhone.equals(existing.getSoDienThoai())) {
                if (khDao.existsByPhoneExceptId(newPhone, ma)) {
                    if (dup.length() > 0) dup.append(", ");
                    dup.append("số điện thoại");
                }
            }

            if (dup.length() > 0) {
                DatabaseUtil.hoanTacGiaoTac();
                throw new IllegalStateException("Dữ liệu trùng: " + dup.toString() + ".");
            }

            // 3) cập nhật
            KhachHang updated = khDao.capNhatKhachHang(khachHang);
            if (updated == null) {
                DatabaseUtil.hoanTacGiaoTac();
                return null;
            }

            // 4) ghi lịch sử thao tác
            LichSuThaoTac last = lichSuDao.timLichSuThaoTacMoiNhat();
            String lastMa = last != null ? last.getMaLichSuThaoTac() : null;
            String maLichSu = EntityUtil.increaseEntityID(lastMa, EntityIDSymbol.WORKING_HISTORY_PREFIX.getPrefix(), EntityIDSymbol.WORKING_HISTORY_PREFIX.getLength());

            LichSuThaoTac lichSu = new LichSuThaoTac();
            lichSu.setMaLichSuThaoTac(maLichSu);
            // nếu bạn có ActionType.UPDATE_CUSTOMER thì dùng: ActionType.UPDATE_CUSTOMER.getActionName()
            lichSu.setTenThaoTac(vn.iuh.constraint.ActionType.UPDATE_CUSTOMER.getActionName());
            String moTa = String.format("Cập nhật khách hàng %s: tên '%s' -> '%s', CCCD '%s' -> '%s', điện thoại '%s' -> '%s'",
                    ma,
                    existing.getTenKhachHang(), newTen,
                    existing.getCCCD(), newCCCD,
                    existing.getSoDienThoai(), newPhone);
            lichSu.setMoTa(moTa);
            lichSu.setMaPhienDangNhap(vn.iuh.gui.base.Main.getCurrentLoginSession());
            lichSuDao.themLichSuThaoTac(lichSu);

            // 5) commit
            DatabaseUtil.thucHienGiaoTac();

            return updated;

        } catch (Exception ex) {
            DatabaseUtil.hoanTacGiaoTac();
            throw new RuntimeException("Lỗi khi cập nhật khách hàng: " + ex.getMessage(), ex);
        }
    }

    // kiểm tra khách hàng có đơn hiện/tương lai
    public boolean hasCurrentOrFutureBookings(String maKhachHang) {
        return khachHangDAO.hasCurrentOrFutureBookings(maKhachHang);
    }

    @Override
    public boolean deleteCustomerByIDV2(String id) {
        if (id == null || id.trim().isEmpty()) throw new IllegalArgumentException("Mã khách hàng không hợp lệ");

        try {
            // mở transaction
            DatabaseUtil.khoiTaoGiaoTac();

            KhachHangDAO khDao = new KhachHangDAO();
            ChiTietDatPhongDAO donDao = new ChiTietDatPhongDAO();
            LichSuThaoTacDAO lichSuDao = new LichSuThaoTacDAO();

            // 1) kiểm tra khách có đơn hiện/tương lai không
            if (khDao.hasCurrentOrFutureBookings(id)) {
                DatabaseUtil.hoanTacGiaoTac();
                throw new IllegalStateException("Không thể xóa: khách hàng đang có đơn đặt hiện tại hoặc trong tương lai.");
            }

            // 2) xóa (logical) chi tiết đơn của khách
            donDao.markBookingDetailsAsDeletedByCustomer(id);

            // 3) xóa (logical) các đơn của khách
            donDao.markBookingsAsDeletedByCustomer(id);

            // 4) xóa (logical) khách hàng
            boolean ok = khDao.xoaKhachHang(id);
            if (!ok) {
                DatabaseUtil.hoanTacGiaoTac();
                return false;
            }

            // 5) ghi lịch sử thao tác
            LichSuThaoTac last = lichSuDao.timLichSuThaoTacMoiNhat();
            String lastMa = last != null ? last.getMaLichSuThaoTac() : null;
            String maLs = EntityUtil.increaseEntityID(lastMa, EntityIDSymbol.WORKING_HISTORY_PREFIX.getPrefix(), EntityIDSymbol.WORKING_HISTORY_PREFIX.getLength());

            LichSuThaoTac lichSu = new LichSuThaoTac();
            lichSu.setMaLichSuThaoTac(maLs);
            // dùng ActionType phù hợp (nếu không có, thay bằng chuỗi)
            lichSu.setTenThaoTac(vn.iuh.constraint.ActionType.DELETE_CUSTOMER.getActionName());
            lichSu.setMoTa("Xóa khách hàng: " + id);
            lichSu.setMaPhienDangNhap(vn.iuh.gui.base.Main.getCurrentLoginSession());
            lichSuDao.themLichSuThaoTac(lichSu);

            // commit
            DatabaseUtil.thucHienGiaoTac();
            return true;
        } catch (IllegalStateException ise) {
            // để caller xử lý thông báo
            throw ise;
        } catch (Exception ex) {
            DatabaseUtil.hoanTacGiaoTac();
            throw new RuntimeException("Lỗi khi xóa khách hàng: " + ex.getMessage(), ex);
        }
    }


}
