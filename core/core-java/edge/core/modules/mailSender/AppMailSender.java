package edge.core.modules.mailSender;

import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.velocity.app.VelocityEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.ui.velocity.VelocityEngineUtils;

import edge.core.modules.auth.SignUpEntity;

public class AppMailSender implements InitializingBean{

	private static final Logger logger = LoggerFactory.getLogger(AppMailSender.class);
	
	@Value(value = "${property.appName}")
	private String appName;
	
	@Value(value = "${mail.smtp.fromAddress}")
	private InternetAddress fromAddress;
	
	@Value(value = "${mail.smtp.threadCount}")
	private Integer threadCount;
	
	private static AppMailSender INSTANCE = null;
	
	private static ExecutorService executor = null;
	
	@Autowired
	private JavaMailSender mailSender;

	@Autowired
	private VelocityEngine velocityEngine;

	public AppMailSender() {
		INSTANCE = this;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		executor = Executors.newFixedThreadPool(getThreadCount());
	}
	
	public void setMailSender(JavaMailSender mailSender) {
		this.mailSender = mailSender;
	}

	public void setVelocityEngine(VelocityEngine velocityEngine) {
		this.velocityEngine = velocityEngine;
	}
	
	public static void sendEmail(final SignUpEntity signUpEntity, final Map<String, Object> dataObject, final EventDetails eventDetails) throws Exception {
		sendEmail(signUpEntity.getProfileId(), signUpEntity.getEmailId(), dataObject, eventDetails);
	}
	
	public static void sendEmail(final String identifier, final String emailId, final Map<String, Object> dataObject, final EventDetails eventDetails) throws Exception {
		try{
			 final String subject = INSTANCE.getSubject(identifier, eventDetails);
			 final MimeMessagePreparator preparator = new MimeMessagePreparator() {
					public void prepare(MimeMessage mimeMessage) throws Exception {
						MimeMessageHelper message = new MimeMessageHelper(mimeMessage);
						message.setFrom(INSTANCE.fromAddress);						
						message.setTo(new InternetAddress[]{new InternetAddress(emailId), INSTANCE.fromAddress});
						message.setSubject(subject);
						String text = VelocityEngineUtils.mergeTemplateIntoString(INSTANCE.velocityEngine, eventDetails.getTemplatePath(), dataObject);
						message.setText(text, true);
					}
				};
				executor.submit(new Runnable() {
					@Override
					public void run() {
						try{
							logger.debug("Sending Mail : " + subject);
							INSTANCE.mailSender.send(preparator);
							logger.debug("Mail Sent Successfully : " + subject);
						}catch(Exception e){
							e.printStackTrace();
							logger.debug("Error while Sending Mail : " + subject, e);
						}
					}
				});
		} catch (Exception e) {
			e.printStackTrace();
			//throw e;
		}
	}
	
	protected String getSubject(
			String identifier, EventDetails eventDetails) {
		return getAppName() + " - [" + identifier +"] - " + eventDetails.getSubject() + " - " + new Date();
	}

	public static void main(String[] args) throws Exception {
		sendTestEmail();
	}
	
	public static void sendTestEmail() throws Exception {
		
		JavaMailSenderImpl senderImpl = new JavaMailSenderImpl();

		senderImpl.setHost("smtp.office365.com");
		senderImpl.setPort(587);
		
		senderImpl.setUsername("support@saat-phere.com");
		senderImpl.setPassword("Infy@edge9");
		
		Properties javaMailProperties = new Properties();
		javaMailProperties.put("mail.smtp.auth",true);
		javaMailProperties.put("mail.smtp.starttls.enable",true);
		javaMailProperties.put("mail.smtp.timeout",28500);
		
		senderImpl.setJavaMailProperties(javaMailProperties);
		
		sendTestEmail("support@saat-phere.com", "patil.vinayb7@gmail.com", "Shree " + new Date(), "Shree Ganeshay Namah", senderImpl);
		
	}	
	
	public static void sendTestEmail(final String fromAddress, final String toAddress, final String subject, final String text, final JavaMailSender mailSender) throws Exception {
		try {
			MimeMessagePreparator preparator = new MimeMessagePreparator() {
				public void prepare(MimeMessage mimeMessage) throws Exception {
					MimeMessageHelper message = new MimeMessageHelper(mimeMessage);
					message.setFrom(fromAddress);
					message.setTo(toAddress);
					message.setSubject(subject);
					message.setText(text, true);
				}
			};
			mailSender.send(preparator);
			System.out.println("Mail Sent Successfully.");
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}

	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public Integer getThreadCount() {
		return threadCount;
	}

	public void setThreadCount(Integer threadCount) {
		this.threadCount = threadCount;
	}

}
