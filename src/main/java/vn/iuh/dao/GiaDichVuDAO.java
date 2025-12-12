package vn.iuh.dao;

import vn.iuh.dto.response.ServicePriceHistoryResponse;
import vn.iuh.util.DatabaseUtil;
import java.sql.*;
import java.util.*;

public class GiaDichVuDAO {
    private final Connection connection;

    public GiaDichVuDAO() {
        this.connection = DatabaseUtil.getConnect();
    }

    public GiaDichVuDAO(Connection connection) {
        this.connection = connection;
    }

    // Lấy giá dịch vụ mới nhất để sinh ID
    public Double timGiaMoiNhatTheoMaDichVu(String maDichVu) {
        String sql = "SELECT TOP 1 gia_moi FROM GiaDichVu " +
                "WHERE ma_dich_vu = ? AND da_xoa = 0 " +
                "ORDER BY thoi_gian_tao DESC, ma_gia_dich_vu DESC";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, maDichVu);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    double val = rs.getDouble("gia_moi");
                    if (rs.wasNull()) return null;
                    return val;
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Lỗi khi lấy giá mới nhất cho " + maDichVu + ": " + ex.getMessage(), ex);
        }
        return null;
    }

    /**
     * Lấy giá mới nhất cho một danh sách mã dịch vụ.
     * Trả về Map maDichVu -> gia (Double, có thể null nếu không có giá).
     */
    public Map<String, Double> timGiaMoiNhatChoDanhSach(List<String> maDichVus) {
        Map<String, Double> out = new HashMap<>();
        if (maDichVus == null || maDichVus.isEmpty()) return out;

        // Cách đơn giản: dùng vòng for gọi timGiaMoiNhatTheoMaDichVu — OK nếu số lượng vừa phải.
        // Nếu cần hiệu năng cho hàng ngàn id, thì sẽ viết 1 query phức tạp với subquery.
        for (String id : maDichVus) {
            out.put(id, timGiaMoiNhatTheoMaDichVu(id));
        }
        return out;
    }

    // Lấy lịch sử giá theo mã dịch vụ
    public List<ServicePriceHistoryResponse> layLichSuGiaTheoMaDichVu(String maDichVu) {
        List<ServicePriceHistoryResponse> list = new ArrayList<>();

        String sql = """
            SELECT g.thoi_gian_tao AS ngay_thay_doi,
                   g.gia_moi AS gia_hien_tai,
                   nv.ma_nhan_vien,
                   nv.ten_nhan_vien,
                   d.ten_dich_vu
            FROM GiaDichVu g
            LEFT JOIN PhienDangNhap pdn ON pdn.ma_phien_dang_nhap = g.ma_phien_dang_nhap
            LEFT JOIN TaiKhoan tk ON tk.ma_tai_khoan = pdn.ma_tai_khoan
            LEFT JOIN NhanVien nv ON nv.ma_nhan_vien = tk.ma_nhan_vien
            LEFT JOIN DichVu d ON d.ma_dich_vu = g.ma_dich_vu
            WHERE g.ma_dich_vu = ?
              AND g.da_xoa = 0
            ORDER BY g.thoi_gian_tao DESC
        """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, maDichVu);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Timestamp ngay = rs.getTimestamp("ngay_thay_doi");
                    double gia = rs.getDouble("gia_hien_tai");
                    Double giaObj = rs.wasNull() ? null : gia;
                    String maNV = rs.getString("ma_nhan_vien");
                    String tenNV = rs.getString("ten_nhan_vien");
                    String tenDV = rs.getString("ten_dich_vu");

                    ServicePriceHistoryResponse ph = new ServicePriceHistoryResponse(ngay, giaObj, maNV, tenNV, tenDV);
                    list.add(ph);
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Lỗi khi lấy lịch sử giá DichVu " + maDichVu + ": " + ex.getMessage(), ex);
        }

        return list;
    }

    // Lấy mã gia_dich_vu mới nhất
    public String timMaGiaDichVuMoiNhatRaw() {
        String sql = "SELECT TOP 1 ma_gia_dich_vu FROM GiaDichVu WHERE ma_gia_dich_vu IS NOT NULL ORDER BY ma_gia_dich_vu DESC";
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getString("ma_gia_dich_vu");
        } catch (SQLException ex) {
            throw new RuntimeException("Lỗi khi lấy mã GiaDichVu mới nhất: " + ex.getMessage(), ex);
        }
        return null;
    }

    // Thêm giá dịch vụ mới
    public boolean insertGiaDichVu(String maGia, Double giaCu, double giaMoi, String maPhienDangNhap, String maDichVu) {
        String sql = "INSERT INTO GiaDichVu (ma_gia_dich_vu, gia_cu, gia_moi, ma_phien_dang_nhap, ma_dich_vu, thoi_gian_tao, da_xoa) " +
                "VALUES (?, ?, ?, ?, ?, GETDATE(), 0)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, maGia);
            if (giaCu == null) ps.setNull(2, Types.DOUBLE);
            else ps.setDouble(2, giaCu);
            ps.setDouble(3, giaMoi);
            ps.setString(4, maPhienDangNhap);
            ps.setString(5, maDichVu);
            int r = ps.executeUpdate();
            return r > 0;
        } catch (SQLException ex) {
            throw new RuntimeException("Lỗi khi chèn GiaDichVu mới: " + ex.getMessage(), ex);
        }
    }

    // Xóa giá dịch vụ
    public boolean deleteServicePrice(String maDichVu) {
        String sql = "UPDATE GiaDichVu SET da_xoa = 1 WHERE ma_dich_vu = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, maDichVu);
            ps.executeUpdate();
            return true;
        } catch (SQLException ex) {
            throw new RuntimeException("Lỗi khi đánh dấu xóa GiaDichVu cho " + maDichVu + ": " + ex.getMessage(), ex);
        }
    }

}
