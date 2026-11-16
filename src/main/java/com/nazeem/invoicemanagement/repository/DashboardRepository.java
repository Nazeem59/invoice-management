
package com.nazeem.invoicemanagement.repository;

import com.nazeem.invoicemanagement.entity.Invoice;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DashboardRepository extends JpaRepository<Invoice, Long> {

    @Query(value = """
        SELECT
            COUNT(*) AS total_invoices,
            COALESCE(SUM(
                CASE WHEN status <> 'CANCELLED'
                THEN total_amount ELSE 0 END
            ), 0) AS total_amount,
            SUM(status = 'DRAFT') AS draft_count,
            SUM(status = 'SUBMITTED') AS submitted_count,
            SUM(status = 'UNDER_REVIEW') AS under_review_count,
            SUM(status = 'APPROVED') AS approved_count,
            SUM(status = 'REJECTED') AS rejected_count,
            SUM(status = 'PAID') AS paid_count,
            SUM(status = 'CANCELLED') AS cancelled_count
        FROM invoices
        """, nativeQuery = true)
    List<Object[]> getDashboardSummary();

    @Query(value = """
        SELECT
            i.id,
            i.invoice_number,
            s.supplier_name,
            i.invoice_date,
            i.due_date,
            i.currency,
            i.total_amount,
            i.status
        FROM invoices i
        JOIN suppliers s ON s.id = i.supplier_id
        WHERE i.status NOT IN ('PAID', 'CANCELLED')
        ORDER BY i.due_date ASC
        """, nativeQuery = true)
    List<Object[]> getOutstandingInvoices(Pageable pageable);

    @Query(value = """
        SELECT
            COALESCE(SUM(decision = 'PENDING'), 0),
            COALESCE(SUM(decision = 'APPROVED'), 0),
            COALESCE(SUM(decision = 'REJECTED'), 0)
        FROM invoice_approvals
        """, nativeQuery = true)
    List<Object[]> getApprovalStatistics();

    @Query(value = """
        SELECT
            DATE_FORMAT(invoice_date, '%Y-%m') AS month,
            COALESCE(SUM(total_amount), 0) AS total_amount,
            COUNT(*) AS invoice_count
        FROM invoices
        WHERE status <> 'CANCELLED'
          AND invoice_date >= DATE_SUB(
              DATE_FORMAT(CURDATE(), '%Y-%m-01'),
              INTERVAL 11 MONTH
          )
        GROUP BY DATE_FORMAT(invoice_date, '%Y-%m')
        ORDER BY month ASC
        """, nativeQuery = true)
    List<Object[]> getMonthlySpending();

    @Query(value = """
        SELECT
            s.id,
            s.supplier_name,
            COUNT(i.id) AS invoice_count,
            COALESCE(SUM(i.total_amount), 0) AS total_amount
        FROM suppliers s
        JOIN invoices i ON i.supplier_id = s.id
        WHERE i.status <> 'CANCELLED'
        GROUP BY s.id, s.supplier_name
        ORDER BY total_amount DESC
        """, nativeQuery = true)
    List<Object[]> getSupplierReport(Pageable pageable);
}
