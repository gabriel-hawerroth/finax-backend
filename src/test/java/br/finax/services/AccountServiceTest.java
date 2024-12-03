package br.finax.services;

import br.finax.exceptions.WithoutPermissionException;
import br.finax.models.Account;
import br.finax.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.util.Optional;

import static br.finax.MockUtils.mockAuthentication;
import static br.finax.MockUtils.mockAuthenticationUserId;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

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
}
