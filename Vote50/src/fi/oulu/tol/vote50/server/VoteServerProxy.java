package fi.oulu.tol.vote50.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.Vector;

import org.json.JSONException;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import fi.oulu.tol.vote50.service.VoteService;
import fi.oulu.tol.vote50.voting.VoteIdRepository;
import fi.oulu.tol.vote50.voting.Voting;
import fi.oulu.tol.vote50.voting.VotingParser;

public class VoteServerProxy implements VoteServer {

	private String serverAddress = null;
	private VoteService proxyServer = null;
	private Context context = null;
	private Vector<Voting> votings;
   
	private VotingParser votingParser = null;
	private boolean isRequestingAll = false;
	private int nextVotingIndex = -1;
	private String errorMessage = null;

	private static final String LAST_MODIFIED_HEADER_KEY = "Last-Modified";
	private static final String WHEN_MODIFIED_AFTER_HEADER_KEY = "When-Modified-After";

	private static final String TAG = "VoteServerProxy";
	private static final String TAG_DOWNLOAD = "DownloadVotingsTask";
	private static final String TAG_UPLOAD = "UPloadVotingsTask";

	private Handler timerHandler = new Handler();
	private Timer pollingTimer = new Timer();
	private String pollingDateTime = null;

	private int flag = 0;
    
	private void doPollingDownload() {
		timerHandler.post(new Runnable() {
			@Override
			public void run() {
				pollingTimer.schedule(new PollingTask(), 5000);
			}
		});
	}

	/*************** start to implement *********************/
	@Override
	public void initialize(Context c, VoteServerObserver obs) {

		context = c;
		votings = new Vector<Voting>();
		proxyServer = (VoteService) obs;
		proxyServer.updateServerSettings();

	}

	@Override
	public String getServerAddress() {
		return serverAddress;
	}

	@Override
	public void setServerAddress(String serverAddress) {
		this.serverAddress = serverAddress;
	}

	public Vector<Voting> getVotings() {
		Log.d(TAG,"get voting method");
		return votings;
	}

