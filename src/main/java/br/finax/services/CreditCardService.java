package br.finax.services;

import br.finax.models.CreditCard;
import br.finax.repository.CreditCardRepository;
import br.finax.dto.InterfacesSQL;
import br.finax.utils.UtilsService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CreditCardService {

    private final CreditCardRepository creditCardRepository;
    private final UtilsService utilsService;

    public List<InterfacesSQL.UserCreditCards> getByUser() {
        return creditCardRepository.getAllByUser(utilsService.getAuthUser().getId());
    }

    public CreditCard getById(long id) {
        return creditCardRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Credit card not found"));
    }

    public ResponseEntity<CreditCard> save(CreditCard card) {
        return ResponseEntity.ok().body(creditCardRepository.save(card));
    }

    public List<InterfacesSQL.CardBasicList> getBasicList() {
        return creditCardRepository.getBasicList(utilsService.getAuthUser().getId());
    }
}
