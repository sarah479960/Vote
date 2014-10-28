package fi.oulu.tol.vote50.voting;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;

public class VoteIdRepository {

	private static final String VOTE_FILE_NAME = "VoteIds";
	private Context context = null;
	private Map<String, String> votingIds = null;

	public VoteIdRepository(Context c) {
		context = c;
		votingIds = new HashMap<String, String>();
		readLocalVoteUuids();
	}

	public String findLocalUuidForVotingUuid(String votingUuid) {
		if (votingIds.containsKey(votingUuid)) {
			return votingIds.get(votingUuid);
		}
		return null;
	}

	public void readLocalVoteUuids() {
		String line;
		BufferedReader buf;
		try {
			File file = new File(context.getFilesDir(), VOTE_FILE_NAME);
			buf = new BufferedReader(new FileReader(file));
			do {
				line = buf.readLine();
				if (null != line) {
					String[] elements = line.split(" ");
					if (elements.length == 2) {
						votingIds.put(elements[0], elements[1]);
					}
				}
			} while (line != null);
			buf.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void saveLocalVoteUuid(Voting voting) {
		if (findLocalUuidForVotingUuid(voting.getmId().toString()) == null) {
			votingIds.put(voting.getmId().toString(), voting.getVoteUuid()
					.toString());
			String line;
			BufferedWriter buf;
			try {
				File file = new File(context.getFilesDir(), VOTE_FILE_NAME);
				buf = new BufferedWriter(new FileWriter(file));
				for (Map.Entry<String, String> entry : votingIds.entrySet()) {
					line = entry.getKey() + " " + entry.getValue();
					buf.write(line);
					buf.newLine();
				}
				buf.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}