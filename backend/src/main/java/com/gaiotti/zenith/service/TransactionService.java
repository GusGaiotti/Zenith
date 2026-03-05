package com.gaiotti.zenith.service;

import com.gaiotti.zenith.dto.request.CreateTransactionRequest;
import com.gaiotti.zenith.dto.request.UpdateTransactionRequest;
import com.gaiotti.zenith.dto.response.PageResponse;
import com.gaiotti.zenith.dto.response.TransactionResponse;
import com.gaiotti.zenith.exception.AccessDeniedException;
import com.gaiotti.zenith.exception.ResourceNotFoundException;
import com.gaiotti.zenith.model.Category;
import com.gaiotti.zenith.model.Ledger;
import com.gaiotti.zenith.model.Transaction;
import com.gaiotti.zenith.model.User;
import com.gaiotti.zenith.repository.CategoryRepository;
import com.gaiotti.zenith.repository.LedgerMemberRepository;
import com.gaiotti.zenith.repository.LedgerRepository;
import com.gaiotti.zenith.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TransactionService {

    private static final int EXPORT_ROW_LIMIT = 10_000;
    private static final DateTimeFormatter EXPORT_DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter EXPORT_DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final TransactionRepository transactionRepository;
    private final LedgerRepository ledgerRepository;
    private final LedgerMemberRepository ledgerMemberRepository;
    private final CategoryRepository categoryRepository;
    private final NotificationService notificationService;

    @Transactional(readOnly = true)
    public PageResponse<TransactionResponse> listTransactions(
            Long ledgerId,
            User authenticatedUser,
            LocalDate startDate,
            LocalDate endDate,
            Long categoryId,
            Long createdByUserId,
            Transaction.TransactionType type,
            Pageable pageable
    ) {
        assertLedgerExists(ledgerId);
        assertMembership(ledgerId, authenticatedUser);

        var spec = TransactionSpecification.withFilters(
                ledgerId, startDate, endDate, categoryId, createdByUserId, type
        );

        Page<Transaction> page = transactionRepository.findAll(spec, pageable);

        List<TransactionResponse> content = page.getContent().stream()
                .map(this::mapToResponse)
                .toList();

        return new PageResponse<>(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getMonthlyTransactions(Long ledgerId, YearMonth yearMonth, User authenticatedUser) {
        assertLedgerExists(ledgerId);
        assertMembership(ledgerId, authenticatedUser);

        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        var spec = TransactionSpecification.withFilters(
                ledgerId, startDate, endDate, null, null, null
        );

        return transactionRepository.findAll(spec).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public TransactionResponse createTransaction(Long ledgerId, CreateTransactionRequest request, User authenticatedUser) {
        Ledger ledger = ledgerRepository.findById(ledgerId)
                .orElseThrow(() -> new ResourceNotFoundException("Ledger not found"));
        assertMembership(ledgerId, authenticatedUser);

        Category category = resolveCategory(request.getCategoryId(), ledgerId);

        Transaction transaction = Transaction.builder()
                .ledger(ledger)
                .amount(request.getAmount())
                .type(request.getType())
                .date(request.getDate())
                .category(category)
                .description(request.getDescription())
                .createdBy(authenticatedUser)
                .build();

        Transaction savedTransaction = transactionRepository.save(transaction);
        notificationService.createTransactionNotifications(savedTransaction, authenticatedUser);
        return mapToResponse(savedTransaction);
    }

    @Transactional(readOnly = true)
    public TransactionResponse getTransaction(Long ledgerId, Long transactionId, User authenticatedUser) {
        assertLedgerExists(ledgerId);
        assertMembership(ledgerId, authenticatedUser);

        Transaction transaction = transactionRepository.findByIdAndLedgerId(transactionId, ledgerId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        return mapToResponse(transaction);
    }

    public TransactionResponse updateTransaction(Long ledgerId, Long transactionId, UpdateTransactionRequest request, User authenticatedUser) {
        assertLedgerExists(ledgerId);
        assertMembership(ledgerId, authenticatedUser);

        Transaction transaction = transactionRepository.findByIdAndLedgerId(transactionId, ledgerId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        if (request.getAmount() != null) {
            transaction.setAmount(request.getAmount());
        }
        if (request.getType() != null) {
            transaction.setType(request.getType());
        }
        if (request.getDate() != null) {
            transaction.setDate(request.getDate());
        }
        if (request.getDescription() != null) {
            transaction.setDescription(request.getDescription());
        }

        if (request.getCategoryId() != null) {
            transaction.setCategory(resolveCategory(request.getCategoryId(), ledgerId));
        }

        return mapToResponse(transactionRepository.save(transaction));
    }

    public void deleteTransaction(Long ledgerId, Long transactionId, User authenticatedUser) {
        assertLedgerExists(ledgerId);
        assertMembership(ledgerId, authenticatedUser);

        Transaction transaction = transactionRepository.findByIdAndLedgerId(transactionId, ledgerId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        transactionRepository.delete(transaction);
    }

    @Transactional(readOnly = true)
    public byte[] exportTransactionsXlsx(
            Long ledgerId,
            User authenticatedUser,
            LocalDate startDate,
            LocalDate endDate,
            Long createdByUserId
    ) {
        assertLedgerExists(ledgerId);
        assertMembership(ledgerId, authenticatedUser);

        if (createdByUserId != null) {
            assertMembershipByUserId(ledgerId, createdByUserId);
        }
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("startDate cannot be after endDate");
        }

        var spec = TransactionSpecification.withFilters(
                ledgerId,
                startDate,
                endDate,
                null,
                createdByUserId,
                null
        );
        var page = transactionRepository.findAll(
                spec,
                PageRequest.of(0, EXPORT_ROW_LIMIT, Sort.by(Sort.Order.desc("date"), Sort.Order.desc("id")))
        );

        if (page.hasNext()) {
            throw new IllegalArgumentException("Export exceeds maximum supported rows");
        }

        return writeExportWorkbook(page.getContent());
    }

    private void assertLedgerExists(Long ledgerId) {
        if (!ledgerRepository.existsById(ledgerId)) {
            throw new ResourceNotFoundException("Ledger not found");
        }
    }

    private void assertMembership(Long ledgerId, User user) {
        assertMembershipByUserId(ledgerId, user.getId());
    }

    private void assertMembershipByUserId(Long ledgerId, Long userId) {
        if (!ledgerMemberRepository.existsByLedgerIdAndUserId(ledgerId, userId)) {
            throw new AccessDeniedException("You are not a member of this ledger");
        }
    }

    private Category resolveCategory(Long categoryId, Long ledgerId) {
        if (categoryId == null) {
            return null;
        }
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        if (!category.getLedger().getId().equals(ledgerId)) {
            throw new IllegalArgumentException("Category does not belong to this ledger");
        }
        return category;
    }

    private TransactionResponse mapToResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .amount(transaction.getAmount())
                .type(transaction.getType().name())
                .date(transaction.getDate())
                .categoryId(transaction.getCategory() != null ? transaction.getCategory().getId() : null)
                .categoryName(transaction.getCategory() != null ? transaction.getCategory().getName() : null)
                .description(transaction.getDescription())
                .createdByUserId(transaction.getCreatedBy().getId())
                .createdByDisplayName(transaction.getCreatedBy().getDisplayName())
                .createdAt(transaction.getCreatedAt())
                .build();
    }

    private byte[] writeExportWorkbook(List<Transaction> transactions) {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Transactions");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Data");
            header.createCell(1).setCellValue("Valor");
            header.createCell(2).setCellValue("Categoria");
            header.createCell(3).setCellValue("Pessoa");
            header.createCell(4).setCellValue("Descricao");
            header.createCell(5).setCellValue("Tipo");
            header.createCell(6).setCellValue("Criado em");

            int rowIndex = 1;
            for (Transaction transaction : transactions) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(sanitizeCellValue(formatDate(transaction.getDate())));
                row.createCell(1).setCellValue(transaction.getAmount() != null ? transaction.getAmount().toPlainString() : "");
                row.createCell(2).setCellValue(sanitizeCellValue(
                        transaction.getCategory() != null ? transaction.getCategory().getName() : "Sem categoria"
                ));
                row.createCell(3).setCellValue(sanitizeCellValue(
                        transaction.getCreatedBy() != null ? transaction.getCreatedBy().getDisplayName() : "-"
                ));
                row.createCell(4).setCellValue(sanitizeCellValue(
                        transaction.getDescription() != null ? transaction.getDescription() : "Sem descricao"
                ));
                row.createCell(5).setCellValue(sanitizeCellValue(
                        mapTypeLabel(transaction.getType())
                ));
                row.createCell(6).setCellValue(sanitizeCellValue(
                        formatDateTime(transaction.getCreatedAt())
                ));
            }

            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to generate export file", ex);
        }
    }

    private String sanitizeCellValue(String value) {
        if (value == null) {
            return "";
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return "";
        }
        char firstChar = trimmed.charAt(0);
        if (firstChar == '=' || firstChar == '+' || firstChar == '-' || firstChar == '@') {
            return "'" + trimmed;
        }
        return trimmed;
    }

    private String formatDate(LocalDate value) {
        return value != null ? value.format(EXPORT_DATE_FORMAT) : "";
    }

    private String formatDateTime(java.time.LocalDateTime value) {
        return value != null ? value.format(EXPORT_DATE_TIME_FORMAT) : "";
    }

    private String mapTypeLabel(Transaction.TransactionType type) {
        if (type == null) {
            return "";
        }
        return type == Transaction.TransactionType.INCOME ? "Entrada" : "Saida";
    }
}
