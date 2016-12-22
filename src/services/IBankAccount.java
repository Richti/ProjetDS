package services;

public interface IBankAccount {
	
	@ChangingObjectState
	public void deposit(int money);
	
	@ChangingObjectState
	public void withdraw(int money);
	
	public void displayBalance();
}
