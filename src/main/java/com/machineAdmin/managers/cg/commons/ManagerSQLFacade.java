/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.machineAdmin.managers.cg.commons;

import com.machineAdmin.daos.cg.commons.DaoSQLFacade;
import com.machineAdmin.daos.cg.exceptions.ConstraintException;
import com.machineAdmin.daos.cg.exceptions.SQLPersistenceException;
import com.machineAdmin.entities.cg.commons.EntitySQL;
import com.machineAdmin.managers.cg.exceptions.UsuarioNoAsignadoException;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import static java.util.stream.Collectors.toList;
import org.jinq.jpa.JPAJinqStream;

/**
 *
 * @author Ulises Beltrán Gómez --- beltrangomezulises@gmail.com
 * @param <T> Entidad a manejar
 * @param <K> Tipo de dato de llave primaria de la entidad
 */
public abstract class ManagerSQLFacade<T extends EntitySQL, K> extends ManagerFacade<T, K> {

    private final DaoSQLFacade<T, K> dao;

    public ManagerSQLFacade(String usuario, DaoSQLFacade dao) {
        super(usuario);
        this.dao = dao;
    }

    public ManagerSQLFacade(DaoSQLFacade dao) {
        super();        
        this.dao = dao;
    }

    @Override
    public T persist(T entity) throws Exception {
        try {
            entity.setUsuarioCreador(UUID.fromString(this.getUsuario()));
        } catch (UnsupportedOperationException e) { //significa que no esta preparada la entidad para tener usuario creador
        }
        dao.persist(entity);
        try {                        
            this.bitacorizar("alta", this.getModeloBitacorizar(entity));
        } catch (UsuarioNoAsignadoException ex) {
            Logger.getLogger(ManagerSQLFacade.class.getName()).log(Level.SEVERE, null, ex);
        }
        return entity;
    }

    @Override
    public List<T> persistAll(List<T> entities) throws Exception {        
        try {
            entities.forEach((entity) -> entity.setUsuarioCreador(UUID.fromString(this.getUsuario())));            
        } catch (UnsupportedOperationException e) { //significa que no esta preparada la entidad para tener usuario creador
        }        
        List<T> ts = dao.persistAll(entities);
        try {
            ts.stream().forEach(t -> {
                t.setUsuarioCreador(UUID.fromString(this.getUsuario()));
                try {
                    this.bitacorizar("alta", this.getModeloBitacorizar(t));
                } catch (UsuarioNoAsignadoException ex) {
                    Logger.getLogger(ManagerSQLFacade.class.getName()).log(Level.SEVERE, null, ex);
                }
            });
        } catch (Exception ex) {
            Logger.getLogger(ManagerSQLFacade.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ts;
    }

    @Override
    public void delete(K id) throws Exception {
        T t = dao.findOne(id);
        dao.delete(id);
        try {
            this.bitacorizar("eliminar", this.getModeloBitacorizar(t));
        } catch (UsuarioNoAsignadoException ex) {
            Logger.getLogger(ManagerSQLFacade.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @Override
    public void deleteAll(List<K> ids) throws SQLPersistenceException, Exception {
        List<T> ts = dao.stream().filter((T t) -> ids.contains((K) t.getId())).collect(toList());
        dao.deleteAll(ids);
        try {
            ts.stream().forEach(t -> this.getModeloBitacorizar(t));
        } catch (Exception e) {
            Logger.getLogger(ManagerSQLFacade.class.getName()).log(Level.SEVERE, null, e);
        }

    }

    @Override
    public void update(T entity) throws SQLPersistenceException, ConstraintException {
        dao.update(entity);
        try {
            this.bitacorizar("actualizar", this.getModeloBitacorizar(entity));
        } catch (UsuarioNoAsignadoException ex) {
            Logger.getLogger(ManagerSQLFacade.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public T findOne(K id) {
        T t = (T) dao.findOne(id);
        try {
            this.bitacorizar("obtener", this.getModeloBitacorizar(t));
        } catch (UsuarioNoAsignadoException ex) {
            Logger.getLogger(ManagerSQLFacade.class.getName()).log(Level.SEVERE, null, ex);
        }
        return t;
    }

    @Override
    public List<T> findAll() {
        return dao.findAll();
    }

    @Override
    public List<T> findAll(int max) {
        return dao.findAll(max);
    }

    @Override
    public long count() {
        return dao.count();
    }

    @Override
    public T findFirst() {
        return (T) dao.findFirst();
    }

    public JPAJinqStream<T> stream() {
        return dao.stream();
    }

    @Override
    public K stringToKey(String s) {
        return dao.stringToPK(s);
    }

}
