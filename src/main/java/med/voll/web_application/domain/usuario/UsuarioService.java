package med.voll.web_application.domain.usuario;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService implements UserDetailsService {


    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder encriptador;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder encriptador) {
        this.usuarioRepository = usuarioRepository;
        this.encriptador = encriptador;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return usuarioRepository.findByEmailIgnoreCase(username)
                .orElseThrow(()-> new UsernameNotFoundException("Usuário não foi encontrado!"));
    }

    public Long salvarUsuario(@NotBlank String nome, @NotBlank @Email String email, @NotBlank @Pattern(regexp = "\\d{4,6}", message = "CRM deve ter de 4 a 6 digitos numéricos") String senha) {
        String senhaCripto = encriptador.encode(senha);
        Usuario usuario =  usuarioRepository.save(new Usuario(nome,email,senhaCripto));
        return usuario.getId();

    }
}
