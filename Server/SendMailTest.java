public class SendMailTest {

	public static void main(String[] args) {

		String from = "admins@athenachat.org";
		String to = "maclennann@wit.edu";
		String subject = "Test - Welcome to AthenaChat";
		String message = "Welcome to AthenaChat here's a guide or something";

		SendMail sendMail = new SendMail(from, to, subject, message);
		sendMail.send();
	}
}
