package br.finax.utils;

import br.finax.models.Category;
import lombok.experimental.UtilityClass;

import java.util.List;

@UtilityClass
public class DefaultCategories {

    public static final List<Category> DEFAULT_EXPENSE_CATEGORIES = List.of(
            new Category("Alimentação", "#EC61A1", "restaurant", "E", true),
            new Category("Assinaturas e serviços", "#E454ED", "subscriptions", "E", false),
            new Category("Compras", "#7253C8", "local_mall", "E", false),
            new Category("Cuidados pessoais", "#94CD7A", "person", "E", true),
            new Category("Dívidas", "#FB6467", "request_quote", "E", true),
            new Category("Educação", "#787878", "school", "E", true),
            new Category("Investimentos", "#FCA52D", "finance_mode", "E", true),
            new Category("Impostos e taxas", "#FFA490", "receipt_long", "E", true),
            new Category("Saúde", "#82C8F1", "medication", "E", true),
            new Category("Transporte", "#D9AA6A", "directions_bus", "E", true),
            new Category("Lazer", "#7253C8", "local_bar", "E", false),
            new Category("Roupas", "#5096DE", "apparel", "E", false),
            new Category("Presentes e doações", "#FF494D", "redeem", "E", false),
            new Category("Outras despesas", "#AFAFAF", "trending_down", "E", false)
    );

    public static final List<Category> DEFAULT_REVENUE_CATEGORIES = List.of(
            new Category("Salário", "#86BB5D", "receipt_long", "R", false),
            new Category("Investimentos", "#FCA52D", "finance_mode", "R", false),
            new Category("Empréstimos", "#787878", "order_approve", "R", false),
            new Category("Presentes e doações", "#FF494D", "redeem", "R", false),
            new Category("Outras receitas", "#D9AA6A", "attach_money", "R", false)
    );
}
