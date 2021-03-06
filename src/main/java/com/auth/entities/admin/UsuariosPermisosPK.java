/*
 * Copyright (C) 2018 Alonso - Alonso@kriblet.com
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
package com.auth.entities.admin;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 *
 * @author Alonso - Alonso@kriblet.com
 */
@Embeddable
public class UsuariosPermisosPK implements Serializable {

    @Basic(optional = false)
    @NotNull
    @Column(name = "usuario")
    private int usuario;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 2147483647)
    @Column(name = "permiso")
    private String permiso;
    @Basic(optional = false)
    @NotNull
    @Column(name = "sucursal")
    private int sucursal;

    public UsuariosPermisosPK() {
    }

    public UsuariosPermisosPK(int usuario, String permiso, int sucursal) {
        this.usuario = usuario;
        this.permiso = permiso;
        this.sucursal = sucursal;
    }

    public int getUsuario() {
        return usuario;
    }

    public void setUsuario(int usuario) {
        this.usuario = usuario;
    }

    public String getPermiso() {
        return permiso;
    }

    public void setPermiso(String permiso) {
        this.permiso = permiso;
    }

    public int getSucursal() {
        return sucursal;
    }

    public void setSucursal(int sucursal) {
        this.sucursal = sucursal;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (int) usuario;
        hash += (permiso != null ? permiso.hashCode() : 0);
        hash += (int) sucursal;
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof UsuariosPermisosPK)) {
            return false;
        }
        UsuariosPermisosPK other = (UsuariosPermisosPK) object;
        if (this.usuario != other.usuario) {
            return false;
        }
        if (!this.permiso.equals(other.permiso)) {
            return false;
        }
        return this.sucursal == other.sucursal;
    }

    @Override
    public String toString() {
        return "com.auth.entities.admin.UsuariosPermisosPK[ usuario=" + usuario + ", permiso=" + permiso + ", sucursal=" + sucursal + " ]";
    }

}
