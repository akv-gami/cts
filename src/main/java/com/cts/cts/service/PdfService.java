package com.cts.cts.service;

import com.cts.cts.dto.LlcResponseDto;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.lowagie.text.pdf.draw.LineSeparator;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
public class PdfService {

    private static final Color COLOR_DARK      = new Color(17, 24, 39);
    private static final Color COLOR_ACCENT    = new Color(79, 110, 247);
    private static final Color COLOR_LIGHT_BG  = new Color(248, 250, 252);
    private static final Color COLOR_BORDER    = new Color(226, 232, 240);
    private static final Color COLOR_SUBTEXT   = new Color(100, 116, 139);
    private static final Color COLOR_SUCCESS   = new Color(34, 197, 94);
    private static final Color COLOR_WARNING   = new Color(245, 158, 11);

    private static final Font FONT_TITLE           = new Font(Font.HELVETICA, 22, Font.BOLD,   new Color(255, 255, 255));
    private static final Font FONT_SUBTITLE        = new Font(Font.HELVETICA, 11, Font.NORMAL, new Color(199, 210, 254));
    private static final Font FONT_SECTION         = new Font(Font.HELVETICA, 11, Font.BOLD,   COLOR_ACCENT);
    private static final Font FONT_BODY            = new Font(Font.HELVETICA, 10, Font.NORMAL, COLOR_DARK);
    private static final Font FONT_BODY_BOLD       = new Font(Font.HELVETICA, 10, Font.BOLD,   COLOR_DARK);
    private static final Font FONT_SMALL           = new Font(Font.HELVETICA,  9, Font.NORMAL, COLOR_SUBTEXT);
    private static final Font FONT_TABLE_HEAD      = new Font(Font.HELVETICA,  9, Font.BOLD,   new Color(255, 255, 255));
    private static final Font FONT_TABLE_CELL      = new Font(Font.HELVETICA,  9, Font.NORMAL, COLOR_DARK);
    private static final Font FONT_TABLE_CELL_BOLD = new Font(Font.HELVETICA,  9, Font.BOLD,   COLOR_DARK);
    private static final Font FONT_LABEL           = new Font(Font.HELVETICA,  8, Font.BOLD,   COLOR_SUBTEXT);
    private static final Font FONT_VALUE           = new Font(Font.HELVETICA, 10, Font.NORMAL, COLOR_DARK);

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", new Locale("es", "ES"));

