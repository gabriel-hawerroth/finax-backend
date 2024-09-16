package br.finax.email;

import br.finax.dto.EmailDTO;

public interface EmailProvider {

    void sendMail(EmailDTO emailDTO);
}
