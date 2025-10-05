package vn.iuh.service.impl;

import vn.iuh.constraint.EntityIDSymbol;
import vn.iuh.dao.LichSuRaNgoaiDAO;
import vn.iuh.entity.ChiTietDatPhong;
import vn.iuh.entity.LichSuDiVao;
import vn.iuh.entity.LichSuRaNgoai;
import vn.iuh.util.EntityUtil;

import java.util.ArrayList;
import java.util.List;

public class LichSuRaNgoaiServiceimpl {
    private final LichSuRaNgoaiDAO lichSuRaNgoaiDAO;

    public LichSuRaNgoaiServiceimpl() {
        this.lichSuRaNgoaiDAO = new LichSuRaNgoaiDAO();
    }

    public void themLichSuRaNgoai(List<ChiTietDatPhong> chiTietDatPhongs) {
        List<LichSuRaNgoai> historyCheckOuts = new ArrayList<>();
        LichSuRaNgoai lichSuRaNgoaiMoiNhat = lichSuRaNgoaiDAO.timLichSuRaNgoaiMoiNhat();
        String maLichSuRaNgoaiMoiNhat = lichSuRaNgoaiMoiNhat == null ? null : lichSuRaNgoaiMoiNhat.getMaLichSuRaNgoai();

        for (ChiTietDatPhong ct : chiTietDatPhongs) {
            LichSuRaNgoai lichSuRaNgoai = createHistoryCheckOutEntity(maLichSuRaNgoaiMoiNhat, ct.getMaChiTietDatPhong(), true);
            maLichSuRaNgoaiMoiNhat = lichSuRaNgoai.getMaLichSuRaNgoai();
            historyCheckOuts.add(lichSuRaNgoai);
        }

        lichSuRaNgoaiDAO.themDanhSachLichSuRaNgoai(historyCheckOuts);
    }

    private LichSuRaNgoai createHistoryCheckOutEntity(String maLichSuRaNgoaiMoiNhat, String maChiTietDatPhong, boolean isFinal) {
        String id;
        String prefix = EntityIDSymbol.HISTORY_CHECKOUT_PREFIX.getPrefix();

        int numberLength = EntityIDSymbol.HISTORY_CHECKOUT_PREFIX.getLength();
        if (maLichSuRaNgoaiMoiNhat == null) {
            id = EntityUtil.increaseEntityID(null, prefix, numberLength);
        } else {
            id = EntityUtil.increaseEntityID(maLichSuRaNgoaiMoiNhat, prefix, numberLength);
        }

        return new LichSuRaNgoai(
                id,
                isFinal,
                maChiTietDatPhong,
                null
        );
    }

}
