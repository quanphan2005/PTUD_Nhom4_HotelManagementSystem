package vn.iuh.dao;

import vn.iuh.dto.repository.ThongTinDichVu;
import vn.iuh.entity.PhongDungDichVu;
import vn.iuh.exception.TableEntityMismatch;
import vn.iuh.util.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class GoiDichVuDao {
    private final Connection connection;

    public GoiDichVuDao() {
        this.connection = DatabaseUtil.getConnect();
    }

    public GoiDichVuDao(Connection connection) {
        this.connection = connection;
    }

    public List<ThongTinDichVu> timTatCaThongTinDichVu() {
        String query = "SELECT dv.ma_dich_vu, dv.ten_dich_vu, dv.ton_kho, dv.co_the_tang, gdv.gia_moi, ldv.ten_loai_dich_vu" +
                       " FROM DichVu dv" +
                       " JOIN LoaiDichVu ldv ON dv.ma_loai_dich_vu = ldv.ma_loai_dich_vu" +
                       " LEFT JOIN GiaDichVu gdv ON dv.ma_dich_vu = gdv.ma_dich_vu" +
                       " WHERE dv.da_xoa = 0";

        List<ThongTinDichVu> danhSachThongTinDichVu = new ArrayList<>();
        try {
            PreparedStatement ps = connection.prepareStatement(query);

            var rs = ps.executeQuery();
            while (rs.next())
                danhSachThongTinDichVu.add(chuyenKetQuaThanhThongTinDichVu(rs));

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (TableEntityMismatch mismatchException) {
            System.out.println(mismatchException.getMessage());
        }

        return danhSachThongTinDichVu;
    }

    public boolean goiDichVu(List<PhongDungDichVu> phongDungDichVus) {
        return false;
    }

    private ThongTinDichVu chuyenKetQuaThanhThongTinDichVu(ResultSet rs) throws SQLException, TableEntityMismatch {
        try {
            String maDichVu = rs.getString("ma_dich_vu");
            String tenDichVu = rs.getString("ten_dich_vu");
            String tenLoaiDichVu = rs.getString("ten_loai_dich_vu");
            double giaMoi = rs.getDouble("gia_moi");
            int tonKho = rs.getInt("ton_kho");
            boolean coTheTang = rs.getBoolean("co_the_tang");

            return new ThongTinDichVu(maDichVu, tenDichVu, tonKho, coTheTang, giaMoi, tenLoaiDichVu);
        } catch (SQLException e) {
            throw new TableEntityMismatch("Lỗi khi chuyển kết quả sang ThongTinDichVu: " + e.getMessage());
        }
    }
}
