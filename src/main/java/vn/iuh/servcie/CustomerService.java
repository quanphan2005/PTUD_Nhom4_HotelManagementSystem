package vn.iuh.servcie;

import vn.iuh.entity.Customer;

public interface CustomerService {
    Customer getCustomerByID(String id);
    Customer createCustomer(Customer customer);
    Customer updateCustomer(Customer customer);
    boolean deleteCustomerByID(String id);
}
