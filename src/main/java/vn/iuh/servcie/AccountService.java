package vn.iuh.servcie;

import vn.iuh.entity.Account;

public interface AccountService {
    Account getAccountByID(String id);
    Account createAccount(Account account);
    Account updateAccount(Account account);
    boolean deleteAccountByID(String id);
}
