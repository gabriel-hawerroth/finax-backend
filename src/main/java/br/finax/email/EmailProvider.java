package br.finax.email;

import br.finax.dto.app.emails.EmailDTO;

public interface EmailProvider {

    void sendMail(EmailDTO emailDTO);
}
