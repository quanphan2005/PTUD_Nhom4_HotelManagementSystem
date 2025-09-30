package vn.iuh.servcie;

import vn.iuh.entity.AdditionalFee;

public interface AdditionalFeeService {
    AdditionalFee getAdditionalFeeByID(String id);
    AdditionalFee createAdditionalFee(AdditionalFee additionalFee);
    AdditionalFee updateAdditionalFee(AdditionalFee additionalFee);
    boolean deleteAdditionalFeeByID(String id);
}
