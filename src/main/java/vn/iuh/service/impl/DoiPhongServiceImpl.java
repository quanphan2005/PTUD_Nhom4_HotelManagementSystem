package vn.iuh.service.impl;

import com.mchange.v2.encounter.StrongEqualityEncounterCounter;
import vn.iuh.dao.*;
import vn.iuh.service.DoiPhongService;

import java.sql.Connection;

public class DoiPhongServiceImpl {

    private final LichSuRaNgoaiDAO lichSuRaNgoaiDAO;
    private final LichSuDiVaoDAO lichSuDiVaoDAO;
    private final ChiTietDatPhongDAO chiTietDatPhongDAO;
    private final CongViecDAO congViecDAO;
    private final DatPhongDAO  datPhongDAO;
    private final LichSuThaoTacDAO lichSuThaoTacDAO;
//    private final PhienDangNhapDAO phienDangNhapDAO;

    public DoiPhongServiceImpl() {
        this.lichSuDiVaoDAO  = new LichSuDiVaoDAO();
        this.lichSuRaNgoaiDAO = new LichSuRaNgoaiDAO();
        this.chiTietDatPhongDAO = new ChiTietDatPhongDAO();
        this.congViecDAO = new CongViecDAO();
        this.datPhongDAO = new DatPhongDAO();
        this.lichSuThaoTacDAO = new LichSuThaoTacDAO();
    }

//    public boolean doiPhong(String maDonDatPhong, String maPhongMuonDoi) {
//        DatPhongDAO  datPhongDAO = new DatPhongDAO();
//        try {
//            datPhongDAO.khoiTaoGiaoTac();
//            Connection conn = datPhongDAO.getConnection();
//
//            LichSuDiVaoDAO lichSuDiVaoDAO = new LichSuDiVaoDAO(conn);
//            LichSuRaNgoaiDAO lichSuRaNgoaiDAO = new LichSuRaNgoaiDAO(conn);
//            CongViecDAO congViecDAO = new CongViecDAO(conn);
//            LichSuThaoTacDAO lichSuThaoTacDAO = new LichSuThaoTacDAO(conn);
//            ChiTietDatPhongDAO chiTietDatPhongDAO = new ChiTietDatPhongDAO(conn);
//
//
//
//        } catch() {
//
//        }
//    }

//    public boolean themPhuPhi(String maDonDatPhong) {
//
//    }
}
