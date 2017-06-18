/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.machineAdmin.managers;

import com.machineAdmin.daos.DaoFacade;
import com.machineAdmin.entities.Entity;
import java.util.List;
import org.mongojack.DBQuery.Query;
import org.mongojack.WriteResult;

/**
 *
 * @author Ulises Beltrán Gómez --- beltrangomezulises@gmail.com
 * @param <T> is an entity
 */
public class ManagerFacade<T extends Entity> {

    DaoFacade dao;

    public ManagerFacade(DaoFacade dao) {
        this.dao = dao;
    }

    public WriteResult<T,String> persist(T t) {
        return dao.persist(t);
    }

    public WriteResult<T,String> update(T t) {        
        return dao.update(t);
    }

    public WriteResult<T,String> delete(T t) {
        return dao.delete(t);
    }

    public T find(String id){
        return (T) dao.find(id);
    }
    
    public T find(Query query){
        return (T) dao.find(query);
    }
    
    public List<T> findAll() {        
        return dao.findAll();
    }         

    public List<T> findAll(int max){        
        return dao.findAll(max);
    }       
    
    public List<T> findAll(Query query){
        return dao.findAll(query);
    }
    
    public List<T> findAll(Query query, int max){
        return dao.findAll(query, max);
    }
    
    public long count(){
        return dao.count();
    }
}
