/*
 * Copyright (C) 2017 Ulises Beltrán Gómez --- beltrangomezulises@gmail.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.machineAdmin.managers.cg.admin.postgres;

import com.machineAdmin.daos.cg.admin.postgres.DaoUsuario;
import com.machineAdmin.entities.cg.admin.postgres.BitacoraContras;
import com.machineAdmin.entities.cg.admin.postgres.Usuario;
import com.machineAdmin.entities.cg.admin.postgres.UsuariosPerfil;
import com.machineAdmin.entities.cg.admin.postgres.UsuariosPerfilPK;
import com.machineAdmin.entities.cg.admin.postgres.UsuariosPermisos;
import com.machineAdmin.entities.cg.commons.Profundidad;
import com.machineAdmin.managers.cg.commons.ManagerSQLCatalog;
import com.machineAdmin.managers.cg.exceptions.ContraseñaIncorrectaException;
import com.machineAdmin.managers.cg.exceptions.ParametroInvalidoException;
import com.machineAdmin.managers.cg.exceptions.TokenExpiradoException;
import com.machineAdmin.managers.cg.exceptions.TokenInvalidoException;
import com.machineAdmin.managers.cg.exceptions.UserException;
import com.machineAdmin.managers.cg.exceptions.UsuarioBlockeadoException;
import com.machineAdmin.managers.cg.exceptions.UsuarioInexistenteException;
import com.machineAdmin.models.cg.ModelAltaUsuario;
import com.machineAdmin.models.cg.ModelAsignarPermisos;
import com.machineAdmin.models.cg.ModelCodigoRecuperacionUsuario;
import com.machineAdmin.models.cg.ModelPermisoAsignado;
import com.machineAdmin.utils.UtilsBitacora;
import com.machineAdmin.utils.UtilsConfig;
import com.machineAdmin.utils.UtilsDate;
import com.machineAdmin.utils.UtilsJWT;
import com.machineAdmin.utils.UtilsMail;
import com.machineAdmin.utils.UtilsSMS;
import com.machineAdmin.utils.UtilsSecurity;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import static java.util.stream.Collectors.toList;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.mail.EmailException;

/**
 *
 * @author Ulises Beltrán Gómez --- beltrangomezulises@gmail.com
 */
public class ManagerUsuario extends ManagerSQLCatalog<Usuario, Long> {

    public ManagerUsuario() {
        super(new DaoUsuario());
    }

    public ManagerUsuario(String token, Profundidad profundidad) throws TokenInvalidoException, TokenExpiradoException {
        super(new DaoUsuario(), token, profundidad);
    }

    @Override
    public Usuario persist(Usuario entity) throws UserException.UsuarioYaExistente, Exception {
        entity.setContra(UtilsSecurity.cifrarMD5(entity.getContra()));
        entity.setCorreo(entity.getCorreo().toLowerCase());
        try {
            Usuario persisted = super.persist(entity);            
            //bitacorizar la contraseña
            BitacoraContras bc = new BitacoraContras(persisted.getId(), persisted.getContra());
            bc.setUsuario1(persisted);

            ManagerBitacoraContra managerBitacoraContra = new ManagerBitacoraContra();
            managerBitacoraContra.setUsuario(this.getUsuario());

            managerBitacoraContra.persist(bc);

            return persisted;
        } catch (Exception e) {
            if (e.toString().contains("duplicate key value violates unique constraint")) {
                throw new UserException.UsuarioYaExistente(getMessageOfUniqueContraint(entity));
            }
            throw e;
        }
    }

    @Override
    public void update(Usuario entity) throws Exception {
        super.update(entity);
    }

    @Override
    public void delete(Long id) throws Exception {
        Usuario u = this.findOne(id);
        u.setInhabilitado(Boolean.TRUE);
        this.update(u);
    }

