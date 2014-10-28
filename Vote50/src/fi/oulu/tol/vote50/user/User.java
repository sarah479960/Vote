package fi.oulu.tol.vote50.user;

public class User {
	
	private String name;// string uniquely identifying the user
	private String password; // string that is only known by the user
	
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	
	

}
