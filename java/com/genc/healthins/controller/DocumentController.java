package com.genc.healthins.controller;

import com.genc.healthins.model.UserPolicy;
import com.genc.healthins.repository.UserPolicyRepository;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import com.genc.healthins.model.Payment;
import com.genc.healthins.repository.PaymentRepository;

@Controller
public class DocumentController {

    private final UserPolicyRepository userPolicyRepository;
    private final PaymentRepository paymentRepository;

    public DocumentController(UserPolicyRepository userPolicyRepository, PaymentRepository paymentRepository) {
        this.userPolicyRepository = userPolicyRepository;
        this.paymentRepository = paymentRepository;
    }

    // ==========================================
    // 1. DOWNLOAD POLICY DOCUMENT
    // ==========================================
    @GetMapping("/user/download-policy")
    public void downloadPolicy(@RequestParam("id") Long policyId, HttpServletResponse response) {
        try {
            UserPolicy policy = userPolicyRepository.findById(policyId).orElse(null);
            if (policy == null) return;

            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=Policy_" + policy.getPolicyNumber() + ".pdf");

            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, response.getOutputStream());
            document.open();

            // Styling
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20);
            Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            Font valueFont = FontFactory.getFont(FontFactory.HELVETICA, 12);

            // Header
            Paragraph title = new Paragraph("CERTIFICATE OF INSURANCE", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph("\n\n"));

            // Content
            addPair(document, "Policy Number:", policy.getPolicyNumber(), labelFont, valueFont);
            addPair(document, "Insured Name:", policy.getUser().getUsername(), labelFont, valueFont);
            addPair(document, "Plan Type:", policy.getCoverageType(), labelFont, valueFont);
            addPair(document, "Coverage Amount:", "$" + policy.getCoverageAmount(), labelFont, valueFont);
            addPair(document, "Premium:", "$" + policy.getPremiumAmount() + "/month", labelFont, valueFont);
            addPair(document, "Valid From:", policy.getStartDate().toLocalDate().toString(), labelFont, valueFont);
            addPair(document, "Valid Until:", policy.getEndDate().toLocalDate().toString(), labelFont, valueFont);

            // Footer
            document.add(new Paragraph("\n------------------------------------------------------------\n"));
            document.add(new Paragraph("This document certifies that the policy holder named above is insured under the HIMS Master Policy.", FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10)));

            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ==========================================
    // 2. DOWNLOAD TERMS & CONDITIONS
    // ==========================================
    @GetMapping("/user/download-terms")
    public void downloadTerms(HttpServletResponse response) {
        try {
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=Terms_Conditions.pdf");

            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, response.getOutputStream());
            document.open();

            document.add(new Paragraph("TERMS AND CONDITIONS", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18)));
            document.add(new Paragraph("\n"));
            
            String[] terms = {
                "1. PREAMBLE: This policy is a contract between the Insurer and the Policyholder.",
                "2. COVERAGE: The company agrees to pay for medical expenses incurred due to illness or injury.",
                "3. EXCLUSIONS: Pre-existing conditions are not covered for the first 24 months.",
                "4. CLAIMS: Written notice of claim must be given within 30 days of occurrence.",
                "5. CANCELLATION: This policy may be cancelled by either party with 30 days notice."
            };

            for (String term : terms) {
                document.add(new Paragraph(term));
                document.add(new Paragraph("\n"));
            }

            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ==========================================
    // 3. DOWNLOAD ALL (ZIP)
    // ==========================================
    @GetMapping("/user/download-all")
    public void downloadAll(@RequestParam("id") Long policyId, HttpServletResponse response) {
        try {
            UserPolicy policy = userPolicyRepository.findById(policyId).orElse(null);
            if (policy == null) return;

            response.setContentType("application/zip");
            response.setHeader("Content-Disposition", "attachment; filename=HIMS_Documents_Bundle.zip");

            try (ZipOutputStream zos = new ZipOutputStream(response.getOutputStream())) {
                
                // 1. Add Policy PDF
                byte[] policyBytes = generatePolicyBytes(policy);
                ZipEntry policyEntry = new ZipEntry("Policy_" + policy.getPolicyNumber() + ".pdf");
                policyEntry.setSize(policyBytes.length);
                zos.putNextEntry(policyEntry);
                zos.write(policyBytes);
                zos.closeEntry();

                // 2. Add Terms PDF
                byte[] termsBytes = generateTermsBytes();
                ZipEntry termsEntry = new ZipEntry("Terms_Conditions.pdf");
                termsEntry.setSize(termsBytes.length);
                zos.putNextEntry(termsEntry);
                zos.write(termsBytes);
                zos.closeEntry();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // --- Helper Methods for ZIP Generation ---
    private byte[] generatePolicyBytes(UserPolicy policy) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter.getInstance(document, out);
        document.open();
        document.add(new Paragraph("POLICY DOCUMENT: " + policy.getPolicyNumber()));
        document.add(new Paragraph("Holder: " + policy.getUser().getUsername()));
        document.close();
        return out.toByteArray();
    }

    private byte[] generateTermsBytes() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter.getInstance(document, out);
        document.open();
        document.add(new Paragraph("TERMS AND CONDITIONS..."));
        document.close();
        return out.toByteArray();
    }

    // Helper to format text neatly
    private void addPair(Document doc, String label, String value, Font labelFont, Font valueFont) throws DocumentException {
        Paragraph p = new Paragraph();
        p.add(new Chunk(label + " ", labelFont));
        p.add(new Chunk(value, valueFont));
        doc.add(p);
    }
    
 // ==========================================
    // 4. DOWNLOAD PAYMENT INVOICE
    // ==========================================
    @GetMapping("/user/download-invoice")
    public void downloadInvoice(@RequestParam("id") Long paymentId, HttpServletResponse response) {
        try {
            Payment payment = paymentRepository.findById(paymentId).orElse(null);
            if (payment == null) return;

            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=Invoice_" + payment.getId() + ".pdf");

            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, response.getOutputStream());
            document.open();

            // HEADER
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 24);
            Paragraph title = new Paragraph("PAYMENT RECEIPT", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph("\n\n"));

            // DETAILS TABLE
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            
            addTableRow(table, "Transaction ID:", "TXN-" + payment.getId());
            addTableRow(table, "Date:", payment.getPaymentDate().toLocalDate().toString());
            addTableRow(table, "Policy Type:", payment.getPolicy().getCoverageType());
            addTableRow(table, "Amount Paid:", "$" + payment.getPaymentAmount());
            addTableRow(table, "Status:", "COMPLETED");

            document.add(table);

            // FOOTER
            document.add(new Paragraph("\n\nThank you for your payment.", FontFactory.getFont(FontFactory.HELVETICA, 12)));
            document.add(new Paragraph("HIMS Accounts Department", FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10)));

            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Helper for table rows
    private void addTableRow(PdfPTable table, String label, String value) {
        PdfPCell cell1 = new PdfPCell(new Phrase(label, FontFactory.getFont(FontFactory.HELVETICA_BOLD)));
        cell1.setBorder(Rectangle.NO_BORDER);
        cell1.setPadding(5);
        
        PdfPCell cell2 = new PdfPCell(new Phrase(value, FontFactory.getFont(FontFactory.HELVETICA)));
        cell2.setBorder(Rectangle.NO_BORDER);
        cell2.setPadding(5);

        table.addCell(cell1);
        table.addCell(cell2);
    }
}