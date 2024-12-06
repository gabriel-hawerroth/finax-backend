package br.finax.controllers;

import br.finax.dto.EmailDTO;
import br.finax.enums.EmailType;
import br.finax.enums.ErrorCategory;
import br.finax.exceptions.ServiceException;
import br.finax.models.User;
import br.finax.repository.UserRepository;
import br.finax.services.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = lombok.AccessLevel.PRIVATE)
@RequestMapping("/email")
public class EmailSendController {

    UserRepository userRepository;
    EmailService emailService;

    public void sendEmail() {
        List<User> users = userRepository.findAll().stream().filter(User::isActive).toList();

        users.forEach(user -> {
            final String mailContent = emailService.buildEmailContent(EmailType.SYSTEM_UPDATE, user, "");

            try {
                emailService.sendMail(
                        new EmailDTO(
                                user.getEmail(),
                                "Atualização liberada Finax",
                                emailService.buildEmailTemplate(mailContent)
                        )
                );
            } catch (Exception e) {
                throw new ServiceException(ErrorCategory.BAD_GATEWAY, "Error sending email");
            }
        });
    }
}
