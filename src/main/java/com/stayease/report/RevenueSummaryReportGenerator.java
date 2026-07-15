package com.stayease.report;

public class RevenueSummaryReportGenerator extends JasperReportGenerator {

    @Override
    protected String getTemplatePath() {
        return "/reports/RevenueSummaryReport.jrxml";
    }
}
