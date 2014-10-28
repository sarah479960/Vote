package fi.oulu.tol.vote50.server;

import java.util.Vector;

import org.json.JSONException;

import fi.oulu.tol.vote50.voting.Voting;
import android.content.Context;

public interface VoteServer {
	//initializing the protocol client
	public void initialize(Context c, VoteServerObserver obs);
    public void setServerAddress(String serverAddress);
    public String getServerAddress();
    public boolean retrieveVotings();
    public Vector<Voting> getVotings();   
	public void setVotings(Vector<Voting> votings);
    public boolean retrieveOneVoting(Voting voting);
    public boolean retrieveVotingResults(Voting voting);
    public Voting getNextVoting();
    public boolean submitVote(Voting voting) ;
    public boolean forceRefresh();

}
