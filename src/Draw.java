import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.Scanner;
import javax.mail.*;
import javax.mail.internet.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Draw {
	private static JSONObject CONFIG;
	private static String FROM;
	private static String HOST;
	private static String PORT;
	private static String USERNAME;
	private static String PASSWORD;

	public static void main(String[] args) throws FileNotFoundException, JSONException {
		CONFIG = new JSONObject(new Scanner(new File("config.json")).useDelimiter("\\Z").next());

		setSenderAttributes();

		ArrayList<Member> members = getMembers();
		HashMap<Member, Member> matches = getGiftMatches(members);

		System.out.println("All the matches are now done...");
		if (CONFIG.getBoolean("SEND")) {
			Scanner kbd = new Scanner(System.in);

			System.out.print("Emails will be sent. Do you wish to continue? (Y/n) ");

			String decision = kbd.nextLine();

			if (decision.toLowerCase().equals("y") || decision.equals("")) {
				sendEmail(members, matches);
			} else {
				System.out.println("Operation aborted.");
			}
		} else {
			printMatches(members, matches);
		}
	}

	/**
	 * Sets all private sender attributes
	 */
	private static void setSenderAttributes() throws JSONException {
		JSONObject sender = CONFIG.getJSONObject("sender");

		FROM = sender.getString("from");
		HOST = sender.getString("host");
		PORT = sender.getString("port");
		USERNAME = sender.getString("username");
		PASSWORD = sender.getString("password");
	}

	/**
	 * Add all the final static Member
	 * @return ArrayList<Member> of all the final static Member
	 */
	private static ArrayList<Member> getMembers() throws JSONException {
		ArrayList<Member> members = new ArrayList<Member>();
		JSONArray parsedMembers = CONFIG.getJSONArray("members");

		for (int i = 0; i < parsedMembers.length(); ++i) {
			String email = parsedMembers.getJSONObject(i).getString("email");
			String name = parsedMembers.getJSONObject(i).getString("name");
			Member m = new Member(name, email);
			members.add(m);
		}

		return members;
	}

	private static HashMap<Member, Member> getGiftMatches(ArrayList<Member> members) throws JSONException {
		ArrayList<Member> receivers = getMembers();
		HashMap<Member, Member> matches = new HashMap<Member, Member>();

		int i = 0;
		int attempts = 0;

		while (receivers.size() > 0) {
			int n = ((int) (Math.random() * 100)) % receivers.size();

			if (isValidPair(members.get(i), receivers.get(n))) {
				matches.put(members.get(i), receivers.get(n));
				receivers.remove(n);
				++i;
				System.out.println("Match done. (" + i + "/" + members.size() + ")");
			}

			++attempts;

			if (attempts > members.size() * 10) {
				attempts = 0;
				i = 0;
				matches = new HashMap<Member, Member>();
				receivers = getMembers();
				System.out.println("Could not find a good match...");
				System.out.println("Retrying...");
			}
		}

		return matches;
	}

	/**
	 * Checks if the match is proper based on what the user wants
	 * @param giver
	 * @param receiver
	 * @return
	 */
	private static boolean isValidPair(Member giver, Member receiver) throws JSONException {
		if (giver.getEmail().equals(receiver.getEmail())) {
			return false;
		}

		JSONObject rules = CONFIG.isNull("rules") ? null : CONFIG.getJSONObject("rules");

		if (rules == null) {
			return true;
		}

		JSONArray couples = rules.isNull("couples") ? null : rules.getJSONArray("couples");

		if (couples != null) {
			for (int i = 0; i < couples.length(); ++i) {
				JSONArray rule = couples.getJSONArray(i);
				String forbid1 = rule.getString(0);
				String forbid2 = rule.getString(1);

				if ((giver.getEmail().equals(forbid1) && receiver.getEmail().equals(forbid2)) ||
				(giver.getEmail().equals(forbid2) && receiver.getEmail().equals(forbid1))) {
					return false;
				}
			}
		}

		JSONArray pastyears = rules.isNull("pastyears") ? null : rules.getJSONArray("pastyears");

		if (couples != null) {
			for (int i = 0; i < pastyears.length(); ++i) {
				JSONArray rule = pastyears.getJSONArray(i);
				String forbid1 = rule.getString(0);
				String forbid2 = rule.getString(1);

				if (giver.getEmail().equals(forbid1) && receiver.getEmail().equals(forbid2)) {
					return false;
				}
			}
		}

		return true;
	}

	private static void sendEmail(ArrayList<Member> members, HashMap<Member, Member> matches) throws JSONException {
		System.out.println("About to send emails to all recipients...");
		System.out.println("Setting all the connection settings...");
		Properties properties = getMailProperties();

		Authenticator authenticator = new Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(USERNAME, PASSWORD);
			}
		};

		Session session = Session.getInstance(properties, authenticator);

		for (int i = 0; i < matches.size(); ++i) {
			Member emailRecipient = members.get(i);
			String giftReceiver = matches.get(emailRecipient).getName();

			send(emailRecipient, session, giftReceiver);

			System.out.println("Sent message successfully... (" + (i + 1) + "/" + members.size() + ")");
		}
	}

	private static Properties getMailProperties() {
		Properties properties = new Properties();
		properties.put("mail.smtp.host", HOST);
		properties.put("mail.smtp.port", PORT);
		properties.put("mail.smtp.starttls.enable", "true");
		properties.put("mail.smtp.auth", "true");
		properties.put("mail.user", USERNAME);
		properties.put("mail.password", PASSWORD);
		properties.put("mail.smtp.socketFactory.port", PORT);
		properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		properties.put("mail.smtp.socketFactory.fallback", "false");

		return properties;
	}

	private static void send(Member emailRecipient, Session session, String giftReceiver) throws JSONException {
		try {
			JSONObject emailConfig = CONFIG.getJSONObject("email");
			JSONObject override = emailConfig.isNull("override") ? null : emailConfig.getJSONObject("override");
			JSONObject memberOverride = override != null && !override.isNull(emailRecipient.getEmail()) ? override.getJSONObject(emailRecipient.getEmail()) : null;

			// Create a default MimeMessage object.
			MimeMessage message = new MimeMessage(session);

			// Set From: header field of the header.
			message.setFrom(new InternetAddress(FROM));

			// Set To: header field of the header.
			String recipientName = memberOverride != null && !memberOverride.isNull("to") ? memberOverride.getString("to") : emailRecipient.getName();
			String recipient = recipientName + " <" + emailRecipient.getEmail() + ">";
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient));

			// Set Subject: header field
			String subject = memberOverride != null && !memberOverride.isNull("subject") ? memberOverride.getString("subject") : emailConfig.getString("subject");
			subject = subject.replaceAll("emailRecipient", emailRecipient.getName());
			message.setSubject(subject);

			// Send the actual HTML message
			String content = emailConfig.getString("content").replaceAll("giftReceiver", giftReceiver);

			message.setContent(content, "text/html");

			// Send message
			Transport t = session.getTransport("smtp");
			t.connect(HOST, USERNAME, PASSWORD);
			t.sendMessage(message, message.getAllRecipients());
			t.close();
		} catch (MessagingException mex) {
			mex.printStackTrace();
		}
	}

	private static void printMatches(ArrayList<Member> members, HashMap<Member, Member> matches) {
		for (int i = 0; i < matches.size(); ++i) {
			System.out.println(members.get(i).getName() + ": " + matches.get(members.get(i)).getName());
		}
	}
}