    public void altaUsuario(ModelAltaUsuario model) throws UserException.UsuarioYaExistente, ParametroInvalidoException, Exception {
        //validar que venga minimo un perfil
        if (model.getPerfiles() == null) {
            throw new ParametroInvalidoException("Debe de asignar por lo menos 1 perfil cuando crea un usuario");
        }else{
            if (model.getPerfiles().isEmpty()) {
                throw new ParametroInvalidoException("Debe de asignar por lo menos 1 perfil cuando crea un usuario");
            }
        }

        Usuario nuevoUsuario = new Usuario();
        BeanUtils.copyProperties(nuevoUsuario, model);
        this.persist(nuevoUsuario);
        //generar relacion con los ids de los perfiles del usuario
        ManagerUsuariosPerfil managerUsuariosPerfil = new ManagerUsuariosPerfil();
        UsuariosPerfil up;
        List<UsuariosPerfil> usuariosPerfilesRelacion = new ArrayList<>();
        for (Long perfilId : model.getPerfiles()) {
            up = new UsuariosPerfil();
            up.setHereda(Boolean.TRUE);
            up.setUsuariosPerfilPK(new UsuariosPerfilPK(nuevoUsuario.getId(), perfilId));
            usuariosPerfilesRelacion.add(up);
        }
        managerUsuariosPerfil.persistAll(usuariosPerfilesRelacion);
    }

    /**
     * Metodo de login para autentificar usuarios
     *
     * @param usuarioAutenticando -> el usuario puede contener en su atributo
     * user el nombre de usuario, el correo o el telefono como identificador
     * @return loged, usuario logeado
     * @throws UsuarioInexistenteException
     * @throws
     * com.machineAdmin.managers.cg.exceptions.ContraseñaIncorrectaException
     * @throws com.machineAdmin.managers.cg.exceptions.UsuarioBlockeadoException
     */
    public Usuario login(Usuario usuarioAutenticando) throws UsuarioInexistenteException, ContraseñaIncorrectaException, UsuarioBlockeadoException, Exception {
        Usuario loged;
        try {
            DaoUsuario daoUsuario = new DaoUsuario();
            String identificador = usuarioAutenticando.getNombre();
            String contraseña = usuarioAutenticando.getContra();
            switch (getUserIdentifierType(usuarioAutenticando.getNombre())) {
                case MAIL:
                    loged = daoUsuario.stream().where(u -> u.getCorreo().equals(identificador.toLowerCase()) && u.getContra().equals(contraseña)).findFirst().get();
                    break;
                case PHONE:
                    loged = daoUsuario.stream().where(u -> u.getTelefono().equals(identificador) && u.getContra().equals(contraseña)).findFirst().get();
                    break;
                default:
                    loged = daoUsuario.stream().where(u -> u.getNombre().equals(identificador) && u.getContra().equals(contraseña)).findFirst().get();
                    break;
            }

            if (loged.getBloqueado() && loged.getBloqueadoHastaFecha().after(new Date())) {
                throw new UsuarioBlockeadoException("Usuario bloqueado hasta " + UtilsDate.format_D_MM_YYYY_HH_MM(loged.getBloqueadoHastaFecha()));
            }
            if (loged.getInhabilitado()) {
                throw new ContraseñaIncorrectaException("No se encontro un usuario con esa contraseña");
            }

            loged.setNumeroIntentosLogin(0);
            daoUsuario.update(loged);

            //login exitoso, generar bitácora                                     
            UtilsBitacora.bitacorizarLogIn(this.nombreDeUsuario(loged.getId()));
        } catch (NoSuchElementException e) {
            //verificar si existe el usuario
            this.numberAttemptVerification(usuarioAutenticando);
            throw new ContraseñaIncorrectaException("No se encontro un usuario con esa contraseña");
        }
        return loged;
    }

    public void logout(String token) throws TokenInvalidoException, TokenExpiradoException {
        UtilsBitacora.bitacorizarLogOut(this.nombreDeUsuario(UtilsJWT.getUserIdFrom(token)));
    }

