package fi.oulu.tol.vote50.voting;

import java.util.Vector;

public class VotingContainer {

	private Vector<Voting> votings = null;
	private boolean includeClosed = false;
    private boolean includeOpen = true;
	private boolean includeFuture = false;

	public VotingContainer() {
		votings = new Vector<Voting>();
	}
	
	public void add(Voting v) {
		boolean found = false;
		for (Voting v1 : votings) {
			if (v1.equals(v)) {
				found = true;
				v1.merge(v);
				break;
			}
		}
		if (found == false)
			votings.add(v);
	}

	public void remove(Voting v) {
		votings.remove(v);

	}

	public void clear() {
		votings.clear();
	}

	public int size() {
		int count = 0;
		if (votings != null) {
			for (Voting v : votings) {
				if (isIncludedByFilter(v))
					count++;
			}
		}
		return count;
	}

	private boolean isIncludedByFilter(Voting v) {
		if (includeClosed) {
			if (v.isClosed())
				return true;
		}
		if (includeFuture) {
			if (v.isUpcoming())
				return true;
		}

		if (includeOpen) {
			if (v.isOpen())
				return true;
		}

		return false;

	}

	public void applyFilter(boolean inclClosed, boolean inclOpen,boolean inclFuture) {
		includeClosed = inclClosed;
		includeOpen = inclOpen;
		includeFuture = inclFuture;
	}

	public Voting get(int index) {
		int includedInd = 0;
		for (Voting v : votings) {
			if (isIncludedByFilter(v)) {
				if (index == includedInd)
					return v;
				includedInd++;
			}
		}
		return null;
	}

	public int getIndex(Voting voting) {
		int index = -1;
		for (Voting v : votings) {
			if (v.equals(voting))
				index = votings.indexOf(v);
		}

		return index;
	}

	public Vector<Voting> getVotings() {
		return votings;
	}

	public void setVotings(Vector<Voting> votings) {
		this.votings = votings;
	}

}
