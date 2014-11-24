import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

import javax.mail.*;
import javax.mail.internet.*;

public class Draw {
	private final static Member GORDON = new Member("Gordon Freeman", "gordon@hl.com");
	private final static Member EDWARD = new Member("Edward Carnby", "edward@infogrames.com");
	
	private final static String SENDER = "gordon@hl.com";
	private final static String HOST = "smtp.gmail.com";
	private final static String USERNAME = "gordon";
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
		if (giver.getEmail().equals(receiver.getEmail())) {
			return false;
		}
		
		/* add extra conditions here */
		
		return true;
	}
	
	private static void sendEmail(ArrayList<Member> members, HashMap<Member, Member> matches) {
		System.out.println("Setting all the connection settings...");
	    Properties properties = getMailProperties();
	    Session session = Session.getInstance(properties);
	    
		for (int i = 0; i < matches.size(); i++) {
		    String to = members.get(i).getEmail();
		    String giftReceiver = matches.get(members.get(i)).getName();
		    
			send(SENDER, to, HOST, USERNAME, PASSWORD, session, giftReceiver);
			
			System.out.println("Sent message successfully... (" + (i + 1) + "/" + members.size() + ")");
		}
	}
	
	private static Properties getMailProperties() {
		Properties properties = new Properties();
	    properties.setProperty("mail.smtp.host", HOST);
	    properties.setProperty("mail.user", USERNAME);
	    properties.setProperty("mail.password", PASSWORD);
	    return properties;
	}

	private static void send(String from, String to, String host, String username, String password, Session session, String giftReceiver) {
		try {
		    // Create a default MimeMessage object.
		    MimeMessage message = new MimeMessage(session);

		    // Set From: header field of the header.
		    message.setFrom(new InternetAddress(from));

		    // Set To: header field of the header.
		    message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

		    // Set Subject: header field
		    message.setSubject("Your draw result!");

		    // Send the actual HTML message, as big as you like
		    message.setContent(
		    		"<body>" + 
		        		"<h1>Your draw: " + giftReceiver + "</h1>" +
			        "</body>"
		    		, "text/html");

		    // Send message
		    Transport t = session.getTransport("smtps");
		    t.connect(host, username, password);
		    t.sendMessage(message, message.getAllRecipients());
		    t.close();
		} catch (MessagingException mex) {
		    mex.printStackTrace();
		}
	}
}
