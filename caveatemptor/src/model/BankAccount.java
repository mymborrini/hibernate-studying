package model;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("BA")
public class BankAccount extends BillingDetails {

  @Column(name = "ACCOUNT")
  private String account;

  @Column(name = "BANK_NAME")
  private String bankName;

  @Column(name = "SWIFT")
  private String swift;

  public String getAccount() {
    return account;
  }

  public String getBankName() {
    return bankName;
  }

  public String getSwift() {
    return swift;
  }

  public void setAccount(String account) {
    this.account = account;
  }

  public void setBankName(String bankName) {
    this.bankName = bankName;
  }

  public void setSwift(String swift) {
    this.swift = swift;
  }
}
