package vn.iuh.servcie;

import vn.iuh.entity.ServiceCategory;

public interface ServiceCategoryService {
    ServiceCategory getServiceCategoryByID(String id);
    ServiceCategory createServiceCategory(ServiceCategory serviceCategory);
    ServiceCategory updateServiceCategory(ServiceCategory serviceCategory);
    boolean deleteServiceCategoryByID(String id);
}
