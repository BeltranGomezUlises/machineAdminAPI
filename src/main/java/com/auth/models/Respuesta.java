/*
 * Copyright (C) 2018 Alonso --- alonso@kriblet.com
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
package com.auth.models;

import com.auth.models.enums.Status;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Modelo de respuesta para notificar el estatus de la petición
 *
 * @author Alonso --- alonso@kriblet.com
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Respuesta<T> {

    private Status estado;
    private String mensaje;
    private T data;

    public Respuesta() {
    }

    public Respuesta(Status estado, String mensaje) {
        this.estado = estado;
        this.mensaje = mensaje;
    }

    public Respuesta(Status estado, String mensaje, T data) {
        this.estado = estado;
        this.mensaje = mensaje;
        this.data = data;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public Status getEstado() {
        return estado;
    }

    public void setEstado(Status estado) {
        this.estado = estado;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

}
