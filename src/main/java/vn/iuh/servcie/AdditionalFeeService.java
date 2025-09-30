package vn.iuh.servcie;

import vn.iuh.entity.PhuPhi;

public interface AdditionalFeeService {
    PhuPhi getAdditionalFeeByID(String id);
    PhuPhi createAdditionalFee(PhuPhi phuPhi);
    PhuPhi updateAdditionalFee(PhuPhi phuPhi);
    boolean deleteAdditionalFeeByID(String id);
}
