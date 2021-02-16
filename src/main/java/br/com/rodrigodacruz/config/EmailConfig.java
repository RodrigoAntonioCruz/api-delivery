package br.com.rodrigodacruz.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import br.com.rodrigodacruz.services.EmailService;
import br.com.rodrigodacruz.services.SmtpEmailService;

@Configuration
public class EmailConfig {
	@Bean
	public EmailService emailService() {
		return new SmtpEmailService();
	}
}
