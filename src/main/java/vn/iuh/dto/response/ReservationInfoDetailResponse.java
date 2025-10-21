package vn.iuh.dto.response;

import java.util.List;

public class ReservationInfoDetailResponse {
    private String CCCD;
    private String customerName;
    private String maDonDatPhong;
    private String status;
    private boolean isAdvance;
    private List<ReservationDetailResponse> details;
    private List<RoomUsageServiceResponse> services;
    private List<MovingHistoryResponse> movingHistories;

    public ReservationInfoDetailResponse() {
    }

    public ReservationInfoDetailResponse(String CCCD, String customerName, String maDonDatPhong, String status,
                                         boolean isAdvance, List<ReservationDetailResponse> details,
                                         List<RoomUsageServiceResponse> services,
                                         List<MovingHistoryResponse> movingHistories) {
        this.CCCD = CCCD;
        this.customerName = customerName;
        this.maDonDatPhong = maDonDatPhong;
        this.status = status;
        this.isAdvance = isAdvance;
        this.details = details;
        this.services = services;
        this.movingHistories = movingHistories;
    }

    public String getCCCD() {
        return CCCD;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getMaDonDatPhong() {
        return maDonDatPhong;
    }

    public String getStatus() {
        return status;
    }

    public boolean isAdvance() {
        return isAdvance;
    }

    public List<ReservationDetailResponse> getDetails() {
        return details;
    }

    public List<RoomUsageServiceResponse> getServices() {
        return services;
    }

    public List<MovingHistoryResponse> getMovingHistories() {
        return movingHistories;
    }
}
