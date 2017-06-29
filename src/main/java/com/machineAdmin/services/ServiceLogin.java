/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.machineAdmin.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.machineAdmin.entities.cg.admin.User;
import com.machineAdmin.managers.cg.admin.ManagerUser;
import com.machineAdmin.managers.cg.exceptions.UsuarioInexistenteException;
import com.machineAdmin.models.cg.ModelEncryptContent;
import com.machineAdmin.models.cg.enums.Status;
import com.machineAdmin.models.cg.responses.Response;
import com.machineAdmin.services.cg.ServiceFacade;
import com.machineAdmin.utils.UtilsJWT;
import com.machineAdmin.utils.UtilsJson;
import com.machineAdmin.utils.UtilsSecurity;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.apache.commons.mail.EmailException;

/**
 *
 * @author Ulises Beltrán Gómez --- beltrangomezulises@gmail.com
 */
@Path("/login")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ServiceLogin {

    @POST
    public Response login(ModelEncryptContent content) {
        Response res = new Response();
        ManagerUser managerUsuario = new ManagerUser();
        try {
            User usuarioAutenticando = UtilsJson.jsonDeserialize(UtilsSecurity.decryptBase64ByPrivateKey(content.getContent()), User.class);
            usuarioAutenticando.setPass(UtilsSecurity.cifrarMD5(usuarioAutenticando.getPass()));
            User usuarioLogeado = managerUsuario.Login(usuarioAutenticando);
            //no enviar pass
            usuarioLogeado.setPass(null);
            
            res.setData(usuarioLogeado);
            res.setMetaData(UtilsJWT.generateToken(usuarioLogeado));
        } catch (UsuarioInexistenteException e) {
            res.setStatus(Status.WARNING);
            res.setMessage("Usuario y/o contraseña incorrecto");
            res.setDevMessage("imposible inicio de sesión, por: " + e.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            res.setStatus(Status.ERROR);
            res.setDevMessage("imposible inicio de sesión, por: " + ex.getMessage());
        }
        return res;
    }

    @GET
    @Path("/publicKey")
    public Response getPublicKey() {
        Response r = new Response();
        r.setData(UtilsSecurity.getPublicKey());
        r.setDevMessage("llave publica de cifrado RSA Base64");
        return r;
    }

    @GET
    @Path("/recoverCodeMail/{mail}")
    public Response recoverCodeMail(@PathParam("mail") String mail) {
        Response res = new Response();
        try {
            ManagerUser managerUser = new ManagerUser();
            managerUser.enviarCodigoMail(mail);
            res.setMetaData(UtilsJWT.generateResetToken());
            res.setDevMessage("token de codigo para restaurar contraseña");
            res.setMessage("El código para recuperar contraseña fue enviado por correo electrónico");
        } catch (MalformedURLException | EmailException e) {
            res.setStatus(Status.ERROR);
            res.setMessage("No fue posible enviar el código al correo registrado, comuniquese con su administrador");
            ServiceFacade.setCauseMessage(res, e);
        } catch (UsuarioInexistenteException ex) {
            res.setStatus(Status.WARNING);
            res.setMessage("No se encontro el usuario con el correo especificado");
            ServiceFacade.setCauseMessage(res, ex);
        }
        return res;
    }

    @GET
    @Path("/tokenReset/{mail}/{code}")
    public Response getTokenReset(@HeaderParam("Authorization") String token, @PathParam("mail") String mail, @PathParam("code") String code) {
        Response res = new Response();
        if (UtilsJWT.isTokenValid(token)) {
            try {
                ManagerUser managerUser = new ManagerUser();
                res.setData(managerUser.generateTokenResetPassword(mail, code));
            } catch (UsuarioInexistenteException ex) {
                res.setStatus(Status.WARNING);
                res.setMessage("No se encontro el usuario con el correo especificado");
                ServiceFacade.setCauseMessage(res, ex);
            } catch (JsonProcessingException ex) {
                res.setStatus(Status.ERROR);
                res.setMessage("Existion un error en la generacion del token de reseteo");
                ServiceFacade.setCauseMessage(res, ex);
            }
        } else {
            res.setMessage("Token inválido");
            res.setDevMessage("Token inválido");
            res.setStatus(Status.WARNING);
        }
        return res;
    }

    @POST
    @Path("/resetPassword")
    public Response resetPassword(@HeaderParam("Authorization") String tokenResetPassword, ModelEncryptContent content) {
        Response res = new Response();
        if (UtilsJWT.isTokenValid(tokenResetPassword)) {
            try {
                User u = UtilsJson.jsonDeserialize(UtilsJWT.getBodyToken(tokenResetPassword), User.class);
                String pass = UtilsSecurity.decryptBase64ByPrivateKey(content.getContent());

                ManagerUser managerUser = new ManagerUser();
                if (managerUser.resetPassword(u, pass)) {
                    res.setMessage("La contraseña fue restablecida con éxito");
                } else {
                    res.setMessage("No se logró restablecer la contraseña, intente repetir el proceso completo");
                    res.setStatus(Status.WARNING);
                    res.setDevMessage("Falló al actualizar la contraseña del usuario");
                }
            } catch (Exception ex) {
                res.setMessage("No se logró restablecer la contraseña, intente repetir el proceso completo");
                ServiceFacade.setCauseMessage(res, ex);
                res.setStatus(Status.ERROR);
            }
        } else {
            res.setStatus(Status.WARNING);
            res.setMessage("No se logró restablecer la contraseña, intente repetir el proceso completo");
            res.setDevMessage("token inválido");
        }
        return res;
    }

}