    private void numberAttemptVerification(Usuario usuario) throws UsuarioInexistenteException, UsuarioBlockeadoException, Exception {
        try {
            String identi = usuario.getNombre();

            Usuario intentoLogin = this.dao.stream().where(u
                    -> u.getCorreo().equals(identi.toLowerCase())
                    || u.getNombre().equals(identi)
                    || u.getTelefono().equals(identi)).findFirst().get();

            if (intentoLogin.getBloqueado()) {
                throw new UsuarioBlockeadoException("El usuario fue blockeado por el número de intentos fallidos hasta " + UtilsDate.format_D_MM_YYYY_HH_MM(intentoLogin.getBloqueadoHastaFecha()));
            }
            //<editor-fold defaultstate="collapsed" desc="CRITERIOS DE VERIFICACION DE INTENTOS DE LOGIN"> 
//             aumentar numero de intentos para bloqueo temporal si el lapso de tiempo es mayor al configurado 
//             si el numero de intentos realizados es nulo, inicializar y actualizar 
//             si realiza un intento en un ranto menor a los segundos permitidos entre intentos aumentar intentos 
//             si el numero de intentos es mayor al permitido dejar el usuario bloqueado por un timepo configurado 
//             formula = (now() - lastUserAttemptLoginDate) > timeBetweenAttempts             
            //</editor-fold>                
            if (intentoLogin.getNumeroIntentosLogin() == 0) {
                intentoLogin.setNumeroIntentosLogin(1);
                intentoLogin.setFechaUltimoIntentoLogin(new Date());
            } else {
                long intervaloDeIntento = (new Date().getTime() - intentoLogin.getFechaUltimoIntentoLogin().getTime());
                if (intervaloDeIntento < UtilsConfig.getSecondsBetweenLoginAttempt() * 1000) { //es un intento fuera del rango permitido de tiempo 
                    intentoLogin.setNumeroIntentosLogin(intentoLogin.getNumeroIntentosLogin() + 1);
                } else { //es un intento dentro del rango permitido de tiempo 
                    intentoLogin.setNumeroIntentosLogin(1);
                    intentoLogin.setFechaUltimoIntentoLogin(new Date());
                }
            }
            /**
             * si el numero de intentos excede el permitido, bloquear usuario
             */
            try {
                if (intentoLogin.getNumeroIntentosLogin() > UtilsConfig.getMaxNumberLoginAttempt()) {
                    intentoLogin.setBloqueado(true);
                    intentoLogin.setBloqueadoHastaFecha(UtilsConfig.getDateUtilUserStillBlocked());
                    this.dao.update(intentoLogin);
                    throw new UsuarioBlockeadoException("El usuario fue blockeado por el número de intentos fallidos hasta " + UtilsDate.format_D_MM_YYYY_HH_MM(intentoLogin.getBloqueadoHastaFecha()));
                } else {
                    this.dao.update(intentoLogin);
                }
            } catch (UsuarioBlockeadoException e) {
                throw e;
            } catch (Exception e) {
                throw e;
            }
        } catch (NoSuchElementException e) {
            throw new UsuarioInexistenteException("La contraseña es incorrecta");
        }
    }

    private String getMessageOfUniqueContraint(Usuario entity) {
        //buscar que atributo ya ocupado          
        String mensaje = "ya existen un usuario con el atributo";
        String nombre = entity.getNombre();
        String correo = entity.getCorreo();
        String telefono = entity.getTelefono();
        if (this.dao.stream().where(u -> u.getNombre().equals(nombre)).findFirst().isPresent()) {
            mensaje += " nombre,";
        }
        if (this.dao.stream().where(u -> u.getCorreo().equals(correo.toLowerCase())).findFirst().isPresent()) {
            mensaje += " correo,";
        }
        if (this.dao.stream().where(u -> u.getTelefono().equals(telefono)).findFirst().isPresent()) {
            mensaje += " telefono,";
        }
        mensaje = mensaje.substring(0, mensaje.length() - 1);
        return mensaje;
    }

    private userIdentifierType getUserIdentifierType(String userIdentifier) {
        if (userIdentifier.contains("@")) { //es un correo 
            return userIdentifierType.MAIL;
        } else {
            if (userIdentifier.matches("^[0-9]{10}$")) {
                return userIdentifierType.PHONE;
            } else {
                return userIdentifierType.USER;
            }
        }
    }

