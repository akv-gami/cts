package com.cts.cts.service;

import com.cts.cts.model.LlcEntity;
import com.cts.cts.repository.LlcRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
public class NotificationService {

    private final LlcRepository llcRepository;
    private final JavaMailSender mailSender;

    @Value("${notification.days-before:30}")
    private int daysBefore;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", new Locale("es", "ES"));

    public NotificationService(LlcRepository llcRepository, JavaMailSender mailSender) {
        this.llcRepository = llcRepository;
        this.mailSender = mailSender;
    }

    @Scheduled(cron = "0 0 9 * * *")
    public void checkUpcomingDeadlines() {
        if (fromEmail == null || fromEmail.isBlank()) return;

        LocalDate today = LocalDate.now();
        LocalDate threshold = today.plusDays(daysBefore);

        List<LlcEntity> llcs = llcRepository.findAll();

        for (LlcEntity llc : llcs) {
            if (llc.getAnnualReportDueDate() == null || llc.getOwnerEmail() == null) continue;

            LocalDate due = llc.getAnnualReportDueDate();

            if (!due.isBefore(today) && !due.isAfter(threshold)) {
                sendAnnualReportReminder(llc);
            }

            if (llc.isRequiresForm5472()) {
                LocalDate form5472Due = LocalDate.of(today.getYear(), 4, 15);
                if (form5472Due.isBefore(today)) {
                    form5472Due = form5472Due.plusYears(1);
                }
                if (!form5472Due.isBefore(today) && !form5472Due.isAfter(threshold)) {
                    sendForm5472Reminder(llc, form5472Due);
                }
            }
        }
    }

    private void sendAnnualReportReminder(LlcEntity llc) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(llc.getOwnerEmail());
            message.setSubject("Recordatorio: Annual Report de " + llc.getBusinessName() + " vence pronto");
            message.setText(
                "Estimado/a " + llc.getOwnerName() + ",\n\n" +
                "Le recordamos que el Annual Report de su empresa " + llc.getBusinessName() +
                ", constituida en el estado de " + llc.getStateOfFormation() +
                ", vence el " + llc.getAnnualReportDueDate().format(DATE_FORMAT) + ".\n\n" +
                "El no presentar el Annual Report a tiempo puede resultar en la disolución administrativa " +
                "de su LLC por parte del Secretario de Estado.\n\n" +
                "Por favor contáctenos a la brevedad si necesita asistencia para cumplir con esta obligación.\n\n" +
                "Saludos,\n" +
                "CTS Consulting\n" +
                "Equipo de Cumplimiento Corporativo"
            );
            mailSender.send(message);
        } catch (Exception ignored) {
        }
    }

    private void sendForm5472Reminder(LlcEntity llc, LocalDate dueDate) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(llc.getOwnerEmail());
            message.setSubject("Recordatorio: Form 5472 de " + llc.getBusinessName() + " vence el 15 de abril");
            message.setText(
                "Estimado/a " + llc.getOwnerName() + ",\n\n" +
                "Le recordamos que el Form 5472 correspondiente a su empresa " + llc.getBusinessName() +
                " vence el " + dueDate.format(DATE_FORMAT) + ".\n\n" +
                "Como propietario extranjero de una LLC clasificada como Disregarded Entity, " +
                "está obligado a presentar el Form 5472 ante el IRS anualmente. " +
                "El incumplimiento puede resultar en multas de $25,000 o más.\n\n" +
                "Por favor contáctenos a la brevedad si necesita asistencia.\n\n" +
                "Saludos,\n" +
                "CTS Consulting\n" +
                "Equipo de Cumplimiento Corporativo"
            );
            mailSender.send(message);
        } catch (Exception ignored) {
        }
    }
}