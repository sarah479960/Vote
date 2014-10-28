package fi.oulu.tol.vote50.server;

public interface VoteServerObserver {
	
	public void votingsReceived();
	public void noNewVotingsAvailable();  
    public void votePosted();   
    public void errorNotification(final String error);   
}
