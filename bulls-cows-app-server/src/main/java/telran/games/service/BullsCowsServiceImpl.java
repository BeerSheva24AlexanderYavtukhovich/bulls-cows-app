package telran.games.service;

import java.time.LocalDate;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import telran.games.entities.Gamer;

public class BullsCowsServiceImpl implements BullsCowsService {
    private final EntityManager entityManager;


    public BullsCowsServiceImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }


  


}
