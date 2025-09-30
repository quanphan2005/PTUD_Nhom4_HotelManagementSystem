package vn.iuh.servcie;

import vn.iuh.entity.ServiceItem;

public interface ServiceItemService {
    ServiceItem getServiceItemByID(String id);
    ServiceItem createServiceItem(ServiceItem serviceItem);
    ServiceItem updateServiceItem(ServiceItem serviceItem);
    boolean deleteServiceItemByID(String id);
}
