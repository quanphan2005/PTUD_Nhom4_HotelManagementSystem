package vn.iuh.dao;

import vn.iuh.dto.repository.RoomJob;
import vn.iuh.entity.CongViec;
import vn.iuh.exception.TableEntityMismatch;
import vn.iuh.util.DatabaseUtil;

import java.sql.*;
import java.util.List;

public class CongViecDAO {
    private final Connection connection;

    public CongViecDAO() {
        this.connection = DatabaseUtil.getConnect();
    }
    public void thucHienGiaoTac() {
        try {
            if (connection != null && !connection.getAutoCommit()) {
                connection.commit();
                DatabaseUtil.disableTransaction(connection);
            }
        } catch (SQLException e) {
            System.out.println("Lỗi commit transaction: " + e.getMessage());
            DatabaseUtil.closeConnection(connection);
            throw new RuntimeException(e);
        }
    }

    public void hoanTacGiaoTac() {
        try {
            if (connection != null && !connection.getAutoCommit()) {
                connection.rollback();
                DatabaseUtil.disableTransaction(connection);
            }
        } catch (SQLException e) {
            System.out.println("Lỗi rollback transaction: " + e.getMessage());
            DatabaseUtil.closeConnection(connection);
            throw new RuntimeException(e);
        }
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
        String query = "SELECT TOP 1 * FROM CongViec WHERE ma_phong = ? AND GETDATE() BETWEEN tg_bat_dau AND tg_ket_thuc ORDER BY tg_bat_dau DESC";

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

    public String taoMaCongViecMoi(){
        CongViec congViecMoiNhat = timCongViecMoiNhat();
        String maCongViecMoi;
        if (congViecMoiNhat == null) {
            maCongViecMoi = "CV00000001";
        } else {
            String maCongViecCu = congViecMoiNhat.getMaCongViec();
            int soThuTu = Integer.parseInt(maCongViecCu.substring(2)); // Lấy phần số sau "CV"
            soThuTu++; // Tăng số thứ tự lên 1
            maCongViecMoi = String.format("CV%03d", soThuTu); // Định dạng lại thành CVxxx
        }
        return maCongViecMoi;
    }

    public List<RoomJob> findAllRoomJobNow(){
        String query = "select cv.ma_cong_viec, p.ma_phong,cv.ten_trang_thai ,cv.tg_bat_dau, cv.tg_ket_thuc, cv.da_xoa from Phong p\n" +
                "outer apply (\n" +
                "\tselect top 1 * from CongViec cv\n" +
                "\twhere p.ma_phong = cv.ma_phong and (getdate() between cv.tg_bat_dau and cv.tg_ket_thuc) and cv.da_xoa = 0\n" +
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

    public void removeJob(String maCongViec) {
        String query = "UPDATE CongViec SET da_xoa = 1 WHERE ma_cong_viec = ?";
        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, maCongViec);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }


}
