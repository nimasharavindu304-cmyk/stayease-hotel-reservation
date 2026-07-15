package com.stayease.report;

public class BookingInvoiceReportGenerator extends JasperReportGenerator {

    @Override
    protected String getTemplatePath() {
        return "/reports/BookingInvoiceReport.jrxml";
    }
}
