package fi.oulu.tol.vote50;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import fi.oulu.tol.vote50.server.VoteServerObserver;
import fi.oulu.tol.vote50.service.VoteService;
import fi.oulu.tol.vote50.service.VoteService.VoteBinder;
import fi.oulu.tol.vote50.service.VoteServiceInterface;
import fi.oulu.tol.vote50.voting.Voting;
import fi.oulu.tol.vote50.voting.VotingOptionsView;

public class VoteActivity extends Activity implements
		ActionBar.OnNavigationListener, VoteServerObserver,
		DeviceOrientationHandler {

	private TextView mVotingNumber;
	private TextView mVotingTopic;
	private TextView mVotingText;
	private TextView mVotingTime;
	private Button mSubmitVote;

	private VotingOptionsView mVotingOptions;
	private GestureDetector mGestureDetector;
	int mCurrentVotingIndex; 
	int mCurrentOptionIndex;
	private VoteService mVoteService;

	private SensorManager mSensorManager;
	private Sensor mSensor;
	private boolean isUsingSensors = true;
	private DeviceOrientationRecognizer mRecognizer = null;

	private static final String TAG = "VoteActivity";	
	private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";

	private ServiceConnection mServiceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder binder) {

			VoteBinder voteBinder = (VoteBinder) binder;
			mVoteService = voteBinder.getService();
			mVoteService.setObserver(VoteActivity.this);
			mVoteService.forceRefresh(); 
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mVoteService = null;
		}
	};

	
	private OnClickListener submitButtonListener = new OnClickListener() {
		public void onClick(View v) {
			
			mVoteService.SubmitVote();
		}
	};

	/************* start to implement ******************/

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_vote);

		mVotingNumber = (TextView) this.findViewById(R.id.voting_number);
		mVotingTopic = (TextView) this.findViewById(R.id.voting_topic);
		mVotingText = (TextView) this.findViewById(R.id.voting_text);
		mVotingTime = (TextView) this.findViewById(R.id.voting_time);
		mSubmitVote = (Button) this.findViewById(R.id.submit_vote);
		mSubmitVote.setOnClickListener(submitButtonListener);
		mVotingOptions = (VotingOptionsView) this
				.findViewById(R.id.voting_option_view);

		// connect Activity to MyListener/
		mGestureDetector = new GestureDetector(this, new MyListener());
		Intent intent = new Intent(this, VoteService.class);
		startService(intent);
        final ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

		final String[] dropdownValues = getResources().getStringArray(
				R.array.voting_types);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
				actionBar.getThemedContext(),
				android.R.layout.simple_spinner_item, android.R.id.text1,
				dropdownValues);

		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		actionBar.setListNavigationCallbacks(adapter, this);
        mRecognizer = new DeviceOrientationRecognizer(this);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		
		getMenuInflater().inflate(R.menu.vote, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.action_settings:
			Intent new_intent = new Intent(this, SettingsActivity.class);
			startActivity(new_intent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}

	}

	
	@Override
	public void onResume() {
		super.onResume();
		this.updateView();
		this.activateSensor();
	}

	@Override
	protected void onPause() {
		super.onPause();
		this.deactivateSensors();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		super.onTouchEvent(event);
		this.mGestureDetector.onTouchEvent(event);
		return true;
	}

	
	private void updateView() {

		if (mVoteService != null) {
			Voting currentVoting = mVoteService.getCurrentVoting();
			if (currentVoting != null) {
				mVotingTopic.setText(currentVoting.getmTitle());
				mVotingNumber.setText(String.valueOf(mVoteService
						.getCurrentVotingIndex() + 1)
						+ "/"
						+ mVoteService.getVotingCount());
				mVotingText.setText(currentVoting.getmText());
				mVotingOptions.setVoting(currentVoting);

				if (currentVoting.isOpen() == true) {

					Log.d(TAG, "yes is open" + currentVoting.getmStartTime());
					mVotingTime.setText("Start: "
							+ currentVoting.getmStartTime().toString() + "\n"
							+ "End: " + currentVoting.getmEndTime().toString());
					mSubmitVote.setEnabled(true);
				}
				if (currentVoting.isClosed()) {
					Log.d(TAG, "yes is close" + currentVoting.getmEndTime());
					mVotingTime.setText("Closed by "
							+ currentVoting.getmEndTime());
					mSubmitVote.setEnabled(false);
					Toast.makeText(VoteActivity.this, "It's already end!",
							Toast.LENGTH_SHORT).show();
				}
				if (currentVoting.isUpcoming()) {
					mVotingTime.setText("Start from "
							+ currentVoting.getmStartTime());
					Toast.makeText(VoteActivity.this, "It's not yet start!",
							Toast.LENGTH_SHORT).show();
					mSubmitVote.setEnabled(false);
				}
			} else {
				mVotingTopic.setText(null);
				mVotingNumber.setText(null);
				mVotingOptions.setVoting(null);
			}
		}
	}

	class MyListener extends GestureDetector.SimpleOnGestureListener {

		private static final int distance = 50;
		private static final int velocity = 50;

		@Override
		public boolean onDown(MotionEvent e) {
			return true;
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {

			if (e1.getX() - e2.getX() > distance
					&& Math.abs(velocityX) > velocity) {
				left();
			} 
			else if (e2.getX() - e1.getX() > distance
					&& Math.abs(velocityX) > velocity) {
				right();
			} 
			else if (e1.getY() - e2.getY() > distance
					&& Math.abs(velocityY) > velocity) {
				up();
			} 
			else if (e2.getY() - e1.getY() > distance
					&& Math.abs(velocityY) > velocity) {
				down();
			}
			updateView();
			return false;
		}
	}

	public void right() {

		if (mVoteService != null) {
			mVoteService.previousVoting();
			updateView();
		}
	}

	public void left() {

		if (mVoteService != null) {
			mVoteService.nextVoting();
			updateView();
		}
	}

	public void up() {

		if (mVoteService != null) {
			mVoteService.nextOption();
			updateView();
		}

	}

	public void down() {

		if (mVoteService != null) {
			mVoteService.previousOption();
			updateView();
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		Intent intent = new Intent(this, VoteService.class);
		bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (mVoteService != null) {
			mVoteService = null;
			unbindService(mServiceConnection);

		}
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		
		if (savedInstanceState.containsKey(STATE_SELECTED_NAVIGATION_ITEM)) {
			getActionBar().setSelectedNavigationItem(
					savedInstanceState.getInt(STATE_SELECTED_NAVIGATION_ITEM));
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		
		outState.putInt(STATE_SELECTED_NAVIGATION_ITEM, getActionBar()
				.getSelectedNavigationIndex());
	}

	@Override
	public boolean onNavigationItemSelected(int arg0, long arg1) {
		
		if (mVoteService != null) {
			if (arg0 == 0) {
				mVoteService
						.applySimpleFilter(VoteServiceInterface.VotingFilterItems.SHOW_ALL);
			}
			if (arg0 == 1) {
				mVoteService
						.applySimpleFilter(VoteServiceInterface.VotingFilterItems.SHOW_CLOSED);
			}
			if (arg0 == 2) {
				mVoteService
						.applySimpleFilter(VoteServiceInterface.VotingFilterItems.SHOW_OPEN);
			}
			if (arg0 == 3) {
				mVoteService
						.applySimpleFilter(VoteServiceInterface.VotingFilterItems.SHOW_FUTURE);
			}
		}

		updateView();

		return false;
	}

	@Override
	public void votingsReceived() {

		updateView();
		Toast.makeText(this, R.string.new_votings_arrived, Toast.LENGTH_LONG)
				.show();
	}

	@Override
	public void noNewVotingsAvailable() {
		Toast.makeText(this, R.string.no_new_voting, Toast.LENGTH_LONG).show();

	}

	@Override
	public void votePosted() {
		Toast.makeText(this, R.string.vote_posted, Toast.LENGTH_LONG).show();

	}

	@Override
	public void errorNotification(String error) {
		Toast.makeText(this, error, Toast.LENGTH_LONG).show();

	}

	@Override
	public void tiltedLeft() {
		Log.d(TAG, "titledleft");
		this.left();

	}

	@Override
	public void tiltedRight() {

		Log.d(TAG, "titledRight");
		this.right();
	}

	@Override
	public void tiltedAway() {

		Log.d(TAG, "titledAway");
		this.up();

	}

	@Override
	public void tiltedTowards() {

		Log.d(TAG, "titledTowards");
		this.down();
	}

	public void activateSensor() {
		if (isUsingSensors == true) {
			this.mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
			mSensor = mSensorManager
					.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
			mRecognizer = new DeviceOrientationRecognizer(this);
			mSensorManager.registerListener(mRecognizer, mSensor,
					SensorManager.SENSOR_DELAY_UI);

		}
	}

	public void deactivateSensors() {
		mSensorManager.unregisterListener(mRecognizer);
		mRecognizer = null;
		mSensor = null;
		mSensorManager = null;
	}
}
