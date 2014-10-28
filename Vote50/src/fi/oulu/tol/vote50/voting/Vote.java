package fi.oulu.tol.vote50.voting;

import java.util.UUID;

public class Vote {
	
	private UUID id;
	private int option;
	
	
	public UUID getId() {
		return id;
	}
	public void setId(UUID id) {
		this.id = id;
	}
	public int getOption() {
		return option;
	}
	public void setOption(int option) {
		this.option = option;
	}
	
	

}
