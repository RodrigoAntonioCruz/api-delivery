package br.com.rodrigodacruz.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.rodrigodacruz.domain.Usuario;
import br.com.rodrigodacruz.domain.Endereco;
import br.com.rodrigodacruz.domain.enums.Perfil;
import br.com.rodrigodacruz.domain.enums.TipoUsuario;
import br.com.rodrigodacruz.dto.UsuarioDTO;
import br.com.rodrigodacruz.dto.UsuarioNewDTO;
import br.com.rodrigodacruz.repositories.UsuarioRepository;
import br.com.rodrigodacruz.repositories.EnderecoRepository;
import br.com.rodrigodacruz.security.UserSS;
import br.com.rodrigodacruz.services.exceptions.AuthorizationException;
import br.com.rodrigodacruz.services.exceptions.DataIntegrityException;
import br.com.rodrigodacruz.services.exceptions.ObjectNotFoundException;

@Service
public class UsuarioService {
	
	@Autowired
	private UsuarioRepository repo;
	
	@Autowired
	private EnderecoRepository enderecoRepository;
	
	@Autowired
	private BCryptPasswordEncoder pe;


	public Usuario find(Integer id) {
		
		UserSS user = UserService.authenticated();
		if (user==null || !user.hasRole(Perfil.ADMIN) && !id.equals(user.getId())) {
			throw new AuthorizationException("Acesso negado");
		}
		
		Optional<Usuario> obj = repo.findById(id);
		return obj.orElseThrow(() -> new ObjectNotFoundException(
				"Objeto não encontrado! Id: " + id + ", Tipo: " + Usuario.class.getName()));
	}
	
	@Transactional
	public Usuario insert(Usuario obj) {
		obj.setId(null);
		obj = repo.save(obj);
		enderecoRepository.saveAll(obj.getEnderecos());
		return obj;
	}
	
	public Usuario update(Usuario obj) {
		Usuario newObj = find(obj.getId());
		updateData(newObj, obj);
		return repo.save(newObj);
	}

	public void delete(Integer id) {
		find(id);
		try {
			repo.deleteById(id);
		}
		catch (DataIntegrityViolationException e) {
			throw new DataIntegrityException("Não é possível excluir porque há pedidos relacionados");
		}
	}
	
	public List<Usuario> findAll() {
		return repo.findAll();
	}
	
	public Usuario findByEmail(String email) {
		UserSS user = UserService.authenticated();
		if (user == null || !user.hasRole(Perfil.ADMIN) && !email.equals(user.getUsername())) {
			throw new AuthorizationException("Acesso negado");
		}
	
		Usuario obj = repo.findByEmail(email);
		if (obj == null) {
			throw new ObjectNotFoundException("Objeto não encontrado! Id: " + user.getId() + ", Tipo: " + Usuario.class.getName());
		}
		return obj;
	}
	
	public Page<Usuario> findPage(Integer page, Integer linesPerPage, String orderBy, String direction) {
		PageRequest pageRequest = PageRequest.of(page, linesPerPage, Direction.valueOf(direction), orderBy);
		return repo.findAll(pageRequest);
	}
	
	public Usuario fromDTO(UsuarioDTO objDto) {
		return new Usuario(objDto.getId(), objDto.getNome(), objDto.getEmail(), null, null, null);
	}
	
	public Usuario fromDTO(UsuarioNewDTO objDto) {
		Usuario usuario = new Usuario(null, objDto.getNome(), objDto.getEmail(), objDto.getCpfOuCnpj(),TipoUsuario.toEnum(objDto.getTipo()), pe.encode(objDto.getSenha()));
		Endereco end = new Endereco(null, objDto.getCep(), objDto.getLogradouro(), objDto.getNumero(), objDto.getComplemento(), objDto.getBairro(), objDto.getCidade(), objDto.getEstado(), usuario);
		usuario.getEnderecos().add(end);
		usuario.getTelefones().add(objDto.getTelefone1());
		if (objDto.getTelefone2()!=null) {
			usuario.getTelefones().add(objDto.getTelefone2());
		}
		if (objDto.getTelefone3()!=null) {
			usuario.getTelefones().add(objDto.getTelefone3());
		}
		return usuario;
	}
	
	private void updateData(Usuario newObj, Usuario obj) {
		newObj.setNome(obj.getNome());
		newObj.setEmail(obj.getEmail());
	}
}
