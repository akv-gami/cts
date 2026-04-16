package com.cts.cts.service;

import com.cts.cts.dto.LlcResponseDto;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;

@Service
public class PdfService {

    public byte[] generateOperatingAgreement(LlcResponseDto llc) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter.getInstance(document, out);
        document.open();

        Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD);
        Font textFont = new Font(Font.HELVETICA, 12, Font.NORMAL);

        Paragraph title = new Paragraph("OPERATING AGREEMENT OF " + llc.businessName().toUpperCase(), titleFont);
        title.setAlignment(Paragraph.ALIGN_CENTER);
        document.add(title);
        document.add(new Paragraph("\n\n"));

        String body = String.format(
            "This Operating Agreement is entered into as of %s, by %s (the \"Member\").\n\n" +
            "1. FORMATION.\nThe Member has formed a Limited Liability Company named %s under the laws of the State of %s.\n\n" +
            "2. PURPOSE.\nThe Company may engage in any lawful business permitted by the laws of %s.\n\n" +
            "3. MANAGEMENT.\nThe Company shall be managed by the Member. The Member's official email is %s and foreign identification (RUT) is %s.\n\n" +
            "4. TAX COMPLIANCE.\nThe Member acknowledges that the Annual Report is due on or before %s. Furthermore, form 5472 compliance is marked as: %s.",
            llc.creationDate(), 
            llc.ownerName(), 
            llc.businessName(), 
            llc.stateOfFormation(),
            llc.stateOfFormation(), 
            llc.ownerEmail(), 
            llc.ownerRut(), 
            llc.annualReportDueDate(),
            llc.requiresForm5472() ? "REQUIRED" : "EXEMPT"
        );

        Paragraph content = new Paragraph(body, textFont);
        document.add(content);
        document.close();

        return out.toByteArray();
    }
}