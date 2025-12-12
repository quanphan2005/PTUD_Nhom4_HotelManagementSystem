package vn.iuh.service;

import vn.iuh.dto.response.ServicePriceHistoryResponse;
import vn.iuh.dto.response.ServiceResponse;
import vn.iuh.entity.LoaiDichVu;

import java.util.List;
import java.util.Map;

public interface ServiceService {
    List<ServiceResponse> layTatCaDichVuCungGia();
    Map<String, String> layMapMaThanhTenLoaiDichVu();
    List<LoaiDichVu> layTatCaLoaiDichVu();
    List<ServicePriceHistoryResponse> layLichSuGiaDichVu(String maDichVu);
    ServiceResponse themDichVuMoi(String tenDichVu, int tonKho, boolean coTheTang, String maLoaiDichVu, double giaMoi);
    ServiceResponse capNhatDichVu(String maDichVu, String tenDichVu, int tonKho, String maLoaiDichVu, double giaMoi);
    boolean isServiceCurrentlyUsed(String maDichVu);
    boolean xoaDichVu(String maDichVu);

}
