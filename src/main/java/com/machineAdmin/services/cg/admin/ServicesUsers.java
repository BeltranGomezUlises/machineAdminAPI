/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.machineAdmin.services.cg.admin;

import com.machineAdmin.daos.cg.admin.DaoUser;
import com.machineAdmin.entities.cg.admin.User;
import com.machineAdmin.managers.cg.admin.ManagerUser;
import com.machineAdmin.models.cg.responses.Response;
import com.machineAdmin.services.cg.ServiceFacade;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

/**
 *
 * @author Ulises Beltrán Gómez --- beltrangomezulises@gmail.com
 */
@Path("/users")
public class ServicesUsers extends ServiceFacade<User>{
    
    public ServicesUsers() {
        super(new ManagerUser());
    }

    @Override
    public Response delete(String token, User t) {
    //delete solo deshabilita los usuarios
        return super.delete(token, t); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Response put(String token, User t) {
        return super.put(token, t); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Response post(String token, User t) {
        
        return super.post(token, t); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Response get(String token, String id) {
        return super.get(token, id); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Response get(String token) {
        return super.get(token); //To change body of generated methods, choose Tools | Templates.
    }
       
    @GET
    @Path("/getUser")
    public User getUser(){
        DaoUser daoUser = new DaoUser();
        return (User) daoUser.findOne("59541b37af0feb893b3102bf", "user", "mail");                
    }
    
    
}