package services;

public class BankAccount implements IBankAccount {

	private int balance = 0;
	
	@Override
	public void displayBalance() {
		System.out.println("Your account balance is " + balance);
	}

	@Override
	public void deposit(int money) {
		balance += money;
	}

	@Override
	public void withdraw(int money) {
		balance -= money;
	}
	
}
