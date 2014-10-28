package fi.oulu.tol.vote50.voting;

import java.util.UUID;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.util.Log;

import fi.oulu.tol.vote50.voting.Voting;
import fi.oulu.tol.vote50.voting.Voting.Option;

public class VotingParser {
	private static final String TAG = "VoteParser";
	private static final String TAG_OPTION = "VoteParserOption";

	private VotingParser() {
	}

	public static int parseString(String content, Vector<Voting> results)
			throws JSONException {

		int count = 0;		
		String contentTrim = content.trim();
		if (contentTrim.startsWith("[")) {
			
			count = parseArrayString(content, results);
		} else if (contentTrim.startsWith("{")) {
			Voting voting = parseObjectString(content);
			if (voting != null) {
				results.add(voting);
				count = 1;
			} else {
				count = 0;
			}
		}
		return count;
	}

	private static int parseArrayString(String content, Vector<Voting> results)
			throws JSONException, NumberFormatException,
			IllegalArgumentException {

		JSONArray array = (JSONArray) new JSONTokener(content).nextValue();
		Voting voting = new Voting();
		for (int arrayIndex = 0; arrayIndex < array.length(); arrayIndex++) {
			JSONObject object = array.getJSONObject(arrayIndex);
			voting = parseSingleVoting(object);
			if (voting != null)
				results.add(voting);
		}
		return results.size();
	}

	private static Voting parseObjectString(String content)
			throws JSONException, NumberFormatException,
			IllegalArgumentException {

		JSONObject object = (JSONObject) new JSONTokener(content).nextValue();
		Voting voting = parseSingleVoting(object);
		return voting;
	}

	private static Voting parseSingleVoting(JSONObject object)
			throws JSONException, NumberFormatException,
			IllegalArgumentException {

		Voting voting = new Voting();
		String uuid = object.optString("id");
		if (uuid.length() == 0)
			uuid = null;
		voting.setmId(UUID.fromString(uuid));
		String title = object.optString("title");
		if (title.length() == 0)
			title = null;
		voting.setmTitle(title);
		String startTime = object.optString("start-time");
		if (startTime.length() == 0)
			startTime = null;
		voting.setStartTime(startTime);
		String endTime = object.optString("end-time");
		if (endTime.length() == 0)
			endTime = null;
		voting.setEndTime(endTime);
		String text = object.optString("text");
		if (text.length() == 0)
			text = null;
		voting.setmText(text);
		JSONArray optionsArr = null;
		optionsArr = object.optJSONArray("options");
		Vector<Option> options = new Vector<Option>();
		if (optionsArr != null) {
			for (int i = 0; i < optionsArr.length(); i++) {
				Option option = voting.new Option();
				option.setmText(optionsArr.get(i).toString());
				options.add(option);
			}
			voting.setOptions(options);
		}

		return voting;
	}
}
