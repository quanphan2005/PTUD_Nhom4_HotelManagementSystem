package vn.iuh.dao;

import vn.iuh.dto.response.ServiceResponse;
import vn.iuh.entity.DichVu;
import vn.iuh.exception.TableEntityMismatch;
import vn.iuh.util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DichVuDAO {
    public DichVu timDichVu(String id) {
        String query = "SELECT * FROM DichVu WHERE ma_dich_vu = ? AND da_xoa = 0";

        try {
            Connection connection = DatabaseUtil.getConnect();
            PreparedStatement ps = connection.prepareStatement(query);

            ps.setString(1, id);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return chuyenKetQuaThanhDichVu(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (TableEntityMismatch te) {
            System.out.println(te.getMessage());
        }

        System.out.println("Không tìm thấy dịch vụ, mã: " + id);
        return null;
    }

    public List<DichVu> timTatCaDichVu() {
        String query = "SELECT * FROM DichVu WHERE da_xoa = 0";
        List<DichVu> dichVus = new java.util.ArrayList<>();

        try {
            Connection connection = DatabaseUtil.getConnect();
            PreparedStatement ps = connection.prepareStatement(query);


            ResultSet rs = ps.executeQuery();
            while (rs.next())
                dichVus.add(chuyenKetQuaThanhDichVu(rs));

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (TableEntityMismatch mismatchException) {
            System.out.println(mismatchException.getMessage());
        }

        return dichVus;
    }

    public DichVu createServiceItem(DichVu dichVu) {
        String query = "INSERT INTO DichVu " +
                "(ma_dich_vu, ten_dich_vu, ton_kho, co_the_tang, ma_loai_dich_vu) " +
                "VALUES (?, ?, ?, ?, ?)";

        try {
            Connection connection = DatabaseUtil.getConnect();
            PreparedStatement ps = connection.prepareStatement(query);

            ps.setString(1, dichVu.getMaDichVu());
            ps.setString(2, dichVu.getTenDichVu());
            ps.setInt(3, dichVu.getTonKho());
            ps.setBoolean(4, dichVu.getCoTheTang());
            ps.setString(5, dichVu.getMaLoaiDichVu());

            ps.executeUpdate();
            return dichVu;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return null;
    }

    public DichVu updateServiceItem(DichVu dichVu) {
        if (timDichVu(dichVu.getMaDichVu()) == null) {
            System.out.println("Không tìm thấy dịch vụ, mã: " + dichVu.getMaDichVu());
            return null;
        }

        String query = "UPDATE DichVu SET ten_dich_vu = ?, ton_kho = ?, co_the_tang = ?, ma_loai_dich_vu = ?" +
                "WHERE ma_dich_vu = ? AND da_xoa = 0";

        try {
            Connection connection = DatabaseUtil.getConnect();
            PreparedStatement ps = connection.prepareStatement(query);

            ps.setString(1, dichVu.getTenDichVu());
            ps.setInt(2, dichVu.getTonKho());
            ps.setBoolean(3, dichVu.getCoTheTang());
            ps.setString(4, dichVu.getMaLoaiDichVu());
            ps.setString(5, dichVu.getMaDichVu());


            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                return timDichVu(dichVu.getMaDichVu());
            } else {
                System.out.println("No service item found with ID: " + dichVu.getMaDichVu());
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return null;
    }

    public boolean deleteServiceItemByID(String id) {
        if (timDichVu(id) == null) {
            System.out.println("Không tìm thấy dịch vụ, mã: " + id);
            return false;
        }

        String query = "UPDATE DichVu SET da_xoa = 1 WHERE ma_dich_vu = ?";

        try {
            Connection connection = DatabaseUtil.getConnect();
            PreparedStatement ps = connection.prepareStatement(query);

            ps.setString(1, id);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Xóa dịch vụ thành công!");
                return true;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return false;
    }

    public DichVu timDichVuMoiNhat() {
        String query = "SELECT TOP 1 * FROM DichVu ORDER BY ma_dich_vu DESC WHERE da_xoa = 0";

        try {
            Connection connection = DatabaseUtil.getConnect();
            PreparedStatement ps = connection.prepareStatement(query);


            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return chuyenKetQuaThanhDichVu(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (TableEntityMismatch et) {
            System.out.println(et.getMessage());
        }

        return null;
    }

    public List<DichVu> timDanhSachDichVuBangDanhSachMa(List<String> serviceIds) {
        List<DichVu> dichVus = new java.util.ArrayList<>();
        if (serviceIds == null || serviceIds.isEmpty()) {
            return dichVus;
        }

        StringBuilder queryBuilder = new StringBuilder("SELECT * FROM DichVu WHERE da_xoa = 0 AND ma_dich_vu IN (");
        for (int i = 0; i < serviceIds.size(); i++) {
            queryBuilder.append("?");
            if (i < serviceIds.size() - 1) {
                queryBuilder.append(", ");
            }
        }
        queryBuilder.append(")");

        try {
            Connection connection = DatabaseUtil.getConnect();
            PreparedStatement ps = connection.prepareStatement(queryBuilder.toString());
            for (int i = 0; i < serviceIds.size(); i++) {
                ps.setString(i + 1, serviceIds.get(i));
            }

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                dichVus.add(chuyenKetQuaThanhDichVu(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (TableEntityMismatch te) {
            System.out.println(te.getMessage());
        }

        return dichVus;
    }

    private DichVu chuyenKetQuaThanhDichVu(ResultSet rs) throws SQLException {
        DichVu dichVu = new DichVu();
        try {
            dichVu.setMaDichVu(rs.getString("ma_dich_vu"));
            dichVu.setTenDichVu(rs.getString("ten_dich_vu"));
            dichVu.setTonKho(rs.getInt("ton_kho"));
            dichVu.setCoTheTang(rs.getBoolean("co_the_tang"));
            dichVu.setMaLoaiDichVu(rs.getString("ma_loai_dich_vu"));
            dichVu.setThoiGianTao(rs.getTimestamp("thoi_gian_tao"));
            return dichVu;
        } catch (SQLException e) {
            throw new TableEntityMismatch("Không thể chuyển kết quả thành DichVu: " + e.getMessage());
        }
    }

    // Tìm tất cả dịch vụ để hiển thị
    public List<ServiceResponse> timTatCaDichVuVoiGia() {
        List<ServiceResponse> out = new ArrayList<>();
        String sql =
                "SELECT d.ma_dich_vu, d.ten_dich_vu, d.ton_kho, d.co_the_tang, d.ma_loai_dich_vu, d.thoi_gian_tao, " +
                        "(SELECT TOP 1 g.gia_moi FROM GiaDichVu g WHERE g.ma_dich_vu = d.ma_dich_vu AND g.da_xoa = 0 " +
                        " ORDER BY g.thoi_gian_tao DESC, g.ma_gia_dich_vu DESC) AS gia_hien_tai " +
                        "FROM DichVu d " +
                        "WHERE d.da_xoa = 0 " +
                        "ORDER BY d.ma_dich_vu ASC";
        Connection connection = DatabaseUtil.getConnect();

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                ServiceResponse dto = new ServiceResponse();
                dto.setMaDichVu(rs.getString("ma_dich_vu"));
                dto.setTenDichVu(rs.getString("ten_dich_vu"));
                dto.setTonKho(rs.getInt("ton_kho"));
                dto.setCoTheTang(rs.getBoolean("co_the_tang"));
                dto.setMaLoaiDichVu(rs.getString("ma_loai_dich_vu"));
                dto.setThoiGianTao(rs.getTimestamp("thoi_gian_tao"));

                double g = rs.getDouble("gia_hien_tai");
                if (rs.wasNull()) dto.setGiaHienTai(null);
                else dto.setGiaHienTai(g);

                out.add(dto);
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Lỗi khi lấy danh sách dịch vụ kèm giá: " + ex.getMessage(), ex);
        }
        return out;
    }

    // Kiểm tra tên dịch vụ đã tồn tại
    public boolean existsByTenDichVu(String tenDichVu) {
        if (tenDichVu == null) return false;
        String sql = "SELECT 1 FROM DichVu WHERE LOWER(ten_dich_vu) = LOWER(?) AND da_xoa = 0";
        Connection connection = DatabaseUtil.getConnect();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, tenDichVu.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Lỗi khi kiểm tra trùng tên dịch vụ: " + ex.getMessage(), ex);
        }
    }

    // Tìm mã dịch vụ mới nhất để sinh ID
    public String timMaDichVuMoiNhatRaw() {
        String sql = "SELECT TOP 1 ma_dich_vu FROM DichVu WHERE ma_dich_vu IS NOT NULL ORDER BY ma_dich_vu DESC";
        Connection connection = DatabaseUtil.getConnect();
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getString("ma_dich_vu");
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Lỗi khi lấy mã DichVu mới nhất: " + ex.getMessage(), ex);
        }
        return null;
    }

    // Thêm dịch vụ mới
    public boolean insertNewDichVu(String maDichVu, String tenDichVu, int tonKho, boolean coTheTang, String maLoaiDichVu) {
        String sql = "INSERT INTO DichVu (ma_dich_vu, ten_dich_vu, ton_kho, co_the_tang, ma_loai_dich_vu, thoi_gian_tao, da_xoa) " +
                "VALUES (?, ?, ?, ?, ?, GETDATE(), 0)";
        Connection connection = DatabaseUtil.getConnect();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, maDichVu);
            ps.setString(2, tenDichVu);
            ps.setInt(3, tonKho);
            ps.setBoolean(4, coTheTang);
            ps.setString(5, maLoaiDichVu);
            int r = ps.executeUpdate();
            return r > 0;
        } catch (SQLException ex) {
            throw new RuntimeException("Lỗi khi chèn DichVu mới: " + ex.getMessage(), ex);
        }
    }

    // Kiểm tra trùng tên
    public boolean existsByTenDichVuExceptId(String tenDichVu, String excludeId) {
        if (tenDichVu == null) return false;
        String sql = "SELECT 1 FROM DichVu WHERE LOWER(ten_dich_vu) = LOWER(?) AND ma_dich_vu <> ? AND da_xoa = 0";
        Connection connection = DatabaseUtil.getConnect();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, tenDichVu.trim());
            ps.setString(2, excludeId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Lỗi khi kiểm tra trùng tên dịch vụ (except): " + ex.getMessage(), ex);
        }
    }

    // Sửa thông tin dịch vụ
    public boolean capNhatDichVu(String maDichVu, String tenDichVu, int tonKho, boolean coTheTang, String maLoaiDichVu) {
        String sql = "UPDATE DichVu SET ten_dich_vu = ?, ton_kho = ?, co_the_tang = ?, ma_loai_dich_vu = ?, thoi_gian_tao = GETDATE() " +
                "WHERE ma_dich_vu = ? AND ISNULL(da_xoa,0) = 0";
        Connection connection = DatabaseUtil.getConnect();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, tenDichVu);
            ps.setInt(2, tonKho);
            ps.setBoolean(3, coTheTang);
            ps.setString(4, maLoaiDichVu);
            ps.setString(5, maDichVu);
            int r = ps.executeUpdate();
            return r > 0;
        } catch (SQLException ex) {
            throw new RuntimeException("Lỗi khi cập nhật DichVu: " + ex.getMessage(), ex);
        }
    }

    // Tìm dịch vụ
    public ServiceResponse timDichVuV2(String maDichVu) {
        String sql = """
        SELECT d.ma_dich_vu,
               d.ten_dich_vu,
               d.ton_kho,
               d.co_the_tang,
               d.ma_loai_dich_vu,
               d.thoi_gian_tao,
               (SELECT TOP 1 g.gia_moi
                FROM GiaDichVu g
                WHERE g.ma_dich_vu = d.ma_dich_vu AND ISNULL(g.da_xoa,0)=0
                ORDER BY g.thoi_gian_tao DESC, g.ma_gia_dich_vu DESC) AS gia_hien_tai
        FROM DichVu d
        WHERE d.ma_dich_vu = ? AND ISNULL(d.da_xoa,0)=0
        """;
        Connection connection = DatabaseUtil.getConnect();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, maDichVu);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ServiceResponse dto = new ServiceResponse();
                    dto.setMaDichVu(rs.getString("ma_dich_vu"));
                    dto.setTenDichVu(rs.getString("ten_dich_vu"));
                    dto.setTonKho(rs.getInt("ton_kho"));
                    dto.setCoTheTang(rs.getBoolean("co_the_tang"));
                    dto.setMaLoaiDichVu(rs.getString("ma_loai_dich_vu"));
                    dto.setThoiGianTao(rs.getTimestamp("thoi_gian_tao"));

                    double g = rs.getDouble("gia_hien_tai");
                    if (rs.wasNull()) dto.setGiaHienTai(null);
                    else dto.setGiaHienTai(g);

                    return dto;
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Lỗi khi lấy thông tin dịch vụ (timDichVuV2): " + ex.getMessage(), ex);
        }

        return null;
    }

    public boolean markAsDeleted(String maDichVu) {
        String sql = "UPDATE DichVu SET da_xoa = 1 WHERE ma_dich_vu = ?";
        Connection connection = DatabaseUtil.getConnect();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, maDichVu);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new RuntimeException("Lỗi khi đánh dấu xóa DichVu " + maDichVu + ": " + ex.getMessage(), ex);
        }
    }

    // trong DichVuDAO (add method)
    public boolean capNhatTonKho(String maDichVu, int tonKho) {
        String sql = "UPDATE DichVu SET ton_kho = ?, thoi_gian_tao = GETDATE() WHERE ma_dich_vu = ? AND ISNULL(da_xoa,0) = 0";
        Connection connection = DatabaseUtil.getConnect();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, tonKho);
            ps.setString(2, maDichVu);
            int r = ps.executeUpdate();
            return r > 0;
        } catch (SQLException ex) {
            throw new RuntimeException("Lỗi khi cập nhật tồn kho: " + ex.getMessage(), ex);
        }
    }

}
