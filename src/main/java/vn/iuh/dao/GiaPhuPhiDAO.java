package vn.iuh.dao;

import vn.iuh.dto.response.HistoryFeeResponse;
import vn.iuh.entity.GiaPhuPhi;
import vn.iuh.entity.PhuPhi;
import vn.iuh.exception.TableEntityMismatch;
import vn.iuh.util.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class GiaPhuPhiDAO {
    private final Connection connection;

    public GiaPhuPhiDAO() {
        this.connection = DatabaseUtil.getConnect();
    }

    public GiaPhuPhiDAO(Connection connection) {
        this.connection = connection;
    }

    public GiaPhuPhi themGiaPhuPhi(GiaPhuPhi giaPhuPhi) {
        String query = "INSERT INTO GiaPhuPhi (ma_gia_phu_phi, gia_truoc_do, gia_hien_tai,ma_phien_dang_nhap,ma_phu_phi) VALUES (?, ?, ?, ? , ?)";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1,giaPhuPhi.getMaGiaPhuPhi());
            ps.setDouble(2, giaPhuPhi.getGiaTruocDo());
            ps.setDouble(3, giaPhuPhi.getGiaHienTai());
            ps.setString(4,giaPhuPhi.getMaPhienDangNhap());
            ps.setString(5,giaPhuPhi.getMaPhuPhi());
            ps.executeUpdate();
            return giaPhuPhi;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return null;
    }

    public GiaPhuPhi timPhuPhiMoiNhat() {
        String query = "SELECT TOP 1 * FROM GiaPhuPhi WHERE da_xoa = 0 ORDER BY thoi_gian_tao DESC";

        try (PreparedStatement ps = connection.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return mapResultSetToGiaPhuPhi(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public List<HistoryFeeResponse> getHistoryByMaPhuPhi(String maPhuPhi) {
        List<HistoryFeeResponse> list = new ArrayList<>();

        String sql = """
        SELECT gpp.thoi_gian_tao AS ngay_thay_doi,
               gpp.gia_hien_tai,
               nv.ma_nhan_vien,
               nv.ten_nhan_vien
        FROM GiaPhuPhi gpp
        LEFT JOIN PhienDangNhap pdn ON pdn.ma_phien_dang_nhap = gpp.ma_phien_dang_nhap
        LEFT JOIN TaiKhoan tk ON tk.ma_tai_khoan = pdn.ma_tai_khoan
        LEFT JOIN NhanVien nv ON nv.ma_nhan_vien = tk.ma_nhan_vien
        WHERE gpp.ma_phu_phi = ?
        ORDER BY gpp.thoi_gian_tao DESC
    """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, maPhuPhi);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    HistoryFeeResponse history = new HistoryFeeResponse(
                            rs.getTimestamp("ngay_thay_doi"),
                            rs.getDouble("gia_hien_tai"),
                            rs.getString("ma_nhan_vien"),
                            rs.getString("ten_nhan_vien")
                    );
                    list.add(history);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    private GiaPhuPhi mapResultSetToGiaPhuPhi(ResultSet rs) throws SQLException {
        GiaPhuPhi giaPhuPhi = new GiaPhuPhi();
        giaPhuPhi.setMaGiaPhuPhi(rs.getString("ma_gia_phu_phi"));
        giaPhuPhi.setGiaTruocDo(rs.getDouble("gia_truoc_do"));
        giaPhuPhi.setGiaHienTai(rs.getDouble("gia_hien_tai"));
        giaPhuPhi.setLaPhanTram(rs.getBoolean("la_phan_tram"));
        giaPhuPhi.setMaPhienDangNhap(rs.getString("ma_phien_dang_nhap"));
        giaPhuPhi.setMaPhuPhi(rs.getString("ma_phu_phi"));
        giaPhuPhi.setThoiGianTao(rs.getTimestamp("thoi_gian_tao"));
        return giaPhuPhi;
    }

}
