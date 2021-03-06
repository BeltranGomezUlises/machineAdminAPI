/*
 * Copyright (C) 2017 Alonso --- alonso@kriblet.com
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
package com.auth.services;

import com.auth.entities.admin.Permiso;
import com.auth.entities.admin.Seccion;
import com.auth.entities.commons.Profundidad;
import com.auth.managers.admin.ManagerPermiso;
import com.auth.managers.admin.ManagerSeccion;
import com.auth.managers.exceptions.ParametroInvalidoException;
import com.auth.managers.exceptions.TokenExpiradoException;
import com.auth.managers.exceptions.TokenInvalidoException;
import com.auth.models.ModelPermisoValido;
import com.auth.models.Respuesta;
import com.auth.models.enums.Status;
import com.auth.services.commons.ServiceFacade;
import com.auth.utils.UtilsJWT;
import com.auth.utils.UtilsPermissions;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * servicios relacionados con los permisos del sistema en general
 *
 * @author Alonso --- alonso@kriblet.com
 */
@Path("/permisos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class Permisos extends ServiceFacade<Permiso, String> {

    public Permisos() {
        super(new ManagerPermiso());
    }

    /**
     * sirve para obtener la lista de todos los permisos que pueden ser asignados a un usuario o perfil
     *
     * @param token token de sesion
     * @throws com.auth.managers.exceptions.TokenInvalidoException si el token proporsionado no es valido
     * @throws com.auth.managers.exceptions.TokenExpiradoException si el token proporsionado ya caducó
     * @return retorna en data modelo "Seccion"
     */
    @GET
    @Path("/disponibles")
    public List<Seccion> getPermisosDisponibles(@HeaderParam("Authorization") String token) throws TokenInvalidoException, TokenExpiradoException, Exception {
        UtilsJWT.validateSessionToken(token);
        ManagerSeccion managerSeccion = new ManagerSeccion();
        return managerSeccion.findAll();
    }

    /**
     * obtiene las profundides permitidas para asignar
     *
     * @param token token de sesion
     * @throws com.auth.managers.exceptions.TokenInvalidoException si el token proporsionado no es valido
     * @throws com.auth.managers.exceptions.TokenExpiradoException si el token proporsionado ya caducó
     * @return en data, la lista de profundidaes disponibles
     */
    @GET
    @Path("/profundidades")
    public Profundidad[] getProfundidades(@HeaderParam("Authorization") String token) throws TokenExpiradoException, TokenInvalidoException {
        UtilsJWT.validateSessionToken(token);
        return Profundidad.values();
    }

    /**
     * Consulta si el usuario dueño del token de sesion, tiene el permiso solicitado y su profundidad de mayor alcance
     *
     * @param token token de sesion
     * @param permisoId identificador del permiso
     * @return modelo con la validez y profundidad del permiso
     * @throws ParametroInvalidoException si se encuentra un error en los parametros
     */
    @GET
    @Path("/validar/{permisoId}")
    public Respuesta<ModelPermisoValido> tienePermiso(@HeaderParam("Authorization") final String token, @PathParam("permisoId") final String permisoId) throws ParametroInvalidoException {
        Respuesta<ModelPermisoValido> res;
        try {
            ModelPermisoValido model = UtilsPermissions.tienePermiso(token, permisoId);
            res = new Respuesta<>(Status.OK, null, model);
        } catch (TokenExpiradoException | TokenInvalidoException e) {
            res = new Respuesta<>(Status.WARNING, "Token de sesion inválido");
        }
        return res;
    }

}
