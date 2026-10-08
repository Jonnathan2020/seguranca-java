package med.voll.web_application.controller;

import jakarta.validation.Valid;
import med.voll.web_application.domain.RegraDeNegocioException;
import med.voll.web_application.domain.medico.DadosCadastroMedico;
import med.voll.web_application.domain.medico.DadosListagemMedico;
import med.voll.web_application.domain.medico.Especialidade;
import med.voll.web_application.domain.medico.MedicoService;
import med.voll.web_application.domain.usuario.Perfil;
import med.voll.web_application.domain.usuario.Usuario;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("medicos")
public class MedicoController {

    private static final String PAGINA_LISTAGEM = "medico/listagem-medicos";
    private static final String PAGINA_CADASTRO = "medico/formulario-medico";
    private static final String PAGINA_ERRO = "erro/500";
    private static final String REDIRECT_LISTAGEM = "redirect:/medicos?sucesso";

    private final MedicoService service;

    public MedicoController(MedicoService service) {
        this.service = service;
    }

    @ModelAttribute("especialidades")
    public Especialidade[] especialidades() {
        return Especialidade.values();
    }

    @GetMapping
    @PreAuthorize("hasRole('ATENDENTE') OR hasRole('PACIENTE')")
    public String carregarPaginaListagem(@PageableDefault Pageable paginacao, Model model, @AuthenticationPrincipal Usuario logado) {
        if(logado.getPerfil() == Perfil.MEDICO){
            return PAGINA_ERRO;
        }
        var medicosCadastrados = service.listar(paginacao);
        model.addAttribute("medicos", medicosCadastrados);
        return PAGINA_LISTAGEM;
    }

    @GetMapping("formulario")
    @PreAuthorize("hasRole('ATENDENTE')")
    public String carregarPaginaCadastro(Long id, Model model, @AuthenticationPrincipal Usuario logado) {

        if(logado.getPerfil() != Perfil.ATENDENTE){
            return PAGINA_ERRO;
        }
        if (id != null) {
            model.addAttribute("dados", service.carregarPorId(id));
        } else {
            model.addAttribute("dados", new DadosCadastroMedico(null, "", "", "", "", null));
        }

        return PAGINA_CADASTRO;
    }

    @PostMapping
    @PreAuthorize("hasRole('ATENDENTE')")
    public String cadastrar(@Valid @ModelAttribute("dados") DadosCadastroMedico dados, BindingResult result, Model model, @AuthenticationPrincipal Usuario logado) {

        if(logado.getPerfil() != Perfil.ATENDENTE){
            return PAGINA_ERRO;
        }

        if (result.hasErrors()) {
            model.addAttribute("dados", dados);
            return PAGINA_CADASTRO;
        }

        try {
            service.cadastrar(dados);
            return REDIRECT_LISTAGEM;
        } catch (RegraDeNegocioException e) {
            model.addAttribute("erro", e.getMessage());
            model.addAttribute("dados", dados);
            return PAGINA_CADASTRO;
        }
    }

    @DeleteMapping
    public String excluir(Long id, @AuthenticationPrincipal Usuario logado) {
        if(logado.getPerfil() != Perfil.ATENDENTE){
            return PAGINA_ERRO;
        }

        service.excluir(id);
        return REDIRECT_LISTAGEM;
    }

    @GetMapping("{especialidade}")
    @ResponseBody
    public List<DadosListagemMedico> listarMedicosPorEspecialidade(@PathVariable String especialidade) {

        return service.listarPorEspecialidade(Especialidade.valueOf(especialidade));
    }

}
