package vn.iuh.servcie;

import vn.iuh.entity.DichVu;

public interface ServiceItemService {
    DichVu getServiceItemByID(String id);
    DichVu createServiceItem(DichVu dichVu);
    DichVu updateServiceItem(DichVu dichVu);
    boolean deleteServiceItemByID(String id);
}