    public byte[] generateOperatingAgreement(LlcResponseDto llc) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 50, 50, 40, 50);

        try {
            PdfWriter writer = PdfWriter.getInstance(document, out);
            writer.setPageEvent(new HeaderFooterEvent(llc.businessName()));
            document.open();

            addHeader(document, llc);
            document.add(Chunk.NEWLINE);
            addInfoGrid(document, llc);
            document.add(Chunk.NEWLINE);
            addComplianceBox(document, llc);
            document.add(Chunk.NEWLINE);
            addLegalSection(document, "1. FORMATION",
                "This Operating Agreement (the \"Agreement\") is entered into as of " +
                formatDate(llc) + " by " + llc.ownerName() + " (the \"Member\"), a foreign national " +
                "holding Chilean tax identification number (RUT) " + llc.ownerRut() + ".");
            document.add(Chunk.NEWLINE);
            addLegalSection(document, "2. NAME AND PRINCIPAL OFFICE",
                "The name of the limited liability company is " + llc.businessName() +
                " (the \"Company\"). The Company is organized under the laws of the State of " +
                llc.stateOfFormation() + ", United States of America. The principal office of " +
                "the Company may be established at any place or places inside or outside the " +
                "State of " + llc.stateOfFormation() + " as the Member may from time to time determine.");
            document.add(Chunk.NEWLINE);
            addLegalSection(document, "3. PURPOSE",
                "The purpose of the Company is to engage in any lawful act or activity for " +
                "which limited liability companies may be organized under the laws of the State " +
                "of " + llc.stateOfFormation() + " and to engage in any and all activities " +
                "necessary, convenient, desirable, or incidental to the foregoing.");
            document.add(Chunk.NEWLINE);
            addLegalSection(document, "4. TERM",
                "The Company commenced upon the filing of its Certificate of Formation with " +
                "the Secretary of State of " + llc.stateOfFormation() + " and shall continue " +
                "perpetually, unless sooner dissolved and its affairs wound up in accordance with " +
                "applicable law or this Agreement.");
            document.add(Chunk.NEWLINE);
            addLegalSection(document, "5. MANAGEMENT",
                "The Company shall be managed solely by the Member. The Member shall have " +
                "full, exclusive, and complete authority, power, and discretion to manage and " +
                "control the business, property, and affairs of the Company, to make all " +
                "decisions regarding those matters, and to perform any and all other acts or " +
                "activities customary or incident to the management of the Company's business.");
            document.add(Chunk.NEWLINE);
            addLegalSection(document, "6. TAX STATUS",
                "The Company is intended to be treated as a disregarded entity for United States " +
                "federal income tax purposes. As the sole Member is a foreign person, the Company " +
                "acknowledges its obligations under the Internal Revenue Code, including but not " +
                "limited to the filing requirements applicable to foreign-owned domestic disregarded " +
                "entities. The Member's obligation to file Form 5472 with the Internal Revenue " +
                "Service is indicated as: " + (llc.requiresForm5472() ? "REQUIRED." : "EXEMPT."));
            document.add(Chunk.NEWLINE);
            addLegalSection(document, "7. ANNUAL REPORTING",
                "The Company shall comply with all annual reporting requirements of the State of " +
                llc.stateOfFormation() + ". The Annual Report for the current period is due on or " +
                "before " + (llc.annualReportDueDate() != null
                        ? llc.annualReportDueDate().format(DATE_FORMAT)
                        : "the applicable statutory deadline") +
                ". Failure to timely file the Annual Report may result in administrative dissolution " +
                "of the Company by the Secretary of State.");
            document.add(Chunk.NEWLINE);
            addLegalSection(document, "8. LIMITATION OF LIABILITY",
                "The debts, obligations, and liabilities of the Company, whether arising in " +
                "contract, tort, or otherwise, shall be solely the debts, obligations, and " +
                "liabilities of the Company. The Member shall not be obligated personally for any " +
                "such debt, obligation, or liability of the Company solely by reason of being a " +
                "member of the Company.");
            document.add(Chunk.NEWLINE);
            addSignatureBlock(document, llc);

        } catch (Exception e) {
            throw new RuntimeException("Error al generar el PDF", e);
        } finally {
            document.close();
        }

        return out.toByteArray();
    }

    private void addHeader(Document document, LlcResponseDto llc) throws DocumentException {
        PdfPTable header = new PdfPTable(1);
        header.setWidthPercentage(100);

        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(COLOR_DARK);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(28);

        Paragraph companyName = new Paragraph(llc.businessName().toUpperCase(), FONT_TITLE);
        companyName.setAlignment(Element.ALIGN_CENTER);
        cell.addElement(companyName);

        Paragraph docType = new Paragraph("OPERATING AGREEMENT — SINGLE MEMBER LLC", FONT_SUBTITLE);
        docType.setAlignment(Element.ALIGN_CENTER);
        docType.setSpacingBefore(6);
        cell.addElement(docType);

        Paragraph state = new Paragraph("State of " + llc.stateOfFormation() + "  ·  United States of America", FONT_SUBTITLE);
        state.setAlignment(Element.ALIGN_CENTER);
        state.setSpacingBefore(4);
        cell.addElement(state);

        header.addCell(cell);
        document.add(header);
    }

    private void addInfoGrid(Document document, LlcResponseDto llc) throws DocumentException {
        Paragraph sectionLabel = new Paragraph("INFORMACIÓN DE LA ENTIDAD", FONT_SECTION);
        sectionLabel.setSpacingBefore(4);
        sectionLabel.setSpacingAfter(10);
        document.add(sectionLabel);

        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1f, 1f, 1f});
        table.setSpacingBefore(2);

        addInfoCell(table, "NOMBRE DE LA LLC", llc.businessName());
        addInfoCell(table, "ESTADO DE FORMACIÓN", llc.stateOfFormation());
        addInfoCell(table, "FECHA DE CONSTITUCIÓN",
                llc.creationDate() != null ? llc.creationDate().format(DATE_FORMAT) : "—");

        addInfoCell(table, "TITULAR / MEMBER", llc.ownerName());
        addInfoCell(table, "RUT (CHILE)", llc.ownerRut());
        addInfoCell(table, "CORREO ELECTRÓNICO", llc.ownerEmail());

        addInfoCell(table, "EIN (EMPLOYER ID)",
                (llc.ein() != null && !llc.ein().isBlank()) ? llc.ein() : "Pendiente de asignación");
        addInfoCell(table, "ESTADO DE TRÁMITE",
                llc.status() != null ? llc.status() : "—");
        addInfoCell(table, "ANNUAL REPORT VENCE",
                llc.annualReportDueDate() != null ? llc.annualReportDueDate().format(DATE_FORMAT) : "—");

        document.add(table);
    }

    private void addInfoCell(PdfPTable table, String label, String value) {
        PdfPCell cell = new PdfPCell();
        cell.setBorderColor(COLOR_BORDER);
        cell.setBorderWidth(0.5f);
        cell.setBackgroundColor(COLOR_LIGHT_BG);
        cell.setPadding(10);

        Paragraph labelP = new Paragraph(label, FONT_LABEL);
        labelP.setSpacingAfter(3);
        cell.addElement(labelP);

        cell.addElement(new Paragraph(value != null ? value : "—", FONT_VALUE));
        table.addCell(cell);
    }

    private void addComplianceBox(Document document, LlcResponseDto llc) throws DocumentException {
        Paragraph sectionLabel = new Paragraph("RESUMEN DE OBLIGACIONES TRIBUTARIAS (IRS)", FONT_SECTION);
        sectionLabel.setSpacingBefore(4);
        sectionLabel.setSpacingAfter(10);
        document.add(sectionLabel);

        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1.2f, 1f, 1f});

        PdfPCell hObligation = new PdfPCell(new Phrase("OBLIGACIÓN", FONT_TABLE_HEAD));
        PdfPCell hStatus     = new PdfPCell(new Phrase("ESTADO",     FONT_TABLE_HEAD));
        PdfPCell hDetail     = new PdfPCell(new Phrase("DETALLE",    FONT_TABLE_HEAD));

        for (PdfPCell h : new PdfPCell[]{hObligation, hStatus, hDetail}) {
            h.setBackgroundColor(COLOR_DARK);
            h.setBorder(Rectangle.NO_BORDER);
            h.setPadding(9);
        }
        table.addCell(hObligation);
        table.addCell(hStatus);
        table.addCell(hDetail);

        addComplianceRow(table,
                "Form 5472 (IRS)",
                llc.requiresForm5472() ? "REQUERIDO" : "EXENTO",
                llc.requiresForm5472(),
                llc.requiresForm5472()
                        ? "LLC de propiedad extranjera sujeta a presentación anual."
                        : "No aplica para la estructura de esta LLC.");

        addComplianceRow(table,
                "Annual Report (" + llc.stateOfFormation() + ")",
                "PENDIENTE",
                null,
                llc.annualReportDueDate() != null
                        ? "Vence el " + llc.annualReportDueDate().format(DATE_FORMAT)
                        : "Consultar con el estado.");

        addComplianceRow(table,
                "EIN — Employer ID Number",
                (llc.ein() != null && !llc.ein().isBlank()) ? "ASIGNADO" : "PENDIENTE",
                (llc.ein() != null && !llc.ein().isBlank()) ? false : null,
                (llc.ein() != null && !llc.ein().isBlank())
                        ? "EIN: " + llc.ein()
                        : "En proceso de obtención ante el IRS.");

        document.add(table);
    }

    private void addComplianceRow(PdfPTable table, String obligation, String status, Boolean isAlert, String detail) {
        PdfPCell cObligation = new PdfPCell(new Phrase(obligation, FONT_TABLE_CELL_BOLD));
        cObligation.setPadding(9);
        cObligation.setBorderColor(COLOR_BORDER);
        cObligation.setBorderWidth(0.5f);
        table.addCell(cObligation);

        PdfPCell cStatus = new PdfPCell();
        cStatus.setPadding(9);
        cStatus.setBorderColor(COLOR_BORDER);
        cStatus.setBorderWidth(0.5f);
        Color badgeColor = isAlert == null ? COLOR_ACCENT : (isAlert ? COLOR_WARNING : COLOR_SUCCESS);
        cStatus.addElement(new Paragraph(status, new Font(Font.HELVETICA, 8, Font.BOLD, badgeColor)));
        table.addCell(cStatus);

        PdfPCell cDetail = new PdfPCell(new Phrase(detail, FONT_TABLE_CELL));
        cDetail.setPadding(9);
        cDetail.setBorderColor(COLOR_BORDER);
        cDetail.setBorderWidth(0.5f);
        table.addCell(cDetail);
    }

    private void addLegalSection(Document document, String title, String body) throws DocumentException {
        Paragraph titleP = new Paragraph(title, FONT_BODY_BOLD);
        titleP.setSpacingBefore(2);
        titleP.setSpacingAfter(4);
        document.add(titleP);

        Paragraph bodyP = new Paragraph(body, FONT_BODY);
        bodyP.setLeading(15);
        bodyP.setAlignment(Element.ALIGN_JUSTIFIED);
        document.add(bodyP);

        document.add(new Chunk(new LineSeparator(0.3f, 100, COLOR_BORDER, Element.ALIGN_CENTER, -5)));
    }

    private void addSignatureBlock(Document document, LlcResponseDto llc) throws DocumentException {
        document.add(Chunk.NEWLINE);

        Paragraph sectionLabel = new Paragraph("FIRMA Y ACEPTACIÓN", FONT_SECTION);
        sectionLabel.setSpacingAfter(10);
        document.add(sectionLabel);

        Paragraph intro = new Paragraph(
                "El Miembro Único ha ejecutado este Acuerdo Operativo a partir de la fecha indicada, " +
                "demostrando su consentimiento y aceptación de todos los términos aquí establecidos.",
                FONT_BODY);
        intro.setAlignment(Element.ALIGN_JUSTIFIED);
        intro.setSpacingAfter(24);
        document.add(intro);

        PdfPTable sigTable = new PdfPTable(2);
        sigTable.setWidthPercentage(100);
        sigTable.setWidths(new float[]{1f, 1f});

        LineSeparator sigLine = new LineSeparator(0.5f, 100, COLOR_DARK, Element.ALIGN_LEFT, 0);

        PdfPCell leftCell = new PdfPCell();
        leftCell.setBorder(Rectangle.NO_BORDER);
        leftCell.setPaddingRight(20);
        leftCell.addElement(new Chunk(sigLine));

        Paragraph sigName = new Paragraph(llc.ownerName(), FONT_BODY_BOLD);
        sigName.setSpacingBefore(6);
        leftCell.addElement(sigName);
        leftCell.addElement(new Paragraph("Sole Member", FONT_SMALL));
        leftCell.addElement(new Paragraph("RUT: " + llc.ownerRut(), FONT_SMALL));
        leftCell.addElement(new Paragraph(llc.ownerEmail(), FONT_SMALL));

        PdfPCell rightCell = new PdfPCell();
        rightCell.setBorder(Rectangle.NO_BORDER);
        rightCell.setPaddingLeft(20);
        rightCell.addElement(new Chunk(sigLine));

        Paragraph dateLabel = new Paragraph("Fecha", FONT_BODY_BOLD);
        dateLabel.setSpacingBefore(6);
        rightCell.addElement(dateLabel);
        rightCell.addElement(new Paragraph(
                llc.creationDate() != null ? llc.creationDate().format(DATE_FORMAT) : "—", FONT_SMALL));
        rightCell.addElement(new Paragraph("State of " + llc.stateOfFormation(), FONT_SMALL));

        sigTable.addCell(leftCell);
        sigTable.addCell(rightCell);
        document.add(sigTable);
    }

    private String formatDate(LlcResponseDto llc) {
        if (llc.creationDate() == null) return "the date first written above";
        return llc.creationDate().format(DATE_FORMAT);
    }

    private static class HeaderFooterEvent extends PdfPageEventHelper {

        private final String companyName;

        HeaderFooterEvent(String companyName) {
            this.companyName = companyName;
        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte cb = writer.getDirectContent();
            int pageNumber = writer.getPageNumber();
            int pageWidth  = (int) document.getPageSize().getWidth();
            int pageHeight = (int) document.getPageSize().getHeight();

            try {
                BaseFont baseFont = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, false);
                cb.setColorFill(COLOR_SUBTEXT);
                cb.setFontAndSize(baseFont, 8);

                cb.beginText();
                cb.showTextAligned(PdfContentByte.ALIGN_LEFT,  companyName + " — Operating Agreement", 50, 30, 0);
                cb.showTextAligned(PdfContentByte.ALIGN_RIGHT, "Página " + pageNumber, pageWidth - 50, 30, 0);
                cb.endText();

                cb.setLineWidth(0.3f);
                cb.setColorStroke(COLOR_BORDER);
                cb.moveTo(50, 42);
                cb.lineTo(pageWidth - 50, 42);
                cb.stroke();

                cb.setColorFill(COLOR_ACCENT);
                cb.rectangle(50, pageHeight - 3, pageWidth - 100, 3);
                cb.fill();

            } catch (Exception ignored) {
            }
        }
    }
}