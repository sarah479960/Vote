package fi.oulu.tol.vote50.dateTransfer;

/*
 * Copyright (c) 2014, Henrik Hedberg
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * The names of contributors may not be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
 
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;
 
/**
 * Utility class providing a simple parser for RFC 3339 timestamps.
 *
 * @see <a href="http://tools.ietf.org/html/rfc3339">RFC 3339</a>
 * @author Henrik Hedberg <henrik.hedberg@innologies.fi>
 * @version 20140306.2
 */
public class Rfc3339 {
	// This class is not meant to be instantiated.
	private Rfc3339() {
	}

	// java.lang.Character.isDigit() is not sensitive enough
	private static boolean isDigit(char c) {
		return c >= '0' && c <= '9';
	}
	
	private static void appendTwoDigitInteger(StringBuilder builder, int integer) {
		if (integer < 10)
			builder.append('0');
		builder.append(integer);
	}
	
	/**
	 * Takes a timestamp as a Calendar and returns it as a String in
	 * RFC 3339 format.
	 *
	 * @param timestamp a timestamp as a Calendar
	 * @return the provided timestamp as a RFC 3339 string
	 */
	public static String fromCalendar(Calendar timestamp) {
		StringBuilder builder = new StringBuilder(Integer.toString(timestamp.get(Calendar.YEAR)));
		builder.append('-');
		appendTwoDigitInteger(builder, timestamp.get(Calendar.MONTH) + 1);
		builder.append('-');
		appendTwoDigitInteger(builder, timestamp.get(Calendar.DAY_OF_MONTH));
		builder.append('T');
		appendTwoDigitInteger(builder, timestamp.get(Calendar.HOUR_OF_DAY));
		builder.append(':');
		appendTwoDigitInteger(builder, timestamp.get(Calendar.MINUTE));
		builder.append(':');
		appendTwoDigitInteger(builder, timestamp.get(Calendar.SECOND));

		int millisecond = timestamp.get(Calendar.MILLISECOND);
		if (millisecond != 0) {
			builder.append('.');
			if (millisecond < 100)
				builder.append('0');
			if (millisecond < 10)
				builder.append('0');
			builder.append(millisecond);
		}
		
		int offset = timestamp.get(Calendar.ZONE_OFFSET);
		if (offset == 0) {
			builder.append('Z');
		} else {
			offset /= 60000;
			int hours = offset / 60;
			int minutes = offset - hours * 60;
			if (hours > 0) {
				builder.append('+');
			} else {
				hours = -hours;
				builder.append('-');
				appendTwoDigitInteger(builder, hours);
				builder.append(':');
				appendTwoDigitInteger(builder, minutes);
			}
		}
		
		return builder.toString();
	}
	
	/**
	 * Takes a RFC 3339 timestamp as a string and returns its value as an
	 * instance of a GregorianCalendar.
	 *
	 * The format is yyyy-mm-ddThh:mm:ss[.SSS](Z/(+/-)hh:mm).
	 *
	 * @param timestamp a RFC 339 timestamp
	 * @return the provided timestamp as a GregorianCalendar
	 * @throws NumberFormatException if the provided timestamp is not valid
	 */
	public static GregorianCalendar parse(String timestamp) throws NumberFormatException {
		if (timestamp.length() < 20 ||
		    timestamp.charAt(4) != '-' ||
		    timestamp.charAt(7) != '-' ||
		    Character.toLowerCase(timestamp.charAt(10)) != 't' ||
		    timestamp.charAt(13) != ':' ||
		    timestamp.charAt(16) != ':')
			throw new NumberFormatException("Invalid RFC 3339 timestamp.");

		int year = Integer.parseInt(timestamp.substring(0, 4));
		int month = Integer.parseInt(timestamp.substring(5, 7)) - 1;
		int day = Integer.parseInt(timestamp.substring(8, 10));
		int hour = Integer.parseInt(timestamp.substring(11, 13));
		int minute = Integer.parseInt(timestamp.substring(14, 16));
		int second = Integer.parseInt(timestamp.substring(17, 19));
		
		int i = 19;
		int millisecond = 0;
		if (timestamp.charAt(i) == '.') {
			for (i = 20; i < timestamp.length(); i++) {
				if (!isDigit(timestamp.charAt(i)))
					break;
			}
			if (i >= timestamp.length())
				throw new NumberFormatException("Invalid RFC 3339 timestamp.");

			millisecond = Integer.parseInt(timestamp.substring(20, (i > 23 ? 23 : i)));
		}
		
		TimeZone timeZone;
		if (Character.toLowerCase(timestamp.charAt(i)) == 'z') {
			if (i + 1 != timestamp.length())
				throw new NumberFormatException("Invalid RFC 3339 timestamp.");
			timeZone = TimeZone.getTimeZone("GMT");
		} else if (i + 6 != timestamp.length() ||
		           (timestamp.charAt(i) != '+' && timestamp.charAt(i) != '-') ||
			   !isDigit(timestamp.charAt(i + 1)) ||
			   !isDigit(timestamp.charAt(i + 2)) ||
			   timestamp.charAt(i + 3) != ':' ||
			   !isDigit(timestamp.charAt(i + 4)) ||
			   !isDigit(timestamp.charAt(i + 5))) {
			throw new NumberFormatException("Invalid RFC 3339 timestamp.");
		} else {
			timeZone = TimeZone.getTimeZone("GMT" + timestamp.substring(i, i + 6));
		}

		GregorianCalendar calendar = new GregorianCalendar(timeZone);
		calendar.set(year, month, day, hour, minute, second);
		calendar.set(Calendar.MILLISECOND, millisecond);

		return calendar;
	}
}
