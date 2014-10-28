package fi.oulu.tol.vote50.service;

import java.util.UUID;

import fi.oulu.tol.vote50.R;
import fi.oulu.tol.vote50.VoteActivity;

import fi.oulu.tol.vote50.server.VoteServer;
import fi.oulu.tol.vote50.server.VoteServerObserver;
import fi.oulu.tol.vote50.server.VoteServerProxy;
import fi.oulu.tol.vote50.voting.VoteIdRepository;
import fi.oulu.tol.vote50.voting.Voting;
import fi.oulu.tol.vote50.voting.VotingContainer;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;

import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

import android.util.Log;

public class VoteService extends Service implements VoteServiceInterface,
		VoteServerObserver {

	private static final String TAG = "VoteService";

	private final VoteBinder binder = new VoteBinder();
	private VotingContainer mVotingContainer;
	private int mCurrentVotingIndex;
	private VoteServerObserver mObserver;
	private VoteServer proxy = null;
	private VoteIdRepository repository = null;

	private enum Notification {
		VOTINGS_RECEIVED, NO_NEW_VOTINGS_RECEIVED, VOTE_POSTED, ERROR
	};

	/************* start to implement ******************/
	@Override
	public void onCreate() {
		super.onCreate();
		proxy = new VoteServerProxy();
		proxy.initialize(getBaseContext(), this);
		repository = new VoteIdRepository(getBaseContext());
		mVotingContainer = new VotingContainer();

	}

	@Override
	public IBinder onBind(Intent arg0) {

		return binder;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return START_STICKY;
	}

	public class VoteBinder extends Binder {
		public VoteService getService() {
			return VoteService.this;
		}
	}

	@Override
	public void setObserver(VoteServerObserver obs) {
		this.mObserver = obs;
	}

	@Override
	public void updateServerSettings() {

		SharedPreferences pre = PreferenceManager
				.getDefaultSharedPreferences(this);
		String defaut_address = "http://aani.opimobi.com:2014";
		String serverAddress = pre.getString("server_address", defaut_address);
		if (serverAddress != null
				&& serverAddress.equals(proxy.getServerAddress()) == false) {

			proxy.setServerAddress(serverAddress);
			proxy.forceRefresh();
		}
		this.proxy.setServerAddress(serverAddress);

	}

	@Override
	public Voting getCurrentVoting() {

		return this.mVotingContainer.get(this.mCurrentVotingIndex);
	}

	@Override
	public void setCurrentVoting(int index) {

		if (index >= 0 && index < this.getVotingCount())
			this.mCurrentVotingIndex = index;

	}

	@Override
	public boolean getVotingFromServer(Voting voting) {
		return this.proxy.retrieveOneVoting(voting);
	}

	@Override
	public int getCurrentVotingIndex() {
		if (this.mVotingContainer.size() == 0)
			return 0;
		else
			return this.mCurrentVotingIndex;
	}

	@Override
	public int getVotingCount() {
		return this.mVotingContainer.size();
	}

	@Override
	public void nextVoting() {
		if (this.mCurrentVotingIndex == this.getVotingCount() - 1)
			this.mCurrentVotingIndex = 0;
		else
			this.mCurrentVotingIndex++;
	}

	@Override
	public void previousVoting() {
		if (this.mCurrentVotingIndex == 0)
			this.mCurrentVotingIndex = this.getVotingCount() - 1;
		else
			this.mCurrentVotingIndex--;

	}

	@Override
	public void nextOption() {
		if (this.getCurrentVoting().getmSelectedOption() == this
				.getCurrentVoting().getAllNumOfOptions() - 1)
			this.getCurrentVoting().setmSelectedOption(0);
		else
			this.getCurrentVoting().setmSelectedOption(
					this.getCurrentVoting().getmSelectedOption() + 1);

	}

	@Override
	public void previousOption() {
		if (this.getCurrentVoting().getmSelectedOption() == 0)
			this.getCurrentVoting().setmSelectedOption(
					this.getCurrentVoting().getAllNumOfOptions() - 1);
		else
			this.getCurrentVoting().setmSelectedOption(
					this.getCurrentVoting().getmSelectedOption() - 1);

	}

	@Override
	public int getVotingIndex(Voting voting) {

		int index = 0;
		index = this.mVotingContainer.getIndex(voting);
		if (index == -1)
			this.proxy.retrieveOneVoting(voting);
		return index;
	}

	@Override
	public void SubmitVote() {

		Voting voting = this.getCurrentVoting();
		if (voting.isOpen()) {
			this.proxy.submitVote(voting);
			this.repository.saveLocalVoteUuid(voting);
		}
	}

	@Override
	public boolean forceRefresh() {
		this.proxy.forceRefresh();
		return true;
	}

	@Override
	public void applyVotingsFilterFromSettings() {
		// TODO Auto-generated method stub

	}

	@Override
	public void applySimpleFilter(VotingFilterItems showWhat) {
		this.setCurrentVoting(0);
		switch (showWhat) {
		case SHOW_ALL:
			this.mVotingContainer.applyFilter(true, true, true);
			break;
		case SHOW_CLOSED:
			this.mVotingContainer.applyFilter(true, false, false);
			break;

		case SHOW_OPEN:
			this.mVotingContainer.applyFilter(false, true, false);
			break;
		case SHOW_FUTURE:
			this.mVotingContainer.applyFilter(false, false, true);
			break;
		}
	}

	@Override
	public void votingsReceived() {
		boolean getVoting = false;
		Voting voting = new Voting();
		int i = 0;
		do {
			voting = proxy.getNextVoting();  
			if (voting != null) {
				getVoting = true;
				String votingUuid = voting.getmId().toString();
				String voteUuid = null;
				if (votingUuid != null) {
					voteUuid = repository
							.findLocalUuidForVotingUuid(votingUuid);
					if (voteUuid != null)
						voting.setVoteUuid(UUID.fromString(voteUuid));
				}
				this.mVotingContainer.add(voting);
				if (voting.isClosed() == true) {
					proxy.retrieveVotingResults(voting);                     
					
				}
			}
		} while (voting != null);

		if (getVoting) {
			notifyUser(Notification.VOTINGS_RECEIVED, null);

		}
	}

	@Override
	public void noNewVotingsAvailable() {
		notifyUser(Notification.NO_NEW_VOTINGS_RECEIVED, null);

	}

	@Override
	public void votePosted() {
		notifyUser(Notification.VOTE_POSTED, null);

	}

	@Override
	public void errorNotification(String error) {
		notifyUser(Notification.ERROR, null);

	}

	@SuppressLint("NewApi")
	private void notifyUser(Notification n, String info) {
		if (mObserver != null) {
			switch (n) {
			case VOTINGS_RECEIVED:
				mObserver.votingsReceived();
				break;
			case NO_NEW_VOTINGS_RECEIVED:
				mObserver.noNewVotingsAvailable();
				break;
			case VOTE_POSTED:
				mObserver.votePosted();
				break;
			case ERROR:
				mObserver.errorNotification(info);
				break;
			}
		} else {
			String msg = null;
			switch (n) {
			case VOTINGS_RECEIVED:
				msg = getString(R.string.new_votings_arrived);
				break;
			case NO_NEW_VOTINGS_RECEIVED:
				msg = getString(R.string.no_new_voting);
				break;
			case VOTE_POSTED:
				msg = getString(R.string.vote_posted);
				break;
			case ERROR:
				msg = getString(R.string.error_wtf);
				msg += " " + info;
				break;
			}

			NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
					this).setSmallIcon(R.drawable.ic_launcher)
					.setContentTitle("Voting notification").setContentText(msg);

			Intent resultIntent = new Intent(this, VoteActivity.class);
			TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
			stackBuilder.addParentStack(VoteActivity.class);
			stackBuilder.addNextIntent(resultIntent);
			PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(
					0, PendingIntent.FLAG_UPDATE_CURRENT);
			mBuilder.setContentIntent(resultPendingIntent);
			NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			mNotificationManager.notify(0, mBuilder.build());
		}
	}
}
