-- Default user
INSERT INTO users (id, email, "password", first_name, last_name, "access", active, can_change_password, signature,
                   signature_expiration, profile_image)
VALUES (0, 'default@gmail.com', '123', 'default', NULL, 'free',
        false, false, 'month', NULL, NULL);

-- Default categories
INSERT INTO category ("name", color, icon, "type", user_id, active, essential)
VALUES ('Ajuste de saldo', '#AFAFAF', 'edit', 'A', 0, false, false),
       ('Salário', '#86BB5D', 'receipt_long', 'R', 0, true, false),
       ('Investimentos', '#FCA52D', 'finance_mode', 'R', 0, true, true),
       ('Empréstimos', '#787878', 'order_approve', 'R', 0, true, false),
       ('Outras receitas', '#D9AA6A', 'attach_money', 'R', 0, true, false),
       ('Alimentação', '#EC61A1', 'restaurant', 'E', 0, true, true),
       ('Assinaturas e serviços', '#E454ED', 'subscriptions', 'E', 0, true, false),
       ('Compras', '#7253C8', 'local_mall', 'E', 0, true, false),
       ('Cuidados pessoais', '#94CD7A', 'person', 'E', 0, true, true),
       ('Dívidas e empréstimos', '#FB6467', 'request_quote', 'E', 0, true, true);
INSERT INTO category ("name", color, icon, "type", user_id, active, essential)
VALUES ('Educação', '#787878', 'school', 'E', 0, true, true),
       ('Investimentos', '#FCA52D', 'finance_mode', 'E', 0, true, true),
       ('Impostos e taxas', '#FFA490', 'receipt_long', 'E', 0, true, true),
       ('Saúde', '#82C8F1', 'medication', 'E', 0, true, true),
       ('Transporte', '#D9AA6A', 'directions_bus', 'E', 0, true, true),
       ('Lazer', '#7253C8', 'local_bar', 'E', 0, true, false),
       ('Roupas', '#5096DE', 'apparel', 'E', 0, true, false),
       ('Outras despesas', '#AFAFAF', 'trending_down', 'E', 0, true, false),
       ('Presentes', '#FF494D', 'redeem', 'R', 0, true, false),
       ('Presentes e doações', '#FF494D', 'redeem', 'E', 0, true, false);
