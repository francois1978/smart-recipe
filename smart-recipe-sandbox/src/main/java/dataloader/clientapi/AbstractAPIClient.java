package dataloader.clientapi;

import dataloader.entity.Entity;

import java.util.List;

public abstract class AbstractAPIClient extends APIClient{


    public abstract List<Entity> findAll();

    public abstract List findByName(String name);

    public  abstract Entity create(Entity entity);

    public abstract Entity buildNewEntity(String name);

    }