	public void setVotings(Vector<Voting> votings) {
		this.votings = votings;
	}
    
	
	@Override
	public boolean retrieveVotings() {

		if (checkNetwork() == true && isRequestingAll == false) {

			isRequestingAll = true;
			String baseURL = this.getServerAddress();
			this.setServerAddress(baseURL);
			String APIVersion = "1.0";
			String resource = "votings";
			String requestURL = baseURL + "/" + APIVersion + "/" + resource;
			DownloadVotingsTask task = new DownloadVotingsTask(true);
			task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, requestURL);
			return true;
		} else
			return false;
	}

	private boolean checkNetwork() {
		ConnectivityManager connMgr = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected()) {
			return true;
		}
		return false;
	}

	@Override
	public boolean retrieveOneVoting(Voting voting) {
		if (checkNetwork()) {
			String baseURL = this.serverAddress;
			String APIVersion = "1.0";
			String resource = "votings";
			String votingUuid = voting.getmId().toString();
			String requestURL = baseURL + "/" + APIVersion + "/" + resource
					+ "/" + votingUuid;
			DownloadVotingsTask task = new DownloadVotingsTask(false);
			task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, requestURL);
			return true;
		} else
			return false;
	}

	@Override
	public boolean retrieveVotingResults(Voting voting) {
		if (checkNetwork()) {
			String baseURL = this.serverAddress;
			String APIVersion = "1.0";
			String resource = "votings";
			String votingUuid = voting.getmId().toString();
			String requestURL = baseURL + "/" + APIVersion + "/" + resource
					+ "/" + votingUuid + "/results";
			DownloadVotingsTask task = new DownloadVotingsTask(false);
			task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, requestURL);
			this.flag = 1;
			//this.isRequestResult=false;
			return true;
		} else
			return false;
	}

	@Override
	public Voting getNextVoting() {

		if (nextVotingIndex < votings.size() - 1) {
			nextVotingIndex++;
			return this.votings.get(nextVotingIndex);
		} else
			return null;

	}

	@Override
	public boolean submitVote(Voting voting) {

		if (checkNetwork()) {
			String baseURL = this.serverAddress;
			String APIVersion = "1.0";
			String resource = "votings";
			String votingId = voting.getmId().toString();
			String voteId = "";
			VoteIdRepository repository = new VoteIdRepository(context);
			if (votingId != null) {
				voteId = repository.findLocalUuidForVotingUuid(votingId);
				if (voting.getVoteUuid() == null) {
					voteId = UUID.randomUUID().toString();
					voting.setVoteUuid(UUID.fromString(voteId));
				} else
					voteId = voting.getVoteUuid().toString();
				Log.d(TAG_UPLOAD, voting.getVoteUuid().toString());
				String requestURL = baseURL + "/" + APIVersion + "/" + resource
						+ "/" + votingId + "/" + "votes" + "/" + voteId;
				String uploadContent = "{" + '"' + "option" + '"' + ": "
						+ String.valueOf(voting.getmSelectedOption()) + "}";
				UploadVoteTask task = new UploadVoteTask();
				task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
						requestURL, uploadContent);
				return true;
			} else
				return false;
		} else
			return false;
	}

	@Override
	public boolean forceRefresh() {
		votings.clear();
		return this.retrieveVotings();
	}

	private class DownloadVotingsTask extends AsyncTask<String, Void, String> {

		private int response = 0;
		private boolean doLongPolling = false;

		public DownloadVotingsTask(boolean longPoll) {
			doLongPolling = longPoll;
		}

		@Override
		protected String doInBackground(String... urls) {

			try {
				return downloadUrl(urls[0]);
			} catch (IOException e) {
				return "Unable to retrieve web page. URL may be invalid.";
			}
		}

		// onPostExecute displays the results of the AsyncTask.
		@Override
		protected void onPostExecute(String result) {
			isRequestingAll = false;
			if (result == null) {
				proxyServer.errorNotification("No votings downloaded");
			} else {

				if (response >= 200 && response <= 299) {
					try {
						int number = votingParser.parseString(result, votings);
                        if (number > 0 && flag == 0 )
							proxyServer.votingsReceived();
						if (number == 0)
							proxyServer.noNewVotingsAvailable();
					} catch (JSONException e) {
						e.printStackTrace();
						proxyServer.errorNotification(e.getMessage());
					}
				}
				if (response < 200 && response > 299) {
					proxyServer.errorNotification(errorMessage);
				}
			}

			if (doLongPolling) {
				isRequestingAll = false;
				Log.d(TAG,
						"Is not requesting all, request to retrieve all after timer");
				doPollingDownload();
			}

		}

		private String downloadUrl(String myurl) throws IOException {
			InputStream content = null;
			String result = null;
			int len = 1024;
			try {
				URL url = new URL(myurl);
				HttpURLConnection conn = (HttpURLConnection) url
						.openConnection();
				conn.setReadTimeout(10000000);
				conn.setConnectTimeout(15000);
				conn.setRequestProperty("Accept-Charset", "utf-8");
				conn.setRequestMethod("GET");
				if (doLongPolling && null != pollingDateTime) {
					conn.setRequestProperty(WHEN_MODIFIED_AFTER_HEADER_KEY,
							pollingDateTime);
					
				}

				conn.connect();
				response = conn.getResponseCode();
				if (response < 200 && response > 299)
					errorMessage = conn.getResponseMessage();
				else {
					if (doLongPolling) {
						pollingDateTime = conn
								.getHeaderField(LAST_MODIFIED_HEADER_KEY);
					}
					content = conn.getInputStream();
					result = readIt(content, len);
				}
			} finally {
				if (content != null)
					content.close();
			}
			return result;
		}

		// Reads an InputStream and converts it to a String.
		public String readIt(InputStream stream, int len) throws IOException,
		UnsupportedEncodingException {
			Reader reader = null;
			reader = new InputStreamReader(stream, "UTF-8");
			char[] buffer = new char[len];
			String content = "";
			int read;
			do {
				read = reader.read(buffer);
				if (read > 0) {
					content += String.valueOf(buffer, 0, read);
				}
			} while (read > 0);
			return content;
		}
	}

	// inner class
	private class UploadVoteTask extends AsyncTask<String, String, Integer> {

		private int response = 0;

		@Override
		protected Integer doInBackground(String... urls) {

			try {
				return uploadUrl(urls[0], urls[1]);
			} catch (IOException e) {
				return -1;
			}

		}

		// onPostExecute displays the results of the AsyncTask.
		@Override
		protected void onPostExecute(Integer result) {

			if (result >= 200 && result < 300)
				proxyServer.votePosted();
			else
				proxyServer.errorNotification(errorMessage);
		}

		private int uploadUrl(String myurl, String actual_content)
				throws IOException {
			int length = actual_content.length();
			String lengthToString = String.valueOf(length);
			URL url = new URL(myurl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setReadTimeout(10000);
			conn.setConnectTimeout(15000);
			conn.setRequestMethod("PUT");
			conn.setDoInput(true);
			conn.setChunkedStreamingMode(0);
			conn.setRequestProperty("Content-Length", lengthToString);
			conn.setRequestProperty("Content-Type", "application/json");
			OutputStreamWriter writer = new OutputStreamWriter(
					conn.getOutputStream());
			writer.write(actual_content);
			writer.flush();
			writer.close();
			response = conn.getResponseCode();
			errorMessage = conn.getResponseMessage();
			return response;
		}
	}

	private class PollingTask extends TimerTask {
		@Override
		public void run() {
			retrieveVotings();
		}
	}

}
