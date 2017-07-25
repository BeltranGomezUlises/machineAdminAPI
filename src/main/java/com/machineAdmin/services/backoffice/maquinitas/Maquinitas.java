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
package com.machineAdmin.services.backoffice.maquinitas;

import com.machineAdmin.entities.mongo.Maquina;
import com.machineAdmin.managers.mongo.ManagerMaquina;
import com.machineAdmin.models.cg.responsesCG.Response;
import com.machineAdmin.services.cg.commons.ServiceFacade;
import javax.ws.rs.Path;

/**
 *
 * @author Ulises Beltrán Gómez --- beltrangomezulises@gmail.com
 */
@Path("/maquinitas")
public class Maquinitas extends ServiceFacade<Maquina>{
    
    public Maquinitas() {
        super(new ManagerMaquina());
    }

    @Override
    public Response eliminar(String token, Maquina t) {
        return super.eliminar(token, t); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Response modificar(String token, Maquina t) {
        return super.modificar(token, t); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Response alta(String token, Maquina t) {
        return super.alta(token, t); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Response obtener(String token, String id) {
        return super.obtener(token, id); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Response listar(String token) {
        return super.listar(token); //To change body of generated methods, choose Tools | Templates.
    }
           
}
