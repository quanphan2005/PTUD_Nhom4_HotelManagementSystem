    package vn.iuh.dao;

    import vn.iuh.entity.PhuPhi;
    import vn.iuh.exception.TableEntityMismatch;
    import vn.iuh.util.DatabaseUtil;

    import java.sql.Connection;
    import java.sql.PreparedStatement;
    import java.sql.ResultSet;
    import java.sql.SQLException;

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

        private PhuPhi chuyenKetQuaThanhPhuPhi(ResultSet rs) {
            PhuPhi phuPhi = new PhuPhi();
            try {
                phuPhi.setMaPhuPhi(rs.getString("ma_phu_phi"));
                phuPhi.setTenPhuPhi(rs.getString("ten_phu_phi"));
                return phuPhi;
            } catch (SQLException e) {
                throw new TableEntityMismatch("Không thể chuyển kết quả thành phụ phí: " + e.getMessage());
            }
        }
    }