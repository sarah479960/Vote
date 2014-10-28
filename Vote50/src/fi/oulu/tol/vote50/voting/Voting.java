package fi.oulu.tol.vote50.voting;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.UUID;
import java.util.Vector;

import fi.oulu.tol.vote50.dateTransfer.Rfc3339;

public class Voting {

	private UUID mId; 
	private String mTitle; 
	private Date mStartTime = null; 
	private Date mEndTime = null; 
	private String mText; 
	private UUID voteUuid = null; 
	private Vector<Option> mOptions;
	private int mSelectedOption;

	public Voting(UUID mId, String mTitle, Date mStartTime, Date mEndTime,
			String mText, UUID voteUuid, Vector<Option> mOptions,
			int mSelectedOption) {
		super();
		this.mId = mId;
		this.mTitle = mTitle;
		this.mStartTime = mStartTime;
		this.mEndTime = mEndTime;
		this.mText = mText;
		this.voteUuid = voteUuid;
		this.mOptions = mOptions;
		this.mSelectedOption = mSelectedOption;
	}

	public Voting() {
		super();
	}

	public UUID getmId() {
		return mId;
	}

	public void setmId(UUID mId) {
		this.mId = mId;
	}

	public void setmTitle(String mTitle) {
		this.mTitle = mTitle;
	}

	public String getmTitle() {
		return mTitle;
	}

	public Date getmStartTime() {
		return mStartTime;
	}

	public void setStartTime(String start) throws NumberFormatException {
		mStartTime = null;
		if (null != start) {
			mStartTime = Rfc3339.parse(start).getTime();
		}
	}

	public Date getmEndTime() {
		return mEndTime;
	}

	public void setmText(String mText) {
		this.mText = mText;
	}

	public void setEndTime(String end) throws NumberFormatException {
		mEndTime = null;
		if (null != end) {
			mEndTime = Rfc3339.parse(end).getTime();
		}
	}

	public String getmText() {
		return mText;
	}

	public UUID getVoteUuid() {
		return voteUuid;
	}

	public void setVoteUuid(UUID voteUuid) {
		this.voteUuid = voteUuid;
	}

	public boolean isUpcoming() {

		Date currentTime = new Date(System.currentTimeMillis());
		if (currentTime.before(this.mStartTime))
			return true;
		else
			return false;
	}

	public boolean isOpen() {

		Date currentTime = new Date(System.currentTimeMillis());
		if (this.mStartTime != null) {
			if (currentTime.after(this.mStartTime)
					&& currentTime.before(this.mEndTime))
				return true;
			else
				return false;
		} else
			return false;
	}

	public boolean isClosed() {

		Date currentTime = new Date(System.currentTimeMillis());
		if (this.mEndTime != null) {
			if (currentTime.after(this.mEndTime))
				return true;
			else
				return false;
		} else
			return false;
	}

	// define inner nested class Option
	public class Option {

		private String mText; 
		private int mCount; 

		public Option(String mText, int mCount) {
			super();
			this.mText = mText;
			this.mCount = mCount;
		}

		public Option() {
			super();

		}

		public String getmText() {
			return mText;
		}

		public void setmText(String mText) {
			this.mText = mText;
		}

		public int getmCount() {
			return mCount;
		}

		public void setmCount(int mCount) {
			this.mCount = mCount;
		}

	}

	public int getAllNumOfOptions() {

		return this.mOptions.size();

	}

	public Option getSpecifiedOption(int index) {

		return this.mOptions.get(index);
	}

	public Vector<Option> getOptions() {
		return this.mOptions;
	}

	public void setOptions(Vector<Option> options) {

		int i = 0;
		if (mOptions != null)
			this.mOptions.clear();
		else
			mOptions = new Vector<Option>();
		while (i < options.size()) {

			this.mOptions.add(options.get(i));
			i++;
		}
	}

	public int getmSelectedOption() {
		return mSelectedOption;
	}

	public void setmSelectedOption(int mSelectedOption) {
		this.mSelectedOption = mSelectedOption;
	}

	public boolean equals(Voting v) {
		if (v.getmId().equals(this.mId))
			return true;
		else
			return false;
	}

	public void merge(Voting v) {

		if (v.getmTitle() != null)
			this.mTitle = v.getmTitle();
		if (v.getmText() != null)
			this.mText = v.getmText();
		if (v.getmStartTime() != null)
			this.mStartTime = v.getmStartTime();
		if (v.getmEndTime() != null)
			this.mEndTime = v.getmEndTime();
		if (v.getOptions() != null)
			this.setOptions(v.getOptions());

	}
}
