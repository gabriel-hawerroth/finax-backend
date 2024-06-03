package br.finax.services;

import br.finax.dto.InterfacesSQL.CardBasicList;
import br.finax.dto.InterfacesSQL.UserCreditCards;
import br.finax.exceptions.NotFoundException;
import br.finax.models.CreditCard;
import br.finax.repository.CreditCardRepository;
import br.finax.utils.UtilsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CreditCardService {

    private final CreditCardRepository creditCardRepository;
    private final UtilsService utilsService;

    public List<UserCreditCards> getByUser() {
        return creditCardRepository.getAllByUser(utilsService.getAuthUser().getId());
    }

    public CreditCard getById(long id) {
        return creditCardRepository.findById(id).orElseThrow(NotFoundException::new);
    }

    public CreditCard save(CreditCard card) {
        return creditCardRepository.save(card);
    }

    public List<CardBasicList> getBasicList() {
        return creditCardRepository.getBasicList(utilsService.getAuthUser().getId());
    }
}
