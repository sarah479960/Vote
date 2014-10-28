package fi.oulu.tol.vote50.service;

import fi.oulu.tol.vote50.server.VoteServerObserver;
import fi.oulu.tol.vote50.voting.Voting;

/**
 * The interface to the Voting Service in the Android app architecture.
 * The interface should be implemented by an Android Service, according
 * to the interface documentation.
 * 
 * @author juustila
 *
 */
public interface VoteServiceInterface {
	
	/**
	 * Filter describing the way Votings can be filtered by the user.
	 * You can implement the filter so that it can filter a combination of
	 * closed/open/future votings, e.g. using a checkbox type of UI. Alternatively,
	 * you can implement simple filtering so that user can specify only one of these
	 * filters to be active at any time, using e.g. a selection list or radio button.
	 * 
	 * @author juustila
	 *
	 */
	public enum VotingFilterItems { 
		SHOW_ALL, /** Show all Votings */
		SHOW_CLOSED, /** Show closed Votings. */ 
		SHOW_OPEN, /** Show open Votings. */
		SHOW_FUTURE /** Show future (not yet open) Votings. */
		};

	/** Set the observer for the Service. Activities must set themselves to 
	 * be the observer if they wish to receive callbacks on asynchronous event completions
	 * as the Service and the protocol handle requests with the AANI server. If the
	 * Service does not have an observer, it should use the Android Notification Service to
	 * show notifications to user about service request completions.
	 * @param obs The observer of the Service.
	 */
	public void setObserver(VoteServerObserver obs);
	
	/** Reads the settings related to the server from the default settings registry. Currently
	 * this includes only the server's URL. If the server URL changes, client should clear
	 * all Votings in client end and refresh new Votings from the new server.
	 */
	public void updateServerSettings();
	
	/** 
	 * Gets the currently selected (visible to the user) voting. May be null.
	 * @returns The current Voting or null.
	 */
	public Voting getCurrentVoting();
	
	/**
	 * Sets the current voting index. Implementation should check that the index
	 * is valid (between 0 and voting count-1).
	 * @param index The index to the current Voting.
	 */
	public void setCurrentVoting(int index);
	
	/** Asks the Service to get the Voting from the server. The Voting object should have
	 * the server's URL as well as the UUID for the Voting. Other member variables may be null.
	 * The request is asynchronous, so the method returns true if the request was initiated
	 * successfully using the AANI protocol implementation. The completion will happen later,
	 * and the observer (if any) is notified of the arriving Voting data. Service should
	 * store the arrived voting in a container (or update an already existing Voting in the 
	 * container).
	 * 
	 * @param voting The voting describing what to get (AANI URL and the UUID).
	 * @return Returns true, if the request was initiated successfully.
	 */
	public boolean getVotingFromServer(Voting voting);
	
	/**
	 * Returns the current Voting's index in the container. If there are no
	 * Votings, the method returns 0.
	 * @return The current Voting's index.
	 */
	public int getCurrentVotingIndex();
	
	/**
	 * Returns the count of Votings in the Votings container.
	 * @return The count of Votings.
	 */
	public int getVotingCount();
	
	/**
	 * Selects the next Voting to be the current Voting. Recommended functionality is
	 * that after selecting the last Voting, selects the first Voting as the current Voting.
	 * Implementations may differ, however, based on UX design.
	 */
	public void nextVoting();

	/**
	 * Selects the previous Voting to be the current Voting. Recommended functionality is
	 * that after selecting the first Voting, selects the last Voting as the current Voting.
	 * Implementations may differ, however, based on UX design.
	 */
	public void previousVoting();
	
	/**
	 * Selects the previous option to be the current Voting.*/
	public void previousOption();
	/**
	 * Selects the next option to be the current Voting.*/
	public void nextOption();
	
	/**
	 * Returns the index of the specified Voting in the votings container. If the Voting
	 * is not found, returns -1 and submits an asynchronous request for the AANI protocol
	 * to retrieve the indicated voting from the server (optional feature).
	 * @param voting The voting to find.
	 * @return Returns the index, -1 if not found.
	 */
	public int getVotingIndex(Voting voting);
	
	/**
	 * Submits the Vote of the current Voting to the AANI server. There must be a current Voting.
	 * User is able to submit a Vote multiple times so that only the last Vote is considered. Therefore,
	 * the implementation must make sure that the UUID of the Vote does not change when submitting the Vote.
	 */
	public void SubmitVote();
	
	/**
	 * Sends a request to the AANI server to retrieve all Votings.
	 * @return Returns true if the request could be submitted to the server.
	 */
	public boolean forceRefresh();
	
	/**
	 * Reads from the app settings the last applied voting filters. The filter can be
	 * set from a setting Activity or any other means (e.g. an ActionBar) in the UI.
	 * Filter can be complex, e.g. "show closed and future votings" or simple ("show
	 * only open votings"). You may decide to leave this unsupported and only implement
	 * simple filtering using applySimpleFilter(VotingFilterItems showWhat) API method.
	 */
	public void applyVotingsFilterFromSettings();
	
	/**
	 * Applies a simple filter, to show only one specific kind of Votings, e.g. closed 
	 * or open Votings.
	 * @param showWhat Which Votings to show.
	 */
	public void applySimpleFilter(VotingFilterItems showWhat);
}
