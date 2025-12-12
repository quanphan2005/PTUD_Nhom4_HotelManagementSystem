package vn.iuh.util;

import vn.iuh.constraint.EntityIDSymbol;
import vn.iuh.constraint.Fee;
import vn.iuh.dao.GiaPhuPhiDAO;
import vn.iuh.dao.PhuPhiDAO;
import vn.iuh.dto.repository.ThongTinPhuPhi;
import vn.iuh.entity.GiaPhuPhi;
import vn.iuh.gui.base.Main;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FeeValue {
    private static FeeValue instance;
    private final PhuPhiDAO phuPhiDAO = new PhuPhiDAO();
    private final GiaPhuPhiDAO giaPhuPhiDAO = new GiaPhuPhiDAO();
    private final Map<Fee, ThongTinPhuPhi> allFee = new HashMap<>();

    private FeeValue() {
        loadAllFee();
    }

    public static FeeValue getInstance() {
        if (instance == null) {
            instance = new FeeValue();
        }
        return instance;
    }

    private void loadAllFee() {
        List<ThongTinPhuPhi> danhSachPhuPhi = phuPhiDAO.getDanhSachPhuPhi();
        for (ThongTinPhuPhi thongTin : danhSachPhuPhi) {
            for (Fee fee : Fee.values()) {
                if (fee.getStatus().equalsIgnoreCase(thongTin.getTenPhuPhi().trim())) {
                    allFee.put(fee, thongTin);
                    break;
                }
            }
        }
    }
    // Khi muốn lâys phụ phí chỉ cần chạy lệnh dưới, truyền vào enum Fee trong constraint
    //ThongTinPhuPhi checkOutTre = FeeValue.getInstance().get(Fee.CHECK_OUT_TRE);
    public ThongTinPhuPhi get(Fee fee) {
        return allFee.get(fee);
    }

    public void updateFee(Fee fee, double newGia) {
        ThongTinPhuPhi thongTin = allFee.get(fee);
        if (thongTin != null) {
            GiaPhuPhi newgiaPP = giaPhuPhiDAO.themGiaPhuPhi(createGiaPhuPhi(thongTin, newGia));

            if(newgiaPP != null){
                thongTin.setGiaHienTai(BigDecimal.valueOf(newgiaPP.getGiaHienTai()));
                allFee.put(fee, thongTin);
            }
        }
    }

    private GiaPhuPhi createGiaPhuPhi(ThongTinPhuPhi old, double newGia) {
        var latest = giaPhuPhiDAO.timPhuPhiMoiNhat();
        String maGPP = (latest == null) ? null : latest.getMaGiaPhuPhi();
        String maGPPMoi = EntityUtil.increaseEntityID(
                maGPP,
                EntityIDSymbol.FEE_LIST_PRICE.getPrefix(),
                EntityIDSymbol.FEE_LIST_PRICE.getLength()
        );

        double oldGia = old.getGiaHienTai() != null ? old.getGiaHienTai().doubleValue() : 0.0;

        return new GiaPhuPhi(
                maGPPMoi,
                oldGia,
                newGia,
                old.isLaPhanTram(),
                Main.getCurrentLoginSession(),
                old.getMaPhuPhi(),
                null
        );
    }
}
