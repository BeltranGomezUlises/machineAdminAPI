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
package com.auth.daos.admin;

import com.auth.daos.commons.DaoSQLFacade;
import com.auth.entities.admin.UsuariosPerfil;
import com.auth.entities.admin.UsuariosPerfilPK;
import com.auth.models.ModelPerfilYHereda;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;

/**
 *
 * @author Alonso --- alonso@kriblet.com
 */
public class DaoUsuariosPerfil extends DaoSQLFacade<UsuariosPerfil, UsuariosPerfilPK> {

    public DaoUsuariosPerfil() {
        super(UsuariosPerfil.class, UsuariosPerfilPK.class);
    }

    /**
     * reemplaza los perfiles del usuario
     *
     * @param usuarioId usuario a manejar
     * @param perfiles perfiles a asignar y si hereda permisos del mismo
     * @return
     */
    public List<UsuariosPerfil> reemplazarPerfilesUsuario(Integer usuarioId, List<ModelPerfilYHereda> perfiles) {
        EntityManager em = this.getEM();
        em.getTransaction().begin();
        //eliminar los actuales
        em.createQuery("DELETE FROM UsuariosPerfil u WHERE u.usuario1.id = :usuarioId")
                .setParameter("usuarioId", usuarioId)
                .executeUpdate();
        //reempleazar con los nuevos
        List<UsuariosPerfil> usuarioPerfiles = new ArrayList<>();
        UsuariosPerfil usuarioPerfil;
        for (ModelPerfilYHereda perfile : perfiles) {
            usuarioPerfil = new UsuariosPerfil(usuarioId, perfile.getPerfilId(), perfile.isHereda());
            em.persist(usuarioPerfil);
            usuarioPerfiles.add(usuarioPerfil);
        }
        em.getTransaction().commit();
        return usuarioPerfiles;
    }

}
