# Segurança Java — Voll Med (Spring Security)

Aplicação web para gestão de uma clínica (médicos, pacientes e consultas) usada como laboratório de **autenticação e autorização com Spring Security**. O projeto parte de uma aplicação CRUD já funcional e, a cada commit, adiciona uma camada de segurança: login, persistência de usuários, criptografia de senhas, perfis de acesso e regras de autorização por endpoint.

## Tecnologias

| Camada | Tecnologia |
|---|---|
| Linguagem / Build | Java 17, Maven (wrapper incluso) |
| Framework | Spring Boot 3.3.0 |
| Web / View | Spring MVC, Thymeleaf, Thymeleaf Layout Dialect, `thymeleaf-extras-springsecurity6` |
| Segurança | Spring Security 6 (form login, remember-me, BCrypt) |
| Persistência | Spring Data JPA / Hibernate |
| Banco de dados | Microsoft SQL Server (`mssql-jdbc`) |
| Validação | Bean Validation (`spring-boot-starter-validation`) |

## Perfis e permissões (estado atual)

| Perfil | Pacientes | Médicos (listar) | Médicos (cadastrar/editar/excluir) | Consultas (agendar/alterar) |
|---|:---:|:---:|:---:|:---:|
| `ATENDENTE` | ✅ | ✅ | ✅ | ✅ |
| `PACIENTE` | ❌ | ✅ | ❌ | ✅ |
| `MEDICO` | ❌ | ❌ | ❌ | ❌ |

Rotas públicas: `/`, `/index`, `/home`, `/css/**`, `/js/**`, `/assets/**`. Todo o resto exige login.

---

## Desenvolvimento (cronológico)

### 1. `824765d` — 24/09/2026 — *feat: Funcionalidades com spring-security*
**Ponto de partida: aplicação base + primeiro login.**

- Criação do projeto Spring Boot (Maven, Java 17) com os módulos de **médicos** e **consultas** (CRUD completo com listagem paginada, formulário, exclusão com modal e mensagens de sucesso/erro).
- Camadas organizadas em `controller`, `domain` (entidades, repositórios, serviços, DTOs em formato `record`) e `infra`.
- Tratamento global de exceções (`TratadorDeExceptions`) e `RegraDeNegocioException` para regras de negócio.
- Front-end com Thymeleaf: template base, componentes reutilizáveis (menu, rodapé, paginação, mensagens), páginas de erro 404/500, CSS e JS.
- Migrations iniciais: `V1` (tabela `medicos`) e `V2` (tabela `consultas`).
- **Primeira configuração do Spring Security** (`ConfigSeguranca`):
  - página de login própria (`/login`) com `LoginController` e `Login.html`;
  - logout redirecionando para `/login?logout`;
  - recursos estáticos liberados e demais rotas exigindo autenticação;
  - usuários **em memória** (`InMemoryUserDetailsManager`) apenas para validar o fluxo de login, ainda com senha em texto puro (`{noop}`).

### 2. `32568ca` — 26/09/2026 — *feat: PERSISTENCIA NO BANCO E CODIFICACAO*
**Usuários saem da memória e passam a ficar no banco, com senha criptografada.**

- Nova entidade **`Usuario`** implementando `UserDetails`, com `UsuarioRepository` (busca por e-mail ignorando maiúsculas/minúsculas) e `UsuarioService` implementando `UserDetailsService`, que passa a ser a fonte de autenticação do Spring Security.
- Migration `V3` criando a tabela `usuarios`.
- Remoção dos usuários em memória.
- Criação do bean **`PasswordEncoder` com `BCryptPasswordEncoder`**: senhas passam a ser gravadas como hash.
- Configuração do **remember-me** (chave, nome do parâmetro e validade do token).
- Ajustes em `application.properties` (`ddl-auto`, Flyway, filtro de métodos HTTP ocultos) e nas migrations iniciais.

### 3. `d693927` — 26/09/2026 — *feat: Finalização de camadas de segurança de uma página de login*
**Fechamento da primeira etapa: experiência do usuário logado.**

- Inclusão da dependência `thymeleaf-extras-springsecurity6`.
- Menu passa a exibir **"Olá, {nome do usuário}"** com dropdown contendo o botão **SAIR** (via `sec:authentication="principal.nome"`).
- Acréscimo do `getNome()` em `Usuario` para suportar a exibição.

### 4. `725e6dd` — 06/10/2026 — *feat: trabalhando com spring security pt2*
**Segundo bloco: pacientes e criação automática de usuário no cadastro.**

- Novo módulo de **pacientes** completo (controller, entidade, repositório, serviço, DTOs e telas de listagem/formulário), com migration `V4` (tabela `pacientes`).
- Migration `V5`: a consulta deixa de guardar o nome do paciente em texto e passa a referenciá-lo por `paciente_id` (FK).
- Migration `V6`: ajuste da chave de `medicos` para que o `id` deixe de ser gerado pelo banco.
- **Cadastro de médico agora cria também um `Usuario`**: o `id` do usuário é reutilizado como `id` do médico (relação 1 para 1), e a senha inicial é o CRM, criptografada via BCrypt.
- Construtor de `Usuario` ampliado e `UsuarioService.salvarUsuario(...)` criado.
- Menu e home atualizados para contemplar pacientes.

### 5. `223eb4a` — 06/10/2026 — *feat: Restringindo direito de acesso dos perfis*
**Introdução dos perfis (roles) e primeiras restrições dentro dos controllers.**

- Novo enum **`Perfil`**: `ATENDENTE`, `MEDICO`, `PACIENTE`; campo `perfil` adicionado a `Usuario` (`@Enumerated(EnumType.STRING)`).
- Migration `V7` adicionando a coluna `perfil` em `usuarios` com valor padrão e `CHECK` aceitando só os três valores.
- Cadastro de médico cria usuário com perfil `MEDICO`; cadastro de paciente cria usuário com perfil `PACIENTE` (senha inicial = CPF).
- **`MedicoController`** passa a receber `@AuthenticationPrincipal Usuario logado` e bloqueia (retornando a página de erro) conforme o perfil:
  - listagem: negada para `MEDICO`;
  - formulário, cadastro e exclusão: apenas `ATENDENTE`.
- Exclusão de médico remove também o usuário correspondente.
- Padronização do nome das migrations (`V3__`, `V4__`, `V5__`, com dois underscores).

### 6. `e43c5dc` — 07/10/2026 — *feat: Configuração de autorização por endpoint*
**Autorização declarativa na configuração de segurança.**

- `Usuario.getAuthorities()` passa a devolver a autoridade `ROLE_<PERFIL>` (antes retornava `null`).
- `ConfigSeguranca` ganha regras por rota e método HTTP:
  - `/`, `/index` e `/home` públicos;
  - `/pacientes/**` → `ATENDENTE`;
  - `GET /medicos` → `ATENDENTE` ou `PACIENTE`;
  - `/medicos/**` → `ATENDENTE`;
  - `POST` e `PUT` em `/consultas/**` → `ATENDENTE` ou `PACIENTE`;
  - qualquer outra rota → autenticado.

---

## Autor

Jonnathan — [@Jonnathan2020](https://github.com/Jonnathan2020)
