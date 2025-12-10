package vn.iuh.dao;

import vn.iuh.constraint.RoomStatus;
import vn.iuh.dto.repository.RoomJob;
import vn.iuh.dto.repository.WarningReservation;
import vn.iuh.entity.CongViec;
import vn.iuh.exception.TableEntityMismatch;
import vn.iuh.util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CongViecDAO {
    private final Connection connection;

    public CongViecDAO() {
        this.connection = DatabaseUtil.getConnect();
    }

    public CongViecDAO(Connection connection) {
        this.connection = connection;
    }

    public CongViec timCongViecMoiNhat() {
        String query = "SELECT TOP 1 * FROM CongViec ORDER BY ma_cong_viec DESC";

        try {
            PreparedStatement ps = connection.prepareStatement(query);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return chuyenKetQuaThanhCongViec(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (TableEntityMismatch et) {
            System.out.println(et.getMessage());
        }

        return null;
    }

    public CongViec themCongViec(CongViec newCV){

        String query = "INSERT INTO CongViec (ma_cong_viec, ten_trang_thai, tg_bat_dau, tg_ket_thuc, ma_phong) VALUES (?, ?, ?, ?, ?)";
        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, newCV.getMaCongViec());
            ps.setString(2, newCV.getTenTrangThai());
            ps.setTimestamp(3, newCV.getTgBatDau());
            ps.setTimestamp(4, newCV.getTgKetThuc());
            ps.setString(5, newCV.getMaPhong());

            ps.executeUpdate();
            return newCV;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    public void themDanhSachCongViec(List<CongViec> congViecs) {
        String query = "INSERT INTO CongViec (ma_cong_viec, ten_trang_thai, tg_bat_dau, tg_ket_thuc, ma_phong) VALUES (?, ?, ?, ?, ?)";
        try {
            PreparedStatement ps = connection.prepareStatement(query);
            for (CongViec congViec : congViecs) {
                ps.setString(1, congViec.getMaCongViec());
                ps.setString(2, congViec.getTenTrangThai());
                ps.setTimestamp(3, congViec.getTgBatDau());
                ps.setTimestamp(4, congViec.getTgKetThuc());
                ps.setString(5, congViec.getMaPhong());

                ps.addBatch();
            }
            ps.executeBatch();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public boolean kiemTraThoiGianCVTaiPhong(String maPhong, Timestamp tgBatDau, Timestamp tgKetThuc) {
        String query = "SELECT COUNT(*) AS count FROM CongViec WHERE ma_phong = ? AND (tg_bat_dau < ? AND tg_ket_thuc > ?)";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, maPhong);
            ps.setTimestamp(2, tgKetThuc);
            ps.setTimestamp(3, tgBatDau);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int count = rs.getInt("count");
                return count > 0; // Nếu count > 0, có nghĩa là có công việc trùng thời gian
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return false; // Không có công việc trùng thời gian
    }


    public boolean capNhatThoiGianKetThuc(String congViecId, Timestamp tgKetThuc, boolean isFinish) {

        String query = "UPDATE CongViec SET tg_ket_thuc = ?, da_xoa = ? WHERE ma_cong_viec = ?";
        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setTimestamp(1, tgKetThuc);
            ps.setBoolean(2, isFinish);
            ps.setString(3, congViecId);

            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0; // Trả về true nếu có ít nhất một hàng bị ảnh hưởng
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return false; // Trả về false nếu có lỗi xảy ra
    }

    public CongViec layCongViecHienTaiCuaPhong(String maPhong){
        String query = "SELECT TOP 1 * FROM CongViec WHERE ma_phong = ? AND getdate() >= dateadd(second, -60, tg_bat_dau) and da_xoa = 0 ORDER BY tg_bat_dau DESC";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, maPhong);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return chuyenKetQuaThanhCongViec(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (TableEntityMismatch et) {
            System.out.println(et.getMessage());
        }
        return null;
    }

    public List<CongViec> layCongViecHienTaiCuaCacPhong(List<String> danhSachMaPhong) {
        StringBuilder string = new StringBuilder(
                "SELECT * FROM CongViec WHERE ma_phong IN ("
        );
        for (int i = 0; i < danhSachMaPhong.size(); i++) {
            string.append("?");
            if (i < danhSachMaPhong.size() - 1) {
                string.append(", ");
            }
        }
        string.append(")");
        String query = string.toString();

        List<CongViec> danhSachCongViec = new java.util.ArrayList<>();
        try {
            PreparedStatement ps = connection.prepareStatement(query);
            for (int i = 0; i < danhSachMaPhong.size(); i++) {
                ps.setString(i + 1, danhSachMaPhong.get(i));
            }

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                danhSachCongViec.add(chuyenKetQuaThanhCongViec(rs));
            }
            return danhSachCongViec;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (TableEntityMismatch et) {
            System.out.println(et.getMessage());
        }

        return danhSachCongViec;
    }

    public CongViec chuyenKetQuaThanhCongViec(ResultSet rs) throws SQLException, TableEntityMismatch {
        try {
            String maCongViec = rs.getString("ma_cong_viec");
            String tenTrangThai = rs.getString("ten_trang_thai");
            Timestamp tgBatDau = rs.getTimestamp("tg_bat_dau");
            Timestamp tgKetThuc = rs.getTimestamp("tg_ket_thuc");
            String maPhong = rs.getString("ma_phong");
            Timestamp thoiGianTao = rs.getTimestamp("thoi_gian_tao");

            return new CongViec(maCongViec, tenTrangThai, tgBatDau, tgKetThuc, maPhong, thoiGianTao);
        } catch (SQLException e) {
            throw new TableEntityMismatch("Lỗi chuyển kết quả thành CongViec" + e.getMessage());
        }
    }


    public List<RoomJob> findAllRoomJobNow(){
        String query = "select cv.ma_cong_viec, p.ma_phong,cv.ten_trang_thai ,cv.tg_bat_dau, cv.tg_ket_thuc, cv.da_xoa from Phong p\n" +
                        "outer apply (\n" +
                        "\tselect top 1 * from CongViec cv\n" +
                        "\twhere p.ma_phong = cv.ma_phong AND getdate() >= cv.tg_bat_dau and cv.da_xoa = 0\n" +
                        "\torder by cv.thoi_gian_tao\n" +
                        ") as cv";
        try {
            PreparedStatement ps = connection.prepareStatement(query);

            ResultSet rs = ps.executeQuery();
            List<RoomJob> roomJobs = new java.util.ArrayList<>();
            while (rs.next()) {
                String maCongViec = rs.getString("ma_cong_viec");
                String maPhong = rs.getString("ma_phong");
                String tenTrangThai = rs.getString("ten_trang_thai");
                Timestamp tgBatDau = rs.getTimestamp("tg_bat_dau");
                Timestamp tgKetThuc = rs.getTimestamp("tg_ket_thuc");
                boolean isDeleted = rs.getBoolean("da_xoa");
                roomJobs.add(new RoomJob(maCongViec, maPhong, tenTrangThai, tgBatDau, tgKetThuc, isDeleted));
            }
            return roomJobs;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean removeJob(String maCongViec) {
        String query = "UPDATE CongViec SET da_xoa = 1 WHERE ma_cong_viec = ?";
        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, maCongViec);
            return  ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return false;
    }

    public int xoaDanhSachCongViec(List<String> danhSacMaCongViec) {
        if (danhSacMaCongViec == null || danhSacMaCongViec.isEmpty()) {
            return 0; // Không có công việc nào để xóa
        }

        StringBuilder string = new StringBuilder(
                "UPDATE CongViec SET da_xoa = 1 WHERE ma_cong_viec IN ("
        );
        for (int i = 0; i < danhSacMaCongViec.size(); i++) {
            string.append("?");
            if (i < danhSacMaCongViec.size() - 1) {
                string.append(", ");
            }
        }
        string.append(")");
        String query = string.toString();

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            for (int i = 0; i < danhSacMaCongViec.size(); i++) {
                ps.setString(i + 1, danhSacMaCongViec.get(i));
            }

             return ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Lỗi xóa danh sách công việc: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public CongViec layCongViecHienTaiCuaPhongChoCheckin(String maPhong) {
        String query = "SELECT TOP 1 * FROM CongViec WHERE ma_phong = ? and da_xoa = 0 ORDER BY tg_bat_dau DESC";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, maPhong);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return chuyenKetQuaThanhCongViec(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (TableEntityMismatch et) {
            System.out.println(et.getMessage());
        }

        return null;
    }

    public boolean xoaCongViecChoCheckIn(String maChiTietDatPhong) {
        String query = "UPDATE CongViec SET da_xoa = 1" +
                       " WHERE tg_bat_dau = (SELECT TOP 1 tg_nhan_phong FROM ChiTietDatPhong Phong WHERE ma_chi_tiet_dat_phong = ? )" +
                       " AND ma_phong = (SELECT TOP 1 ma_phong FROM ChiTietDatPhong Phong WHERE ma_chi_tiet_dat_phong = ? )" +
                       " AND da_xoa = 0";
        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, maChiTietDatPhong);

            ps.setString(2, maChiTietDatPhong);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return false;
    }

    // Cập nhật trường ma_phong cho một CongViec (cập nhập luôn thoi_gian_tao)
    public boolean capNhatMaPhongChoCongViec(String maCongViec, String maPhongMoi, Timestamp thoiGianCapNhat) {
        String sql = "UPDATE CongViec SET ma_phong = ?, thoi_gian_tao = ? WHERE ma_cong_viec = ? AND ISNULL(da_xoa,0)=0";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, maPhongMoi);
            ps.setTimestamp(2, thoiGianCapNhat);
            ps.setString(3, maCongViec);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Lỗi cập nhật ma_phong cho CongViec " + maCongViec + ": " + e.getMessage());
            return false;
        }
    }


    public List<CongViec> layDSCVChoCheckInCuaDDP(String maDonDatPhong) {
        String query =
                "select cv.* from DonDatPhong ddp\n" +
                        "left join ChiTietDatPhong ctdp on ctdp.ma_don_dat_phong = ddp.ma_don_dat_phong and ctdp.da_xoa = 0\n" +
                        "left join Phong p on p.ma_phong = ctdp.ma_phong\n" +
                        "cross apply (\n" +
                        "\tselect top 1 * from CongViec cv \n" +
                        "\twhere cv.ma_phong = p.ma_phong\n" +
                        "\t\tand cv.da_xoa = 0 \n" +
                        "\t\tand cv.ten_trang_thai = N'CHỜ CHECKIN'\n" +
                        ")as cv\n" +
                        "where ddp.ma_don_dat_phong = ?";



        List<CongViec> danhSachCongViec = new java.util.ArrayList<>();
        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, maDonDatPhong);
            ResultSet rs =ps.executeQuery();
            while(rs.next()){
                danhSachCongViec.add(chuyenKetQuaThanhCongViec(rs));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return danhSachCongViec;
    }


    public List<WarningReservation> getAllWarningReservations() {
        String query =
                "select \n" +
                        "\tddp.ma_don_dat_phong,\n" +
                        "\tddp.loai,\n" +
                        "\tddp.da_dat_truoc,\n" +
                        "\tddp.tg_nhan_phong,\n" +
                        "\tddp.tg_tra_phong,\n" +
                        "\tctdp.ma_chi_tiet_dat_phong,\n" +
                        "\tp.ma_phong,\n" +
                        "\tcv.ma_cong_viec,\n" +
                        "\tcv.tg_bat_dau,\n" +
                        "\tcv.tg_ket_thuc,\n" +
                        "\tcv.ten_trang_thai,\n" +
                        "\tdatediff(MILLISECOND,cv.tg_ket_thuc, getdate()) as thoi_gian_qua_han\n" +
                        "from DonDatPhong ddp\n" +
                        "join ChiTietDatPhong ctdp on ctdp.ma_don_dat_phong = ddp.ma_don_dat_phong \n" +
                        "\tand ctdp.kieu_ket_thuc is null\n" +
                        "\tand ctdp.da_xoa = 0\n" +
                        "join Phong p on p.ma_phong = ctdp.ma_phong\n" +
                        "\tand p.da_xoa = 0\n" +
                        "join CongViec cv on cv.ma_phong = p.ma_phong\n" +
                        "\tand cv.da_xoa = 0\n" +
                        "\tand getdate() >= cv.tg_ket_thuc\n" +
                        "where ddp.da_xoa = 0\n" +
                        "order by ddp.ma_don_dat_phong";

        List<WarningReservation> warningList = new ArrayList<>();
        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                WarningReservation wr = new WarningReservation(
                        rs.getString("ma_don_dat_phong"),     // reservationId
                        rs.getString("loai"),                 // reservationType
                        rs.getBoolean("da_dat_truoc"),        // isAdvanced
                        rs.getTimestamp("tg_nhan_phong"),     // checkinTime
                        rs.getTimestamp("tg_tra_phong"),      // checkoutTime
                        rs.getString("ma_chi_tiet_dat_phong"),// reservationDetailId
                        rs.getString("ma_phong"),             // roomId
                        rs.getString("ma_cong_viec"),         // jobId
                        rs.getTimestamp("tg_bat_dau"),        // startTimeJob
                        rs.getTimestamp("tg_ket_thuc"),       // endTimeJob
                        rs.getString("ten_trang_thai"),        // jobName
                        rs.getLong("thoi_gian_qua_han")
                );

                warningList.add(wr);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return warningList;
    }
}
