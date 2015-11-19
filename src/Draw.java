import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

import javax.mail.*;
import javax.mail.internet.*;

public class Draw {
	private final static Member GORDON = new Member("Gordon Freeman", "gordon@hl.com");
	private final static Member EDWARD = new Member("Edward Carnby", "edward@infogrames.com");
	
	private final static String SENDER = "gordon@hl.com";
	private final static String HOST = "smtp-mail.outlook.com";
	private final static String PORT = "587";
	private final static String USERNAME = SENDER;
	private final static String PASSWORD = "halflife3";
	
	public static void main(String[] args) {
		ArrayList<Member> members = getMembers();
		
		HashMap<Member, Member> matches = getGiftMatches(members);
		
		System.out.println("All the matches are now done...");
	    System.out.println("About to send emails to all recipients...");
	    
		sendEmail(members, matches);
	}
	
	/**
	 * Add all the final static Member
	 * @return ArrayList<Member> of all the final static Member
	 */
	private static ArrayList<Member> getMembers() {
		ArrayList<Member> members = new ArrayList<Member>();
		members.add(GORDON);
		members.add(EDWARD);
		return members;
	}
	
	private static HashMap<Member, Member> getGiftMatches(ArrayList<Member> members) {
		ArrayList<Member> receivers = getMembers();
		HashMap<Member, Member> matches = new HashMap<Member, Member>();
		
		int i = 0;
		while (receivers.size() > 0) {
			int n = ((int) (Math.random() * 100)) % receivers.size();
			if (isValidPair(members.get(i), receivers.get(n))) {
				matches.put(members.get(i), receivers.get(n));
				receivers.remove(n);
				i++;
				
				System.out.println("Match done. (" + i + "/" + members.size() + ")");
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
	private static boolean isValidPair(Member giver, Member receiver) {
		boolean isValid = true;

		if (giver.getEmail().equals(receiver.getEmail())) {
			isValid = false;
		}

		/* add extra conditions here */
		
		return isValid;
	}
	
	private static void sendEmail(ArrayList<Member> members, HashMap<Member, Member> matches) {
		System.out.println("Setting all the connection settings...");
	    Properties properties = getMailProperties();
		Authenticator authenticator = new Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(USERNAME, PASSWORD);
			}
		};
	    Session session = Session.getInstance(properties, authenticator);
	    
		for (int i = 0; i < matches.size(); i++) {
		    Member emailRecipient = members.get(i);
		    String giftReceiver = matches.get(emailRecipient).getName();
		    
			send(SENDER, emailRecipient, HOST, USERNAME, PASSWORD, session, giftReceiver);
			
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

	private static void send(String from, Member emailRecipient, String host, String username, String password, Session session, String giftReceiver) {
		try {
		    // Create a default MimeMessage object.
		    MimeMessage message = new MimeMessage(session);

		    // Set From: header field of the header.
		    message.setFrom(new InternetAddress(from));

		    // Set To: header field of the header.
		    message.addRecipient(Message.RecipientType.TO, new InternetAddress(emailRecipient.getEmail()));

		    // Set Subject: header field
			message.setSubject("Your draw result!");

		    // Send the actual HTML message, as big as you like
		    message.setContent(
					"<body>" +
						"<h1>Your draw: " + giftReceiver + "</h1>" +
						"</body>"
					, "text/html");

		    // Send message
		    Transport t = session.getTransport("smtp");
		    t.connect(host, username, password);
		    t.sendMessage(message, message.getAllRecipients());
		    t.close();
		} catch (MessagingException mex) {
		    mex.printStackTrace();
		}
	}
}
