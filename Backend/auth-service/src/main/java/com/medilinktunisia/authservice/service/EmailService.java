package com.medilinktunisia.authservice.service;

import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

/**
 * Envoi des emails de l'auth-service.
 * <p>
 * En l'absence de mot de passe SMTP configuré ({@code MAIL_PASSWORD}), le service
 * bascule en <b>mode développement</b> : au lieu d'envoyer réellement l'email,
 * il journalise le lien de réinitialisation. Il suffira de définir
 * {@code MAIL_PASSWORD} (mot de passe d'application Gmail) pour activer l'envoi réel.
 * <p>
 * L'email est envoyé au format <b>HTML</b>, habillé aux couleurs de MediLink Tunisia
 * (dégradé bleu Méditerranée → cyan, accent sable), avec un repli texte brut.
 */
@Service
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final String from;
    private final String smtpPassword;

    public EmailService(JavaMailSender mailSender,
                        @Value("${app.mail.from:medilink.tunisia@gmail.com}") String from,
                        @Value("${spring.mail.password:}") String smtpPassword) {
        this.mailSender = mailSender;
        this.from = from;
        this.smtpPassword = smtpPassword;
    }

    /** Vrai si un mot de passe SMTP est réellement configuré (envoi réel possible). */
    private boolean isMailConfigured() {
        return smtpPassword != null && !smtpPassword.isBlank();
    }

    /**
     * Envoie l'email contenant le lien de réinitialisation du mot de passe.
     * @param to          destinataire (email du compte)
     * @param displayName nom affiché dans le message (peut être vide)
     * @param resetLink   lien complet vers la page de réinitialisation
     */
    public void sendPasswordResetEmail(String to, String displayName, String resetLink) {
        if (!isMailConfigured()) {
            log.warn("""
                    [EMAIL DEV] SMTP non configuré (MAIL_PASSWORD vide).
                    Lien de réinitialisation pour {} :
                    {}""", to, resetLink);
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
            helper.setFrom(from, "MediLink Tunisia");
            helper.setTo(to);
            helper.setSubject("MediLink Tunisia — Réinitialisation de votre mot de passe");
            // text/plain (repli) + text/html (affichage riche)
            helper.setText(buildText(displayName, resetLink), buildHtml(displayName, resetLink));
            mailSender.send(message);
            log.info("Email de réinitialisation envoyé à {}", to);
        } catch (Exception e) {
            // L'échec d'envoi ne doit pas casser le flux ; on journalise le lien en secours.
            log.error("Échec de l'envoi de l'email à {} : {}. Lien : {}", to, e.getMessage(), resetLink);
        }
    }

    /** Version texte brut (repli pour les clients sans HTML). */
    private String buildText(String displayName, String resetLink) {
        String hello = (displayName != null && !displayName.isBlank())
                ? "Bonjour " + displayName + ","
                : "Bonjour,";
        return hello + "\n\n"
                + "Vous avez demandé la réinitialisation de votre mot de passe MediLink Tunisia.\n"
                + "Ouvrez le lien ci-dessous pour choisir un nouveau mot de passe :\n\n"
                + resetLink + "\n\n"
                + "Ce lien est valable 60 minutes. Si vous n'êtes pas à l'origine de cette demande, "
                + "ignorez cet email ; votre mot de passe restera inchangé.\n\n"
                + "— L'équipe MediLink Tunisia";
    }

    /**
     * Email HTML habillé au thème de la plateforme (compatible clients mail :
     * tables, styles inline, dégradés avec repli couleur unie pour Outlook).
     */
    private String buildHtml(String displayName, String resetLink) {
        String hello = (displayName != null && !displayName.isBlank())
                ? "Bonjour " + escape(displayName) + ","
                : "Bonjour,";
        String safeLink = escape(resetLink);

        return """
                <!DOCTYPE html>
                <html lang="fr">
                <head>
                  <meta charset="utf-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1.0">
                  <meta name="color-scheme" content="light only">
                  <title>Réinitialisation de mot de passe</title>
                </head>
                <body style="margin:0; padding:0; background-color:#F0F4F8;">
                  <!-- Préheader masqué -->
                  <div style="display:none; max-height:0; overflow:hidden; opacity:0; color:#F0F4F8; font-size:1px; line-height:1px;">
                    Réinitialisez votre mot de passe MediLink Tunisia — lien valable 60 minutes.
                  </div>

                  <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="background-color:#F0F4F8;">
                    <tr>
                      <td align="center" style="padding:32px 16px;">

                        <table role="presentation" width="600" cellpadding="0" cellspacing="0" style="width:600px; max-width:600px; background-color:#ffffff; border-radius:18px; overflow:hidden; box-shadow:0 18px 44px rgba(15,44,76,0.14); font-family:'Poppins','Segoe UI',Helvetica,Arial,sans-serif;">

                          <!-- En-tête : dégradé bleu → cyan (repli #0066A2 pour Outlook) -->
                          <tr>
                            <td bgcolor="#0066A2" style="background-color:#0066A2; background-image:linear-gradient(135deg,#0066A2 0%%,#00A8B5 100%%); padding:38px 40px 34px;">
                              <table role="presentation" width="100%%" cellpadding="0" cellspacing="0">
                                <tr>
                                  <td style="vertical-align:middle;">
                                    <table role="presentation" cellpadding="0" cellspacing="0">
                                      <tr>
                                        <td style="vertical-align:middle;">
                                          <div style="width:46px; height:46px; border-radius:50%%; background-color:#ffffff; text-align:center; line-height:46px; font-size:26px; font-weight:700; color:#0066A2; font-family:'Poppins','Segoe UI',Helvetica,Arial,sans-serif;">+</div>
                                        </td>
                                        <td style="vertical-align:middle; padding-left:14px;">
                                          <div style="color:#ffffff; font-size:20px; font-weight:700; letter-spacing:0.2px;">MediLink&nbsp;Tunisia</div>
                                          <div style="color:rgba(255,255,255,0.82); font-size:12px; letter-spacing:1.5px; text-transform:uppercase; margin-top:2px;">Votre santé connectée</div>
                                        </td>
                                      </tr>
                                    </table>
                                  </td>
                                </tr>
                              </table>
                            </td>
                          </tr>

                          <!-- Filet doré (accent sable de la home) -->
                          <tr><td style="height:4px; background-color:#D4A843; font-size:0; line-height:0;">&nbsp;</td></tr>

                          <!-- Corps -->
                          <tr>
                            <td style="padding:40px 40px 8px;">
                              <h1 style="margin:0 0 6px; font-size:23px; font-weight:700; color:#1A2B3C;">Réinitialisation du mot de passe</h1>
                              <p style="margin:0 0 22px; font-size:15px; line-height:1.6; color:#5A6A7A;">%s</p>
                              <p style="margin:0 0 28px; font-size:15px; line-height:1.7; color:#1A2B3C;">
                                Vous avez demandé à réinitialiser votre mot de passe. Cliquez sur le bouton ci-dessous pour en choisir un nouveau en toute sécurité.
                              </p>

                              <!-- Bouton bulletproof -->
                              <table role="presentation" cellpadding="0" cellspacing="0" style="margin:0 auto 28px;">
                                <tr>
                                  <td align="center" bgcolor="#0066A2" style="border-radius:12px; background-color:#0066A2; background-image:linear-gradient(135deg,#0066A2 0%%,#00A8B5 100%%);">
                                    <a href="%s" target="_blank" style="display:inline-block; padding:15px 38px; font-family:'Poppins','Segoe UI',Helvetica,Arial,sans-serif; font-size:15px; font-weight:700; color:#ffffff; text-decoration:none; border-radius:12px;">
                                      Réinitialiser mon mot de passe
                                    </a>
                                  </td>
                                </tr>
                              </table>

                              <p style="margin:0 0 8px; font-size:13px; line-height:1.6; color:#5A6A7A;">
                                Si le bouton ne fonctionne pas, copiez-collez ce lien dans votre navigateur :
                              </p>
                              <p style="margin:0 0 26px; font-size:13px; line-height:1.6; word-break:break-all;">
                                <a href="%s" target="_blank" style="color:#00A8B5; text-decoration:none;">%s</a>
                              </p>

                              <!-- Encadré validité -->
                              <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="background-color:#F0F4F8; border-left:4px solid #D4A843; border-radius:8px;">
                                <tr>
                                  <td style="padding:14px 18px; font-size:13px; line-height:1.6; color:#5A6A7A;">
                                    ⏱ Ce lien est valable <strong style="color:#1A2B3C;">60 minutes</strong>. Passé ce délai, refaites une demande depuis la page de connexion.
                                  </td>
                                </tr>
                              </table>

                              <p style="margin:24px 0 0; font-size:13px; line-height:1.6; color:#5A6A7A;">
                                Vous n'êtes pas à l'origine de cette demande ? Ignorez cet email : votre mot de passe restera inchangé.
                              </p>
                            </td>
                          </tr>

                          <!-- Signature -->
                          <tr>
                            <td style="padding:24px 40px 36px;">
                              <p style="margin:0; font-size:15px; color:#1A2B3C;">Bien à vous,</p>
                              <p style="margin:2px 0 0; font-size:15px; font-weight:700; color:#0066A2;">L'équipe MediLink Tunisia</p>
                            </td>
                          </tr>

                          <!-- Pied -->
                          <tr>
                            <td style="background-color:#1A2B3C; padding:22px 40px;">
                              <p style="margin:0; font-size:12px; line-height:1.6; color:rgba(255,255,255,0.66); text-align:center;">
                                © 2026 MediLink Tunisia — Plateforme de santé connectée.<br>
                                Cet email a été envoyé automatiquement, merci de ne pas y répondre.
                              </p>
                            </td>
                          </tr>

                        </table>

                      </td>
                    </tr>
                  </table>
                </body>
                </html>
                """.formatted(hello, safeLink, safeLink, safeLink);
    }

    /** Échappe le strict minimum pour une insertion sûre dans le HTML. */
    private String escape(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    /**
     * Envoie l'email contenant le code OTP de validation du compte.
     */
    public void sendEmailVerificationOtp(String to, String displayName, String code) {
        if (!isMailConfigured()) {
            log.warn("""
                    [EMAIL DEV] SMTP non configuré (MAIL_PASSWORD vide).
                    Code OTP de vérification de compte pour {} :
                    {}""", to, code);
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
            helper.setFrom(from, "MediLink Tunisia");
            helper.setTo(to);
            helper.setSubject("MediLink Tunisia — Code de vérification de votre compte");
            helper.setText(buildOtpText(displayName, code), buildOtpHtml(displayName, code));
            mailSender.send(message);
            log.info("Email OTP envoyé à {}", to);
        } catch (Exception e) {
            log.error("Échec de l'envoi de l'email OTP à {} : {}. Code : {}", to, e.getMessage(), code);
        }
    }

    private String buildOtpText(String displayName, String code) {
        String hello = (displayName != null && !displayName.isBlank())
                ? "Bonjour " + displayName + ","
                : "Bonjour,";
        return hello + "\n\n"
                + "Pour valider la vérification de votre compte MediLink Tunisia, veuillez saisir le code de sécurité à 6 chiffres ci-dessous :\n\n"
                + code + "\n\n"
                + "Ce code est valable 10 minutes. Si vous n'êtes pas à l'origine de cette demande, ignorez cet email.\n\n"
                + "— L'équipe MediLink Tunisia";
    }

    private String buildOtpHtml(String displayName, String code) {
        String hello = (displayName != null && !displayName.isBlank())
                ? "Bonjour " + escape(displayName) + ","
                : "Bonjour,";
        String safeCode = escape(code);

        return """
                <!DOCTYPE html>
                <html lang="fr">
                <head>
                  <meta charset="utf-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1.0">
                  <meta name="color-scheme" content="light only">
                  <title>Code de vérification</title>
                </head>
                <body style="margin:0; padding:0; background-color:#F0F4F8;">
                  <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="background-color:#F0F4F8;">
                    <tr>
                      <td align="center" style="padding:32px 16px;">
                        <table role="presentation" width="600" cellpadding="0" cellspacing="0" style="width:600px; max-width:600px; background-color:#ffffff; border-radius:18px; overflow:hidden; box-shadow:0 18px 44px rgba(15,44,76,0.14); font-family:'Poppins','Segoe UI',Helvetica,Arial,sans-serif;">
                          <tr>
                            <td bgcolor="#0066A2" style="background-color:#0066A2; background-image:linear-gradient(135deg,#0066A2 0%%,#00A8B5 100%%); padding:38px 40px 34px;">
                              <table role="presentation" width="100%%" cellpadding="0" cellspacing="0">
                                <tr>
                                  <td style="vertical-align:middle;">
                                    <table role="presentation" cellpadding="0" cellspacing="0">
                                      <tr>
                                        <td style="vertical-align:middle;">
                                          <div style="width:46px; height:46px; border-radius:50%%; background-color:#ffffff; text-align:center; line-height:46px; font-size:26px; font-weight:700; color:#0066A2; font-family:'Poppins','Segoe UI',Helvetica,Arial,sans-serif;">+</div>
                                        </td>
                                        <td style="vertical-align:middle; padding-left:14px;">
                                          <div style="color:#ffffff; font-size:20px; font-weight:700; letter-spacing:0.2px;">MediLink&nbsp;Tunisia</div>
                                          <div style="color:rgba(255,255,255,0.82); font-size:12px; letter-spacing:1.5px; text-transform:uppercase; margin-top:2px;">Votre santé connectée</div>
                                        </td>
                                      </tr>
                                    </table>
                                  </td>
                                </tr>
                              </table>
                            </td>
                          </tr>
                          <tr><td style="height:4px; background-color:#D4A843; font-size:0; line-height:0;">&nbsp;</td></tr>
                          <tr>
                            <td style="padding:40px 40px 8px;">
                              <h1 style="margin:0 0 6px; font-size:23px; font-weight:700; color:#1A2B3C;">Vérification de votre compte</h1>
                              <p style="margin:0 0 22px; font-size:15px; line-height:1.6; color:#5A6A7A;">%s</p>
                              <p style="margin:0 0 28px; font-size:15px; line-height:1.7; color:#1A2B3C;">
                                Saisissez le code de validation OTP ci-dessous dans votre espace de connexion pour finaliser la vérification de votre adresse e-mail.
                              </p>
                              <div style="text-align:center; margin:30px 0;">
                                <div style="display:inline-block; letter-spacing:6px; font-family:'Courier New',Courier,monospace; font-size:36px; font-weight:bold; color:#0066A2; background-color:#F0F4F8; padding:15px 30px; border-radius:10px; border:1px solid #D4A843;">
                                  %s
                                </div>
                              </div>
                              <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="background-color:#F0F4F8; border-left:4px solid #D4A843; border-radius:8px; margin-top:20px;">
                                <tr>
                                  <td style="padding:14px 18px; font-size:13px; line-height:1.6; color:#5A6A7A;">
                                    ⏱ Ce code est valable <strong style="color:#1A2B3C;">10 minutes</strong>. Passé ce délai, veuillez effectuer une nouvelle demande.
                                  </td>
                                </tr>
                              </table>
                              <p style="margin:24px 0 0; font-size:13px; line-height:1.6; color:#5A6A7A;">
                                Vous n'êtes pas à l'origine de cette demande ? Ignorez simplement cet e-mail.
                              </p>
                            </td>
                          </tr>
                          <tr>
                            <td style="padding:24px 40px 36px;">
                              <p style="margin:0; font-size:15px; color:#1A2B3C;">Bien à vous,</p>
                              <p style="margin:2px 0 0; font-size:15px; font-weight:700; color:#0066A2;">L'équipe MediLink Tunisia</p>
                            </td>
                          </tr>
                          <tr>
                            <td style="background-color:#1A2B3C; padding:22px 40px;">
                              <p style="margin:0; font-size:12px; line-height:1.6; color:rgba(255,255,255,0.66); text-align:center;">
                                © 2026 MediLink Tunisia — Plateforme de santé connectée.<br>
                                Cet e-mail a été envoyé automatiquement, merci de ne pas y répondre.
                              </p>
                            </td>
                          </tr>
                        </table>
                      </td>
                    </tr>
                  </table>
                </body>
                </html>
                """.formatted(hello, safeCode);
    }
}
