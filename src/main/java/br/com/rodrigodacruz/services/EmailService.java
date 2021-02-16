package br.com.rodrigodacruz.services;

import org.springframework.mail.SimpleMailMessage;
import br.com.rodrigodacruz.domain.Usuario;
import br.com.rodrigodacruz.domain.Pedido;

public interface EmailService {

	void sendOrderConfirmationEmail(Pedido obj);
	
	void sendEmail(SimpleMailMessage msg);
	
	void sendNewPasswordEmail(Usuario usuario, String newPass);
}
