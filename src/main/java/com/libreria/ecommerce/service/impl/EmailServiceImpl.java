package com.libreria.ecommerce.service.impl;

import com.libreria.ecommerce.entity.EmailConfig;
import com.libreria.ecommerce.entity.Order;
import com.libreria.ecommerce.entity.OrderItem;
import com.libreria.ecommerce.entity.Payment;
import com.libreria.ecommerce.entity.StoreConfig;
import com.libreria.ecommerce.repository.StoreConfigRepository;
import com.libreria.ecommerce.service.EmailConfigService;
import com.libreria.ecommerce.service.EmailService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

/**
 * Envía el ticket de compra por correo al comprador (usuario registrado o invitado, según el
 * email capturado en el checkout). La configuración SMTP se lee de la tabla {@code
 * email_config} en cada envío (vía {@link EmailConfigService}) en vez de propiedades
 * estáticas de Spring — así el ADMIN puede capturar/cambiar las credenciales desde
 * {@code /api/admin/email-config} sin reiniciar el Backend.
 * <p>
 * Nunca deja que una falla de SMTP tumbe el checkout: cada método atrapa sus propias
 * excepciones y solo registra en el log — el pedido ya quedó confirmado en Backend
 * independientemente de si el correo llega o no.
 */
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);
    private static final NumberFormat CURRENCY = NumberFormat.getCurrencyInstance(new Locale("es", "MX"));

    private final EmailConfigService emailConfigService;
    private final StoreConfigRepository storeConfigRepository;

    @Override
    public void sendOrderApproved(Order order, List<OrderItem> items, Payment payment) {
        EmailConfig mailConfig = emailConfigService.getEntity();
        if (!Boolean.TRUE.equals(mailConfig.getEnabled())) {
            log.info("Envío de correo deshabilitado (email_config.enabled=false); se omite el ticket del pedido {}", order.getFolio());
            return;
        }
        try {
            StoreConfig store = currentStoreConfig();
            String subject = "Tu pedido " + order.getFolio() + " fue confirmado — " + store.getStoreName();
            send(mailConfig, order.getBuyerEmail(), subject, buildApprovedHtml(order, items, payment, store));
            log.info("Ticket del pedido {} enviado a {}", order.getFolio(), order.getBuyerEmail());
        } catch (Exception e) {
            log.error("No se pudo enviar el ticket del pedido {} a {}", order.getFolio(), order.getBuyerEmail(), e);
        }
    }

    @Override
    public void sendOrderRejected(Order order) {
        EmailConfig mailConfig = emailConfigService.getEntity();
        if (!Boolean.TRUE.equals(mailConfig.getEnabled())) {
            log.info("Envío de correo deshabilitado (email_config.enabled=false); se omite el aviso de rechazo del pedido {}", order.getFolio());
            return;
        }
        try {
            StoreConfig store = currentStoreConfig();
            String subject = "Tu pago no pudo procesarse — " + store.getStoreName();
            send(mailConfig, order.getBuyerEmail(), subject, buildRejectedHtml(order, store));
            log.info("Aviso de pago rechazado del pedido {} enviado a {}", order.getFolio(), order.getBuyerEmail());
        } catch (Exception e) {
            log.error("No se pudo enviar el aviso de pago rechazado del pedido {} a {}", order.getFolio(), order.getBuyerEmail(), e);
        }
    }

    private void send(EmailConfig mailConfig, String to, String subject, String html) throws Exception {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(mailConfig.getSmtpHost());
        sender.setPort(mailConfig.getSmtpPort());
        sender.setUsername(mailConfig.getSmtpUsername());
        sender.setPassword(mailConfig.getSmtpPassword());
        Properties props = sender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        MimeMessage message = sender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
        helper.setTo(to);
        String from = mailConfig.getFromAddress() != null && !mailConfig.getFromAddress().isBlank()
                ? mailConfig.getFromAddress() : mailConfig.getSmtpUsername();
        helper.setFrom(from);
        helper.setSubject(subject);
        helper.setText(html, true);
        sender.send(message);
    }

    private StoreConfig currentStoreConfig() {
        return storeConfigRepository.findAll().stream().findFirst()
                .orElseGet(() -> StoreConfig.builder().storeName("Librería Online").build());
    }

    private String buildApprovedHtml(Order order, List<OrderItem> items, Payment payment, StoreConfig store) {
        StringBuilder rows = new StringBuilder();
        for (OrderItem item : items) {
            rows.append("""
                    <tr>
                      <td style="padding:8px 0;border-bottom:1px solid #eee;">%s</td>
                      <td style="padding:8px 0;border-bottom:1px solid #eee;text-align:center;">%d</td>
                      <td style="padding:8px 0;border-bottom:1px solid #eee;text-align:right;">%s</td>
                      <td style="padding:8px 0;border-bottom:1px solid #eee;text-align:right;font-weight:bold;">%s</td>
                    </tr>
                    """.formatted(item.getTitle(), item.getQuantity(), money(item.getUnitPrice()), money(item.getSubtotal())));
        }

        String paymentLine = payment != null
                ? "Pago con tarjeta " + nullSafe(payment.getCardBrand()) + " terminación " + nullSafe(payment.getCardLast4())
                : "";

        return """
                <div style="font-family:Arial,sans-serif;max-width:600px;margin:0 auto;color:#1f2937;">
                  <h2 style="color:#155DEA;">%s</h2>
                  <p>Hola %s, gracias por tu compra. Este es tu comprobante:</p>
                  <p><strong>Folio:</strong> %s</p>
                  <table style="width:100%%;border-collapse:collapse;margin-top:12px;">
                    <thead>
                      <tr style="text-align:left;color:#6b7280;font-size:13px;">
                        <th style="padding:8px 0;border-bottom:2px solid #e5e7eb;">Producto</th>
                        <th style="padding:8px 0;border-bottom:2px solid #e5e7eb;text-align:center;">Cant.</th>
                        <th style="padding:8px 0;border-bottom:2px solid #e5e7eb;text-align:right;">Precio</th>
                        <th style="padding:8px 0;border-bottom:2px solid #e5e7eb;text-align:right;">Importe</th>
                      </tr>
                    </thead>
                    <tbody>
                      %s
                    </tbody>
                  </table>
                  <p style="text-align:right;font-size:18px;font-weight:bold;margin-top:12px;">Total: %s</p>
                  <p style="color:#6b7280;font-size:13px;">%s</p>
                  <p style="color:#6b7280;font-size:13px;">Dirección de envío: %s, %s, %s %s</p>
                  <hr style="border:none;border-top:1px solid #e5e7eb;margin:20px 0;">
                  <p style="color:#6b7280;font-size:13px;">%s</p>
                </div>
                """.formatted(
                store.getStoreName(), order.getBuyerFirstName(), order.getFolio(),
                rows, money(order.getTotal()), paymentLine,
                order.getShippingAddressLine1(), order.getShippingCity(), order.getShippingState(), order.getShippingPostalCode(),
                store.getTicketMessage() != null ? store.getTicketMessage() : "Gracias por tu compra."
        );
    }

    private String buildRejectedHtml(Order order, StoreConfig store) {
        return """
                <div style="font-family:Arial,sans-serif;max-width:600px;margin:0 auto;color:#1f2937;">
                  <h2 style="color:#DC2626;">%s</h2>
                  <p>Hola %s, no pudimos procesar el pago de tu pedido con folio <strong>%s</strong>.</p>
                  <p>Puedes intentarlo de nuevo con otro método de pago desde la tienda.</p>
                  <p style="color:#6b7280;font-size:13px;margin-top:20px;">Si tienes dudas, contáctanos en %s.</p>
                </div>
                """.formatted(store.getStoreName(), order.getBuyerFirstName(), order.getFolio(),
                store.getEmail() != null ? store.getEmail() : "");
    }

    private String money(BigDecimal amount) {
        return CURRENCY.format(amount != null ? amount : BigDecimal.ZERO);
    }

    private String nullSafe(String value) {
        return value != null ? value : "";
    }
}
