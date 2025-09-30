    package vn.iuh.dao;

    import vn.iuh.entity.AdditionalFee;
    import vn.iuh.exception.TableEntityMismatch;
    import vn.iuh.util.DatabaseUtil;

    import java.sql.Connection;
    import java.sql.PreparedStatement;
    import java.sql.ResultSet;
    import java.sql.SQLException;

    public class AdditionalFeeDAO {
        private final Connection connection;

        public AdditionalFeeDAO() {
            this.connection = DatabaseUtil.getConnect();
        }

        public AdditionalFeeDAO(Connection connection) {
            this.connection = connection;
        }

        public AdditionalFee getAdditionalFeeByID(String id) {
            String query = "SELECT * FROM AdditionalFee WHERE id = ?";

            try {
                PreparedStatement ps = connection.prepareStatement(query);
                ps.setString(1, id);

                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    return mapResultSetToAdditionalFee(rs);
                }

            } catch (SQLException e) {
                throw new RuntimeException(e);
            } catch (TableEntityMismatch et) {
                System.out.println(et.getMessage());
            }

            return null;
        }

        // Tạo mới AdditionalFee
        public AdditionalFee createAdditionalFee(AdditionalFee additionalFee) {
            String query = "INSERT INTO AdditionalFee (id, fee_name, create_at) VALUES (?, ?, ?)";

            try (PreparedStatement ps = connection.prepareStatement(query)) {
                ps.setString(1, additionalFee.getId());
                ps.setString(2, additionalFee.getFeeName());
                ps.setDate(3, new java.sql.Date(additionalFee.getCreateAt().getTime()));

                ps.executeUpdate();
                return additionalFee;
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }

            return null;
        }

        public AdditionalFee updateAdditionalFee(AdditionalFee additionalFee) {
            String query = "UPDATE AdditionalFee SET fee_name = ? WHERE id = ?";

            try (PreparedStatement ps = connection.prepareStatement(query)) {
                ps.setDate(1, new java.sql.Date(additionalFee.getCreateAt().getTime()));
                ps.setString(2, additionalFee.getId());

                int rowsAffected = ps.executeUpdate();
                if (rowsAffected > 0) {
                    return getAdditionalFeeByID(additionalFee.getId());
                } else {
                    System.out.println("No AdditionalFee found with ID: " + additionalFee.getId());
                    return null;
                }

            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }

            return null;
        }

        public boolean deleteAdditionalFeeByID(String id) {
            if (getAdditionalFeeByID(id) == null) {
                System.out.println("No AdditionalFee found with ID: " + id);
                return false;
            }

            String query = "DELETE FROM AdditionalFee WHERE id = ?";

            try {
                PreparedStatement ps = connection.prepareStatement(query);
                ps.setString(1, id);
                int rowsAffected = ps.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("AdditionalFee has been deleted successfully");
                    return true;
                }

            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }

            return false;
        }

        private AdditionalFee mapResultSetToAdditionalFee(ResultSet rs) {
            AdditionalFee additionalFee = new AdditionalFee();
            try {
                additionalFee.setId(rs.getString("id"));
                additionalFee.setFeeName(rs.getString("fee_name"));
                additionalFee.setCreateAt(rs.getDate("create_at"));
                return additionalFee;
            } catch (SQLException e) {
                throw new TableEntityMismatch("Can't map ResultSet to AdditionalFee: " + e.getMessage());
            }
        }
    }