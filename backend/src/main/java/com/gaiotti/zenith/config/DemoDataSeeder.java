package com.gaiotti.zenith.config;

import com.gaiotti.zenith.model.Category;
import com.gaiotti.zenith.model.Ledger;
import com.gaiotti.zenith.model.LedgerMember;
import com.gaiotti.zenith.model.Transaction;
import com.gaiotti.zenith.model.User;
import com.gaiotti.zenith.repository.CategoryRepository;
import com.gaiotti.zenith.repository.LedgerMemberRepository;
import com.gaiotti.zenith.repository.LedgerRepository;
import com.gaiotti.zenith.repository.TransactionRepository;
import com.gaiotti.zenith.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@Profile("prod")
@RequiredArgsConstructor
public class DemoDataSeeder implements CommandLineRunner {

    private static final String ANA_EMAIL = "ana.demo@zenith.app";
    private static final String BRUNO_EMAIL = "bruno.demo@zenith.app";
    private static final String DEMO_PASSWORD = "Zenith#Demo2026";
    private static final String LEDGER_NAME = "Casa Ana & Bruno";

    private final UserRepository userRepository;
    private final LedgerRepository ledgerRepository;
    private final LedgerMemberRepository ledgerMemberRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.existsByEmail(ANA_EMAIL)) {
            return;
        }

        User ana = userRepository.save(User.builder()
                .email(ANA_EMAIL)
                .passwordHash(passwordEncoder.encode(DEMO_PASSWORD))
                .displayName("Ana")
                .aiEnabled(true)
                .build());

        User bruno = userRepository.save(User.builder()
                .email(BRUNO_EMAIL)
                .passwordHash(passwordEncoder.encode(DEMO_PASSWORD))
                .displayName("Bruno")
                .aiEnabled(true)
                .build());

        Ledger ledger = ledgerRepository.save(Ledger.builder().name(LEDGER_NAME).build());
        ledgerMemberRepository.save(LedgerMember.builder().ledger(ledger).user(ana).build());
        ledgerMemberRepository.save(LedgerMember.builder().ledger(ledger).user(bruno).build());

        Category salario = category(ledger, ana, "Salário", "#22C55E");
        Category moradia = category(ledger, ana, "Moradia", "#6366F1");
        Category mercado = category(ledger, ana, "Mercado", "#F59E0B");
        Category transporte = category(ledger, bruno, "Transporte", "#3B82F6");
        Category lazer = category(ledger, bruno, "Lazer", "#EC4899");
        Category saude = category(ledger, bruno, "Saúde", "#14B8A6");
        categoryRepository.saveAll(List.of(salario, moradia, mercado, transporte, lazer, saude));

        List<Transaction> transactions = new ArrayList<>();
        for (int monthsAgo = 2; monthsAgo >= 0; monthsAgo--) {
            YearMonth month = YearMonth.now().minusMonths(monthsAgo);
            transactions.add(income(ledger, ana, salario, "5200.00", month.atDay(5), "Salário Ana"));
            transactions.add(income(ledger, bruno, salario, "4300.00", month.atDay(5), "Salário Bruno"));
            transactions.add(expense(ledger, ana, moradia, "1800.00", month.atDay(10), "Aluguel"));
            transactions.add(expense(ledger, ana, moradia, "320.00", month.atDay(12), "Luz e água"));
            transactions.add(expense(ledger, ana, mercado, "640.50", month.atDay(8), "Compras do mês"));
            transactions.add(expense(ledger, bruno, mercado, "215.90", month.atDay(21), "Feira e padaria"));
            transactions.add(expense(ledger, bruno, transporte, "280.00", month.atDay(6), "Combustível"));
            transactions.add(expense(ledger, ana, transporte, "96.00", month.atDay(16), "Transporte por app"));
            transactions.add(expense(ledger, bruno, lazer, "189.90", month.atDay(18), "Jantar fora"));
            transactions.add(expense(ledger, ana, lazer, "55.00", month.atDay(23), "Streaming"));
            transactions.add(expense(ledger, bruno, saude, "140.00", month.atDay(14), "Farmácia"));
        }
        transactionRepository.saveAll(transactions);

        log.info("demo data seeded: ledger=\"{}\" users=[{}, {}] transactions={}",
                LEDGER_NAME, ANA_EMAIL, BRUNO_EMAIL, transactions.size());
    }

    private Category category(Ledger ledger, User createdBy, String name, String color) {
        return Category.builder()
                .ledger(ledger)
                .createdBy(createdBy)
                .name(name)
                .color(color)
                .build();
    }

    private Transaction income(Ledger ledger, User user, Category category, String amount, LocalDate date, String description) {
        return transaction(ledger, user, category, Transaction.TransactionType.INCOME, amount, date, description);
    }

    private Transaction expense(Ledger ledger, User user, Category category, String amount, LocalDate date, String description) {
        return transaction(ledger, user, category, Transaction.TransactionType.EXPENSE, amount, date, description);
    }

    private Transaction transaction(Ledger ledger, User user, Category category, Transaction.TransactionType type,
                                    String amount, LocalDate date, String description) {
        return Transaction.builder()
                .ledger(ledger)
                .createdBy(user)
                .category(category)
                .type(type)
                .amount(new BigDecimal(amount))
                .date(date)
                .description(description)
                .build();
    }
}
