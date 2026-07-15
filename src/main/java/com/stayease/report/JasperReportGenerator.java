package com.stayease.report;

import com.stayease.db.DBConnection;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.view.JasperViewer;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * Compiles a .jrxml template (found on the classpath under /reports) and
 * fills it directly against the application's live database connection.
 * Concrete subclasses only need to say which template file to use.
 */
public abstract class JasperReportGenerator {

    /** Classpath location of the .jrxml source, e.g. "/reports/BookingInvoiceReport.jrxml". */
    protected abstract String getTemplatePath();

    private JasperPrint fill(Map<String, Object> parameters) throws JRException {
        try (InputStream template = getClass().getResourceAsStream(getTemplatePath())) {
            if (template == null) {
                throw new JRException("Report template not found on classpath: " + getTemplatePath());
            }
            JasperReport report = JasperCompileManager.compileReport(template);
            return JasperFillManager.fillReport(report, parameters, DBConnection.getInstance().getConnection());
        } catch (IOException e) {
            throw new JRException("Failed to read report template: " + getTemplatePath(), e);
        }
    }

    /** Exports the filled report to a PDF file at the given path. */
    public void generateToPdf(Map<String, Object> parameters, String outputPath) throws JRException {
        JasperPrint print = fill(parameters);
        JasperExportManager.exportReportToPdfFile(print, outputPath);
    }

    /** Opens the standard JasperReports on-screen preview window. */
    public void generateAndShow(Map<String, Object> parameters) throws JRException {
        JasperPrint print = fill(parameters);
        JasperViewer.viewReport(print, false);
    }
}
