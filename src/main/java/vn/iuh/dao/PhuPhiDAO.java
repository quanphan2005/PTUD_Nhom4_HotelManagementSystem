package vn.iuh.dao;

import vn.iuh.dto.repository.ThongTinPhuPhi;
import vn.iuh.entity.PhuPhi;
import vn.iuh.exception.TableEntityMismatch;
import vn.iuh.util.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PhuPhiDAO {
    private final Connection connection;

    public PhuPhiDAO() {
        this.connection = DatabaseUtil.getConnect();
    }

    public PhuPhiDAO(Connection connection) {
        this.connection = connection;
    }

    public PhuPhi timPhuPhi(String id) {
        String query = "SELECT * FROM PhuPhi WHERE ma_phu_phi = ?";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, id);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return chuyenKetQuaThanhPhuPhi(rs);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (TableEntityMismatch et) {
            System.out.println(et.getMessage());
        }

        return null;
    }

    // Tạo mới AdditionalFee
    public PhuPhi themPhuPhi(PhuPhi phuPhi) {
        String query = "INSERT INTO PhuPhi (ma_phu_phi, ten_phu_phi) VALUES (?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, phuPhi.getMaPhuPhi());
            ps.setString(2, phuPhi.getTenPhuPhi());

            ps.executeUpdate();
            return phuPhi;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return null;
    }

    public PhuPhi capNhatPhuPhi(PhuPhi phuPhi) {
        String query = "UPDATE PhuPhi SET ten_phu_phi = ? WHERE ma_phu_phi = ?";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setDate(1, new java.sql.Date(phuPhi.getThoiGianTao().getTime()));
            ps.setString(2, phuPhi.getMaPhuPhi());

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                return timPhuPhi(phuPhi.getMaPhuPhi());
            } else {
                System.out.println("Không tìm thấy phụ phí có mã: " + phuPhi.getMaPhuPhi());
                return null;
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return null;
    }

    public boolean xoaPhuPhi(String id) {
        if (timPhuPhi(id) == null) {
            System.out.println("Không tìm thấy phụ phí có mã: " + id);
            return false;
        }

        String query = "DELETE FROM PhuPhi WHERE ma_phu_phi = ?";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, id);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Xóa phụ phí thành công!");
                return true;
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return false;
    }

    private PhuPhi timPhuPhiMoiNhat() {
        String query = "SELECT TOP 1 * FROM PhuPhi WHERE da_xoa = 0 ORDER BY ma_phu_phi DESC";

        try {
            PreparedStatement ps = connection.prepareStatement(query);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return chuyenKetQuaThanhPhuPhi(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (TableEntityMismatch te) {
            System.out.println(te.getMessage());
        }

        return null;
    }

    private PhuPhi chuyenKetQuaThanhPhuPhi(ResultSet rs) {
        PhuPhi phuPhi = new PhuPhi();
        try {
            phuPhi.setMaPhuPhi(rs.getString("ma_phu_phi"));
            phuPhi.setTenPhuPhi(rs.getString("ten_phu_phi"));
            return phuPhi;
        } catch (SQLException e) {
            throw new TableEntityMismatch("Không thể chuyển kết quả thành PhuPhi: " + e.getMessage());
        }
    }
    private ThongTinPhuPhi chuyenKetQuaThanhThongTinPhuPhi(ResultSet rs) {
        ThongTinPhuPhi phuPhi = new ThongTinPhuPhi();
        try {
            phuPhi.setMaPhuPhi(rs.getString("ma_phu_phi"));
            phuPhi.setTenPhuPhi(rs.getString("ten_phu_phi"));
            phuPhi.setLaPhanTram(rs.getBoolean("la_phan_tram"));
            phuPhi.setGiaHienTai(rs.getBigDecimal("gia_hien_tai"));
            return phuPhi;
        } catch (SQLException e) {
            throw new TableEntityMismatch("Không thể chuyển kết quả thành PhuPhi: " + e.getMessage());
        }
    }

    public ThongTinPhuPhi getThongTinPhuPhiByName(String name){
        String query =  "select top 1 pp.ma_phu_phi, pp.ten_phu_phi, gpp.la_phan_tram, gpp.gia_hien_tai from PhuPhi pp\n" +
                        "left join GiaPhuPhi gpp on pp.ma_phu_phi = gpp.ma_phu_phi\n" +
                        "where pp.da_xoa = 0 and gpp.da_xoa = 0 and pp.ten_phu_phi = ?\n"
                        +"order by gpp.thoi_gian_tao desc";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, name);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return chuyenKetQuaThanhThongTinPhuPhi(rs);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (TableEntityMismatch et) {
            System.out.println(et.getMessage());
        }
        return null;
    }


    public List<ThongTinPhuPhi> getDanhSachPhuPhi() {
        List<ThongTinPhuPhi> list = new ArrayList<>();
        String sql = """
            SELECT pp.ma_phu_phi, pp.ten_phu_phi, gpp.la_phan_tram, gpp.gia_hien_tai
            FROM PhuPhi pp
            OUTER APPLY (
                SELECT TOP 1 gia_hien_tai, la_phan_tram, thoi_gian_tao
                FROM GiaPhuPhi gpp
                WHERE gpp.ma_phu_phi = pp.ma_phu_phi
                ORDER BY gpp.ma_gia_phu_phi DESC
            ) AS gpp
        """;

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(chuyenKetQuaThanhThongTinPhuPhi(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }
}