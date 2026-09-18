package com.ktsa.foosball.controller;

import com.ktsa.foosball.dto.ApiResponse;
import com.ktsa.foosball.dto.ContactRequestDto;
import com.ktsa.foosball.service.EmailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contact")
@RequiredArgsConstructor
public class ContactController {

    private final EmailService emailService;

    /** The KTSA inbox that receives all contact form submissions */
    @Value("${ktsa.contact.recipient-email:contact@ktsaofficial.in}")
    private String recipientEmail;

    /**
     * POST /api/contact
     * Accepts a contact form submission and emails it to the KTSA team.
     * Public endpoint — no auth required.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<?>> submitContact(
            @RequestBody @Valid ContactRequestDto dto) {

        String subject = dto.getSubject() != null && !dto.getSubject().isBlank()
                ? "KTSA Contact: " + dto.getSubject()
                : "KTSA Contact Form Submission";

        String body = buildEmailBody(dto);

        emailService.sendEmail(recipientEmail, subject, body);

        // Also send a confirmation to the person who submitted
        String confirmationBody = buildConfirmationBody(dto);
        emailService.sendEmail(dto.getEmail(), "We received your message — KTSA", confirmationBody);

        return ResponseEntity.ok(
                ApiResponse.success(200, "Your message has been sent successfully!", null)
        );
    }

    // ── HTML email builders ────────────────────────────────────────────────

    private String buildEmailBody(ContactRequestDto dto) {
        return """
                <html><body style="font-family:sans-serif;color:#333;max-width:600px;margin:auto">
                  <h2 style="color:#00e5ff">New Contact Form Submission</h2>
                  <table style="width:100%;border-collapse:collapse">
                    <tr><td style="padding:8px;font-weight:bold;width:120px">Name:</td>
                        <td style="padding:8px">%s</td></tr>
                    <tr style="background:#f9f9f9">
                        <td style="padding:8px;font-weight:bold">Email:</td>
                        <td style="padding:8px"><a href="mailto:%s">%s</a></td></tr>
                    <tr><td style="padding:8px;font-weight:bold">Phone:</td>
                        <td style="padding:8px">%s</td></tr>
                    <tr style="background:#f9f9f9">
                        <td style="padding:8px;font-weight:bold">Subject:</td>
                        <td style="padding:8px">%s</td></tr>
                    <tr><td style="padding:8px;font-weight:bold;vertical-align:top">Message:</td>
                        <td style="padding:8px;white-space:pre-wrap">%s</td></tr>
                  </table>
                </body></html>
                """.formatted(
                escape(dto.getName()),
                escape(dto.getEmail()), escape(dto.getEmail()),
                dto.getPhone() != null ? escape(dto.getPhone()) : "—",
                dto.getSubject() != null ? escape(dto.getSubject()) : "General Enquiry",
                escape(dto.getMessage())
        );
    }

    private String buildConfirmationBody(ContactRequestDto dto) {
        return """
                <html><body style="font-family:sans-serif;color:#333;max-width:600px;margin:auto">
                  <h2 style="color:#00e5ff">Thanks for reaching out, %s!</h2>
                  <p>We've received your message and will get back to you shortly.</p>
                  <hr style="border:none;border-top:1px solid #eee;margin:20px 0"/>
                  <p style="font-size:13px;color:#666">
                    <strong>Your message:</strong><br/>
                    <span style="white-space:pre-wrap">%s</span>
                  </p>
                  <hr style="border:none;border-top:1px solid #eee;margin:20px 0"/>
                  <p style="font-size:12px;color:#999">
                    Karnataka Table Soccer Association<br/>
                    <a href="https://ktsaofficial.in" style="color:#00e5ff">ktsaofficial.in</a>
                  </p>
                </body></html>
                """.formatted(escape(dto.getName()), escape(dto.getMessage()));
    }

    /** Minimal HTML escaping to prevent XSS in email body */
    private String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
