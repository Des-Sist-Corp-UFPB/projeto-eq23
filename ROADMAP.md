# Roadmap de Evolução — Mercado DSC

Este documento registra as ideias de melhorias e novas funcionalidades propostas para tornar o sistema mais robusto e completo para a disciplina de Desenvolvimento de Sistemas Corporativos (DSC).

---

## 🚀 Funcionalidades Propostas

### 🛒 1. Sistema de Vendas e Carrinho de Compras (Em Planejamento)
Adição do fluxo de transações e consumo de estoque:
- **Carrinho de Compras**: Permitir adicionar produtos ao carrinho temporário (via sessão HTTP ou persistência leve).
- **Entidades de Venda**: Criar as entidades `Pedido` (com data, cliente, status, valor total) e `ItemPedido` (associação entre pedido, produto, quantidade e preço unitário no momento da compra).
- **Checkout com Baixa de Estoque**: Implementar lógica transacional (`@Transactional`) para salvar o pedido e decrementar o estoque dos produtos correspondentes, validando se há estoque disponível.
- **Histórico de Pedidos**: Página para o usuário visualizar seus pedidos anteriores.

### 🔐 2. Controle de Acesso por Perfis (Roles)
Refinamento da segurança com Spring Security:
- **`ROLE_ADMIN`**: Único perfil autorizado a cadastrar, atualizar e remover produtos ou categorias, além de visualizar painéis gerenciais.
- **`ROLE_USER`**: Perfil voltado ao consumidor, que pode apenas visualizar produtos, gerenciar seu carrinho e realizar compras.

### 📊 3. Dashboard / Painel de Relatórios
Painel gerencial para administradores:
- Total acumulado de vendas.
- Listagem ou gráfico de produtos mais vendidos.
- Alerta visual para produtos com estoque crítico (baixo/zerado).

### 🔍 4. Busca, Paginação e Filtros Dinâmicos
Otimização da usabilidade na listagem principal:
- Filtro por nome de produto (busca textual).
- Filtro rápido por categoria.
- Paginação de dados no banco com Spring Data JPA (`Pageable`) e requisições dinâmicas HTMX.
