package br.finax.services;

import br.finax.exceptions.WithoutPermissionException;
import br.finax.models.Account;
import br.finax.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static br.finax.MockUtils.mockAuthentication;
import static br.finax.MockUtils.mockAuthenticationUserId;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    private final Authentication authentication = mockAuthentication();

    @InjectMocks
    private AccountService accountService;
    @Mock
    private AccountService service;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private ReleaseService releaseService;

    private Account account;

    @BeforeEach
    void setUp() {
        account = new Account();
        account.setId(1L);
        account.setUserId(1L);
        account.setBalance(BigDecimal.ZERO);
        account.setActive(true);
    }

    @ParameterizedTest
    @ValueSource(longs = {1L, 2L})
    @DisplayName("findById - should check permission")
    void testFindById(long accountUserId) {
        long authUserId = 1L;
        mockAuthenticationUserId(authentication, authUserId);
        setAccountUserId(accountUserId);

        final long accountId = 2L;

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        if (accountUserId == authUserId)
            assertDoesNotThrow(() -> accountService.findById(accountId));
        else
            assertThrows(WithoutPermissionException.class, () -> accountService.findById(accountId));
    }

    private void setAccountUserId(long userId) {
        account.setUserId(userId);
    }

    @Test
    @DisplayName("edit - should preserve creation date")
    void testEditPreservesCreationDate() {
        final long authUserId = 1L;
        mockAuthenticationUserId(authentication, authUserId);

        final long accountId = 1L;
        final Instant createdAt = Instant.parse("2026-01-01T00:00:00Z");
        account.setCreatedAt(createdAt);

        when(service.findById(accountId)).thenReturn(account);
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        final var dto = new br.finax.dto.account.SaveAccountDTO(
                "Conta Atualizada",
                BigDecimal.TEN,
                false,
                true,
                null,
                null,
                null,
                null,
                null,
                null,
                true,
                false
        );

        accountService.edit(accountId, dto);

        final ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(accountCaptor.capture());

        assertEquals(createdAt, accountCaptor.getValue().getCreatedAt());
    }
}
