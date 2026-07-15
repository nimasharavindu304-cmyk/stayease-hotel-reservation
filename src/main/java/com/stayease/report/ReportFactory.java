package com.stayease.report;

/**
 * Factory pattern (Design Pattern #4).
 * <p>
 * Callers ask for a {@link ReportType} and get back a ready-to-use
 * {@link JasperReportGenerator} without needing to know which .jrxml
 * template or subclass backs it. Adding a new report later only means
 * adding an enum value + a case here, not touching the UI code.
 */
public final class ReportFactory {

    private ReportFactory() {
    }

    public static JasperReportGenerator getGenerator(ReportType type) {
        switch (type) {
            case BOOKING_INVOICE:
                return new BookingInvoiceReportGenerator();
            case REVENUE_SUMMARY:
                return new RevenueSummaryReportGenerator();
            default:
                throw new IllegalArgumentException("Unsupported report type: " + type);
        }
    }
}