    public ModelCodigoRecuperacionUsuario enviarCodigo(String identifier) throws UsuarioInexistenteException,
            ParametroInvalidoException, EmailException, MalformedURLException {

        Usuario usuarioARecuperar = null;

        try {
            switch (getUserIdentifierType(identifier)) {
                case MAIL:
                    usuarioARecuperar = this.dao.stream().where(u -> u.getCorreo().equals(identifier.toLowerCase())).findFirst().get();
                    break;
                case PHONE:
                    usuarioARecuperar = this.dao.stream().where(u -> u.getTelefono().equals(identifier)).findFirst().get();
                    break;
                default:
                    throw new ParametroInvalidoException("el identificador proporsionado no es váliodo. Debe de utilizar un correo electronico ó número de teléfono de 10 dígitos");
            }

            Random r = new Random();
            //generar codigo de 8 digitos aleatorios
            String code = String.valueOf(r.nextInt(99));
            code += String.valueOf(r.nextInt(99));
            code += String.valueOf(r.nextInt(99));
            code += String.valueOf(r.nextInt(99));
            //enviar correo con codigo de recuperacion
            switch (getUserIdentifierType(identifier)) {
                case MAIL:
                    UtilsMail.sendRecuperarContraseñaHTMLMail(identifier, usuarioARecuperar.getNombre(), code);
                    break;
                case PHONE:
                    UtilsSMS.sendSMS(identifier, "Hola " + usuarioARecuperar.getNombre() + " su código de recuperacion de contraseña es: " + code);
                    break;
            }
            ModelCodigoRecuperacionUsuario model = new ModelCodigoRecuperacionUsuario();
            model.setCode(code);
            model.setIdUser(usuarioARecuperar.getId().toString());

            return model;

        } catch (NoSuchElementException e) {
            throw new UsuarioInexistenteException("No se encontro usuario con el identificador proporsionado");
        }
    }

    public void resetPassword(Long userId, String pass) throws Exception {
        pass = UtilsSecurity.cifrarMD5(pass);

        ManagerBitacoraContra managerBitacoraContra = new ManagerBitacoraContra();
        managerBitacoraContra.setUsuario(userId);

        BitacoraContras bitacoraContra = new BitacoraContras(userId, pass);

        if (managerBitacoraContra.stream().anyMatch(e -> e.equals(bitacoraContra))) {
            throw new ParametroInvalidoException("La contraseña que esta ingresando ya fué utilizada, intente con otra");
        }

        DaoUsuario daoUsuario = new DaoUsuario();

        Usuario u = dao.findOne(userId);
        u.setContra(pass);
        daoUsuario.update(u);

        List<BitacoraContras> bitacoraContras = managerBitacoraContra.stream()
                .filter(b -> b.getBitacoraContrasPK().getUsuario().equals(u.getId()))
                .sorted((b1, b2) -> b1.getFechaAsignada().compareTo(b2.getFechaAsignada()))
                .collect(toList());

        //obtener el numero maximo de contraseñas a guardar para impedir repeticion
        int maxNumber = UtilsConfig.getMaxPasswordRecords();
        // lastPassword.size() < maxNumber -> agregar pass actual al registro
        // lastPassword.size() >= maxNumber -> resize de lastPassword con los ultimos maxNumber contraseñas                        

        bitacoraContra.setUsuario1(u);

        if (bitacoraContras.size() < maxNumber) {
            managerBitacoraContra.persist(bitacoraContra); //añadir la bitacora de la contra usada            
        } else {
            managerBitacoraContra.delete(bitacoraContras.get(0).getBitacoraContrasPK()); //remover la ultima contra asignada
            managerBitacoraContra.persist(bitacoraContra);//agregar nueva
        }
    }

    public void asignarPermisos(ModelAsignarPermisos modelo) throws Exception {
        ManagerUsuariosPermisos managerUsuariosPermisos = new ManagerUsuariosPermisos();
        //eliminar los permisos anteriores
        Long usuarioId = modelo.getId();
        managerUsuariosPermisos.deleteAll(managerUsuariosPermisos.stream()
                .where(up -> up.getUsuariosPermisosPK().getUsuario().equals(usuarioId))
                .select(up -> up.getUsuariosPermisosPK())
                .collect(toList())
        );
        //asignar los nuevos
        List<UsuariosPermisos> permisosNuevos = new ArrayList<>();
        for (ModelPermisoAsignado permiso : modelo.getPermisos()) {
            UsuariosPermisos u = new UsuariosPermisos(modelo.getId(), permiso.getId());
            u.setProfundidad(permiso.getProfundidad());

            permisosNuevos.add(u);
        }

        managerUsuariosPermisos.persistAll(permisosNuevos);
    }

    public String nombreDeUsuario(Long usuarioId) {
        return dao.stream().where(u -> u.getId().equals(usuarioId)).findFirst().get().getNombre();
    }

    private enum userIdentifierType {
        PHONE, MAIL, USER
    }

    @Override
    public String nombreColeccionParaRegistros() {
        return "usuarios";
    }

}